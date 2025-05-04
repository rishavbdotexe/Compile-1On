package com.example.compiler.service.languagehandler;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class AbstractLanguageHandler implements LanguageHandler {
    protected static final int TIMEOUT_SECONDS = 10;
    protected static final int MAX_OUTPUT_SIZE = 1024 * 1024; // 1MB

    @Override
    public String executeCode(String code, String stdin) throws IOException, InterruptedException {
        File tempDir = Files.createTempDirectory("code_exec_" + UUID.randomUUID()).toFile();
        try {
            File sourceFile = new File(tempDir, "source." + getFileExtension());
            Files.write(sourceFile.toPath(), code.getBytes());
            return executeFile(sourceFile, stdin);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    protected String executeCommand(String[] command, String stdin, File workingDir) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .directory(workingDir)
                .redirectErrorStream(true)
                .start();

        if (stdin != null && !stdin.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(stdin);
                writer.flush();
            }
        }

        Future<String> future = Executors.newSingleThreadExecutor().submit(() -> {
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (output.length() + line.length() > MAX_OUTPUT_SIZE) {
                        throw new IOException("Output exceeds maximum size limit");
                    }
                    output.append(line).append("\n");
                }
            }
            return output.toString();
        });

        try {
            process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            process.destroyForcibly();
            throw new InterruptedException("Execution timed out after " + TIMEOUT_SECONDS + " seconds");
        } catch (ExecutionException e) {
            throw new IOException("Error reading process output: " + e.getCause().getMessage());
        }
    }

    protected void deleteDirectory(File dir) {
        if (dir.exists()) {
            try {
                Files.walk(dir.toPath())
                        .sorted((a, b) -> -a.compareTo(b))
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace(); // You can replace this with logging
            }
        }
    }

    protected String getCompilationError(Process process) throws IOException {
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        return error.toString();
    }

    // 🔧 Fix #1 — Add missing writeCodeToFile method
    protected void writeCodeToFile(File file, String code) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
        }
    }

    // 🔧 Fix #2 — Add missing executeInProcess method
    protected String executeInProcess(ProcessBuilder processBuilder, String stdin) throws IOException, InterruptedException {
        return executeCommand(
                processBuilder.command().toArray(new String[0]),
                stdin,
                processBuilder.directory()
        );
    }
}
