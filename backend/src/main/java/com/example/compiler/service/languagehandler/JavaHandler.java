package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

public class JavaHandler extends AbstractLanguageHandler {
    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        return matcher.find() ? matcher.group(1) : "Main";
    }

    @Override
    public String executeCode(String code, String stdin) throws IOException, InterruptedException {
        File tempDir = java.nio.file.Files.createTempDirectory("java_exec_" + java.util.UUID.randomUUID()).toFile();
        try {
            String className = extractClassName(code);
            File file = new File(tempDir, className + ".java");
            writeCodeToFile(file, code);

            // Compile
            Process compile = new ProcessBuilder("javac", file.getName())
                    .directory(tempDir)
                    .start();
            compile.waitFor();
            if (compile.exitValue() != 0) {
                return "Compilation error:\n" + new String(compile.getErrorStream().readAllBytes());
            }

            // Execute
            ProcessBuilder processBuilder = new ProcessBuilder("java", className)
                    .directory(tempDir);
            return executeInProcess(processBuilder, stdin);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Override
    public String executeFile(File file, String stdin) throws IOException, InterruptedException {
        File workingDir = file.getParentFile();
        
        // Compile
        String[] compileCommand = {"javac", file.getName()};
        Process compileProcess = new ProcessBuilder(compileCommand)
                .directory(workingDir)
                .redirectErrorStream(true)
                .start();

        if (!compileProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            compileProcess.destroyForcibly();
            throw new InterruptedException("Compilation timed out");
        }

        if (compileProcess.exitValue() != 0) {
            return "Compilation error:\n" + getCompilationError(compileProcess);
        }

        // Run
        String className = file.getName().replace(".java", "");
        String[] runCommand = {"java", className};
        return executeCommand(runCommand, stdin, workingDir);
    }

    @Override
    public String getFileExtension() {
        return "java";
    }

    @Override
    public String getLanguageName() {
        return "java";
    }
} 