package com.zijie.treader.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.zijie.treader.FileActivity;
import com.zijie.treader.db.BookList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/22.
 */
public class Fileutil {

    public static String name ;

    public static int folderNum = 0;

    /**
     * 计算目录大小
     *
     * @param dir
     * @return
     */

    public static long getDirSize(File dir) {
        if (dir == null) {
            return 0;
        }
        // 不是目录
        if (!dir.isDirectory()) {
            return 0;
        }
        long dirSize = 0;

        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                dirSize += file.length();
            } else if (file.isDirectory()) {
                dirSize += file.length();
                // 递归调用
                dirSize += getDirSize(file);
            }

        }
        return dirSize;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return B/KB/MB/GB
     */
    public static String formatFileSize(long fileS) {

        if (fileS == 0) {
            return "0.00B";
        }

        DecimalFormat dFormat = new DecimalFormat("#.00");

        String fileSizeString = "";

        if (fileS < 1024) {
            fileSizeString = dFormat.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = dFormat.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = dFormat.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = dFormat.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 文件目录地址
     *
     * @return
     */
    public static String fileDirectory(String dirPath, String fileName) {
        String filePath = "";

        String storageState = Environment.getExternalStorageState();

        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            filePath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + dirPath;

            File file = new File(filePath);

            if (!file.exists()) {
                // 建立一个新的目录
                file.mkdirs();
            }
            filePath = filePath + fileName;
        }

        return filePath;
    }

    /**
     * 获取文件目录
     *
     * @return
     */
    public static File getDirectoryFile(String dirPath) {

        String storageState = Environment.getExternalStorageState();

        File file = null;

        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            String filePath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + dirPath;

            file = new File(filePath);

            if (!file.exists()) {
                // 建立一个新的目录
                file.mkdirs();
            }
        }

        return file;
    }

    /**
     * 检查文件后缀
     *
     * @param checkItsEnd
     * @param fileEndings
     * @return
     */
    private static boolean checkEndsWithInStringArray(String checkItsEnd,
                                                      String[] fileEndings) {
        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd))
                return true;
        }
        return false;
    }

    /**
     * 根据不同的后缀打开不同的文件
     *
     * @param fileName
     */
  /**  public static void openFile(Context context, String fileName, File file) {
        Intent intent;
        if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingImage))) {
            intent = OpenFiles.getImageFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingWebText))) {
            intent = OpenFiles.getHtmlFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingPackage))) {
            intent = OpenFiles.getApkFileIntent(file);
            context.startActivity(intent);

        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingAudio))) {
            intent = OpenFiles.getAudioFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingVideo))) {
            intent = OpenFiles.getVideoFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingText))) {
            intent = OpenFiles.getTextFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingPdf))) {
            intent = OpenFiles.getPdfFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingWord))) {
            intent = OpenFiles.getWordFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingExcel))) {
            intent = OpenFiles.getExcelFileIntent(file);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingPPT))) {
            intent = OpenFiles.getPPTFileIntent(file);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "打开文件错误", Toast.LENGTH_SHORT).show();
        }
    }     */

    /**
     * 根据不同的后缀imageView设置不同的值
     *
     * @param fileName
     */
 /**   public static void setImage(Context context, String fileName,
                                ImageView imageView) {
        if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingImage))) {
            imageView.setImageResource(R.drawable.file_icon_picture);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingWebText))) {
            imageView.setImageResource(R.drawable.file_icon_txt);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingPackage))) {
            imageView.setImageResource(R.drawable.file_icon_rar);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingAudio))) {
            imageView.setImageResource(R.drawable.file_icon_mp3);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingVideo))) {
            imageView.setImageResource(R.drawable.file_icon_video);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingText))) {
            imageView.setImageResource(R.drawable.file_icon_txt);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingPdf))) {
            imageView.setImageResource(R.drawable.file_icon_pdf);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingWord))) {
            imageView.setImageResource(R.drawable.file_icon_office);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingExcel))) {
            imageView.setImageResource(R.drawable.file_icon_office);
        } else if (checkEndsWithInStringArray(fileName, context.getResources()
                .getStringArray(R.array.fileEndingPPT))) {
            imageView.setImageResource(R.drawable.file_icon_office);
        } else {
            imageView.setImageResource(R.drawable.file);
        }
    }    */

    /**
     *
     * 返回本地文件列表
     *
     * @param //本地文件夹路径
     */
    public static List<File> getFileListByPath( String path) {
        BookList bookList = new BookList();

        int fileNum = 0 ;

        File dir = new File(path);

        List<File> folderList = new ArrayList<File>();

        List<File> fileList = new ArrayList<File>();

        // 获取指定盘符下的所有文件列表。（listFiles可以获得指定路径下的所有文件，以数组方式返回）
        File[] files = dir.listFiles();
        // 如果该目录下面为空，则该目录的此方法执行
        if (files == null) {
            return folderList;
        }

        // 通过循环将所遍历所有文件
        for (int i = 0; i < files.length; i++) {

            if (!files[i].isHidden()) {

                if (files[i].isDirectory()) {
                    folderList.add(files[i]);
                    folderNum++;
                }
                if (files[i].isFile()) {

                    if (files[i].toString().contains(".txt")) {           //txt".equals(extName)
                        fileList.add(files[i]);
                     //   name = files[i].toString();
//                        FileActivity.paths.add(files[i].toString());

                    }

                }

            }
            Log.d("Fileutil", folderNum + "");
        }
        folderList.addAll(fileList);

        return folderList;


    }

    public static int getFileNum (List<File> list) {
        File file;
        int num = 0;
        for (int i = 0 ; i<list.size(); i++) {
            file = list.get(i);
            if (file.isFile()){
                num++;
            }
        }

        return num;
    }


    /**
     * 复制一个目录及其子目录、文件到另外一个目录
     *
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copyFolder(File src, File dest) {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归复制
                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in;
            OutputStream out;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];

                int length;

                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * @param src
     *            源文件路径
     * @param name
     *            源文件名字
     * @param dest
     *            目标目录
     */
    public static void copyFile(File src, String name, File dest) {
        File file = new File(dest, name);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(src);
            outputStream = new FileOutputStream(file);
            byte[] b = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, len);
            }

            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 创建目录或文件
     */
    public static void createDirorFile(String path, String name, Context context,int check) {
        File file = new File(path + File.separator + name);

        if (check==0) {// 如果为文件
            try {
                file.createNewFile();
                Toast.makeText(context, "创建文件成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(context, "创建文件失败", Toast.LENGTH_SHORT).show();
            }
        } else if(check==1){
            // 创建目录
            if (file.mkdirs()) {
                Toast.makeText(context, "创建目录成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "创建目录失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 删除一个目录
     */
    public static void deleteDir(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDir(file); // 递规的方式删除文件夹
        }
        dir.delete();
    }

    /**
     * @param fromDir
     *            这个为源目录
     * @param fromPath
     *            这个为源目录的上一级路径
     * @param toName
     *            要修改的名字
     */
    public static boolean renameFile(File fromDir, String fromPath,
                                     String toName) {
        File tempFile = new File(fromPath + File.separator + toName);
        if (tempFile.exists()) {
            return false;
        } else {
            return fromDir.renameTo(tempFile);
        }
    }

    /** 去掉文件扩展名
     *
     *
     * */
    public static String getFileNameNoEx (String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
