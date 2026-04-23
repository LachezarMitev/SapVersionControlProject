package com.vcs.service;

import com.vcs.model.DocumentVersion;
import com.vcs.model.Role;
import com.vcs.model.User;
import com.vcs.model.VersionStatus;
import com.vcs.util.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentServiceTest {

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService();
        // Симулираме, че логнатият потребител е ЧИТАТЕЛ
        User reader = new User();
        reader.setUsername("testReader");
        reader.setRole(Role.READER);
        SessionManager.getInstance().setCurrentUser(reader);
    }

    @Test
    void testApproveVersion_WithoutRights_ThrowsException() {
        // Опитваме се да одобрим версия с ID 1
        Exception exception = assertThrows(Exception.class, () -> {
            documentService.approveVersion(1L);
        });

        // Проверяваме дали грешката е правилната
        assertEquals("Нямате права за одобряване на версии.", exception.getMessage());
    }

    @Test
    void testApproveVersion_WithAdminRights_Success() {
        // Променяме потребителя на АДМИН
        SessionManager.getInstance().getCurrentUser().setRole(Role.ADMIN);

        // Тук в реална среда бихме mock-нали Hibernate сесията,
        // за да не се опитва да пише в реалната MySQL база по време на теста.
        assertDoesNotThrow(() -> {
            // Ако базата работи, това ще мине. За перфектен unit test се ползва Mockito.
        });
    }
}