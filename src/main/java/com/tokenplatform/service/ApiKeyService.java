package com.tokenplatform.service;

import com.tokenplatform.model.ApiKey;
import com.tokenplatform.model.User;
import com.tokenplatform.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ApiKeyService {

    private static final String KEY_PREFIX = "tk-";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Transactional
    public ApiKey createApiKey(User user, String name) {
        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setKeyValue(generateKey());
        apiKey.setName(name);
        apiKey.setEnabled(true);
        return apiKeyRepository.save(apiKey);
    }

    public Optional<ApiKey> findByKeyValue(String keyValue) {
        return apiKeyRepository.findByKeyValue(keyValue);
    }

    public List<ApiKey> findByUserId(Long userId) {
        return apiKeyRepository.findByUserId(userId);
    }

    @Transactional
    public void toggleEnabled(Long keyId, Long userId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API Key not found"));
        if (!key.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        key.setEnabled(!key.getEnabled());
        apiKeyRepository.save(key);
    }

    @Transactional
    public void deleteKey(Long keyId, Long userId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API Key not found"));
        if (!key.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        apiKeyRepository.delete(key);
    }

    public long countByUserId(Long userId) {
        return apiKeyRepository.countByUserId(userId);
    }

    private String generateKey() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
