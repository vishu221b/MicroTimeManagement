package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import com.microtimemanagement.apiservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    Optional<Session> findByRefreshTokenAndIsActiveTrue(RefreshToken token);

    Optional<Session> findByUserAndIsActiveTrue(User user);
}
