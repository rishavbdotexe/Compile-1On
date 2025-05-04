package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class BashHandler extends AbstractLanguageHandler {
    @Override
    public String executeCode(String code, String stdin) throws IOException, InterruptedException {
        File tempDir = Files.createTempDirectory("bash_exec").toFile();
        try {
            File scriptFile = new File(tempDir, "script.sh");
            Files.write(scriptFile.toPath(), code.getBytes());
            scriptFile.setExecutable(true);

            ProcessBuilder pb = new ProcessBuilder("bash", scriptFile.getAbsolutePath());
            pb.directory(tempDir);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            
            // Handle stdin
            if (stdin != null && !stdin.isEmpty()) {
                try (var writer = new java.io.OutputStreamWriter(process.getOutputStream())) {
                    writer.write(stdin);
                    writer.flush();
                }
            }
            process.getOutputStream().close();

            // Read output
            StringBuilder output = new StringBuilder();
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for process with timeout
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "Execution timed out after " + TIMEOUT_SECONDS + " seconds";
            }

            // Check exit code
            if (process.exitValue() != 0) {
                return "Runtime error:\n" + output.toString();
            }

            return output.toString();
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Override
    public String executeFile(File file, String stdin) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("bash", file.getAbsolutePath());
        pb.directory(file.getParentFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        
        // Handle stdin
        if (stdin != null && !stdin.isEmpty()) {
            try (var writer = new java.io.OutputStreamWriter(process.getOutputStream())) {
                writer.write(stdin);
                writer.flush();
            }
        }
        process.getOutputStream().close();

        // Read output
        StringBuilder output = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Wait for process with timeout
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return "Execution timed out after " + TIMEOUT_SECONDS + " seconds";
        }

        // Check exit code
        if (process.exitValue() != 0) {
            return "Runtime error:\n" + output.toString();
        }

        return output.toString();
    }

    @Override
    public String getFileExtension() {
        return "sh";
    }

    @Override
    public String getLanguageName() {
        return "bash";
    }
} 