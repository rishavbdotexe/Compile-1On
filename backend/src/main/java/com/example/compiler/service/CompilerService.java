package com.example.compiler.service;

import com.example.compiler.service.languagehandler.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
public class CompilerService {
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private final Map<String, LanguageHandler> handlers = new HashMap<>();

    public CompilerService() {
        // Initialize handlers
        handlers.put("bash", new BashHandler());
        handlers.put("c", new CHandler());
        handlers.put("cpp", new CppHandler());
        handlers.put("dart", new DartHandler());
        handlers.put("go", new GoHandler());
        handlers.put("java", new JavaHandler());
        handlers.put("javascript", new JavaScriptHandler());
        handlers.put("lua", new LuaHandler());
        handlers.put("perl", new PerlHandler());
        handlers.put("php", new PhpHandler());
        handlers.put("python", new PythonHandler());
        handlers.put("r", new RHandler());
        handlers.put("ruby", new RubyHandler());
        handlers.put("typescript", new TypeScriptHandler());
    }

    public String executeCode(String language, String code, String stdin) throws Exception {
        LanguageHandler handler = getHandler(language);
        return handler.executeCode(code, stdin != null ? stdin : "");
    }

    public String executeUploadedFile(MultipartFile file, String language, String stdin) throws Exception {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File too large. Max allowed is 2MB.");
        }

        File tempFile = Files.createTempFile("upload_", "." + getHandler(language).getFileExtension()).toFile();
        try {
            file.transferTo(tempFile);
            return getHandler(language).executeFile(tempFile, stdin != null ? stdin : "");
        } finally {
            tempFile.delete();
        }
    }

    private LanguageHandler getHandler(String language) {
        String normalizedLanguage = language.toLowerCase();
        if (normalizedLanguage.equals("c++")) {
            normalizedLanguage = "cpp";
        }
        
        LanguageHandler handler = handlers.get(normalizedLanguage);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return handler;
    }
}
