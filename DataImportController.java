package com.zstu.math.controller;

import com.zstu.math.entity.DataImportLog;
import com.zstu.math.service.DataImportService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.io.FileNotFoundException;
import java.io.IOException;

@Controller
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    // 统一认证检查
    private boolean isNotAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") == null;
    }

    // 显示数据导入页面
    @GetMapping("/import")
    public String showImportPage(HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        clearImportResults(model);
        return "date/date-import";
    }

    // 处理数据导入请求
    @PostMapping("/import")
    public String importData(
            @RequestParam @NotBlank(message = "文件路径不能为空")
            @Pattern(regexp = "^[a-zA-Z0-9./_\\\\-]+\\.json$", message = "必须是JSON格式文件")
            String filePath,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        if (isNotAuthenticated(session)) return "redirect:/";

        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        // 参数校验
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "参数错误: " + bindingResult.getFieldError().getDefaultMessage());
            return "date/data-import";
        }

        // 安全检查
        String trimmedPath = filePath.trim();
        if (isInvalidPath(trimmedPath)) {
            model.addAttribute("error", "无效的文件路径");
            return "date/data-import";
        }

        // 处理导入逻辑
        handleDataImport(trimmedPath, model);
        return "date/data-import";
    }

    private void clearImportResults(Model model) {
        model.addAttribute("message", null);
        model.addAttribute("error", null);
    }

    private boolean isInvalidPath(String path) {
        return path.contains("..") || path.contains(":") || path.startsWith("/");
    }

    private void handleDataImport(String filePath, Model model) {
        try {
            DataImportLog importLog = dataImportService.importDataFromJson(filePath);
            model.addAttribute("message", String.format(
                    "数据导入成功！共处理 %d 条记录，成功 %d 条，失败 %d 条。",
                    importLog.getTotalRecords(), importLog.getSuccessCount(), importLog.getFailCount()
            ));
        } catch (Exception e) {
            model.addAttribute("error", getErrorMessage(e));
        }
    }

    private String getErrorMessage(Exception e) {
        String baseMsg = "数据导入失败: ";
        if (e instanceof FileNotFoundException) {
            return baseMsg + "文件不存在 - " + e.getMessage();
        } else if (e instanceof IOException) {
            return baseMsg + "文件读取错误 - " + e.getMessage();
        } else if (e instanceof RuntimeException) {
            return baseMsg + e.getMessage();
        } else {
            return baseMsg + "系统错误 - " + e.getMessage();
        }
    }
}