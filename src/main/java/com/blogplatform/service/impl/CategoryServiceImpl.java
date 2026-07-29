package com.blogplatform.service.impl;

import com.blogplatform.dto.request.CategoryRequest;
import com.blogplatform.dto.response.CategoryResponse;
import com.blogplatform.entity.Category;
import com.blogplatform.exception.DuplicateResourceException;
import com.blogplatform.exception.ResourceNotFoundException;
import com.blogplatform.mapper.CategoryMapper;
import com.blogplatform.repository.CategoryRepository;
import com.blogplatform.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        categoryRepository.save(category);
        return categoryMapper.toResponse(category, 0);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);
        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.toResponse(category, category.getPosts().size());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findOrThrow(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = findOrThrow(id);
        return categoryMapper.toResponse(category, category.getPosts().size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(c -> categoryMapper.toResponse(c, c.getPosts().size()))
                .toList();
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", id));
    }
}
