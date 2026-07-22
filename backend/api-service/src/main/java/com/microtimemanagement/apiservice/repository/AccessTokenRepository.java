package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {
    AccessToken findByTokenAndIsActiveTrue(String token);
}
