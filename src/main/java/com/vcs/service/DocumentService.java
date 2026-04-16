package com.vcs.service;

import com.vcs.model.*;
import com.vcs.util.HibernateUtil;
import com.vcs.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDateTime;
import java.util.List;

public class DocumentService {

    public void createDocument(String title, String metadata, byte[] initialContent) throws Exception {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser.getRole() != Role.AUTHOR && currentUser.getRole() != Role.ADMIN) {
            throw new Exception("Нямате права за създаване на документи.");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Document doc = new Document();
            doc.setTitle(title);
            doc.setMetadata(metadata);
            session.persist(doc);

            DocumentVersion version = new DocumentVersion();
            version.setDocument(doc);
            version.setVersionNumber(1);
            version.setAuthor(currentUser);
            version.setCreatedAt(LocalDateTime.now());
            version.setStatus(VersionStatus.DRAFT);
            version.setContent(initialContent);
            session.persist(version);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Document", Document.class).list();
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
        User currentUser = SessionManager.getInstance().getCurrentUser();
        // Само автори и админи могат да създават нови версии
        if (currentUser.getRole() != Role.AUTHOR && currentUser.getRole() != Role.ADMIN) {
            throw new Exception("Нямате права за създаване на нови версии.");
        }

        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = com.vcs.util.HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            com.vcs.model.Document doc = session.get(com.vcs.model.Document.class, documentId);

            // Намираме номера на последната версия
            Integer lastVersionNum = (Integer) session.createQuery(
                            "SELECT MAX(v.versionNumber) FROM DocumentVersion v WHERE v.document.id = :docId")
                    .setParameter("docId", documentId)
                    .uniqueResult();

            com.vcs.model.DocumentVersion newVersion = new com.vcs.model.DocumentVersion();
            newVersion.setDocument(doc);
            newVersion.setVersionNumber(lastVersionNum + 1);
            newVersion.setAuthor(currentUser);
            newVersion.setCreatedAt(java.time.LocalDateTime.now());
            newVersion.setStatus(com.vcs.model.VersionStatus.DRAFT);
            newVersion.setContent(content);

            session.persist(newVersion);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
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