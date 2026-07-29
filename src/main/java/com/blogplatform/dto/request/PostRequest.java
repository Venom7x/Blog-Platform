package com.blogplatform.dto.request;

import com.blogplatform.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 250)
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500)
    private String summary;

    @Size(max = 500)
    private String coverImageUrl;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Status is required (DRAFT or PUBLISHED)")
    private PostStatus status;
}
