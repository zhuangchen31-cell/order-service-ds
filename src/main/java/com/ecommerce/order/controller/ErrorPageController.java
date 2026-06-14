package com.ecommerce.order.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义错误页面控制器
 * 处理404等HTTP错误，跳转到自定义错误页面
 */
@Controller
public class ErrorPageController implements ErrorController {

    /**
     * 统一错误处理入口
     * 匹配Spring Boot默认的 /error 路径
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response) {
        // 获取HTTP错误状态码
        Object statusObj = request.getAttribute(
            javax.servlet.RequestDispatcher.ERROR_STATUS_CODE);

        if (statusObj != null) {
            int statusCode = Integer.parseInt(statusObj.toString());

            if (statusCode == 404) {
                // 返回自定义404页面
                return "redirect:/404.html";
            }

            if (statusCode == 500) {
                return "redirect:/404.html";
            }
        }

        // 默认跳转到404页面
        return "redirect:/404.html";
    }
}
