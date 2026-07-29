package com.blogplatform.service.impl;

import com.blogplatform.dto.request.PostRequest;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.dto.response.PostResponse;
import com.blogplatform.entity.Category;
import com.blogplatform.entity.Post;
import com.blogplatform.entity.User;
import com.blogplatform.enums.PostStatus;
import com.blogplatform.exception.ResourceNotFoundException;
import com.blogplatform.exception.UnauthorizedActionException;
import com.blogplatform.mapper.PostMapper;
import com.blogplatform.repository.CategoryRepository;
import com.blogplatform.repository.CommentRepository;
import com.blogplatform.repository.PostLikeRepository;
import com.blogplatform.repository.PostRepository;
import com.blogplatform.repository.UserRepository;
import com.blogplatform.service.PostService;
import com.blogplatform.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public PostResponse createPost(Long authorId, PostRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", authorId));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category", request.getCategoryId()));

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(generateUniqueSlug(request.getTitle()))
                .content(request.getContent())
                .summary(request.getSummary())
                .coverImageUrl(request.getCoverImageUrl())
                .status(request.getStatus())
                .author(author)
                .category(category)
                .build();

        postRepository.save(post);
        return postMapper.toResponse(post, 0, 0, false);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long requesterId, boolean isAdmin, PostRequest request) {
        Post post = postRepository.findWithDetailsById(postId)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", postId));

        assertOwnerOrAdmin(post.getAuthor().getId(), requesterId, isAdmin, "update this post");

        if (!post.getTitle().equals(request.getTitle())) {
            post.setSlug(generateUniqueSlug(request.getTitle()));
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setStatus(request.getStatus());

        if (!post.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Category", request.getCategoryId()));
            post.setCategory(category);
        }

        long likeCount = postLikeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, requesterId);
        return postMapper.toResponse(post, likeCount, commentCount, liked);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long requesterId, boolean isAdmin) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", postId));
        assertOwnerOrAdmin(post.getAuthor().getId(), requesterId, isAdmin, "delete this post");
        postRepository.delete(post); // cascades to comments/likes
    }

    @Override
    @Transactional
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findWithDetailsById(postId)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", postId));

        post.setViewCount(post.getViewCount() + 1); // dirty-checked, flushed on commit

        long likeCount = postLikeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        boolean liked = currentUserId != null && postLikeRepository.existsByPostIdAndUserId(postId, currentUserId);

        return postMapper.toResponse(post, likeCount, commentCount, liked);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getAllPublishedPosts(String title, Long categoryId, int page, int size,
                                                            String sortBy, String direction, Long currentUserId) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Post> posts = postRepository.searchPosts(PostStatus.PUBLISHED, title, categoryId, pageable);
        return PageResponse.from(posts.map(p -> toResponseWithCounts(p, currentUserId)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getMyPosts(Long authorId, int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Post> posts = postRepository.findAllByAuthorId(authorId, pageable);
        return PageResponse.from(posts.map(p -> toResponseWithCounts(p, authorId)));
    }

    private PostResponse toResponseWithCounts(Post post, Long currentUserId) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());
        boolean liked = currentUserId != null && postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        return postMapper.toResponse(post, likeCount, commentCount, liked);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        String property = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(dir, property));
    }

    private String generateUniqueSlug(String title) {
        String base = SlugUtil.toSlug(title);
        String slug = base;
        int suffix = 1;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private void assertOwnerOrAdmin(Long ownerId, Long requesterId, boolean isAdmin, String action) {
        if (!isAdmin && !ownerId.equals(requesterId)) {
            throw new UnauthorizedActionException("You are not allowed to " + action);
        }
    }
}
