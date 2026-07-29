package com.blogplatform.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateUserRolesRequest {

    @NotEmpty(message = "At least one role must be provided")
    private Set<String> roles; // e.g. ["ROLE_USER", "ROLE_ADMIN"]
}
