package priv.lhl.takeout.food.helper.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/1/22 17:36
 * @description : 生成Excel
 */
public class POIUtil {
    public static void main(String[] args) {
        List<List<String>> dataList = new ArrayList<>();
        String fileName = String.valueOf(System.currentTimeMillis());

        for (int i = 0; i < 10; ++i) {
            List<String> data = new ArrayList<>();
            if (i == 3) {
                data.add(null);
            } else {
                data.add("张三、李四");
            }

            data.add("张三");
            data.add("10086");
            data.add("30.2");
            data.add("1");
            data.add("电子");
            data.add("");
            dataList.add(data);
        }
        exportExcel(fileName, dataList);
    }

    public static File exportExcel(String fileName, List<List<String>> data) {
        File file = new File(fileName + ".xlsx");
        // 标题
        String[] rowsName = new String[]{"用餐人", "报单人", "报单账号", "报销金额", "报单数量", "报单类型", "总计"};
        // 单元格间距微调参数
        int[] fineTuning = new int[]{20, 6, 15, 6, 6, 6, 8};
        int countColumnNum = rowsName.length;
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();

        // 间距微调
        for (int i = 0; i < countColumnNum; ++i) {
            sheet.setColumnWidth(i, 512 * fineTuning[i]);
        }

        // 第一行
        Row nameRow = sheet.createRow(0);
        for (int nameIndex = 0; nameIndex < countColumnNum; ++nameIndex) {
            Cell nameCell = nameRow.createCell(nameIndex);
            nameCell.setCellType(1);
            nameCell.setCellValue(new XSSFRichTextString(rowsName[nameIndex]));
            nameCell.setCellStyle(getStyle(wb, 1, nameIndex));
        }

        // 第二行
        Row titleRow = sheet.createRow(1);
        CellRangeAddress range = new CellRangeAddress(1, 1, 0, 6);
        sheet.addMergedRegion(range);
        Cell cell = titleRow.createCell(0);
        cell.setCellType(1);
        String time = (new SimpleDateFormat("yyyy-MM-dd")).format((new GregorianCalendar()).getTime());
        cell.setCellValue(new XSSFRichTextString(time));
        cell.setCellStyle(getStyle(wb, 1, 0));
        setRegionStyle(sheet, range, cell.getCellStyle());

        // 写入数据
        for (int dataIndex = 0; dataIndex < data.size(); ++dataIndex) {
            Row row = sheet.createRow(dataIndex + 2);
            for (int i = 0; i < countColumnNum; ++i) {
                Cell dataCell = row.createCell(i);
                dataCell.setCellType(1);
                dataCell.setCellValue(data.get(dataIndex).get(i));
                dataCell.setCellStyle(getStyle(wb, 0, -1));
            }
        }

        // 写入数据
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    /**
     * 解决合并单元格后边框消失
     *
     * @param sheet
     * @param region
     * @param cs
     */
    private static void setRegionStyle(Sheet sheet, CellRangeAddress region, CellStyle cs) {
        for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                row = sheet.createRow(i);
            }
            for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    cell = row.createCell(j);
                    cell.setCellValue("");
                }
                cell.setCellStyle(cs);

            }
        }
    }

    /**
     * 公用设置样式
     *
     * @param workbook
     * @param type
     * @param cell
     * @return
     */
    private static CellStyle getStyle(Workbook workbook, Integer type, Integer cell) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight(Font.SS_SUPER);
        font.setFontName("宋体");
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom((short) 1);
        style.setBottomBorderColor((short) 8);
        style.setBorderRight((short) 1);
        style.setRightBorderColor((short) 8);
        style.setFont(font);
        style.setWrapText(false);
        style.setAlignment((short) 2);
        style.setVerticalAlignment((short) 1);
        if (type == 1) {
            switch (cell) {
                case 0:
                    font.setColor(IndexedColors.LIGHT_BLUE.index);
                    break;
                case 1:
                    font.setColor(IndexedColors.ORANGE.index);
                    break;
                case 2:
                    font.setColor(IndexedColors.BLUE.index);
                    break;
                case 3:
                    font.setColor(IndexedColors.GOLD.index);
                    break;
                case 4:
                    font.setColor(IndexedColors.VIOLET.index);
                    break;
                case 5:
                    font.setColor(IndexedColors.GREEN.index);
                    break;
                case 6:
                    font.setColor(IndexedColors.RED.index);
                    break;
                default:
            }
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            style.setFillPattern((short) 1);
            style.setFillForegroundColor(IndexedColors.WHITE.index);
            style.setFont(font);
        }
        if (type == 0) {
            style.setAlignment((short) 1);
        }
        return style;
    }
}
