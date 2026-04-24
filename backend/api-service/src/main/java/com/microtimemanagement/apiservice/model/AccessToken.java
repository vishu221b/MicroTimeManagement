package com.microtimemanagement.apiservice.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;

@Data
@SuperBuilder
@ToString(exclude = {"refreshToken"})
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
