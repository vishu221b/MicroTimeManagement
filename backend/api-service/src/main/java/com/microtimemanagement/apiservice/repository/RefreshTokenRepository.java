package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    RefreshToken findByAccessTokensAndIsActiveTrue(List<String> tokens);

    RefreshToken findByTokenAndIsActiveTrue(String token);
}
