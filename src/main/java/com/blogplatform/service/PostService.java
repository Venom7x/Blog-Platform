package com.blogplatform.service;

import com.blogplatform.dto.request.PostRequest;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.dto.response.PostResponse;
import com.blogplatform.enums.PostStatus;

public interface PostService {

    PostResponse createPost(Long authorId, PostRequest request);

    PostResponse updatePost(Long postId, Long requesterId, boolean isAdmin, PostRequest request);

    void deletePost(Long postId, Long requesterId, boolean isAdmin);

    PostResponse getPostById(Long postId, Long currentUserId);

    PageResponse<PostResponse> getAllPublishedPosts(String title, Long categoryId, int page, int size,
                                                     String sortBy, String direction, Long currentUserId);

    PageResponse<PostResponse> getMyPosts(Long authorId, int page, int size, String sortBy, String direction);
}
