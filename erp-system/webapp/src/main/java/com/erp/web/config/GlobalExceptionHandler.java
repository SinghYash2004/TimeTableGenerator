package com.erp.web.config;

import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccess(Model model) {
        model.addAttribute("title", "Database Error");
        model.addAttribute("message", "Database connection/query failed. Check MySQL credentials and schema.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("title", "Application Error");
        model.addAttribute("message", ex.getMessage() == null ? "Unexpected error." : ex.getMessage());
        return "error";
    }
}
