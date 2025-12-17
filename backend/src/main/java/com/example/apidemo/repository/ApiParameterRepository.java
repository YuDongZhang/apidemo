package com.example.apidemo.repository;

import com.example.apidemo.entity.ApiParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * API参数数据访问层
 */
@Repository
public interface ApiParameterRepository extends JpaRepository<ApiParameter, Long> {

    /**
     * 按接口ID查询顶层参数（parent为null）
     */
    List<ApiParameter> findByApiInterfaceIdAndParentIsNullOrderBySortOrderAsc(Long interfaceId);

    /**
     * 按接口ID查询所有参数
     */
    List<ApiParameter> findByApiInterfaceIdOrderBySortOrderAsc(Long interfaceId);

    /**
     * 删除指定接口的所有参数
     */
    void deleteByApiInterfaceId(Long interfaceId);
}
