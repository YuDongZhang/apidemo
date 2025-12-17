package com.example.apidemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * API参数定义实体
 * 支持树形结构（嵌套对象、数组）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_parameters")
public class ApiParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联接口
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_id", nullable = false)
    private ApiInterface apiInterface;

    /**
     * 父参数ID (用于嵌套结构)
     * null表示顶层参数
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ApiParameter parent;

    /**
     * 子参数列表
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiParameter> children = new ArrayList<>();

    /**
     * 参数名
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 参数类型: string, number, boolean, object, array
     */
    @Column(length = 20)
    private String type = "string";

    /**
     * 是否必填
     */
    @Column(nullable = false)
    private Boolean required = false;

    /**
     * 参数描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 示例值
     */
    @Column(name = "example_value", length = 500)
    private String exampleValue;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @PrePersist
    protected void onCreate() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (type == null) {
            type = "string";
        }
        if (required == null) {
            required = false;
        }
    }
}
