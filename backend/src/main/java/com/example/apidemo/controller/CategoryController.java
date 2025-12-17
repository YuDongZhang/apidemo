package com.example.apidemo.controller;

import com.example.apidemo.common.Result;
import com.example.apidemo.entity.Category;
import com.example.apidemo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 栏目控制器
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有栏目
     */
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.findAll());
    }

    /**
     * 获取单个栏目
     */
    @GetMapping("/{id}")
    public Result<Category> get(@PathVariable Long id) {
        return Result.success(categoryService.findById(id));
    }

    /**
     * 创建栏目
     */
    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        return Result.success(categoryService.create(category));
    }

    /**
     * 更新栏目
     */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return Result.success(categoryService.update(id, category));
    }

    /**
     * 删除栏目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
