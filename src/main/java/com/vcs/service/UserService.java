package com.vcs.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcs.model.Role;
import com.vcs.model.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final String API_URL = "http://localhost:8080/api/users";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<User> getAllUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), new TypeReference<List<User>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void registerUser(String username, String rawPassword, Role role) throws Exception {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(rawPassword); // Пращаме суровата парола, сървърът ще я хешира
        user.setRole(role);

        String json = mapper.writeValueAsString(user);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception(response.body());
        }
    }

    public void deleteUser(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/" + userId))
                .DELETE().build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void updateUserRole(Long userId, Role newRole) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/" + userId + "/role?newRole=" + newRole.name()))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}