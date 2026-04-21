package com.microtimemanagement.apiservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class JwtCreationRequestDTO {

    private Date expiry;
    private String principal;
    private Set<String> principalAuthorities;
    private Map<String, ?> additionalClaims;

}
