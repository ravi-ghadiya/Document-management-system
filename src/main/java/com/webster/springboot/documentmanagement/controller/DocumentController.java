package com.webster.springboot.documentmanagement.controller;

import com.webster.springboot.documentmanagement.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/documents")
@RestController
public class DocumentController {


    @Autowired
    DocumentService documentService;

//    @GetMapping
//    private ResponseEntity<Object> getFiles() {
//        return documentService.getDocuments();
//    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam MultipartFile file, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {

        ResponseEntity response = documentService.store(file, sessionId);

        return response;
    }


    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity downloadDocument(@PathVariable String fileName, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {

        ResponseEntity response = documentService.downloadDocument(fileName, sessionId);

        return response;

    }


    @GetMapping("/view/{fileName:.+}")

    public ResponseEntity showDocument(@PathVariable String fileName, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {


        ResponseEntity response = documentService.showDocument(fileName, sessionId);

        return response;

    }

    @GetMapping("/view")
    public ResponseEntity findAllDocuments(@RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {
        ResponseEntity response = documentService.findAllDocuments(sessionId);
        return response;
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity deleteDocument(@PathVariable String filename, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {
        ResponseEntity response = documentService.deleteDocument(filename, sessionId);
        return response;
    }

    @PostMapping("/share/{userId}/{documentId}")
    public ResponseEntity shareDocument(@PathVariable("userId") Long userid, @PathVariable("documentId") Long documentId, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {
        ResponseEntity response = documentService.shareDocument(userid, documentId, sessionId);
        return response;
    }

    @PostMapping("/uploadShared/{link}")
    public ResponseEntity uploadSharedDocument(@PathVariable String link, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {
        ResponseEntity response = documentService.uploadSharedDocument(link, sessionId);
        return response;
    }

    @GetMapping("/downloadShared/{link}")
    public ResponseEntity downloadSharedDocument(@PathVariable String link, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {

        ResponseEntity response = documentService.downloadSharedDocument(link, sessionId);

        return response;
    }

    @GetMapping("/viewShared/{link}")
    public ResponseEntity viewSharedDocument(@PathVariable String link, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {

        ResponseEntity response = documentService.viewSharedDocument(link, sessionId);

        return response;
    }

    @DeleteMapping("/deleteShared/{link}")
    public ResponseEntity deleteSharedDocument(@PathVariable String link, @RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {
        ResponseEntity response = documentService.deleteShareddocument(link, sessionId);
        return response;
    }


}
