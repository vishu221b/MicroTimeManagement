package com.microtimemanagement.apiservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class SessionPrincipalDTO {

    private String uid;
    private Set<String> authorities;

}
