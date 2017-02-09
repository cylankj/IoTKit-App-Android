package com.cylan.jiafeigou.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;

/**
 * Created by yzd on 16-12-9.
 */

public class FileUtils {

    private final static String fileName = "smarttemp.txt";

    public static void copyFile(File in, File out) {
        if (in == null || out == null || !in.canRead()) {
            return;
        }

        if (!out.getParentFile().exists()) out.getParentFile().mkdirs();

        FileInputStream fin = null;
        FileOutputStream fos = null;
        try {
            fin = new FileInputStream(in);
            fos = new FileOutputStream(out);
            byte[] buffer = new byte[1024];
            int len;
            while (((len = fin.read(buffer)) != -1)) {
                fos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向File中保存数据
     */
    public static void saveDataToFile(Context context, String data){
        FileOutputStream fileOutputStream=null;
        OutputStreamWriter outputStreamWriter=null;
        BufferedWriter bufferedWriter=null;
        try {
            fileOutputStream=context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStreamWriter=new OutputStreamWriter(fileOutputStream);
            bufferedWriter=new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (bufferedWriter!=null) {
                    bufferedWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 从File中读取数据
     */
    public static String getDataFromFile(Context context){

        File file = new File(context.getFilesDir(),"smarttemp.txt");
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        FileInputStream fileInputStream=null;
        InputStreamReader inputStreamReader=null;
        BufferedReader bufferedReader=null;
        StringBuilder stringBuilder=null;
        String line=null;
        try {
            stringBuilder=new StringBuilder();
            fileInputStream=context.openFileInput(fileName);
            inputStreamReader=new InputStreamReader(fileInputStream);
            bufferedReader=new BufferedReader(inputStreamReader);
            while((line=bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (bufferedReader!=null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
