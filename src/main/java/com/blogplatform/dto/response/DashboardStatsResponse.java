package com.blogplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalPosts;
    private long publishedPosts;
    private long draftPosts;
    private long totalComments;
    private long totalLikes;
    private long totalCategories;
}
