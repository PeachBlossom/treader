package com.zijie.treader.util;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/11 0011.
 */
public class FileUtils {

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
