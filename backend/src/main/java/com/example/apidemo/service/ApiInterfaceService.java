package com.example.apidemo.service;

import com.example.apidemo.common.BusinessException;
import com.example.apidemo.entity.ApiInterface;
import com.example.apidemo.entity.ApiParameter;
import com.example.apidemo.entity.Category;
import com.example.apidemo.repository.ApiInterfaceRepository;
import com.example.apidemo.repository.ApiParameterRepository;
import com.example.apidemo.repository.CategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * API接口管理服务
 */
@Service
public class ApiInterfaceService {

    @Autowired
    private ApiInterfaceRepository interfaceRepository;

    @Autowired
    private ApiParameterRepository parameterRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper;

    public ApiInterfaceService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 获取所有接口
     */
    public List<ApiInterface> findAll() {
        return interfaceRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 按栏目获取接口
     */
    public List<ApiInterface> findByCategoryId(Long categoryId) {
        return interfaceRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
    }

    /**
     * 根据ID获取接口
     */
    public ApiInterface findById(Long id) {
        return interfaceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("接口不存在"));
    }

    /**
     * 创建接口
     */
    @Transactional
    public ApiInterface create(ApiInterface apiInterface, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("栏目不存在"));
            apiInterface.setCategory(category);
        }
        return interfaceRepository.save(apiInterface);
    }

    /**
     * 更新接口
     */
    @Transactional
    public ApiInterface update(Long id, ApiInterface data, Long categoryId) {
        ApiInterface apiInterface = findById(id);
        apiInterface.setName(data.getName());
        apiInterface.setMethod(data.getMethod());
        apiInterface.setPath(data.getPath());
        apiInterface.setDescription(data.getDescription());
        apiInterface.setSortOrder(data.getSortOrder());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("栏目不存在"));
            apiInterface.setCategory(category);
        } else {
            apiInterface.setCategory(null);
        }

        return interfaceRepository.save(apiInterface);
    }

    /**
     * 删除接口
     */
    @Transactional
    public void delete(Long id) {
        if (!interfaceRepository.existsById(id)) {
            throw new BusinessException("接口不存在");
        }
        // 先删除参数
        parameterRepository.deleteByApiInterfaceId(id);
        interfaceRepository.deleteById(id);
    }

    /**
     * 获取接口的参数列表（树形结构）
     */
    public List<ApiParameter> getParameters(Long interfaceId) {
        return parameterRepository.findByApiInterfaceIdAndParentIsNullOrderBySortOrderAsc(interfaceId);
    }

    /**
     * 保存参数（支持批量保存树形结构）
     */
    @Transactional
    public void saveParameters(Long interfaceId, List<Map<String, Object>> parametersData) {
        ApiInterface apiInterface = findById(interfaceId);

        // 删除旧参数
        parameterRepository.deleteByApiInterfaceId(interfaceId);

        // 递归保存新参数
        saveParametersRecursive(apiInterface, null, parametersData);
    }

    private void saveParametersRecursive(ApiInterface apiInterface, ApiParameter parent,
            List<Map<String, Object>> parametersData) {
        if (parametersData == null)
            return;

        int sortOrder = 0;
        for (Map<String, Object> paramData : parametersData) {
            ApiParameter param = new ApiParameter();
            param.setApiInterface(apiInterface);
            param.setParent(parent);
            param.setName((String) paramData.get("name"));
            param.setType((String) paramData.getOrDefault("type", "string"));
            param.setRequired((Boolean) paramData.getOrDefault("required", false));
            param.setDescription((String) paramData.get("description"));
            param.setExampleValue((String) paramData.get("exampleValue"));
            param.setSortOrder(sortOrder++);

            parameterRepository.save(param);

            // 递归保存子参数
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) paramData.get("children");
            if (children != null && !children.isEmpty()) {
                saveParametersRecursive(apiInterface, param, children);
            }
        }
    }

    /**
     * 生成JSON示例
     */
    public String generateJsonExample(Long interfaceId) {
        List<ApiParameter> rootParams = getParameters(interfaceId);
        Map<String, Object> jsonObject = buildJsonObject(rootParams);

        try {
            return objectMapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON生成失败: " + e.getMessage());
        }
    }

    private Map<String, Object> buildJsonObject(List<ApiParameter> parameters) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (ApiParameter param : parameters) {
            Object value = buildParameterValue(param);
            result.put(param.getName(), value);
        }

        return result;
    }

    private Object buildParameterValue(ApiParameter param) {
        String type = param.getType();
        String exampleValue = param.getExampleValue();
        List<ApiParameter> children = param.getChildren();

        switch (type.toLowerCase()) {
            case "object":
                if (children != null && !children.isEmpty()) {
                    return buildJsonObject(children);
                }
                return new LinkedHashMap<>();

            case "array":
                List<Object> arrayResult = new ArrayList<>();
                if (children != null && !children.isEmpty()) {
                    // 数组元素结构
                    ApiParameter firstChild = children.get(0);
                    if ("object".equalsIgnoreCase(firstChild.getType())) {
                        arrayResult.add(buildJsonObject(firstChild.getChildren()));
                    } else {
                        arrayResult.add(buildPrimitiveValue(firstChild));
                    }
                    // 添加第二个示例元素
                    arrayResult.add(arrayResult.get(0));
                }
                return arrayResult;

            case "number":
                if (exampleValue != null && !exampleValue.isEmpty()) {
                    try {
                        if (exampleValue.contains(".")) {
                            return Double.parseDouble(exampleValue);
                        }
                        return Long.parseLong(exampleValue);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
                return 12345;

            case "boolean":
                if (exampleValue != null && !exampleValue.isEmpty()) {
                    return Boolean.parseBoolean(exampleValue);
                }
                return true;

            case "string":
            default:
                if (exampleValue != null && !exampleValue.isEmpty()) {
                    return exampleValue;
                }
                return "示例" + param.getName();
        }
    }

    private Object buildPrimitiveValue(ApiParameter param) {
        String type = param.getType();
        String exampleValue = param.getExampleValue();

        switch (type.toLowerCase()) {
            case "number":
                return exampleValue != null ? Long.parseLong(exampleValue) : 123;
            case "boolean":
                return exampleValue != null ? Boolean.parseBoolean(exampleValue) : true;
            default:
                return exampleValue != null ? exampleValue : "示例值";
        }
    }
}
