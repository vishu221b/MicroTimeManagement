package com.microtimemanagement.apiservice.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mtm_role")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseModel{

    @Id
    private String id;

    private String name;
}
