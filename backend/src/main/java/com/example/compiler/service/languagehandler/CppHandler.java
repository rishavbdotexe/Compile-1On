package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class CppHandler extends AbstractLanguageHandler {
    @Override
    public String executeCode(String code, String stdin) throws IOException, InterruptedException {
        // Create a temporary directory for compilation and execution
        Path tempDir = Files.createTempDirectory("cpp-compiler-");
        String fileName = UUID.randomUUID().toString();
        Path sourcePath = tempDir.resolve(fileName + ".cpp");
        Path executablePath = tempDir.resolve(fileName);
        
        // Write the C++ code to a file, replacing escaped newlines with actual newlines
        String formattedCode = code.replace("\\n", "\n");
        Files.writeString(sourcePath, formattedCode);
        
        // Compile the C++ code
        String[] compileCommand = {"g++", sourcePath.toString(), "-o", executablePath.toString()};
        String compileOutput = executeCommand(compileCommand, null, tempDir.toFile());
        
        // If compilation failed, return the error
        if (!compileOutput.isEmpty()) {
            return "Compilation error:\n" + compileOutput;
        }
        
        // Execute the compiled program
        String[] executeCommand = {executablePath.toString()};
        return executeCommand(executeCommand, stdin, tempDir.toFile());
    }

    @Override
    public String executeFile(File file, String stdin) throws IOException, InterruptedException {
        // Create a temporary directory for compilation and execution
        Path tempDir = Files.createTempDirectory("cpp-compiler-");
        String fileName = UUID.randomUUID().toString();
        Path executablePath = tempDir.resolve(fileName);
        
        // Compile the C++ file
        String[] compileCommand = {"g++", file.getAbsolutePath(), "-o", executablePath.toString()};
        String compileOutput = executeCommand(compileCommand, null, tempDir.toFile());
        
        // If compilation failed, return the error
        if (!compileOutput.isEmpty()) {
            return "Compilation error:\n" + compileOutput;
        }
        
        // Execute the compiled program
        String[] executeCommand = {executablePath.toString()};
        return executeCommand(executeCommand, stdin, tempDir.toFile());
    }

    @Override
    public String getFileExtension() {
        return "cpp";
    }

    @Override
    public String getLanguageName() {
        return "cpp";
    }
} 