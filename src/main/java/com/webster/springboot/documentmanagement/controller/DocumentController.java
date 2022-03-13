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

        //TODO session -> User
        //TODO user --> UserId --> Now u can identity which user has uploaded this document
        //TODO Save document with userId (Document table)

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


}
