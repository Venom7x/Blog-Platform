package com.blogplatform.dto.response;

import com.blogplatform.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private String summary;
    private String coverImageUrl;
    private PostStatus status;
    private long viewCount;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;
    private AuthorSummaryResponse author;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
