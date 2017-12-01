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

import com.walmart.gatling.domain.SimulationJobModel;
import com.walmart.gatling.domain.SubmitResult;
import com.walmart.gatling.service.ServerRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    public FileUploadController(ServerRepository serverRepository) {
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

    @RequestMapping(method = RequestMethod.POST, value = "/uploadFile")
    public String uploadFile(MultipartHttpServletRequest request, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            String filePath = tempFileDir + "/" + UUID.randomUUID().toString() + "/" + file.getOriginalFilename();
            try {
                FileUtils.touch(new File(filePath));

                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File(filePath)));
                FileCopyUtils.copy(file.getInputStream(), stream);
                stream.close();
                return filePath;
            } catch (IOException e) {
                log.error("Error uploading file {}", e);
            }
        }
        return "";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public SubmitResult uploadAndRunSimulation(MultipartHttpServletRequest request, @RequestParam("simulationFile") MultipartFile simulationFile) {
        MultipartFile dataFile = request.getFile("dataFile");
        Map<String, String[]> paramMap = request.getParameterMap();
        String packageName = getValue(paramMap, "packageName"), partitionName = getValue(paramMap, "partitionName");
        String fileName = packageName.replace('.', '/') + ".scala";
        String trackingId = "";
        SimulationJobModel job = new SimulationJobModel();
        String dataFilePath = "";//should be empty by default
        if (!simulationFile.isEmpty()) {
            try {
                if (dataFile != null && !dataFile.isEmpty()) {
                    dataFilePath = tempFileDir + "/" + dataFile.getOriginalFilename();
                    FileUtils.touch(new File(dataFilePath));
                    BufferedOutputStream stream = new BufferedOutputStream(
                            new FileOutputStream(new File(dataFilePath)));
                    FileCopyUtils.copy(dataFile.getInputStream(), stream);
                    stream.close();
                }
                String simulationFilePath = tempFileDir + "/" + fileName;
                FileUtils.touch(new File(simulationFilePath));
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File(simulationFilePath)));
                FileCopyUtils.copy(simulationFile.getInputStream(), stream);
                stream.close();
                job = new SimulationJobModel();
                job.setCount(getValue(paramMap, "parallelism").equals("") ? 0 : Short.valueOf(getValue(paramMap, "parallelism")));
                job.setPartitionAccessKey(getValue(paramMap, "accessKey"));
                job.setRoleId(partitionName);
                job.setTag(getValue(paramMap, "tag"));
                job.setUser(getValue(paramMap, "userName"));
                job.setSimulation(simulationFilePath);
                job.setDataFile(dataFilePath);
                job.setFileFullName(packageName);
                job.setParameterString(getValue(paramMap, "parameter"));
                log.info("Submitting job: {}", job);
                Optional<String> tId = serverRepository.submitSimulationJob(job);
                trackingId = tId.get();
            } catch (Exception e) {
                log.error("Error uploading simulation {}", e);
                return new SubmitResult(false, "", job);
            }
        } else {
            return new SubmitResult(false, "", job);
        }

        return new SubmitResult(true, trackingId, job);
    }

    private String getValue(Map<String, String[]> paramMap, String key) {
        if (!paramMap.containsKey(key))
            return "";
        if (paramMap.get(key).length < 1)
            return "";
        return paramMap.get(key)[0];
    }

}