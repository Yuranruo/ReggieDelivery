package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reggie.common.R;
import reggie.entity.Employee;
import reggie.service.EmployeeService;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        //根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //如果没有查询到则返回登陆失败结果
        if (emp==null)  return R.error("登陆失败");

        //密码对比, 如果不一致则返回登陆失败结果
            //password: 页面提交的用户密码
            //emp.getPassword(): 数据库中的用户密码
        if (!emp.getPassword().equals(password))  return R.error("登陆失败");

        //查看员工状态, 如果为已禁用状态, 则返回员工已禁用结果
        if (emp.getStatus()==0) return R.error("账号已禁用");

        //登陆成功, 将员工id存入session并返回登陆成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }
}
