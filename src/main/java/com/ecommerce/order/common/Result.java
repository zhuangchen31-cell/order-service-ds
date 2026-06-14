package com.ecommerce.order.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应结果
 */
public class Result extends HashMap<String, Object> {

    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String DATA = "data";
    private static final String TOTAL = "total";
    private static final String PAGE_NUM = "pageNum";
    private static final String PAGE_SIZE = "pageSize";
    private static final String PAGES = "pages";

    public static Result ok() {
        Result r = new Result();
        r.put(SUCCESS, true);
        return r;
    }

    public static Result ok(Object data) {
        Result r = new Result();
        r.put(SUCCESS, true);
        r.put(DATA, data);
        return r;
    }

    public static Result ok(String message) {
        Result r = new Result();
        r.put(SUCCESS, true);
        r.put(MESSAGE, message);
        return r;
    }

    public static Result fail(String message) {
        Result r = new Result();
        r.put(SUCCESS, false);
        r.put(MESSAGE, message);
        return r;
    }

    public Result data(Object data) {
        this.put(DATA, data);
        return this;
    }

    public Result message(String message) {
        this.put(MESSAGE, message);
        return this;
    }

    public Result total(long total) {
        this.put(TOTAL, total);
        return this;
    }

    public Result pageNum(long pageNum) {
        this.put(PAGE_NUM, pageNum);
        return this;
    }

    public Result pageSize(long pageSize) {
        this.put(PAGE_SIZE, pageSize);
        return this;
    }

    public Result pages(long pages) {
        this.put(PAGES, pages);
        return this;
    }
}
