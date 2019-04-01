package priv.lhl.takeout.food.helper.enums;

/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/1/22 20:55
 * @description : 员工
 */
public enum Staff {
    // 员工
    员工一("员工一", "13111111111"),
    员工二("员工二", "13122222222"),
    员工三("员工三", "13133333333"),
    员工四("员工四", "13144444444"),

    测试("测试", "1234");

    public String name;
    public String alipay;

    Staff(String name, String alipay) {
        this.name = name;
        this.alipay = alipay;
    }

}
