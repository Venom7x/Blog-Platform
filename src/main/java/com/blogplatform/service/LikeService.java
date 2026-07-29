package com.blogplatform.service;

import com.blogplatform.dto.response.LikeResponse;

public interface LikeService {

    LikeResponse toggleLike(Long postId, Long userId);
}
