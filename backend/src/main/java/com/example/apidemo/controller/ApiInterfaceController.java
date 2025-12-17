package com.example.apidemo.controller;

import com.example.apidemo.common.Result;
import com.example.apidemo.entity.ApiInterface;
import com.example.apidemo.entity.ApiParameter;
import com.example.apidemo.service.ApiInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API接口管理控制器
 */
@RestController
@RequestMapping("/api/interfaces")
public class ApiInterfaceController {

    @Autowired
    private ApiInterfaceService interfaceService;

    /**
     * 获取所有接口
     */
    @GetMapping
    public Result<List<ApiInterface>> list(@RequestParam(required = false) Long categoryId) {
        List<ApiInterface> interfaces;
        if (categoryId != null) {
            interfaces = interfaceService.findByCategoryId(categoryId);
        } else {
            interfaces = interfaceService.findAll();
        }
        return Result.success(interfaces);
    }

    /**
     * 获取单个接口详情
     */
    @GetMapping("/{id}")
    public Result<ApiInterface> get(@PathVariable Long id) {
        return Result.success(interfaceService.findById(id));
    }

    /**
     * 创建接口
     */
    @PostMapping
    public Result<ApiInterface> create(@RequestBody Map<String, Object> requestData) {
        ApiInterface apiInterface = new ApiInterface();
        apiInterface.setName((String) requestData.get("name"));
        apiInterface.setMethod((String) requestData.getOrDefault("method", "GET"));
        apiInterface.setPath((String) requestData.get("path"));
        apiInterface.setDescription((String) requestData.get("description"));
        apiInterface.setSortOrder((Integer) requestData.getOrDefault("sortOrder", 0));

        Long categoryId = requestData.get("categoryId") != null
                ? Long.valueOf(requestData.get("categoryId").toString())
                : null;

        return Result.success(interfaceService.create(apiInterface, categoryId));
    }

    /**
     * 更新接口
     */
    @PutMapping("/{id}")
    public Result<ApiInterface> update(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        ApiInterface apiInterface = new ApiInterface();
        apiInterface.setName((String) requestData.get("name"));
        apiInterface.setMethod((String) requestData.getOrDefault("method", "GET"));
        apiInterface.setPath((String) requestData.get("path"));
        apiInterface.setDescription((String) requestData.get("description"));

        Object sortOrderObj = requestData.get("sortOrder");
        if (sortOrderObj != null) {
            apiInterface.setSortOrder(Integer.valueOf(sortOrderObj.toString()));
        }

        Long categoryId = requestData.get("categoryId") != null
                ? Long.valueOf(requestData.get("categoryId").toString())
                : null;

        return Result.success(interfaceService.update(id, apiInterface, categoryId));
    }

    /**
     * 删除接口
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        interfaceService.delete(id);
        return Result.success();
    }

    /**
     * 获取接口的参数列表
     */
    @GetMapping("/{id}/parameters")
    public Result<List<ApiParameter>> getParameters(@PathVariable Long id) {
        return Result.success(interfaceService.getParameters(id));
    }

    /**
     * 保存接口的参数（批量保存，支持树形结构）
     */
    @PostMapping("/{id}/parameters")
    public Result<Void> saveParameters(@PathVariable Long id, @RequestBody List<Map<String, Object>> parameters) {
        interfaceService.saveParameters(id, parameters);
        return Result.success();
    }

    /**
     * 生成JSON示例
     */
    @GetMapping("/{id}/json-example")
    public Result<Map<String, Object>> getJsonExample(@PathVariable Long id) {
        String jsonString = interfaceService.generateJsonExample(id);
        Map<String, Object> result = new HashMap<>();
        result.put("json", jsonString);
        return Result.success(result);
    }
}
