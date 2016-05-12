package com.cylan.entity;

import com.cylan.support.DswLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 主要是用于压缩打包日志
 * Created by Tim on 2015/6/17.
 * <p/>
 * Adjust by QiangNy 2016/5/11.
 */
public class ZipLog {

    private static final int DEFAULT = -520;
    /**
     * zip文件名
     */
    private String zipName;
    /**
     * zip文件路径
     */
    private String zipPath;
    /**
     * 添加需要过滤文件的后缀名链表
     */
    private List<String> fileMimeTypeList;
    /**
     * 限定文件夹目录层级
     */
    private int dirLevel;
    /**
     * 限定被压缩文件大小单位KB
     */
    private long fileSize_KB;
    /**
     * 限定被压缩文件大小单位MB,优先于fileSize_KB
     */
    private long fileSize_MB;
    /**
     * 加密密码
     */
    private String encryptPasswd;
    /**
     * 反向筛选后缀名开关
     */
    private boolean switchRevFilterMime;
    /**
     * 添加被压缩源文件路径链表
     */
    private ArrayList<File> logtextPathList;

    private static ZipLog instance = new ZipLog();

    public ZipLog() {
    }

    /**
     * ZipLog class field init
     */
    private static void initZiplog(Builder builder) {
        instance.zipName = builder.zipName;
        instance.zipPath = builder.zipPath;
        instance.fileMimeTypeList = builder.fileMimeTypeList;
        instance.dirLevel = builder.dirLevel;
        instance.fileSize_KB = builder.fileSize_KB;
        instance.fileSize_MB = builder.fileSize_MB;
        instance.encryptPasswd = builder.encryptPasswd;
        instance.switchRevFilterMime = builder.switchRevFilterMime;
        instance.logtextPathList = builder.logtextPathList;
    }

    public static class Builder {
        public String zipName;
        private String zipPath;
        private List<String> fileMimeTypeList;
        private int dirLevel;
        private long fileSize_KB;
        private long fileSize_MB;
        private String encryptPasswd;
        private boolean switchRevFilterMime;
        private ArrayList<File> logtextPathList;

        public Builder() {
            initBuilder();
        }

        /**
         * Builder class field init
         */
        private void initBuilder() {
            if (fileMimeTypeList == null) fileMimeTypeList = new ArrayList<String>();
            if (logtextPathList == null) logtextPathList = new ArrayList<File>();
            fileMimeTypeList.clear();
            logtextPathList.clear();
            zipName = null;
            zipPath = null;
            dirLevel = DEFAULT;
            fileSize_KB = DEFAULT;
            fileSize_MB = DEFAULT;
            encryptPasswd = null;
            switchRevFilterMime = false;
        }

        /**
         * set zip name
         *
         * @param zipName name of zip file
         * @return Buider
         */
        public Builder setZipName(String zipName) {
            this.zipName = zipName;
            return this;
        }

        /**
         * set the zip path
         *
         * @param zipPath it will create zip file here
         * @return Builder
         */
        public Builder setZipPath(String zipPath) {
            this.zipPath = zipPath;
            return this;
        }

        /**
         * the file filter which filter the file extension equel the fileMImeType
         *
         * @param fileMImeType you will add the extension one by one
         * @return Builder
         */
        public Builder setFileMImeType(String fileMImeType) {
            this.fileMimeTypeList.add(fileMImeType);
            return this;
        }

        /**
         * the file filter which filter the file extension equel the fileMImeType
         *
         * @param fileMimeTypeList you will add the extension for one time,default is closed
         * @return Builder
         */
        public Builder setFileMimeTypeList(List<String> fileMimeTypeList) {
            this.fileMimeTypeList = fileMimeTypeList;
            return this;
        }

        /**
         * set the the subdir deep of the dir that you want
         *
         * @param dirLevel 0 means current dir,1 means the first subdir and so on,suggeset you set a positive number
         * @return Builder
         */
        public Builder setDirLevel(int dirLevel) {
            this.dirLevel = dirLevel;
            return this;
        }

        /**
         * the file filter which filter the file size more than fileSize_KB
         *
         * @param fileSize_KB limit value,default is cloesed
         * @return Builder
         */
        public Builder setFileSize_KB(long fileSize_KB) {
            this.fileSize_KB = fileSize_KB;
            return this;
        }

        /**
         * the file filter which filter the file size more than fileSize_MB andr it's priority over fileSize_KB
         * i
         *
         * @param fileSize_MB limit value,default is closed
         * @return Builder
         */
        public Builder setFileSize_MB(long fileSize_MB) {
            this.fileSize_MB = fileSize_MB;
            return this;
        }

        /**
         * the passwd of encrypt the zip file,it has not been ready for now
         *
         * @param encryptPasswd passwd String,default is closed
         * @return Builder
         */
        public Builder setEncryptPasswd(String encryptPasswd) {
            this.encryptPasswd = encryptPasswd;
            return this;
        }

        /**
         * the file filter which filter the file extension equel the fileMImeType on contrary
         *
         * @param switchRevFilterMime default value is false
         * @return Builder
         */
        public Builder setSwitchRevFilterMime(boolean switchRevFilterMime) {
            this.switchRevFilterMime = switchRevFilterMime;
            return this;
        }

        /**
         * add the source path that it will be compressed in zip
         *
         * @param filepath add the source one by one
         * @return Builder
         */
        public Builder addLogtextPath(String filepath) {
            logtextPathList.add(new File(filepath));
            return this;
        }

        /**
         * add the source path that it will be compressed in zip
         *
         * @param logtextPathList add the source for one time
         * @return Builder
         */
        public Builder addLogtextPathList(ArrayList<File> logtextPathList) {
            this.logtextPathList = logtextPathList;
            return this;
        }

        /**
         * set all param value for one time
         *
         * @param zipName             set zip name
         * @param zipPath             set zip path
         * @param fileMimeTypeList    add extension list
         * @param dirLevel            set subdir deep
         * @param fileSize_KB         set file limit size
         * @param fileSize_MB         set file limit size,priority over fileSize_KB
         * @param switchRevFilterMime reverse mime filter
         * @param logtextPathList     add source path list
         */
        public Builder setDefaultValue(String zipName,
                                       String zipPath,
                                       List<String> fileMimeTypeList,
                                       int dirLevel,
                                       long fileSize_KB,
                                       long fileSize_MB,
                                       boolean switchRevFilterMime,
                                       ArrayList<File> logtextPathList) {
            this.zipName = zipName;
            this.zipPath = zipPath;
            this.fileMimeTypeList = fileMimeTypeList;
            this.dirLevel = dirLevel;
            this.fileSize_KB = fileSize_KB;
            this.fileSize_MB = fileSize_MB;
            this.switchRevFilterMime = switchRevFilterMime;
            this.logtextPathList = logtextPathList;
            return this;
        }

        public ZipLog build() {
            return getInstance(this);
        }

    }

    public static ZipLog getInstance(Builder builder) {
        initZiplog(builder);
        return instance;
    }

    public String mCompAndGetZip() {
        packZip(logtextPathList);
        return getZipFilePath();
    }

    public void delZip() {
        new File(getZipFilePath()).delete();
    }

    public boolean zipExists() {
        File zip = new File(getZipFilePath());
        return zip.exists();
    }

    private void startZipEncrypt() throws IOException {
        //......

    }

    private String getZipFilePath() {
        return zipPath + File.separator + zipName;
    }

    public synchronized void packZip(ArrayList<File> lists) {
        if (lists != null && lists.size() > 0) {
            if (zipExists()) delZip();
            try {
                toZip(lists);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩list文件/文件夹列表
     *
     * @param lists 要压缩的list目录
     * @throws Exception
     */
    private void toZip(List<File> lists) throws Exception {
        //创建zip包
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(getZipFilePath()));
        //压缩
        for (File file : lists) {
            write2Zip(file, outZip);
        }
        //完成,关闭
        outZip.finish();
        outZip.close();
        //加密zip
        if (encryptPasswd != null && encryptPasswd.length() > 0) startZipEncrypt();
    }

    /**
     * 压缩文件/文件夹压
     *
     * @param file           文件名
     * @param zipOutputSteam 压缩包
     * @throws Exception
     */
    public void write2Zip(File file, ZipOutputStream zipOutputSteam) throws Exception {

        if (!file.exists()) {
            DswLog.i("ZipLog: this file is not exist and the path is " + file.getAbsolutePath());
            //add the log source file is not exits
            return;
        }

        if (zipOutputSteam == null)
            return;

        //判断是不是文件
        if (file.isFile()) {
            // 创建一个文件输入流
            FileInputStream inputStream = new FileInputStream(file);
            // 做一个ZipEntry
            ZipEntry entry = new ZipEntry(file.getName());
            // 存储项信息到压缩文件
            zipOutputSteam.putNextEntry(entry);
            // 复制字节到压缩文件
            int bytes_read;
            // 创建复制缓冲区
            byte[] buffer = new byte[4096];
            while ((bytes_read = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, bytes_read);
            }
            zipOutputSteam.closeEntry();
            inputStream.close();
        } else {
            if (!checkOpenDirLevel()) {
                return;
            }
            //筛选并获取文件夹下的子文件
            String fileList[] = file.list(SFILE_FILTER);
            //如果没有子文件, 则添加进去即可
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //如果有子文件, 遍历子文件
            for (String subString : fileList) {
                File subfile = new File(file.getAbsolutePath() + File.separator + subString);
                write2Zip(subfile, zipOutputSteam);
            }
        }
    }

    private boolean checkOpenDirLevel() {
        if (dirLevel > 0) {
            dirLevel--;
            return true;
        } else {
            return dirLevel == DEFAULT;
        }
    }

    public final FilenameFilter SFILE_FILTER = new FilenameFilter() {

        public boolean accept(File dir, String filename) {
            if (dir.isHidden() || filename.startsWith(".") || filename.startsWith("_")) {
                return false;
            }
            boolean status = true;
            if (fileMimeTypeList != null && fileMimeTypeList.size() > 0) {
                status = switchRevFilterMime;
                for (String extension : fileMimeTypeList)
                    if (filename.endsWith(extension)) status = !switchRevFilterMime;
            }

            File curFile = new File(dir.getAbsolutePath() + File.separator + filename);

            if (status && fileSize_MB != DEFAULT && fileSize_MB > 0) {
                long size_MB = curFile.length() / 1024 / 1024;
                if (size_MB >= fileSize_MB) status = false;
            }
            if (status && fileSize_KB != DEFAULT && fileSize_KB > 0) {
                long size_KB = curFile.length() / 1024;
                if (size_KB >= fileSize_KB) status = false;
            }

            if (!curFile.isFile())
                status = true;
            return status;
        }
    };
}