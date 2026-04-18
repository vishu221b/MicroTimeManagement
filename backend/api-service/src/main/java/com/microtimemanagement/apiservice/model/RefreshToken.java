package com.microtimemanagement.apiservice.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("refresh_token")
@ToString(exclude = {"session"})
@EqualsAndHashCode(callSuper = true)
public class RefreshToken extends BaseModel{

    @Id
    private String id;

    @Field(name = "access_tokens")
    @DocumentReference(lazy = true)
    private List<AccessToken> accessTokens;

    private String token;

    // Back referencing Session for efficiency
    @ReadOnlyProperty
    @DocumentReference(
            lookup = "{refresh_token: ?#{#self._id}}"
    )
    private Session session;

    // Expired refresh token means inactive session
    private Date expiresAt;

    public AccessToken getActiveAccessToken(){
        return this.accessTokens
                .stream()
                .filter(accessToken -> accessToken.getIsActive().equals(Boolean.TRUE))
                .toList().get(0);
    }


}
