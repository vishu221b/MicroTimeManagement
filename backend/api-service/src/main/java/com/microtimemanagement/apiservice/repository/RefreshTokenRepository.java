package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
}
