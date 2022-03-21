package com.webster.springboot.documentmanagement.entity;

import javax.persistence.*;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long id;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "doc_type")
    private String docType;

//    @Lob
//    private byte[] data;
    
    @Column(name = "doc_path")
    private String docPath;

    @Column(name = "user_id")
    private Long userId;


    public Document() {
    }

    public Document(String docName, String docType, Long userId) {
        this.docName = docName;
        this.docType = docType;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", docName='" + docName + '\'' +
                ", docType='" + docType + '\'' +
                ", docPath='" + docPath + '\'' +
                ", userId=" + userId +
                '}';
    }
}
