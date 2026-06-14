package com.ecommerce.order.dto;

import com.ecommerce.order.common.StatusCode;

/**
 * 统一API返回结果类
 *
 * 规范所有接口的响应格式，包含：
 * - code:    状态码（使用 StatusCode 枚举）
 * - message: 提示消息
 * - data:    泛型数据载荷
 * - 分页信息: total / pageNum / pageSize / pages
 *
 * @param <T> 数据类型
 */
public class ApiResponse<T> {

    /** 状态码 */
    private Integer code;

    /** 提示消息 */
    private String message;

    /** 数据载荷 */
    private T data;

    /** 总记录数（分页用） */
    private Long total;

    /** 当前页码（分页用） */
    private Long pageNum;

    /** 每页条数（分页用） */
    private Long pageSize;

    /** 总页数（分页用） */
    private Long pages;

    // ========== 构造方法 ==========

    public ApiResponse() {}

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(StatusCode.SUCCESS.getCode(),
                StatusCode.SUCCESS.getDefaultMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(StatusCode.SUCCESS.getCode(),
                StatusCode.SUCCESS.getDefaultMessage(), data);
    }

    /**
     * 成功响应（自定义消息 + 数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(StatusCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 成功响应（分页数据）
     *
     * @param data     数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param pages    总页数
     */
    public static <T> ApiResponse<T> page(T data, long total, long pageNum, long pageSize, long pages) {
        ApiResponse<T> response = new ApiResponse<>(
                StatusCode.SUCCESS.getCode(),
                StatusCode.SUCCESS.getDefaultMessage(),
                data);
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setPages(pages);
        return response;
    }

    /**
     * 失败响应（自定义消息）— 向后兼容
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(StatusCode.BUSINESS_ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（使用状态码枚举）
     */
    public static <T> ApiResponse<T> error(StatusCode statusCode) {
        return new ApiResponse<>(statusCode.getCode(),
                statusCode.getDefaultMessage(), null);
    }

    /**
     * 失败响应（使用状态码枚举 + 自定义消息）
     */
    public static <T> ApiResponse<T> error(StatusCode statusCode, String message) {
        return new ApiResponse<>(statusCode.getCode(), message, null);
    }

    /**
     * 失败响应（自定义状态码 + 消息）— 向后兼容
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // ========== 链式调用方法 ==========

    public ApiResponse<T> data(T data) {
        this.data = data;
        return this;
    }

    public ApiResponse<T> message(String message) {
        this.message = message;
        return this;
    }

    public ApiResponse<T> total(Long total) {
        this.total = total;
        return this;
    }

    public ApiResponse<T> pageNum(Long pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public ApiResponse<T> pageSize(Long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ApiResponse<T> pages(Long pages) {
        this.pages = pages;
        return this;
    }

    // ========== Getter / Setter ==========

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getPageNum() { return pageNum; }
    public void setPageNum(Long pageNum) { this.pageNum = pageNum; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
    public Long getPages() { return pages; }
    public void setPages(Long pages) { this.pages = pages; }

    /**
     * 向后兼容: 根据code判断是否成功（2xx为成功）
     */
    public Boolean getSuccess() {
        return code != null && code >= 200 && code < 300;
    }

    /**
     * 向后兼容: 设置成功/失败标志
     */
    public void setSuccess(Boolean success) {
        if (success != null && success) {
            this.code = StatusCode.SUCCESS.getCode();
        } else {
            this.code = StatusCode.BUSINESS_ERROR.getCode();
        }
    }
}
