package com.webster.springboot.documentmanagement.dao;

import com.webster.springboot.documentmanagement.entity.LinkShareDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkShareDocumentRepository extends JpaRepository<LinkShareDocument, Long> {
    public LinkShareDocument findByDocumentIdAndLink(Long documentId, String link);

    public LinkShareDocument findByLink(String link);

    public List<LinkShareDocument> findByToUserId(Long userId);
}
