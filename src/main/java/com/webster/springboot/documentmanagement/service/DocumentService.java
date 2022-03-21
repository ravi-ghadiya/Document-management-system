package com.webster.springboot.documentmanagement.service;

import com.webster.springboot.documentmanagement.dao.DocumentRepository;
import com.webster.springboot.documentmanagement.dao.LinkShareDocumentRepository;
import com.webster.springboot.documentmanagement.dao.SessionRepository;
import com.webster.springboot.documentmanagement.entity.Document;
import com.webster.springboot.documentmanagement.entity.LinkShareDocument;
import com.webster.springboot.documentmanagement.entity.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class DocumentService {

    private static String UPLOAD_DIR;

    static {
        try {
            UPLOAD_DIR = new ClassPathResource("/static/documents/").getFile().getAbsolutePath();
//            UPLOAD_DIR = "/home/ravi/SpringBoot project/documentmanagement/src/main/resources/static/documents/";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    DocumentRepository documentRepository;
    SessionRepository sessionRepository;
    LinkShareDocumentRepository linkShareDocumentRepository;


    public DocumentService() throws IOException {
        UPLOAD_DIR = new ClassPathResource("/documents/").getFile().getAbsolutePath();
    }

    @Autowired
    public DocumentService(DocumentRepository documentRepository, SessionRepository sessionRepository, LinkShareDocumentRepository linkShareDocumentRepository) {
        this.documentRepository = documentRepository;
        this.sessionRepository = sessionRepository;
        this.linkShareDocumentRepository = linkShareDocumentRepository;
    }


    //Upload document
    public ResponseEntity store(MultipartFile file, String sessionId) {

        boolean flag = false;

        UserSession session = sessionRepository.findBySessionId(sessionId);
        Long userId = session.getUserId();

        Document doc = documentRepository.findByUserIdAndDocName(session.getUserId(), file.getOriginalFilename());

        if (!session.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active User session!");
        }

        if (doc != null) {
            return ResponseEntity.status(HttpStatus.OK).body("This document is already uploaded.");
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

        Document document = documentRepository.findByDocNameAndUserId(fileName, session.getUserId());


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


    // show all documents related to particular user
    public ResponseEntity findAllDocuments(String sessionId) {
        UserSession session = sessionRepository.findBySessionId(sessionId);
        if (session.isActive()) {
            Long userId = session.getUserId();
            List<Document> documents = documentRepository.findByUserId(userId);

            List<Document> documentList = new ArrayList<>();
            List<LinkShareDocument> linkSharedDocuments = linkShareDocumentRepository.findByToUserId(userId);
            for (LinkShareDocument linksharedoc : linkSharedDocuments) {
                Document sharedDoc = documentRepository.getById(linksharedoc.getDocumentId());
                documentList.add(sharedDoc);
            }

            documents.addAll(documentList);

            return ResponseEntity.status(HttpStatus.OK).body(documents);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active user session found");

    }

    // view document
    public ResponseEntity showDocument(String fileName, String sessionId) {

        UserSession session = sessionRepository.findBySessionId(sessionId);

        Document document = documentRepository.findByDocNameAndUserId(fileName, session.getUserId());

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

    public ResponseEntity deleteDocument(String filename, String sessionId) {
        boolean flag = false;

        UserSession session = sessionRepository.findBySessionId(sessionId);

        Document document = documentRepository.findByDocNameAndUserId(filename, session.getUserId());

        if (document == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No document present with this name : " + filename + " related to current user");
        }
        if (!session.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active User session!");
        }
        if (!session.getUserId().equals(document.getUserId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to delete this document : " + filename);
        }
        Path path = Paths.get(UPLOAD_DIR + File.separator + filename);

        try {
            // Delete file or directory
            Files.delete(path);
            flag = true;
            System.out.println("File or directory deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (flag) {
            Document docToDelete = documentRepository.findByUserIdAndDocName(session.getUserId(), filename);
            documentRepository.delete(docToDelete);
            return ResponseEntity.status(HttpStatus.OK).body("document Deleted Successfully: " + docToDelete.getDocName());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to delete this document : " + filename);
    }

    public ResponseEntity shareDocument(Long toUserId, Long documentId, String sessionId) {
        UserSession session = sessionRepository.findBySessionId(sessionId);

        Optional<Document> document = documentRepository.findById(documentId);

        Long userId = session.getUserId();

        if (!session.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active User session!");
        }

        if (!document.isPresent()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No document present with this id : " + documentId);
        }

        if (session.getUserId().equals(document.get().getUserId())) {
            LinkShareDocument shareDocument = new LinkShareDocument();
            shareDocument.setUserId(userId);
            shareDocument.setToUserId(toUserId);
            shareDocument.setLink(UUID.randomUUID().toString());
            shareDocument.setDocumentId(documentId);

            linkShareDocumentRepository.save(shareDocument);

            return ResponseEntity.status(HttpStatus.OK).body("document shared successfully to userId : " + shareDocument.getToUserId());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to share this document");
    }

    public ResponseEntity uploadSharedDocument(String link, String sessionId) {
        LinkShareDocument linkShareDocument = linkShareDocumentRepository.findByLink(link);

        UserSession session = sessionRepository.findBySessionId(sessionId);
        Long userId = session.getUserId();

        if (!session.isActive()) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active user session");
        }
        if (linkShareDocument == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid link, no document present with this link!");
        }

        if (session.getUserId().equals(linkShareDocument.getUserId()) || (session.getUserId().equals(linkShareDocument.getToUserId()) && linkShareDocument.isActive())) {

            Long docId = linkShareDocument.getDocumentId();
            Document doc = documentRepository.getById(docId);

            //get multipart file object
            Path path = Paths.get(doc.getDocPath());
            String name = doc.getDocName();
            String originalFileName = doc.getDocName();
            String contentType = doc.getDocType();
            byte[] content = null;
            try {
                content = Files.readAllBytes(path);
            } catch (final IOException e) {
            }
            MultipartFile multipartFile = new MockMultipartFile(name,
                    originalFileName, contentType, content);

            System.out.println(multipartFile);

            boolean flag = false;


            Document docexist = documentRepository.findByUserIdAndDocName(session.getUserId(), multipartFile.getOriginalFilename());

            if (!session.isActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active User session!");
            }

            if (docexist != null) {
                return ResponseEntity.status(HttpStatus.OK).body("This document is already uploaded.");
            }

            try {
                //validation
                if (multipartFile.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request must contain file.");
                }

                String updatedDocName = session.getUserId() + multipartFile.getOriginalFilename();

                //file upload code
                String docPath = UPLOAD_DIR + File.separator + updatedDocName;

                System.out.println(docPath);

                // Files.copy(InputStream, target path object, copyOptions)
                Files.copy(multipartFile.getInputStream(), Paths.get(docPath), StandardCopyOption.REPLACE_EXISTING);


                flag = true;


                if (flag) {
                    Document document = new Document();
                    document.setDocName(updatedDocName);
                    document.setDocType(multipartFile.getContentType());
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

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to re-upload this document.");
    }


    public ResponseEntity downloadSharedDocument(String link, String sessionId) {
        LinkShareDocument linkShareDocument = linkShareDocumentRepository.findByLink(link);

        UserSession session = sessionRepository.findBySessionId(sessionId);

        if (!session.isActive()) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active user session");
        }
        if (linkShareDocument == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid link, no document present with this link!");
        }


        if (session.getUserId().equals(linkShareDocument.getUserId()) || (session.getUserId().equals(linkShareDocument.getToUserId()) && linkShareDocument.isActive())) {

            Optional<Document> document = documentRepository.findById(linkShareDocument.getDocumentId());

            Path path = Paths.get(UPLOAD_DIR + File.separator + document.get().getDocName());
            Resource resource = null;
            try {
                resource = new UrlResource(path.toUri());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.get().getDocType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to download this document.");

    }

    public ResponseEntity viewSharedDocument(String link, String sessionId) {
        LinkShareDocument linkShareDocument = linkShareDocumentRepository.findByLink(link);

        UserSession session = sessionRepository.findBySessionId(sessionId);

        if (!session.isActive()) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active user session");
        }
        if (linkShareDocument == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid link, no document present with this link!");
        }

        if (session.getUserId().equals(linkShareDocument.getUserId()) || (session.getUserId().equals(linkShareDocument.getToUserId()) && linkShareDocument.isActive())) {

            Optional<Document> document = documentRepository.findById(linkShareDocument.getDocumentId());

            Path path = Paths.get(UPLOAD_DIR + File.separator + document.get().getDocName());
            Resource resource = null;
            try {
                resource = new UrlResource(path.toUri());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.get().getDocType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to view this document.");
    }

    public ResponseEntity deleteShareddocument(String link, String sessionId) {
        LinkShareDocument linkShareDocument = linkShareDocumentRepository.findByLink(link);

        UserSession session = sessionRepository.findBySessionId(sessionId);

        if (!session.isActive()) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Active user session");
        }
        if (linkShareDocument == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid link, no document present with this link!");
        }

        if (session.getUserId().equals(linkShareDocument.getUserId()) || (session.getUserId().equals(linkShareDocument.getToUserId()) && linkShareDocument.isActive())) {
            Long docId = linkShareDocument.getDocumentId();
            Document doc = documentRepository.getById(docId);

            ResponseEntity result = deleteDocument(doc.getDocName(), sessionId);

            return result;
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This user is not authorized to delete this document.");
    }

}
