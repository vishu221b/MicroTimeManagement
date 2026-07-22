package com.microtimemanagement.apiservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_refresh_token")
@ToString(exclude = {"session", "accessTokens"})
@EqualsAndHashCode(callSuper = true, exclude = {"session", "accessTokens"})
public class RefreshToken extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Owning side of the FK (access_token.refresh_token_id) — saving a refresh
    // token with its list wires up the children's FK, so the auth services keep
    // working unchanged. Eager: the revoke/refresh flows walk this list.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "refresh_token_id")
    private List<AccessToken> accessTokens;

    private String token;

    // Back-reference to the owning session (Session owns the FK).
    @OneToOne(mappedBy = "refreshToken", fetch = FetchType.EAGER)
    private Session session;

    // Expired refresh token means inactive session
    private Date expiresAt;

    public AccessToken getActiveAccessToken() {
        if (null == accessTokens)
            return null;
        return this.accessTokens
                .stream()
                .filter(accessToken -> Boolean.TRUE.equals(accessToken.getIsActive()))
                .findFirst()
                .orElse(null);
    }

}
