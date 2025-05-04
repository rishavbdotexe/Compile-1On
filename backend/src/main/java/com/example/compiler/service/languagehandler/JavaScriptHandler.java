package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class JavaScriptHandler extends AbstractLanguageHandler {
    @Override
    public String executeCode(String code, String stdin) throws IOException, InterruptedException {
        File tempDir = Files.createTempDirectory("js_exec_" + UUID.randomUUID()).toFile();
        try {
            File file = new File(tempDir, "script.js");
            writeCodeToFile(file, code);

            ProcessBuilder processBuilder = new ProcessBuilder("node", file.getAbsolutePath())
                    .directory(tempDir);
            return executeInProcess(processBuilder, stdin);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Override
    public String executeFile(File file, String stdin) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("node", file.getAbsolutePath())
                .directory(file.getParentFile());
        return executeInProcess(processBuilder, stdin);
    }

    @Override
    public String getFileExtension() {
        return "js";
    }

    @Override
    public String getLanguageName() {
        return "javascript";
    }
} 