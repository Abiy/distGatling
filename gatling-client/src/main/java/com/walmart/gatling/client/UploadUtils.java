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

package com.walmart.gatling.client;

import com.walmart.gatling.commons.HostUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;

/**
 *  on 5/16/17.
 */
public class UploadUtils {
    protected static final Log logger = LogFactory.getLog(UploadUtils.class);

    public static String uploadFile(String server, String path,String basicToken)
        throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create()
                .build();


        HttpPost post = new HttpPost( server + "/uploadFile");
        post.setHeader("Authorization",basicToken);
        //File jarFile = new File(jarFilePath);
        File dataFeedFile = new File(path);
        String message = HostUtils.lookupIp();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        //builder.addBinaryBody("jarFile", jarFile, ContentType.DEFAULT_BINARY, jarFile.getName());
        builder.addBinaryBody("file", dataFeedFile, ContentType.DEFAULT_BINARY, dataFeedFile.getName());
        builder.addTextBody("client", message, ContentType.DEFAULT_BINARY);

        HttpEntity entity = builder.build();
        post.setEntity(entity);
        try {
            HttpResponse response = client.execute(post);
            final int statusCode = response.getStatusLine()
                    .getStatusCode();
            String result = IOUtils.toString(response.getEntity().getContent());
            logger.info(result);
            if (statusCode != 200){
                throw new RuntimeException("Upload Failed");
            }
            return result;
        } catch (IOException e) {
            throw e;
        }
        finally {
            try {
                client.close();
            } catch (IOException e) {
               throw e;
            }
        }
    }
}
