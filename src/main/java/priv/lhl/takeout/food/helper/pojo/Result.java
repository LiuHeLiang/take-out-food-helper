package priv.lhl.takeout.food.helper.pojo;

/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/2/11 11:06
 * @description :
 */
public class Result {
    private String msg;
    private int code;

    public Result(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
