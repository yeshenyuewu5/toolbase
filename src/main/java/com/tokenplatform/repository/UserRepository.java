package com.tokenplatform.repository;

import com.tokenplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance + ?2 WHERE u.id = ?1")
    int addBalance(Long userId, Long amount);
}
