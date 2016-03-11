package com.walmart.gatling.endpoint.v1;

import com.walmart.gatling.repository.ServerRepository;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {
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
    public String handleFileUpload(@RequestParam("name") String name,
                                   @RequestParam("role") String role,
                                   @RequestParam("type") String type,
                                   @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        if (!file.isEmpty()) {
            try {
                String path = tempFileDir + "/" + name;
                System.out.println(path);
                FileUtils.touch(new File(path));
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File(path)));
                FileCopyUtils.copy(file.getInputStream(), stream);
                stream.close();
                String trackingId = serverRepository.uploadFile(path, name, role, type);
                redirectAttributes.addFlashAttribute("message",
                        "You successfully uploaded " + name + "! ");

                redirectAttributes.addFlashAttribute("link",
                        "/#/file/" + trackingId);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("message",
                        "You failed to upload " + name + " => " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "You failed to upload " + name + " because the file was empty");
        }

        return "redirect:upload";
    }

}