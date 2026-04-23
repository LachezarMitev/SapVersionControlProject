package com.vcs.vcs_server.repository;
import com.vcs.vcs_server.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}