package com.lottery.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 静态图片控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @GetMapping("/{folder}/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String folder,
            @PathVariable String filename) {
        try {
            String path = "static/images/" + folder + "/" + filename;
            Resource resource = new ClassPathResource(path);
            
            if (!resource.exists()) {
                log.warn("图片不存在: {}", path);
                return ResponseEntity.notFound().build();
            }
            
            String contentType = "image/svg+xml";
            if (filename.endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            log.error("获取图片失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
