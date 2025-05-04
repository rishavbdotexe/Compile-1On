package com.example.onlinecompiler.models;

public class Language {

    private String name;
    private int imageResId;
    private String compilerVersion;

    public Language(String name, int imageResId, String compilerVersion) {
        this.name = name;
        this.imageResId = imageResId;
        this.compilerVersion = compilerVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public void setCompilerVersion(String compilerVersion) {
        this.compilerVersion = compilerVersion;
    }
}
