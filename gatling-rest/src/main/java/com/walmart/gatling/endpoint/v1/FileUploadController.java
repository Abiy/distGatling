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

import com.walmart.gatling.repository.ServerRepository;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spring mvc controller which exposes the http end point that accepts user file uploads
 * User could upload a lib files, conf files, simulation scripts and simulation data files
 * Files are staged to a temporary directory specified using file.repository configuration property
 */
@org.springframework.web.bind.annotation.RestController
public class FileUploadController {
    private final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    private ServerRepository serverRepository;

    @Value("${file.repository}")
    private String tempFileDir;

    @Autowired
    public FileUploadController(ServerRepository serverRepository){
        this.serverRepository = serverRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/upload")
    public String provideUploadInfo(Model model) throws IOException {
        File rootFolder = new File(tempFileDir);
        if (!rootFolder.exists()) {
            rootFolder.mkdir();
        }
        rootFolder = new File(tempFileDir);

        model.addAttribute("files",
                Arrays.stream(rootFolder.listFiles())
                        .sorted(Comparator.comparingLong(f -> -1 * f.lastModified()))
                        .map(f -> f.getName())
                        .collect(Collectors.toList())
        );

        return "uploadForm";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public SubmitResult handleFileUpload(MultipartHttpServletRequest request,  @RequestParam("file") MultipartFile file) {

        Map<String, String[]> paramMap = request.getParameterMap();
        String packageName = getValue(paramMap, "packageName"),  partitionName = getValue(paramMap, "partitionName");
        String fileName = packageName.replace('.','/') + ".scala";
        String trackingId = "";
        SimulationJobModel job  = new SimulationJobModel();
        if (!file.isEmpty()) {
            try {
                String path = tempFileDir + "/" + fileName;
                FileUtils.touch(new File(path));
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File(path)));
                FileCopyUtils.copy(file.getInputStream(), stream);
                stream.close();
                job = new SimulationJobModel();
                    job.setCount(getValue(paramMap,"parallelism").equals("") ? 0:Short.valueOf(getValue(paramMap,"parallelism")));
                    job.setPartitionAccessKey(getValue(paramMap,"accessKey"));
                    job.setRoleId(partitionName);
                    job.setTag(getValue(paramMap, "tag"));
                    job.setUser(getValue(paramMap, "userName"));
                    job.setSimulation(path);
                    job.setFileFullName(fileName);
                log.info("Submitting job: {}", job);
                Optional<String> tId = serverRepository.submitSimulationJob(job);
                trackingId = tId.get();
            } catch (Exception e) {
                log.error("Error uploading simulation {}", e);
                return new SubmitResult(false,"",job);
            }
        } else {
            return new SubmitResult(false,"",job);
        }

        return new SubmitResult(true,trackingId,job);
    }

    private String getValue(Map<String, String[]> paramMap, String key){
        if (!paramMap.containsKey(key))
            return "";
        if (paramMap.get(key).length < 1)
            return "";
        return paramMap.get(key)[0];
    }

}