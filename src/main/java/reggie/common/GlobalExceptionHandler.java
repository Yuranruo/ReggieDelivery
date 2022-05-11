package reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class}) //所有添加了这两个注解的controller都会被拦截处理
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常方法处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());

        //ex.getMessage():"Duplicate entry 'zhangsan' for key 'idx_username'"
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + " 已存在";  //split[2]: zhangsan
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 自定义异常方法处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomExcetion.class)
    public R<String> exceptionHandler(CustomExcetion ex) {
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
