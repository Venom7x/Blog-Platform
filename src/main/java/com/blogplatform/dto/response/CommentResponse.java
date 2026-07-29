package com.blogplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long postId;
    private AuthorSummaryResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
