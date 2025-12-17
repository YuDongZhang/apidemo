package com.example.apidemo.repository;

import com.example.apidemo.entity.ApiInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * API接口数据访问层
 */
@Repository
public interface ApiInterfaceRepository extends JpaRepository<ApiInterface, Long> {

    /**
     * 按栏目ID查询接口列表
     */
    List<ApiInterface> findByCategoryIdOrderBySortOrderAsc(Long categoryId);

    /**
     * 查询所有接口，按排序字段排序
     */
    List<ApiInterface> findAllByOrderBySortOrderAsc();
}
