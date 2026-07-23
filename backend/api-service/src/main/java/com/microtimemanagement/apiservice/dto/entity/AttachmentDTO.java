package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.enums.AttachmentOwnerType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {

    private String id;
    private AttachmentOwnerType parentType;
    private String parentId;
    private String name;
    private String contentType;
    private Long sizeBytes;

    // Data-URL base64. Capped ~7M chars so decoded payload stays under ~5 MB.
    @Size(max = 7_000_000, message = "File must be at most 5 MB.")
    private String dataBase64;

    private Date createdAt;
}
