package reggie.common;

/**
 * 自定义业务异常类
 */
public class CustomExcetion extends RuntimeException {
    public CustomExcetion(String message) {
        super(message);
    }
}
