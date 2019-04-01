package priv.lhl.takeout.food.helper.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import priv.lhl.takeout.food.helper.config.Config;
import priv.lhl.takeout.food.helper.enums.Staff;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/1/21 15:06
 * @description : http  util
 */
@Component
@ConfigurationProperties(prefix = "helper")
@PropertySource("classpath:helper.properties")
public class Util {

    /**
     * 文件格式
     */
    static private List<String> fileFormat = Lists.newArrayList();

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    private static HttpClientContext httpClientContext = HttpClientContext.create();

    static {
        fileFormat.add("jpg");
        fileFormat.add("png");
    }

    /**
     * post util
     *
     * @param map param
     * @param url url
     */
    private static String post(Map<String, String> map, String url) {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        List<BasicNameValuePair> pairs = Lists.newArrayList();
        map.forEach((k, v) -> pairs.add(new BasicNameValuePair(k, v)));
        try {
            post.setEntity(new UrlEncodedFormEntity(pairs));
            HttpResponse response = client.execute(post, httpClientContext);
            CookieStore cookieStore = httpClientContext.getCookieStore();
            List<Cookie> cookies = cookieStore.getCookies();
            cookies.forEach(r -> LOGGER.info("COOKIE===> key={} value={}", r.getName(), r.getValue()));
            return EntityUtils.toString(response.getEntity(), Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 图片文件转换为base64编码字符串
     *
     * @param file
     * @return
     */
    private static String picture2Base64(String path, File file) {
        String fileName = file.getName();
        String suffix = nameOrSuffix(fileName, "\\.", 1);
        if (!fileFormat.contains(suffix.toLowerCase())) {
            throw new IllegalArgumentException("文件格式不符, 请检查文件后重试");
        }
        InputStream inputStream;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(path + fileName);
            data = new byte[inputStream.available()];
            int read = inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BASE64Encoder().encode(data);
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp;
        for (String str : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + str);
            } else {
                temp = new File(path + File.separator + str);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + str);
                delFolder(path + "/" + str);
                flag = true;
            }
        }
        return flag;
    }

    private static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath);
            File myFilePath = new File(folderPath);
            myFilePath.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取access_token, 并更新token
     *
     * @return
     */
    private static void handelAccessToken() {
        String post, accessToken;
        Format format = new SimpleDateFormat("yyyyMMddHHmmss");

        // 获取配置文件token
        String token = Config.getConfig("helper.accessToken");

        // 获取token失效时间
        String expireTime = Config.getConfig("helper.expireDate");

        Calendar calendar = Calendar.getInstance();
        String nowStr = format.format(calendar.getTime());

        // 若token过期
        boolean isExpire = Long.valueOf(nowStr) > Long.valueOf(StringUtil.isEmpty(expireTime) ? "0" : expireTime);

        if (StringUtil.isEmpty(token) || StringUtil.isEmpty(expireTime) || isExpire) {
            // 重新获取token
            String url = Config.getConfig("helper.accessTokenURL");
            post = post(new HashMap<>(0), url);
            JSONObject jsonObject = JSON.parseObject(post);
            accessToken = jsonObject.getString("access_token");

            // 更新token配置
            Config.setConfig("helper.accessToken", accessToken);

            // 获取此token下次失效时间
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.HOUR, -5);
            String exptreStr = format.format(calendar.getTime());

            // 更新此token失效时间
            Config.setConfig("helper.expireDate", exptreStr);
        }
    }

    /**
     * 获取实付金额
     *
     * @param file
     * @return
     */
    public static String picture2Text(String path, File file) {
        // token 检查
        handelAccessToken();
        // 准备参数
        Map<String, String> params = Maps.newHashMap();
        params.put("access_token", Config.getConfig("helper.accessToken"));
        params.put("image", nameOrSuffix(picture2Base64(path, file), ",", 1));
        params.put("content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        String post = post(params, Config.getConfig("helper.picture2TextURL"));
        JSONArray jsonArray = (JSONArray) JSONArray.parse(JSON.parseObject(post).getString("words_result"));
        String money = "";
        for (Object o : jsonArray) {
            JSONObject tempMoney = (JSONObject) o;
            String value = tempMoney.getString("words");
            // 实付 = [饿了么] 合计 = [美团外卖] 实际支付 = [永辉超市]
            if (value.contains("实付")) {
                money = value.substring(value.lastIndexOf("￥") + 1);
                break;
            } else if (value.contains("合计") && !"商品合计".equals(value)) {
                money = value.substring(value.lastIndexOf("￥") + 1);
                break;
            } else if (value.contains("实际支付:")) {
                money = value.substring(value.lastIndexOf("￥") + 1);
                break;
            } else {
                money = "0";
            }
        }
        return money;
    }

    /**
     * 获取文件的名还是后缀, 亦或是需要的部分
     *
     * @param fileName
     * @param sub
     * @return
     */
    public static String nameOrSuffix(String fileName, String regex, int sub) {
        String[] temp = fileName.split(regex);
        int suffixIndex = temp.length - 1;
        String suffix = "";
        switch (sub) {
            case 0:
                suffix = temp[sub];
                break;
            case 1:
                suffix = temp[suffixIndex];
                break;
            default:
        }
        return suffix;
    }

    /**
     * 获报单人
     *
     * @param fullName
     * @return
     */
    public static String getExpense(String fullName) {
        String expense;
        if (fullName.contains("、")) {
            String[] temp = fullName.split("、");
            expense = temp[0];
        } else {
            if (fullName.contains("-")) {
                String[] temp = fullName.split("-");
                expense = temp[0];
            } else {
                expense = fullName;
            }
        }
        return expense;
    }


    public static class StringUtil {
        static String empty = "null";

        public static boolean isEmpty(String str) {
            return str == null || str.length() == 0 || empty.equals(str) || "".equals(str);
        }

        public static boolean isNotEmpty(String str) {
            return !isEmpty(str);
        }

        public static boolean isDigit(String strNum) {
            return strNum.matches("[0-9]+");
        }
    }

    public static String make(MultipartFile tempFile, String path, String fullName, List<File> zipPictures) throws IOException {
        boolean b;
        // MultipartFile 转 File
        File file = new File(path + fullName);
        b = file.createNewFile();
        LOGGER.info("创建文件: " + file.getName() + " 结果: " + b);
        tempFile.transferTo(file);

        // 获取外卖金额并将报单人和报销金额保存,待写入到Excel中
        String money = Util.picture2Text(path, file);

        // 将文件重命名,并保存,等待放到zip中
        String newFileName;
        if (fullName.contains("-")) {
            newFileName = path + Util.nameOrSuffix(fullName, "\\-", 0) +
                    "-" + money + "." + Util.nameOrSuffix(fullName, "\\.", 1);
        } else {
            newFileName = path + Util.nameOrSuffix(fullName, "\\.", 0) +
                    "-" + money + "." + Util.nameOrSuffix(fullName, "\\.", 1);
        }
        File newFile = new File(newFileName);
        b = file.renameTo(newFile);
        LOGGER.info("重命名文件: " + file.getName() + " 结果: " + newFile.getName());
        zipPictures.add(newFile);
        return money;
    }

    public static String createMkdir(String resPath, String mkdir) {
        File picturesMkdir = new File(resPath + mkdir);
        boolean b = picturesMkdir.mkdir();
        LOGGER.info("创建临时目录: " + picturesMkdir.getPath() + " 结果: " + b);
        return picturesMkdir.getPath() + "/";
    }

    public static void putValue(String dines, String expense, String money, int count, List<List<String>> data) {
        // 临时保存需要的数据
        List<String> temp = Lists.newArrayList();
        temp.add(dines);
        temp.add(expense);
        temp.add(Staff.valueOf(expense).alipay);
        temp.add(money);
        temp.add(String.valueOf(count));
        temp.add("电子");
        temp.add("");
        data.add(temp);
    }

    public static void getBillNum(Map<String, Integer> bills, MultipartFile[] files){
        for (MultipartFile mf : Arrays.asList(files)) {
            String str = Util.nameOrSuffix(mf.getOriginalFilename(), "\\.", 0);
            String str2 = Util.getExpense(str);
            if (str.contains("-")) {
                Integer str3 = Integer.valueOf(str.split("-")[1]);
                bills.put(str2, str3);
            }
        }
    }
}
