package com.example.apidemo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API接口定义实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_interfaces")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ApiInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联栏目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Category category;

    /**
     * 接口名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * HTTP方法 (GET/POST/PUT/DELETE)
     */
    @Column(length = 10)
    private String method = "GET";

    /**
     * 接口路径后缀
     */
    @Column(length = 200)
    private String path;

    /**
     * 接口描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (method == null) {
            method = "GET";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
