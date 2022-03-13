package com.webster.springboot.documentmanagement.dao;

import com.webster.springboot.documentmanagement.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    //
    @Query(value = "SELECT * FROM document WHERE doc_name = ?1", nativeQuery = true)
    public Document findByDocName(String name);

    public List<Document> findByUserId(Long userId);
//
//    public List<Document> findByFromUser(User user);
//
//    public List<Document> findByToUser(User user);
//
}
