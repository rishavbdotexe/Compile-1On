package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class CHandler extends AbstractLanguageHandler {

    @Override
    public String executeCode(String code, String stdin) throws IOException, InterruptedException {
        // Create temp directory
        Path tempDir = Files.createTempDirectory("c-compiler-");

        String fileName = UUID.randomUUID().toString();
        Path sourcePath = tempDir.resolve(fileName + ".c");
        Path executablePath = tempDir.resolve(fileName);

        // Directly write the code to file without modifying it
        Files.writeString(sourcePath, code, StandardCharsets.UTF_8);

        // Compile the code using GCC
        String[] compileCommand = {"gcc", sourcePath.toString(), "-o", executablePath.toString()};
        String compileOutput = super.executeCommand(compileCommand, null, tempDir.toFile());

        // If compilation failed, return error
        if (!compileOutput.isEmpty()) {
            return "Compilation error:\n" + compileOutput;
        }

        // Run the compiled binary
        String[] executeCommand = {executablePath.toString()};
        return super.executeCommand(executeCommand, stdin, tempDir.toFile());
    }

    @Override
    public String executeFile(File file, String stdin) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("c-compiler-");
        String fileName = UUID.randomUUID().toString();
        Path executablePath = tempDir.resolve(fileName);

        // Compile the uploaded .c file
        String[] compileCommand = {"gcc", file.getAbsolutePath(), "-o", executablePath.toString()};
        String compileOutput = super.executeCommand(compileCommand, null, tempDir.toFile());

        if (!compileOutput.isEmpty()) {
            return "Compilation error:\n" + compileOutput;
        }

        // Execute the compiled program
        String[] executeCommand = {executablePath.toString()};
        return super.executeCommand(executeCommand, stdin, tempDir.toFile());
    }

    @Override
    public String getFileExtension() {
        return "c";
    }

    @Override
    public String getLanguageName() {
        return "c";
    }
}
