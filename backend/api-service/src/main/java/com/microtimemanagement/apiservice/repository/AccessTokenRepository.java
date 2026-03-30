package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.AccessToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccessTokenRepository extends MongoRepository<AccessToken, String> {
}
