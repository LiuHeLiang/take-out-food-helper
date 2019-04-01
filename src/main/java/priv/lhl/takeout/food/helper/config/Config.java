package priv.lhl.takeout.food.helper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import priv.lhl.takeout.food.helper.util.Util;

import java.io.*;
import java.util.Properties;


/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/1/21 15:38
 * @description : 配置
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private static Properties properties = new Properties();

    public static String getConfig(String key) {
        try {
            InputStream reader = new FileInputStream(ResourceUtils.getFile("classpath:helper.properties"));
            properties.load(reader);
            reader.close();
            return properties.getProperty(key);
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            return "";
        }
    }

    public static void setConfig(String key, Object value) {
        try {
            OutputStream writer = new FileOutputStream(ResourceUtils.getFile("classpath:helper.properties"));
            properties.setProperty(key, (String) value);
            properties.store(writer, null);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
