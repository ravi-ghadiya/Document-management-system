package com.webster.springboot.documentmanagement.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class LinkShareDocument {

    //TODO id, userId(From sessionId)))))00000,touserId,active,Link,documetnId,createat,updateaat

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Long userId;
    private Long toUserId;
    private boolean active = true;
    private String link;
    private Long documentId;
    private Date createdAt = new Date();
    private Date updatedAt;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}


//        /share/12/35
//
//        /view/link


//    @PostMapping("/api/document/{ravi}/{dilip}")

// in method parameters ---> *@PathVariable("ravi") Long ravi, @PathVariable("dilip") Long dilip*/










