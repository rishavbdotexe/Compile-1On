package com.example.compiler.service.languagehandler;

import java.io.File;
import java.io.IOException;

public interface LanguageHandler {
    String executeCode(String code, String stdin) throws IOException, InterruptedException;
    String executeFile(File file, String stdin) throws IOException, InterruptedException;
    String getFileExtension();
    String getLanguageName();
} 