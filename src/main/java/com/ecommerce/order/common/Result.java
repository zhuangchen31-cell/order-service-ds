package com.ecommerce.order.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一 API 返回结果，包含状态码、消息、数据和分页信息。
 */
public class Result extends HashMap<String, Object> {
    public static final int SUCCESS_CODE = 200;
    public static final int BAD_REQUEST_CODE = 400;
    public static final int UNAUTHORIZED_CODE = 401;
    public static final int FORBIDDEN_CODE = 403;
    public static final int NOT_FOUND_CODE = 404;
    public static final int ERROR_CODE = 500;

    public static Result ok() {
        return code(SUCCESS_CODE).success(true).message("操作成功");
    }

    public static Result ok(Object data) {
        return ok().data(data);
    }

    public static Result ok(String message) {
        return ok().message(message);
    }

    public static Result fail(String message) {
        return code(BAD_REQUEST_CODE).success(false).message(message);
    }

    public static Result fail(int code, String message) {
        return code(code).success(false).message(message);
    }

    public static Result page(IPage<?> page) {
        return ok(page.getRecords())
                .page(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public static Result code(int code) {
        Result r = new Result();
        r.put("code", code);
        return r;
    }

    public Result success(boolean success) {
        this.put("success", success);
        return this;
    }

    public Result data(Object data) {
        this.put("data", data);
        return this;
    }

    public Result message(String message) {
        this.put("message", message);
        return this;
    }

    public Result total(long total) {
        this.put("total", total);
        return this;
    }

    public Result pageNum(long pageNum) {
        this.put("pageNum", pageNum);
        return this;
    }

    public Result pageSize(long pageSize) {
        this.put("pageSize", pageSize);
        return this;
    }

    public Result pages(long pages) {
        this.put("pages", pages);
        return this;
    }

    public Result page(long pageNum, long pageSize, long total, long pages) {
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("pageNum", pageNum);
        pageInfo.put("pageSize", pageSize);
        pageInfo.put("total", total);
        pageInfo.put("pages", pages);
        this.put("page", pageInfo);
        return total(total).pageNum(pageNum).pageSize(pageSize).pages(pages);
    }
}
