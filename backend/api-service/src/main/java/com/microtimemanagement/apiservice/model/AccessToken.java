package com.microtimemanagement.apiservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@Document("access_token")
@EqualsAndHashCode(callSuper = true)
public class AccessToken extends BaseModel{

    @Id
    private String id;

    private String token;

    // Back referencing parent refresh token
    @ReadOnlyProperty
    @DocumentReference(
            lookup = "{access_tokens: ?#{#self._id}}"
    )
    private RefreshToken refreshToken;

    private Date expiresAt;

}
