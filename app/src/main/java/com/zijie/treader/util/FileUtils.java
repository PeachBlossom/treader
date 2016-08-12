package com.zijie.treader.util;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/11 0011.
 */
public class FileUtils {

    /**
     * 获取文件编码
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String getCharset(String fileName) throws IOException{
        String charset;
        FileInputStream fis = new FileInputStream(fileName);
        byte[] buf = new byte[4096];
        // (1)
        UniversalDetector detector = new UniversalDetector(null);
        // (2)
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        // (3)
        detector.dataEnd();
        // (4)
        charset = detector.getDetectedCharset();
        // (5)
        detector.reset();
        return charset;
    }

    /**
     * 根据路径获取文件名
     * @param pathandname
     * @return
     */
    public static String getFileName(String pathandname){
        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            return pathandname.substring(start+1,end);
        }else{
            return "";
        }

    }

    public static  List<File> getSuffixFile(String filePath, String suffere){
        List<File> files = new ArrayList<>();
        File f = new File(filePath);
        return getSuffixFile(files,f,suffere);
    }

    /**
     * 读取sd卡上指定后缀的所有文件
     * @param files 返回的所有文件
     * @param f 路径(可传入sd卡路径)
     * @param suffere 后缀名称 比如 .gif
     * @return
     */
    public static  List<File> getSuffixFile(List<File> files, File f, final String suffere) {
        if (!f.exists()) {
            return null;
        }

        File[] subFiles = f.listFiles();
        for (File subFile : subFiles) {
            if (subFile.isHidden()){
                continue;
            }
            if(subFile.isDirectory()){
                getSuffixFile(files, subFile, suffere);
            }else if(subFile.getName().endsWith(suffere)){
                files.add(subFile);
            } else{
                //非指定目录文件 不做处理
            }
//            Log.e("filename",subFile.getName());
        }
        return files;
    }

}
