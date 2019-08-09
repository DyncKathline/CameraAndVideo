package com.sparksoft.cameraandvideo;

import android.util.Log;

/**
 * Created by jiapeng on 16/07/2017.
 */

public class Config {
    private static final String domain = "192.168.1.5:8080";
//    private static final String domain = "10.10.11.101:8080";
    public static final String videoUrl1 = "http://baobab.wdjcdn.com/1457423930928CGI.mp4";
    public static final String videoUrl2 = "http://baobab.wdjcdn.com/1457423930928CGI.mp4";

    public static void logd(String content) {

        Log.d("jiapeng", content);
    }

}
