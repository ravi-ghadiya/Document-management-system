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


}


//        /share/12/35
//
//        /view/link


//    @PostMapping("/api/document/{ravi}/{dilip}")

// in method parameters ---> *@PathVariable("ravi") Long ravi, @PathVariable("dilip") Long dilip*/










