package com.blogplatform.mapper;

import com.blogplatform.dto.response.PostResponse;
import com.blogplatform.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    public PostResponse toResponse(Post post, long likeCount, long commentCount, boolean likedByCurrentUser) {
        if (post == null) return null;
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .summary(post.getSummary())
                .coverImageUrl(post.getCoverImageUrl())
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .likedByCurrentUser(likedByCurrentUser)
                .author(userMapper.toAuthorSummary(post.getAuthor()))
                .category(categoryMapper.toResponse(post.getCategory(), 0))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
