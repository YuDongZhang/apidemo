package com.example.apidemo.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回体
 * 
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码：0=成功，其他=失败
     */
    private int code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    /**
     * 成功返回（不带数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    /**
     * 成功返回（带消息和数据）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(0, msg, data);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败返回（默认错误码）
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(-1, msg, null);
    }
}
