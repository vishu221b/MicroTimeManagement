package com.microtimemanagement.apiservice.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("mtm_session")
@EqualsAndHashCode(callSuper = true)
public class Session extends BaseModel{

    @Id
    private String id;

    private String token;

    private String userId;

}
