package com.vcs.controller;

import com.vcs.model.User;
import com.vcs.model.Role;
import com.vcs.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserManagementController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<Role> roleComboBox;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(Role.values());
        usernameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().name()));
        refreshTable();
    }

    private void refreshTable() {
        userTable.getItems().setAll(userService.getAllUsers());
    }

    @FXML
    public void handleRegister() {
        try {
            userService.registerUser(newUsernameField.getText(), newPasswordField.getText(), roleComboBox.getValue());
            refreshTable();
            newUsernameField.clear();
            newPasswordField.clear();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Грешка при регистрация: " + e.getMessage()).show();
        }
    }

    @FXML
    public void handleDelete() throws Exception {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            userService.deleteUser(selected.getId());
            refreshTable();
        }
    }

    @FXML
    public void handleChangeRole() throws Exception {
        User selected = userTable.getSelectionModel().getSelectedItem();
        Role newRole = roleComboBox.getValue();
        if (selected != null && newRole != null) {
            userService.updateUserRole(selected.getId(), newRole);
            refreshTable();
        }
    }
}