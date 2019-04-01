package priv.lhl.takeout.food.helper.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipInputStream;


/**
 * Created with IDEA
 *
 * @author : Liang
 * @version : 0.1
 * @date : 2019/1/23 22:29
 * @description :
 */
public class ZipUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    /**
     * 使用GBK编码可以避免压缩中文文件名乱码
     */
    private static final String CHINESE_CHARSET = "GBK";
    private static final String ZIP_NAME = "外卖报销统计.zip";

    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;

    private ZipUtil() {
    }

    /**
     * <p>
     * 压缩文件
     * </p>
     *
     * @param sourceFolder 需压缩文件 或者 文件夹 路径
     * @param zipFilePath  压缩文件输出路径
     */
    public static void zip(String sourceFolder, String zipFilePath) {
        logger.debug("开始压缩 [" + sourceFolder + "] 到 [" + zipFilePath + "]");
        // 默认下载到桌面
        if (StringUtils.isEmpty(zipFilePath)) {
            zipFilePath = FileSystemView.getFileSystemView().getHomeDirectory().getPath() + ZIP_NAME;
        }
        try {
            OutputStream out = new FileOutputStream(zipFilePath);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            ZipOutputStream zos = new ZipOutputStream(bos);
            // 解决中文文件名乱码
            zos.setEncoding(CHINESE_CHARSET);
            File file = new File(sourceFolder);
            String basePath;
            if (file.isDirectory()) {
                basePath = file.getPath();
            } else {
                basePath = file.getParent();
            }
            zipFile(file, basePath, zos);
            zos.flush();
            zos.closeEntry();
            zos.close();
            bos.flush();
            bos.close();
            out.flush();
            out.close();
            logger.debug("压缩 [" + sourceFolder + "] 完成！");
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }

    /**
     * <p>
     * 压缩文件
     * </p>
     *
     * @param files       一组 压缩文件夹 或 文件
     * @param zipFilePath 压缩文件输出路径
     */
    public static File zip(List<File> files, String zipFilePath) {
        File zipFile;
        // 默认下载到桌面
        if (StringUtils.isEmpty(zipFilePath)) {
            zipFilePath = FileSystemView.getFileSystemView().getHomeDirectory().getPath() + ZIP_NAME;
        } else {
            zipFilePath = zipFilePath + ZIP_NAME;
        }
        try {
            OutputStream out = new FileOutputStream(zipFilePath);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            ZipOutputStream zos = new ZipOutputStream(bos);
            // 解决中文文件名乱码
            zos.setEncoding(CHINESE_CHARSET);
            for (File file : files) {
                logger.debug("开始压缩 [" + file.getName() + "] 到 [" + zipFilePath + "]");
                String basePath = file.getParent();
                zipFile(file, basePath, zos);
            }
            zipFile = new File(zipFilePath);
            zos.closeEntry();
            zos.flush();
            zos.close();
            bos.flush();
            bos.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error(e.toString(), e);
            return null;
        }
        return zipFile;
    }

    /**
     * <p>
     * 递归压缩文件
     * </p>
     *
     * @param parentFile
     * @param basePath
     * @param zos
     * @throws Exception
     */
    private static void zipFile(File parentFile, String basePath, ZipOutputStream zos) throws Exception {
        File[] files;
        if (parentFile.isDirectory()) {
            files = parentFile.listFiles();
        } else {
            files = new File[1];
            files[0] = parentFile;
        }
        String pathName;
        InputStream is;
        BufferedInputStream bis;
        byte[] cache = new byte[CACHE_SIZE];
        for (File file : files) {
            if (file.isDirectory()) {
                logger.debug("目录：" + file.getPath());
                basePath = basePath.replace('\\', '/');
                if (basePath.substring(basePath.length() - 1).equals("/")) {
                    pathName = file.getPath().substring(basePath.length()) + "/";
                } else {
                    pathName = file.getPath().substring(basePath.length() + 1) + "/";
                }
                zos.putNextEntry(new ZipEntry(pathName));
                zipFile(file, basePath, zos);
            } else {
                pathName = file.getPath().substring(basePath.length());
                pathName = pathName.replace('\\', '/');
                if (pathName.substring(0, 1).equals("/")) {
                    pathName = pathName.substring(1);
                }
                logger.debug("压缩：" + pathName);
                is = new FileInputStream(file);
                bis = new BufferedInputStream(is);
                zos.putNextEntry(new ZipEntry(pathName));
                int nRead;
                while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                    zos.write(cache, 0, nRead);
                }
                bis.close();
                is.close();
            }
        }
    }

    /**
     * 解压zip文件
     *
     * @param zipFileName     待解压的zip文件路径，例如：c:\\a.zip
     * @param outputDirectory 解压目标文件夹,例如：c:\\a\
     */
    public static void unZip(String zipFileName, String outputDirectory)
            throws Exception {
        logger.debug("开始解压 [" + zipFileName + "] 到 [" + outputDirectory + "]");
        ZipFile zipFile = new ZipFile(zipFileName);
        try {
            Enumeration<?> e = zipFile.getEntries();
            ZipEntry zipEntry;
            createDirectory(outputDirectory, "");
            while (e.hasMoreElements()) {
                zipEntry = (ZipEntry) e.nextElement();
                logger.debug("解压：" + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    String name = zipEntry.getName();
                    name = name.substring(0, name.length() - 1);
                    File f = new File(outputDirectory + File.separator + name);
                    f.mkdir();
                    logger.debug("创建目录：" + outputDirectory + File.separator + name);
                } else {
                    String fileName = zipEntry.getName();
                    fileName = fileName.replace('\\', '/');
                    if (fileName.contains("/")) {
                        createDirectory(outputDirectory, fileName.substring(0, fileName.lastIndexOf("/")));
                    }
                    File f = new File(outputDirectory + File.separator + zipEntry.getName());
                    f.createNewFile();
                    InputStream in = zipFile.getInputStream(zipEntry);
                    FileOutputStream out = new FileOutputStream(f);
                    byte[] by = new byte[1024];
                    int c;
                    while ((c = in.read(by)) != -1) {
                        out.write(by, 0, c);
                    }
                    in.close();
                    out.close();
                }
            }
            logger.debug("解压 [" + zipFileName + "] 完成！");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            zipFile.close();
        }
    }

    /**
     * 创建目录
     *
     * @param directory
     * @param subDirectory
     * @author hezhao
     * @Time 2017年7月28日 下午7:10:05
     */
    private static void createDirectory(String directory, String subDirectory) {
        String dir[];
        File fl = new File(directory);
        try {
            if ("".equals(subDirectory) && fl.exists()) {
                fl.mkdir();
            } else if (!"".equals(subDirectory)) {
                dir = subDirectory.replace('\\', '/').split("/");
                for (String str : dir) {
                    File subFile = new File(directory + File.separator + str);
                    if (!subFile.exists()) {
                        subFile.mkdir();
                    }
                    directory = directory + File.separator + str;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * 无需解压直接读取Zip文件和文件内容
     *
     * @param file 文件
     * @throws Exception
     * @author hezhao
     * @Time 2017年7月28日 下午3:23:10
     */
    public static void readZipFile(String file) throws Exception {
        java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ZipInputStream zin = new ZipInputStream(in);
        java.util.zip.ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.isDirectory()) {
            } else {
                logger.info("file - " + ze.getName() + " : " + ze.getSize() + " bytes");
                long size = ze.getSize();
                if (size > 0) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)));
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                    br.close();
                }
            }
        }
        zin.closeEntry();
    }

    public static void main(String[] args) {
        try {
            // readZipFile("F:/MiFlashvcom.zip");

            // 压缩文件
            String sourceFolder = "F:/统计外卖单.xlsx";
            String zipFilePath = "";
            ZipUtil.zip(sourceFolder, zipFilePath);

//            // 压缩文件夹
//            sourceFolder = "D:/fsc1";
//            zipFilePath = "D:/fsc1.zip";
//            ZipUtil.zip(sourceFolder, zipFilePath);
//
//            // 压缩一组文件
//            String[] paths = {"D:/新建文本文档.txt", "D:\\FastStoneCapturecn.zip", "D:/new1"};
//            zip(paths, "D:/abc.zip");
//
//            unZip("D:\\FastStoneCapturecn.zip", "D:/fsc2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
