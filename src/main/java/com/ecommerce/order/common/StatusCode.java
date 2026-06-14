package com.ecommerce.order.common;

/**
 * 统一返回状态码枚举
 * 规范所有API响应的状态码定义
 */
public enum StatusCode {

    // ========== 成功类 (200-299) ==========
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    NO_CONTENT(204, "无数据"),

    // ========== 客户端错误 (400-499) ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "数据冲突"),

    // ========== 参数校验错误 (4000-4099) ==========
    PARAM_MISSING(4000, "缺少必要参数"),
    PARAM_INVALID(4001, "参数格式错误"),
    PARAM_TYPE_ERROR(4002, "参数类型错误"),
    PARAM_BIND_ERROR(4003, "参数绑定失败"),

    // ========== 业务异常 (5000-5999) ==========
    BUSINESS_ERROR(5000, "业务处理异常"),
    DATA_NOT_FOUND(5001, "数据不存在"),
    DATA_DUPLICATE(5002, "数据重复"),
    OPERATION_FAILED(5003, "操作失败"),
    INVENTORY_SHORTAGE(5004, "库存不足"),

    // ========== 服务端错误 (500-599) ==========
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用");

    private final int code;
    private final String defaultMessage;

    StatusCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
