package SpringAWS.controllers;

import SpringAWS.configs.S3FolderConfig;
import SpringAWS.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private S3FolderConfig folderConfig;

    @GetMapping()
    public String example() {
//        System.out.println(this.folderConfig.images());
//        this.s3Service.nice();
        return "server works";
    }
}
