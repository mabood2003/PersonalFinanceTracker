package com.moabood.financetracker.category;

import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.mapper.CategoryMapper;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    public List<CategoryDto> getAllCategories() {
        User user = userService.getCurrentUser();
        return categoryRepository.findAllByUserIdOrDefault(user.getId())
                .stream().map(categoryMapper::toDto).toList();
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        User user = userService.getCurrentUser();
        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setDefault(false);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CreateCategoryRequest request) {
        User user = userService.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (category.isDefault()) {
            throw new AccessDeniedException("System default categories cannot be modified");
        }
        if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = userService.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (category.isDefault()) {
            throw new AccessDeniedException("System default categories cannot be deleted");
        }
        if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        categoryRepository.delete(category);
    }
}
