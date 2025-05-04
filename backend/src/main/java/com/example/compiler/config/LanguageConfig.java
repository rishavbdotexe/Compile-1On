package com.example.compiler.config;

import com.example.compiler.service.languagehandler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LanguageConfig {

    @Bean
    public Map<String, LanguageHandler> languageHandlers() {
        Map<String, LanguageHandler> handlers = new HashMap<>();
        
        // Register all language handlers
        handlers.put("python", new PythonHandler());
        handlers.put("c", new CHandler());
        handlers.put("cpp", new CppHandler());
        handlers.put("javascript", new JavaScriptHandler());
        handlers.put("php", new PhpHandler());
        handlers.put("ruby", new RubyHandler());
        handlers.put("go", new GoHandler());
        handlers.put("perl", new PerlHandler());
        handlers.put("lua", new LuaHandler());
        handlers.put("r", new RHandler());
        handlers.put("dart", new DartHandler());
        handlers.put("typescript", new TypeScriptHandler());
        handlers.put("bash", new BashHandler());
        handlers.put("java", new JavaHandler());

        return handlers;
    }
} 