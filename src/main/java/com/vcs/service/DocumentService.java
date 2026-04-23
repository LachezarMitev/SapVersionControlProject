package com.vcs.service;

import com.vcs.model.*;
import com.vcs.util.HibernateUtil;
import com.vcs.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.vcs.util.SessionManager;

public class DocumentService {

    public void createDocument(String title, String metadata) throws Exception {
        // Взимаме ID на текущия логнат потребител
        Long currentUserId = SessionManager.getInstance().getCurrentUser().getId();

        Document doc = new Document();
        doc.setTitle(title);
        doc.setMetadata(metadata);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(doc);

        HttpClient client = HttpClient.newHttpClient();
        // Добавяме authorId като параметър в URL-а
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/documents?authorId=" + currentUserId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Грешка от сървъра: " + response.body());
        }
    }

    public void approveVersion(Long versionId) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser.getRole() != Role.REVIEWER && currentUser.getRole() != Role.ADMIN) {
            throw new Exception("Нямате права за одобряване на версии.");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocumentVersion version = session.get(DocumentVersion.class, versionId);
            if (version == null) throw new Exception("Версията не е намерена.");
            
            version.setStatus(VersionStatus.APPROVED);
            session.merge(version);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public List<Document> getAllDocuments() {
        try {
            // 1. Създаваме клиент и пращаме заявка към Spring Boot сървъра
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/documents"))
                    .GET()
                    .build();

            // 2. Получаваме JSON отговора
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. Превръщаме JSON текста обратно в списък от обекти Document
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // За да разчете датите правилно

            return mapper.readValue(response.body(), new TypeReference<List<Document>>(){});

        } catch (Exception e) {
            System.err.println("Грешка при връзка със сървъра: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Връщаме празен списък, за да не крашне таблицата
        }
    }
    public List<DocumentVersion> getVersionsForDocument(Long documentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DocumentVersion v JOIN FETCH v.author WHERE v.document.id = :docId ORDER BY v.versionNumber DESC", DocumentVersion.class)
                    .setParameter("docId", documentId)
                    .list();
        }
    }

    public void renameDocument(Long documentId, String newTitle) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser.getRole() != Role.AUTHOR && currentUser.getRole() != Role.ADMIN) {
            throw new Exception("Нямате права за преименуване на документи.");
        }
        try (org.hibernate.Session session = com.vcs.util.HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction tx = session.beginTransaction();
            com.vcs.model.Document doc = session.get(com.vcs.model.Document.class, documentId);
            if (doc != null) {
                doc.setTitle(newTitle);
                session.merge(doc);
            }
            tx.commit();
        }
    }

    public void deleteDocument(Long documentId) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new Exception("Само Администратор може да изтрива документи.");
        }
        try (org.hibernate.Session session = com.vcs.util.HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction tx = session.beginTransaction();
            com.vcs.model.Document doc = session.get(com.vcs.model.Document.class, documentId);
            if (doc != null) session.remove(doc);
            tx.commit();
        }
    }

    public void createNewVersion(Long documentId, byte[] content) throws Exception {
        Long authorId = SessionManager.getInstance().getCurrentUser().getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/documents/" + documentId + "/versions?authorId=" + authorId))
                .header("Content-Type", "application/octet-stream") // Казваме, че пращаме байтове
                .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Грешка при създаване на версия: " + response.body());
        }
    }

    // Метод за отхвърляне на версия
    public void rejectVersion(Long versionId) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser.getRole() != Role.REVIEWER && currentUser.getRole() != Role.ADMIN) {
            throw new Exception("Нямате права за отхвърляне на версии.");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocumentVersion version = session.get(DocumentVersion.class, versionId);
            if (version == null) throw new Exception("Версията не е намерена.");

            version.setStatus(VersionStatus.REJECTED);
            session.merge(version);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}