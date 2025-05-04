package com.example.onlinecompiler.models;

public class LanguageVersion {
    public static String getVersion(String languageName) {
        switch (languageName.toLowerCase()) {
            case "c":
            case "cpp":
                return "GCC 10.2.1";
            case "java":
                return "OpenJDK 21";
            case "python":
                return "Python 3.9.2";
            case "javascript":
                return "Node.js v18.20.8";
            case "php":
                return "PHP 7.4.33";
            case "ruby":
                return "Ruby 2.7.4";
            case "go":
                return "Go 1.15.15";
            case "bash":
                return "Bash 5.1.4";
            case "perl":
                return "Perl 5.32.1";
            case "lua":
                return "Lua 5.4.2";
            case "r":
                return "R 4.0.4";
            case "dart":
                return "Dart 3.7.3";
            case "swift":
                return "Swift 5.8.3";
            default:
                return "Unknown Version";
        }
    }
}
