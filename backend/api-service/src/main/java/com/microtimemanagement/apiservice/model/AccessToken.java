package com.microtimemanagement.apiservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@ToString(exclude = {"refreshToken"}, callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_access_token")
@EqualsAndHashCode(callSuper = true, exclude = {"refreshToken"})
public class AccessToken extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // JWTs are well over the default varchar(255); give them headroom.
    @Column(length = 4096)
    private String token;

    // Read-only back-reference — RefreshToken.accessTokens owns the FK column.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id", insertable = false, updatable = false)
    private RefreshToken refreshToken;

    private Date expiresAt;

}
