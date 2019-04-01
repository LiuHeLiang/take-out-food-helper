package priv.lhl.takeout.food.helper.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import priv.lhl.takeout.food.helper.pojo.Result;
import priv.lhl.takeout.food.helper.util.POIUtil;
import priv.lhl.takeout.food.helper.util.Util;
import priv.lhl.takeout.food.helper.util.ZipUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/1/21 15:15
 * @description : 外卖助手
 */
@RestController
@RequestMapping("helper")
public class Helper {

    @ResponseBody
    @PostMapping(value = "build")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public Result build(HttpServletRequest request, @RequestParam("file") MultipartFile[] files) {
        // 保存报销人的数据
        List<List<String>> data = Lists.newArrayList();
        List<File> zipFile = Lists.newArrayList();
        Map<String, Integer> bills = Maps.newHashMap();
        // 获取单子数量
        Util.getBillNum(bills, files);
        // 上个外卖报单人
        String lastFileName = "";
        String lastMoney = "";
        // 外卖单数, 初始化为一张
        int count = 1;
        String tempPath = request.getSession().getServletContext().getRealPath("");
        String photoMkdir = Util.createMkdir(tempPath, "temp");
        for (MultipartFile tempFile : files) {
            try {
                // 文件全名
                String fullName = tempFile.getOriginalFilename();
                // 用餐人
                assert fullName != null;
                String dines = Util.nameOrSuffix(fullName, "\\.", 0);
                // 报销人
                String expense = Util.getExpense(dines);
                // 报销金额
                String money;
                // 是否是多张单子
                if (dines.contains("-")) {
                    // 某人的地n张单子
                    int num = Integer.parseInt(dines.split("-")[1]);
                    // 多人用餐
                    dines = Util.nameOrSuffix(dines, "\\-", 0);
                    if (num == 1) {
                        lastFileName = "";
                        lastMoney = "";
                        count = 1;
                        money = Util.make(tempFile, photoMkdir, fullName, zipFile);
                        lastFileName = expense;
                        lastMoney = money;
                    } else if (num == bills.get(expense)) {
                        count++;
                        BigDecimal temp = new BigDecimal(Util.make(tempFile, photoMkdir, fullName, zipFile));
                        money = new BigDecimal(lastMoney).add(temp).toString();
                        Util.putValue(dines, expense, money, count, data);
                        lastFileName = "";
                        lastMoney = "";
                    } else {
                        count++;
                        BigDecimal temp = new BigDecimal(Util.make(tempFile, photoMkdir, fullName, zipFile));
                        money = new BigDecimal(lastMoney).add(temp).toString();
                        lastFileName = expense;
                        lastMoney = money;
                    }
                } else {
                    money = Util.make(tempFile, photoMkdir, fullName, zipFile);
                    Util.putValue(dines, expense, money, 1, data);
                    if (Util.StringUtil.isNotEmpty(lastFileName) || Util.StringUtil.isNotEmpty(lastMoney)) {
                        lastFileName = "";
                        lastMoney = "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 将文件打成zip压缩包
        String zipMkdir = Util.createMkdir(tempPath, "zip");
        zipFile.add(POIUtil.exportExcel(photoMkdir + "外卖报销统计", data));
        File zip = ZipUtil.zip(zipFile, zipMkdir);
        Util.delAllFile(photoMkdir);
        if (zip != null) {
            return new Result(200, "生成成功");
        } else {
            return new Result(-200, "生成失败");
        }
    }
}
