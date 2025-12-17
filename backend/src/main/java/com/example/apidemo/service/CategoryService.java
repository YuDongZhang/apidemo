package com.example.apidemo.service;

import com.example.apidemo.common.BusinessException;
import com.example.apidemo.entity.Category;
import com.example.apidemo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 栏目服务
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取所有栏目（按排序）
     */
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 根据 ID 获取栏目
     */
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("栏目不存在"));
    }

    /**
     * 创建栏目
     */
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * 更新栏目
     */
    public Category update(Long id, Category categoryData) {
        Category category = findById(id);
        category.setName(categoryData.getName());
        category.setDescription(categoryData.getDescription());
        category.setSortOrder(categoryData.getSortOrder());
        return categoryRepository.save(category);
    }

    /**
     * 删除栏目
     */
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException("栏目不存在");
        }
        categoryRepository.deleteById(id);
    }
}
