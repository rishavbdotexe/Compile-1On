package com.example.compiler.controller;

import com.example.compiler.dto.CodeRequest;
import com.example.compiler.service.CompilerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    private final CompilerService compilerService;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "java", "py", "cpp", "c", "sh", "js", "go", "php", "rb", 
        "pl", "lua", "R", "dart", "ts", "kt", "swift"
    );

    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "Compiler Backend is Running!"));
    }

    @PostMapping(value = "/execute", consumes = "application/json")
    public ResponseEntity<Map<String, String>> executeJsonCode(@RequestBody CodeRequest request) {
        return executeCode(request.getLanguage(), request.getCode(), request.getStdin());
    }

    @PostMapping(value = "/execute", consumes = "text/plain")
    public ResponseEntity<Map<String, String>> executePlainTextCode(@RequestBody String rawInput) {
        String[] parts = rawInput.split("\n", 3); // language\ncode\nstdin (optional)
        if (parts.length < 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input. Use format: language\\ncode\\n[stdin]"));
        }

        String language = parts[0].trim();
        String code = parts[1].trim();
        String stdin = parts.length > 2 ? parts[2] : "";

        return executeCode(language, code, stdin);
    }

    @PostMapping(value = "/execute", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Map<String, String>> executeFormDataCode(@RequestParam Map<String, String> params) {
        String language = params.get("language");
        String code = params.get("code");
        String stdin = params.getOrDefault("stdin", "");

        if (language == null || code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "'language' and 'code' are required."));
        }

        return executeCode(language, code, stdin);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> executeUploadedCode(
            @RequestParam("file") MultipartFile file,
            @RequestParam("language") String language,
            @RequestParam(value = "stdin", required = false) String stdin) {
        try {
            if (file.isEmpty() || language == null || language.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 2MB limit"));
            }

            String originalName = file.getOriginalFilename();
            if (originalName == null || !originalName.contains(".")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file name"));
            }

            String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported file type: ." + extension));
            }

            String output = compilerService.executeUploadedFile(file, language.trim(), stdin != null ? stdin.trim() : "");
            return ResponseEntity.ok(Map.of("output", output));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Execution failed: " + e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, String>> executeCode(String language, String code, String stdin) {
        try {
            if (language == null || code == null || code.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Language and code must not be empty!"));
            }

            String result = compilerService.executeCode(language.trim(), code.trim(), stdin != null ? stdin.trim() : "");
            return ResponseEntity.ok(Map.of("output", result));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }
}
