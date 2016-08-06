package com.hw.diaosiclock.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * Created by hw on 2016/6/4.
 */
public class AlarmMusicUtil {
    public final static String ERRTAG = "AlarmMusicUtil";

    //搜索Assets文件中的音乐文件，并列出来
    public static void SearchAssetsMusic(Context context, List<String> list, String path, String[] keywords) {
        String fileName;
        if(null == list || null == path || null == keywords) {
            Log.e(ERRTAG, "list or path or keywords is null");
            return;
        }

        try {
            String[] names =  context.getAssets().list(path);

            //说明names是个目录，因为下面有多个文件
            if(names.length > 0) {
                for(int i = 0; i < names.length; i++) {
                    SearchAssetsMusic(context, list, path + "/" + names[i],  keywords);
                }
            }else {
                //说明names是个文件
                String[] str = path.split("/");
                fileName = str[str.length - 1];

                //看文件名中是否包含关键词，是的话就加进list中
                for(int i = 0; i < keywords.length; i++) {
                    if(CompareMusicExtension(fileName, keywords[i])) {
                        list.add(fileName);
                        break;
                    }
                }
            }
        }catch (Exception e) {
            Log.e(ERRTAG, "SearchAssetsMusic error");
            Log.getStackTraceString(e);
        }

    }

    public static void SearchAllFile(List<String> list, File path, String[] keywords) {
        String fileName = null;

        if(null == list || null == path || null == keywords) {
            Log.e(ERRTAG, "list or path or keywords is null");
            return;
        }

        if(!path.isDirectory()) {
            Log.e(ERRTAG, "file path is not a directory");
            return;
        }

        File[] files = new File(path.getPath()).listFiles();

        for(File file:files) {
            //若发现file还是个目录，则递归处理自己
            if(file.isDirectory()) {
                //此处为了提高搜索效率，文件夹名超过10个字符长度不进行搜索
                if(file.getName().length() <= 10) {
                    SearchAllFile(list, file, keywords);
                }
            }else {
                for(int i = 0; i < keywords.length; i++) {
                    fileName = "";
                    if(file.getName().contains(keywords[i])) {
                        fileName += file.getPath() + "/" + file.getName() + "\n";
                        break;
                    }
                }

                if(null == fileName) {
                    Log.e(ERRTAG, "filename is null");
                }else {
                    if(fileName.equals("")) {
                        Log.e(ERRTAG, file.getPath() + "has no file");
                    }
                }
            }
        }
    }

    public static boolean CompareMusicExtension(final String MusicName, final String KeyWord) {
        if(null == MusicName || null == KeyWord) {
            return false;
        }
        int MusicNameLen = MusicName.length();
        int KeyWordLen = KeyWord.length();

        if(MusicNameLen <= KeyWordLen) {
            return false;
        }

        int i = 1;
        while(i <= KeyWordLen) {
            if(MusicName.charAt(MusicNameLen - i) != KeyWord.charAt(KeyWordLen - i)) {
                return false;
            }
            i++;
        }
        return true;
    }
}
