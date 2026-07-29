package com.blogplatform.service.impl;

import com.blogplatform.dto.response.LikeResponse;
import com.blogplatform.entity.Post;
import com.blogplatform.entity.PostLike;
import com.blogplatform.entity.User;
import com.blogplatform.exception.ResourceNotFoundException;
import com.blogplatform.repository.PostLikeRepository;
import com.blogplatform.repository.PostRepository;
import com.blogplatform.repository.UserRepository;
import com.blogplatform.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LikeResponse toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", postId));

        var existing = postLikeRepository.findByPostIdAndUserId(postId, userId);

        boolean nowLiked;
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            nowLiked = false;
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
            try {
                postLikeRepository.save(PostLike.builder().post(post).user(user).build());
                nowLiked = true;
            } catch (DataIntegrityViolationException e) {
                nowLiked = true;
            }
        }

        long totalLikes = postLikeRepository.countByPostId(postId);
        return LikeResponse.builder().postId(postId).liked(nowLiked).totalLikes(totalLikes).build();
    }
}
