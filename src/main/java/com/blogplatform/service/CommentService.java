package com.blogplatform.service;

import com.blogplatform.dto.request.CommentRequest;
import com.blogplatform.dto.response.CommentResponse;
import com.blogplatform.dto.response.PageResponse;

public interface CommentService {
    CommentResponse addComment(Long postId, Long authorId, CommentRequest request);
    CommentResponse updateComment(Long commentId, Long requesterId, boolean isAdmin, CommentRequest request);
    void deleteComment(Long commentId, Long requesterId, boolean isAdmin);
    PageResponse<CommentResponse> getCommentsForPost(Long postId, int page, int size);
}
