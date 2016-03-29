package com.walmart.gatling.commons;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * Created by walmart on 3/28/16.
 */
public class DownloadFile {
    private static final int READ_TIMEOUT = 1000 * 60;
    private static final int CONNECTION_TIMEOUT = 1000 * 5;

    public static boolean downloadFile(String path,String destPath){
        try {
            FileUtils.copyURLToFile(new URL(path),new File(destPath),CONNECTION_TIMEOUT,READ_TIMEOUT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}



