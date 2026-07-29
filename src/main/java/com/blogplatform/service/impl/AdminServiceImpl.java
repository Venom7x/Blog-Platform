package com.blogplatform.service.impl;

import com.blogplatform.dto.request.UpdateUserRolesRequest;
import com.blogplatform.dto.response.DashboardStatsResponse;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.dto.response.UserResponse;
import com.blogplatform.entity.Comment;
import com.blogplatform.entity.Post;
import com.blogplatform.entity.Role;
import com.blogplatform.entity.User;
import com.blogplatform.enums.PostStatus;
import com.blogplatform.enums.RoleName;
import com.blogplatform.exception.BadRequestException;
import com.blogplatform.exception.ResourceNotFoundException;
import com.blogplatform.mapper.UserMapper;
import com.blogplatform.repository.*;
import com.blogplatform.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void deleteAnyPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ResourceNotFoundException.of("Post", postId));
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void deleteAnyComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id"));
        Page<User> users = userRepository.findAll(pageable);
        return PageResponse.from(users.map(userMapper::toResponse));
    }

    @Override
    @Transactional
    public UserResponse setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        user.setEnabled(enabled);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        Set<Role> newRoles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            RoleName parsed;
            try {
                parsed = RoleName.valueOf(roleName);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Unknown role: " + roleName);
            }
            Role role = roleRepository.findByName(parsed)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(parsed).build()));
            newRoles.add(role);
        }
        user.setRoles(newRoles);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalPosts(postRepository.count())
                .publishedPosts(postRepository.countByStatus(PostStatus.PUBLISHED))
                .draftPosts(postRepository.countByStatus(PostStatus.DRAFT))
                .totalComments(commentRepository.count())
                .totalLikes(postLikeRepository.count())
                .totalCategories(categoryRepository.count())
                .build();
    }
}
