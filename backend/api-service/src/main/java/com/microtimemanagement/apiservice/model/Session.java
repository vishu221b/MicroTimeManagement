package com.microtimemanagement.apiservice.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("session")
@EqualsAndHashCode(callSuper = true)
public class Session extends BaseModel{

    @Id
    private String id;

    @Field(name = "refresh_token")
    @DocumentReference(lazy = true)
    private RefreshToken refreshToken;

    @Field(name = "principal")
    @DocumentReference
    private User user;

}
