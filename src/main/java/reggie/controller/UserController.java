package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reggie.common.R;
import reggie.entity.User;
import reggie.service.UserService;
import reggie.utils.SMSUtils;
import reggie.utils.ValidateCodeUtils;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     *
     * @param user
     * @return
     */
    @RequestMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        log.info(user.toString());

        //获取手机号
        String phone = user.getPhone();
        //短信服务的AppCode
        String AppCode = "";

        if (StringUtils.isNotEmpty(phone)) {
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //调用阿里云api发送短信

//            SMSUtils.sendMessage(AppCode, phone, code);   //调用阿里云发送短信的api
            log.info("code={}", code);

            //将生成的验证码保存到session
//            session.setAttribute(phone, code);    //有了redis后，就不用把验证码放到session中

            //将生成的验证码缓存到Redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("短信发送成功");

        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @return
     */
    @RequestMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session中获取保存的验证码
//        String sessionCode = session.getAttribute(phone).toString();
        
        //从redis中获取验证码
        String sessionCode = redisTemplate.opsForValue().get(phone).toString();

        //进行验证码比对(页面提交的验证码和session中保存的验证码比对)
        if (StringUtils.isNotEmpty(sessionCode) && code.equals(sessionCode)) {
            //如果比对成功, 说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //判断当前手机号对应的用户是否为新用户, 如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            //将user id放入session中
            session.setAttribute("user", user.getId());

            //如果用户登陆成功, 则删除redis中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }

        return R.error("验证码错误, 登陆失败");
    }

}
