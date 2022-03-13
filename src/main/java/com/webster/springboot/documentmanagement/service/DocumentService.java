package com.webster.springboot.documentmanagement.service;

import com.webster.springboot.documentmanagement.dao.DocumentRepository;
import com.webster.springboot.documentmanagement.dao.SessionRepository;
import com.webster.springboot.documentmanagement.entity.Document;
import com.webster.springboot.documentmanagement.entity.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    private static String UPLOAD_DIR;

    static {
        try {
            UPLOAD_DIR = new ClassPathResource("/static/").getFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    DocumentRepository documentRepository;
    SessionRepository sessionRepository;


    public DocumentService() throws IOException {
        UPLOAD_DIR = new ClassPathResource("/documents/").getFile().getAbsolutePath();
    }

    @Autowired
    public DocumentService(DocumentRepository documentRepository, SessionRepository sessionRepository) {
        this.documentRepository = documentRepository;
        this.sessionRepository = sessionRepository;
    }


    //Upload document
    public ResponseEntity store(MultipartFile file, String sessionId) {

        boolean flag = false;

        UserSession session = sessionRepository.findBySessionId(sessionId);
        Long userId = session.getUserId();

        if (!session.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active User session!");
        }

        try {
            //validation
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request must contain file.");
            }

            //file upload code
            String docPath = UPLOAD_DIR + File.separator + file.getOriginalFilename();

            // Files.copy(InputStream, target path object, copyOptions)
            Files.copy(file.getInputStream(), Paths.get(docPath), StandardCopyOption.REPLACE_EXISTING);


            flag = true;

//            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                    .path("/documents//")
//                    .path(fileName)
//                    .toUriString();


            if (flag) {
                Document document = new Document();
                document.setDocName(file.getOriginalFilename());
                document.setDocType(file.getContentType());
                document.setDocPath(docPath);
                document.setUserId(userId);

                Document savedDoc = documentRepository.save(document);

                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                payload.put("savedDoc", savedDoc);


                return ResponseEntity.status(HttpStatus.OK).body(payload);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, try again.");


    }

    // download document
    public ResponseEntity downloadDocument(String fileName, String sessionId) {
        UserSession session = sessionRepository.findBySessionId(sessionId);

        Document document = documentRepository.findByDocName(fileName);

        if (document != null) {
            if (session.isActive()) {

                if (session.getUserId().equals(document.getUserId())) {
                    Path path = Paths.get(UPLOAD_DIR + File.separator + fileName);
                    Resource resource = null;
                    try {
                        resource = new UrlResource(path.toUri());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(document.getDocType()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                }

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to download document : " + fileName);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active User session!");

        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No document present with this name : " + fileName);

    }

    public Document findbyDocName(String fileName) {
        return documentRepository.findByDocName(fileName);
    }

    // show all documents related to particular user
    public ResponseEntity findAllDocuments(String sessionId) {
        UserSession session = sessionRepository.findBySessionId(sessionId);
        if (session.isActive()) {
            Long userId = session.getUserId();
            List<Document> documents = documentRepository.findByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK).body(documents);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active user session found");

    }

    // view document
    public ResponseEntity showDocument(String fileName, String sessionId) {
        UserSession session = sessionRepository.findBySessionId(sessionId);

        Document document = documentRepository.findByDocName(fileName);

        if (document != null) {
            if (session.isActive()) {

                if (session.getUserId().equals(document.getUserId())) {
                    Path path = Paths.get(UPLOAD_DIR + File.separator + fileName);
                    Resource resource = null;
                    try {
                        resource = new UrlResource(path.toUri());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(document.getDocType()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                }

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to view this document : " + fileName);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active User session!");

        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No document present with this name : " + fileName);
    }
}
