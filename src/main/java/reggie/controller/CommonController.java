package reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reggie.common.R;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传和下载
 */

@Slf4j
@RequestMapping("/common")
@RestController
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info(file.toString());

        //获取文件后缀名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用uuid随机生成文件名
        String filename = UUID.randomUUID().toString() + suffix;

        //创建目录对象
        File dir = new File(basePath);
        //目录不存在,需要创建
        if (!dir.exists()) dir.mkdirs();

        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(filename);
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        //输入流, 读取文件内容
        FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

        //输出流, 通过输出流将文件写回浏览七, 在浏览器展示图片
        ServletOutputStream outputStream = response.getOutputStream();

        //向输出流中写入输入流
        response.setContentType("image/jpeg");  //设置文件类型

        int len = 0;
        byte[] bytes = new byte[1024];
        while ((len = fileInputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
            outputStream.flush();
        }

        //关闭资源
        outputStream.close();
        fileInputStream.close();

    }
}
