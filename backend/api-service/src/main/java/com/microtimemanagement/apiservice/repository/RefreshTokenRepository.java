package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /**
     * The active refresh token that owns one of the given access-token ids.
     * An access token belongs to exactly one refresh token (FK), so at most
     * one active row matches.
     */
    @Query("select r from RefreshToken r join r.accessTokens a "
            + "where a.id in :ids and r.isActive = true")
    RefreshToken findByAccessTokensAndIsActiveTrue(@Param("ids") List<String> ids);

    RefreshToken findByTokenAndIsActiveTrue(String token);
}
