package com.ecommerce.order.common.exception;

import com.ecommerce.order.common.StatusCode;
import com.ecommerce.order.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 统一处理各类异常，返回规范的 ApiResponse 响应
 *
 * 处理范围:
 *   1. 业务异常 (BusinessException)
 *   2. 参数校验异常 (MethodArgumentNotValidException / ConstraintViolationException)
 *   3. 接口不存在 (NoHandlerFoundException) — 404
 *   4. 请求方法不允许 (HttpRequestMethodNotSupportedException) — 405
 *   5. 参数缺失 (MissingServletRequestParameterException)
 *   6. 请求体格式错误 (HttpMessageNotReadableException)
 *   7. 其他未知异常 (Exception) — 兜底
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================== 业务异常 ==================

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.ok(
                ApiResponse.error(StatusCode.BUSINESS_ERROR, e.getMessage()));
    }

    // ================== 参数校验异常 ==================

    /**
     * 处理 @Valid 参数校验异常（请求体DTO校验）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ResponseEntity.badRequest().body(
                ApiResponse.error(StatusCode.PARAM_INVALID, "参数校验失败: " + message));
    }

    /**
     * 处理约束校验异常（@Validated 方法参数校验）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", message);
        return ResponseEntity.badRequest().body(
                ApiResponse.error(StatusCode.PARAM_INVALID, "参数校验失败: " + message));
    }

    /**
     * 处理缺失必需参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
            MissingServletRequestParameterException e) {
        log.warn("缺少必需参数: {}", e.getParameterName());
        return ResponseEntity.badRequest().body(
                ApiResponse.error(StatusCode.PARAM_MISSING,
                        "缺少必需参数: " + e.getParameterName()));
    }

    // ================== 请求层面异常 ==================

    /**
     * 处理接口不存在异常 (404)
     * 注意: 需要 spring.mvc.throw-exception-if-no-handler-found=true 才能触发此处理器
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException e) {
        log.warn("接口不存在: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(StatusCode.NOT_FOUND,
                        "接口不存在: " + e.getHttpMethod() + " " + e.getRequestURL()));
    }

    /**
     * 处理请求方法不允许异常 (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不允许: {}", e.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ApiResponse.error(StatusCode.METHOD_NOT_ALLOWED,
                        "请求方法 " + e.getMethod() + " 不允许，支持: " + e.getSupportedHttpMethods()));
    }

    /**
     * 处理请求体格式错误（JSON解析失败等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error(StatusCode.PARAM_TYPE_ERROR,
                        "请求体格式错误，请检查JSON格式"));
    }

    // ================== 兜底异常 ==================

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(StatusCode.INTERNAL_ERROR,
                        "服务器内部错误: " + e.getMessage()));
    }
}
