package com.microtimemanagement.apiservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

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
    @DocumentReference
    private RefreshToken refreshToken;

    @Field(name = "principal")
    @DocumentReference
    private User user;

}
