package com.blogplatform.mapper;

import com.blogplatform.dto.response.CategoryResponse;
import com.blogplatform.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category, long postCount) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .postCount(postCount)
                .build();
    }
}
