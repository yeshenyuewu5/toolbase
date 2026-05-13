package com.tokenplatform.repository;

import com.tokenplatform.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyValue(String keyValue);
    List<ApiKey> findByUserId(Long userId);
    List<ApiKey> findByUserIdAndEnabled(Long userId, Boolean enabled);
    long countByUserId(Long userId);
}
