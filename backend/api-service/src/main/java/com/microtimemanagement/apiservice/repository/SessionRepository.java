package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {

    Optional<Session> findByRefreshTokenAndIsActiveTrue(String token);

    Session findByUserIdAndIsActiveTrue(String userId);

    Optional<Session> findByUserAndIsActiveTrue(User user);
}
