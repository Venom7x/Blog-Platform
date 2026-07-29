package com.blogplatform.service.impl;

import com.blogplatform.dto.request.CommentRequest;
import com.blogplatform.dto.response.CommentResponse;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.entity.Comment;
import com.blogplatform.entity.Post;
import com.blogplatform.entity.User;
import com.blogplatform.exception.ResourceNotFoundException;
import com.blogplatform.exception.UnauthorizedActionException;
import com.blogplatform.mapper.CommentMapper;
import com.blogplatform.repository.CommentRepository;
import com.blogplatform.repository.PostRepository;
import com.blogplatform.repository.UserRepository;
import com.blogplatform.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponse addComment(Long postId, Long authorId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", postId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", authorId));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(author)
                .build();

        commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long requesterId, boolean isAdmin, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));

        if (!isAdmin && !comment.getAuthor().getId().equals(requesterId)) {
            throw new UnauthorizedActionException("You are not allowed to edit this comment");
        }

        comment.setContent(request.getContent());
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long requesterId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));

        if (!isAdmin && !comment.getAuthor().getId().equals(requesterId)) {
            throw new UnauthorizedActionException("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsForPost(Long postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw ResourceNotFoundException.of("Post", postId);
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtDesc(postId, pageable);
        return PageResponse.from(comments.map(commentMapper::toResponse));
    }
}
