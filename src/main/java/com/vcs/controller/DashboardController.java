package com.vcs.controller;

import com.vcs.model.Document;
import com.vcs.model.Role;
import com.vcs.service.DocumentService;
import com.vcs.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Optional;

public class DashboardController {
    @FXML private Label userInfoLabel;
    @FXML private Button newDocButton;
    @FXML private Button adminPanelButton; // Бутонът за админ панела
    @FXML private TableView<Document> documentTable;
    @FXML private TableColumn<Document, String> idCol;
    @FXML private TableColumn<Document, String> titleCol;
    @FXML private TableColumn<Document, String> metadataCol;

    private final DocumentService documentService = new DocumentService();

    @FXML
    public void initialize() {
        Role userRole = SessionManager.getInstance().getCurrentUser().getRole();
        userInfoLabel.setText("Влезли сте като: " + SessionManager.getInstance().getCurrentUser().getUsername() + " (" + userRole + ")");

        // Права за бутоните
        if (userRole == Role.READER || userRole == Role.REVIEWER) {
            newDocButton.setVisible(false);
        }
        if (adminPanelButton != null) {
            adminPanelButton.setVisible(userRole == Role.ADMIN);
        }

        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toString()));
        titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        metadataCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetadata()));

        // --- СЪЗДАВАНЕ НА КОНТЕКСТНО МЕНЮ (Десен клик) ---
        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Преименувай");
        renameItem.setOnAction(event -> handleRenameAction());

        MenuItem deleteItem = new MenuItem("Изтрий документ");
        deleteItem.setOnAction(event -> handleDeleteAction());

        contextMenu.getItems().addAll(renameItem, deleteItem);

        // Свързваме менюто с таблицата
        documentTable.setContextMenu(contextMenu);

        // --- ЛОГИКА ЗА РЕДОВЕТЕ (Двоен клик) ---
        documentTable.setRowFactory(tv -> {
            TableRow<Document> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openDocumentDetails(row.getItem());
                }
            });
            return row;
        });

        loadData();
    }

    private void loadData() {
        documentTable.getItems().setAll(documentService.getAllDocuments());
    }

    @FXML
    public void handleNewDocument() {
        try {
            documentService.createDocument("Нов Документ", "Автоматично генериран", new byte[]{});
            loadData();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    // Метод за преименуване (вика се от контекстното меню)
    private void handleRenameAction() {
        Document selected = documentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(selected.getTitle());
        dialog.setTitle("Преименуване");
        dialog.setHeaderText("Промяна на името на документа");
        dialog.setContentText("Ново заглавие:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newTitle -> {
            try {
                documentService.renameDocument(selected.getId(), newTitle);
                loadData();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        });
    }

    // Метод за изтриване (вика се от контекстното меню)
    private void handleDeleteAction() {
        Document selected = documentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Потвърждение");
        alert.setHeaderText("Изтриване на документ");
        alert.setContentText("Сигурни ли сте, че искате да изтриете '" + selected.getTitle() + "'? Това ще изтрие и всичките му версии!");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                documentService.deleteDocument(selected.getId());
                loadData();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        }
    }

    private void openDocumentDetails(Document document) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
            Parent root = loader.load();

            DocumentDetailController controller = loader.getController();
            controller.setDocument(document);

            Stage stage = new Stage();
            stage.setTitle("Детайли и История на версията");
            stage.setScene(new Scene(root, 700, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Грешка при отваряне: " + e.getMessage()).show();
        }
    }

    @FXML
    public void handleOpenAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_management.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Административен панел");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}