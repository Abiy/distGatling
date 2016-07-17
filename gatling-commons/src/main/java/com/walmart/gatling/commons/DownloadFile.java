/*
 *
 *   Copyright 2016 Walmart Technology
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

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



