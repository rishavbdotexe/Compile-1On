package com.example.onlinecompiler.models;

public class CompileHistoryItem {
    private String languageName;
    private int languageImageResId; // Drawable resource ID
    private String compilerVersion;

    public CompileHistoryItem(String languageName, int languageImageResId, String compilerVersion) {
        this.languageName = languageName;
        this.languageImageResId = languageImageResId;
        this.compilerVersion = compilerVersion;
    }

    public String getLanguageName() {
        return languageName;
    }

    public int getLanguageImageResId() {
        return languageImageResId;
    }

    public String getCompilerVersion() {
        return compilerVersion;
    }
}
