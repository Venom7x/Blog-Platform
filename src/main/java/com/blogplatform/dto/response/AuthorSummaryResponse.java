package com.blogplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthorSummaryResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
}
