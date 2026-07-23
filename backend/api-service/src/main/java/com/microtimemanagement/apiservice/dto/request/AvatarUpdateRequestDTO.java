package com.microtimemanagement.apiservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUpdateRequestDTO {

    // Data-URL base64. Empty string clears the avatar. Cap ~7M chars (~5 MB).
    @Size(max = 7_000_000, message = "Image must be at most 5 MB.")
    private String avatarBase64;
}
