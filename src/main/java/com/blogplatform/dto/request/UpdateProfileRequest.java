package com.blogplatform.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 150)
    private String fullName;

    @Size(max = 500)
    private String bio;

    @Size(max = 500)
    private String avatarUrl;
}
