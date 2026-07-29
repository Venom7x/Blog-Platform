package com.blogplatform.service;

import com.blogplatform.dto.request.CategoryRequest;
import com.blogplatform.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
    CategoryResponse getById(Long id);
    List<CategoryResponse> getAll();
}
