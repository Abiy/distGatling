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

package com.walmart.gatling.endpoint.v1;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

/**
 * Created by walmart
 * This jersey controller exposes http end points for user to view log files ,
 * master also uses this end points to pull simulation log files that are collected to generate reports
 */
@Path("/log/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogResource {

    private final Logger log = LoggerFactory.getLogger(LogResource.class);
    @Context
    UriInfo uriInfo;


    @Inject
    public LogResource(){

    }


    @GET
    @Path("file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@QueryParam("filePath") String filePath) {

        try {
            File logFile = new File(filePath);
            return Response.ok(logFile, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + logFile.getName() + "\"" ) //optional
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

    }

    @GET
    @Path("stream")
    @Produces("text/plain")
    public Response getStream(@QueryParam("filePath") String filePath) {

        try {
            File f = new File(filePath);
            log.info("File: {}",filePath);
            final FileInputStream fStream = new FileInputStream(f);
            StreamingOutput stream = output -> {
                try {
                    pipe(fStream, output);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            };
            return Response.ok(stream).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).build();
        }

    }

    public void pipe(InputStream is, OutputStream os) throws IOException {
        int n;
        byte[] buffer = new byte[1024];
        while ((n = is.read(buffer)) > -1) {
            os.write(buffer, 0, n);   // Don't allow any extra bytes to creep in, final write
        }
        os.close();
    }



}
