package com.vcs.controller;

import com.vcs.model.Document;
import com.vcs.model.DocumentVersion;
import com.vcs.model.Role;
import com.vcs.service.DocumentService;
import com.vcs.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class DocumentDetailController {
    @FXML private Label docTitleLabel;
    @FXML private TableView<DocumentVersion> versionsTable;
    @FXML private TableColumn<DocumentVersion, String> versionCol;
    @FXML private TableColumn<DocumentVersion, String> authorCol;
    @FXML private TableColumn<DocumentVersion, String> dateCol;
    @FXML private TableColumn<DocumentVersion, String> statusCol;
    @FXML private Button approveBtn;
    @FXML private Button rejectBtn;
    @FXML private TextArea contentArea1;
    @FXML private TextArea contentArea2;
    @FXML private Button editBtn;
    @FXML private Button saveBtn;
    @FXML private TextArea contentArea;

    private Document currentDocument;
    private final DocumentService documentService = new DocumentService();



    public void setDocument(Document document) {
        this.currentDocument = document;
        docTitleLabel.setText("Документ: " + document.getTitle());

        Role userRole = SessionManager.getInstance().getCurrentUser().getRole();
        if (userRole == Role.REVIEWER || userRole == Role.ADMIN) {
            approveBtn.setVisible(true);
            rejectBtn.setVisible(true);
        }

        setupTable();
        loadVersions();
    }

    private void setupTable() {
        versionCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getVersionNumber())));
        authorCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAuthor().getUsername()));
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCreatedAt().toString()));
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().name()));
    }

    private void loadVersions() {
        List<DocumentVersion> versions = documentService.getVersionsForDocument(currentDocument.getId());
        versionsTable.getItems().setAll(versions);
    }

    @FXML
    public void handleApprove() {
        DocumentVersion selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Изберете версия от таблицата!");
            return;
        }
        try {
            documentService.approveVersion(selected.getId());
            loadVersions(); // Презареждане на таблицата
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    @FXML
    public void handleReject() {
        DocumentVersion selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            documentService.rejectVersion(selected.getId());
            loadVersions();
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    @FXML
    public void handleShowContent() {
        DocumentVersion selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getContent() != null) {
            contentArea1.setText(new String(selected.getContent()));
            contentArea2.clear();
        }
    }

    @FXML
    public void handleCompare() {
        DocumentVersion selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Изберете версия за сравнение!");
            return;
        }

        List<DocumentVersion> items = versionsTable.getItems();
        int index = items.indexOf(selected);

        if (index + 1 < items.size()) {
            DocumentVersion previous = items.get(index + 1);
            contentArea1.setText(selected.getContent() != null ? new String(selected.getContent()) : "Няма съдържание");
            contentArea2.setText(previous.getContent() != null ? new String(previous.getContent()) : "Няма съдържание");
        } else {
            showAlert("Това е първата версия, няма с какво да се сравни.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    public void handleEdit() {
        // Взимаме последната версия, за да заредим текста ѝ
        DocumentVersion selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            // Ако нищо не е избрано, взимаме най-горната (последната) от таблицата
            if (!versionsTable.getItems().isEmpty()) {
                selected = versionsTable.getItems().get(0);
            } else {
                return;
            }
        }

        contentArea.setText(new String(selected.getContent()));
        contentArea.setEditable(true); // Правим полето редактируемо
        contentArea.setStyle("-fx-control-inner-background: #fffbe6;"); // Визуален индикатор за режим "Edit"

        saveBtn.setVisible(true);
        editBtn.setDisable(true);
    }

    @FXML
    public void handleSave() {
        try {
            byte[] newContent = contentArea.getText().getBytes();
            documentService.createNewVersion(currentDocument.getId(), newContent);

            // Връщаме интерфейса в нормално състояние
            contentArea.setEditable(false);
            contentArea.setStyle("");
            saveBtn.setVisible(false);
            editBtn.setDisable(false);

            loadVersions(); // Презареждаме историята, за да се види новата чернова
            new Alert(Alert.AlertType.INFORMATION, "Новата чернова е създадена успешно!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }
}