package priv.lhl.takeout.food.helper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/1/21 15:00
 * @description : 外卖助手
 */
@SpringBootApplication
public class HelperApplication extends SpringBootServletInitializer {

    public HelperApplication() {
        super();
        setRegisterErrorPageFilter(false);
    }

    /**生产*/
    public static void main(String[] args) {
        SpringApplication.run(HelperApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(HelperApplication.class);
    }
    //------------------------------------------------------------//

    // 本地
    /*public class HelperApplication{
    public static void main(String[] args) {
        SpringApplication.run(HelperApplication.class, args);
    }
}*/

}

