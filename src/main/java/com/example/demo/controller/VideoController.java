package com.example.demo.controller;

import org.apache.tika.Tika;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class VideoController {


    // http://localhost:8080/videos/Stellaris

    @GetMapping("/videos/{fileName}")
    public ResponseEntity<ResourceRegion> getVideos(@PathVariable String fileName, @RequestHeader HttpHeaders headers) {
        try {
            UrlResource video = new UrlResource("file:src/main/resources/videos/" + fileName);
            System.out.println(video.getFile().getAbsolutePath());
            Tika tika = new Tika();
            ResourceRegion region = resourceRegion(video, headers);

            ResponseEntity<ResourceRegion> response = ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.valueOf(tika.detect(video.getFile())))
                    .body(region);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws IOException {
        Long rangeLength;
        Long contentLength = video.contentLength();
        HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().get(0);
        if (null != range) {
            Long start = range.getRangeStart(contentLength);
            Long end = range.getRangeEnd(contentLength);
            rangeLength = Math.min(1024 * 1024L, end - start + 1);
            return new ResourceRegion(video, start, rangeLength);
        } else {
            rangeLength = Math.min(1024 * 1024L, contentLength);
            return new ResourceRegion(video, 0, rangeLength);
        }
    }
}
