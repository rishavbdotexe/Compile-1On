package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TypeScriptHandler extends AbstractLanguageHandler {
    @Override
    public String executeFile(File file, String stdin) throws IOException, InterruptedException {
        File workingDir = file.getParentFile();
        
        // Compile TypeScript to JavaScript
        String[] compileCommand = {"tsc", "--target", "ES2015", "--module", "CommonJS", file.getName()};
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

        // Run the compiled JavaScript
        String jsFile = file.getName().replace(".ts", ".js");
        String[] runCommand = {"node", jsFile};
        return executeCommand(runCommand, stdin, workingDir);
    }

    @Override
    public String getFileExtension() {
        return "ts";
    }

    @Override
    public String getLanguageName() {
        return "typescript";
    }
} 