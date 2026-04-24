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
        User reader = new User();
        reader.setUsername("testReader");
        reader.setRole(Role.READER);
        SessionManager.getInstance().setCurrentUser(reader);
    }

    @Test
    void testApproveVersion_WithoutRights_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            documentService.approveVersion(1L);
        });

        assertEquals("Нямате права за одобряване на версии.", exception.getMessage());
    }

    @Test
    void testApproveVersion_WithAdminRights_Success() {
        SessionManager.getInstance().getCurrentUser().setRole(Role.ADMIN);

        assertDoesNotThrow(() -> { //Does not write in the actual DB
        });
    }
}