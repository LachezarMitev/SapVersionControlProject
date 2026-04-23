package com.vcs.vcs_server.controller;

import com.vcs.vcs_server.model.Document;
import com.vcs.vcs_server.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vcs.vcs_server.model.DocumentVersion;
import com.vcs.vcs_server.model.User;
import com.vcs.vcs_server.model.VersionStatus;
import com.vcs.vcs_server.repository.DocumentVersionRepository;
import com.vcs.vcs_server.repository.UserRepository;
import java.time.LocalDateTime;


import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    // --- ДОБАВИ ТОВА: Метод за създаване на нов документ ---
    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Document doc, @RequestParam Long authorId) {
        // 1. Първо записваме самия документ, за да получи ID
        Document savedDoc = documentRepository.save(doc);

        // 2. Веднага създаваме "Версия 1" за този документ
        DocumentVersion v1 = new DocumentVersion();
        v1.setDocument(savedDoc);
        v1.setVersionNumber(1);
        v1.setAuthor(userRepository.findById(authorId).orElseThrow());
        v1.setStatus(VersionStatus.DRAFT); // Първата версия винаги е чернова
        v1.setCreatedAt(LocalDateTime.now());
        v1.setContent(new byte[0]); // Празно съдържание в началото

        versionRepository.save(v1);

        return ResponseEntity.ok(savedDoc);
    }
    @Autowired
    private DocumentVersionRepository versionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{docId}/versions")
    public ResponseEntity<String> createNewVersion(@PathVariable Long docId, @RequestParam Long authorId, @RequestBody byte[] content) {
        Document doc = documentRepository.findById(docId).orElseThrow();
        User author = userRepository.findById(authorId).orElseThrow();

        // Намираме последната версия (ако има)
        List<DocumentVersion> history = versionRepository.findByDocumentIdOrderByVersionNumberDesc(docId);
        int nextVersionNum = history.isEmpty() ? 1 : history.get(0).getVersionNumber() + 1;

        DocumentVersion newVersion = new DocumentVersion();
        newVersion.setDocument(doc);
        newVersion.setVersionNumber(nextVersionNum);
        newVersion.setAuthor(author);
        newVersion.setStatus(VersionStatus.DRAFT);
        newVersion.setContent(content);
        newVersion.setCreatedAt(LocalDateTime.now());

        versionRepository.save(newVersion);
        return ResponseEntity.ok("Новата чернова е създадена!");
    }
}