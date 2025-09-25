package com.sih.erp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.erp.entity.User;
import com.sih.erp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FaceAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FaceAuthService.class);

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String pythonServiceUrl = "http://127.0.0.1:5001";

    @Transactional
    public void registerUserFace(String userEmail, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String embeddingJson = callPythonEncodeService(file); // Re-use your existing helper
        JsonNode root = objectMapper.readTree(embeddingJson);
        JsonNode embedding = root.get("embedding");

        user.setFaceEmbedding(embedding.toString());
        userRepository.save(user);
    }


    public boolean registerUserFace(UUID userId, MultipartFile file) throws IOException {
        logger.info("Attempting to register face for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String embeddingJson = callPythonEncodeService(file);
        if (embeddingJson == null) {
            return false;
        }

        JsonNode rootNode = objectMapper.readTree(embeddingJson);
        JsonNode embeddingNode = rootNode.get("embedding");

        if (embeddingNode == null) {
            logger.error("Python service did not return an 'embedding' field. Response: {}", embeddingJson);
            throw new RuntimeException("Could not get face embedding from Python service.");
        }

        user.setFaceEmbedding(embeddingNode.toString());
        userRepository.save(user);
        logger.info("Successfully saved face embedding for userId: {}", userId);
        return true;
    }

    // --- THIS IS THE NEWLY ACTIVATED METHOD ---
    @Transactional(readOnly = true)
    public User authenticateUserByFace(String email, MultipartFile file) throws IOException {
        // 1. Get the known embedding from the database for the given email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or face."));

        if (user.getFaceEmbedding() == null || user.getFaceEmbedding().isEmpty()) {
            throw new BadCredentialsException("User has not registered a face.");
        }
        String knownEmbeddingJson = user.getFaceEmbedding();

        // 2. Get the new embedding from the image trying to log in
        String targetEmbeddingJson = callPythonEncodeService(file);
        JsonNode targetRoot = objectMapper.readTree(targetEmbeddingJson);
        String targetEmbedding = targetRoot.get("embedding").toString();

        // 3. Ask the Python service to verify if the two faces match
        boolean isMatch = callPythonVerifyService(targetEmbedding, knownEmbeddingJson);

        if (isMatch) {
            return user; // Success! Return the user object.
        } else {
            throw new BadCredentialsException("Invalid email or face.");
        }
    }

    // --- HELPER METHODS ---

    private String callPythonEncodeService(MultipartFile file) throws IOException {
        // ... (this method is already correct and remains unchanged)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override public String getFilename() { return file.getOriginalFilename(); }
        });
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = pythonServiceUrl + "/encode";
        try {
            logger.info("Sending face encoding request to Python at {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("Received response from Python service. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Python service returned an error. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IOException("Python service error: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to Python service at {}. Error: {}", url, e.getMessage());
            throw new IOException("Unable to connect to the face recognition service.");
        }
    }

    private String callPythonMatchService(String targetEmbedding, List<Map<String, Object>> knownUsers) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "target_embedding", parseJsonString(targetEmbedding),
                "known_users", knownUsers
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        String url = pythonServiceUrl + "/find_match";
        try {
            logger.info("Sending find_match request to Python at {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("Received match response from Python. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Python match service returned an error. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IOException("Python match service error: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to Python service at {}. Error: {}", url, e.getMessage());
            throw new IOException("Unable to connect to the face recognition service.");
        }
    }

    private Object parseJsonString(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON string from database", e);
        }
    }

    private boolean callPythonVerifyService(String targetEmbeddingJson, String knownEmbeddingJson) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "target_embedding", objectMapper.readValue(targetEmbeddingJson, Object.class),
                "known_embedding", objectMapper.readValue(knownEmbeddingJson, Object.class)
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        String url = pythonServiceUrl + "/verify";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("match_found").asBoolean();
        } catch (HttpClientErrorException e) {
            // ... (your existing error logging)
            throw new IOException("Python verify service error: " + e.getResponseBodyAsString());
        } // ...
    }
}