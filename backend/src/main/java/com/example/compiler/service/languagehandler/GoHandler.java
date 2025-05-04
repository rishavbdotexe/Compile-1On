package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class GoHandler extends AbstractLanguageHandler {
    @Override
    public String executeCode(String code, String stdin) throws IOException, InterruptedException {
        // Create a temporary directory for compilation and execution
        Path tempDir = Files.createTempDirectory("go-compiler-");
        String fileName = UUID.randomUUID().toString();
        Path sourcePath = tempDir.resolve(fileName + ".go");
        
        // Write the Go code to a file, replacing escaped newlines with actual newlines
        String formattedCode = code.replace("\\n", "\n");
        Files.writeString(sourcePath, formattedCode);
        
        // Compile and run the Go code
        String[] command = {"go", "run", sourcePath.toString()};
        return executeCommand(command, stdin, tempDir.toFile());
    }

    @Override
    public String executeFile(File file, String stdin) throws IOException, InterruptedException {
        // Create a temporary directory for execution
        Path tempDir = Files.createTempDirectory("go-compiler-");
        
        // Run the Go file
        String[] command = {"go", "run", file.getAbsolutePath()};
        return executeCommand(command, stdin, tempDir.toFile());
    }

    @Override
    public String getFileExtension() {
        return "go";
    }

    @Override
    public String getLanguageName() {
        return "go";
    }
} 