package com.example.onlinecompiler.models;

public class CompileRequest {
    private String language;
    private String code;
    private String stdin;

    public CompileRequest(String language, String code, String stdin) {
        this.language = language;
        this.code = code;
        this.stdin = stdin;
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

    public String getStdin() {
        return stdin;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
}
