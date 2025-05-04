package com.example.onlinecompiler;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.onlinecompiler.models.CompileRequest;
import com.example.onlinecompiler.models.CompileResponse;
import com.example.onlinecompiler.network.ApiClient;
import com.example.onlinecompiler.network.ApiService;

import java.io.*;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.database.Cursor;

import io.github.rosemoe.sora.langs.java.JavaLanguage;
import android.view.MenuItem;
import android.os.Environment;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    // Request code for file picking intent
    private static final int PICK_CODE_FILE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private String selectedLanguage; // Currently selected programming language
    private ApiService apiService; // Retrofit API service interface

    // UI components
    private CodeEditor editor; // Code editor with syntax highlighting
    private EditText userInput; // Input field for stdin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.rootView);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        editor = findViewById(R.id.editor);

        EditorColorScheme scheme = editor.getColorScheme();

        // Set VS Code-like color scheme
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, getResources().getColor(R.color.vscode_editor_bg));
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, getResources().getColor(R.color.vscode_text));
        scheme.setColor(EditorColorScheme.KEYWORD, getResources().getColor(R.color.vscode_keyword));
        scheme.setColor(EditorColorScheme.LITERAL, getResources().getColor(R.color.vscode_string));
        scheme.setColor(EditorColorScheme.COMMENT, getResources().getColor(R.color.vscode_comment));
        scheme.setColor(EditorColorScheme.FUNCTION_NAME, getResources().getColor(R.color.vscode_function));
        scheme.setColor(EditorColorScheme.OPERATOR, getResources().getColor(R.color.vscode_operator));
        scheme.setColor(EditorColorScheme.LINE_NUMBER, getResources().getColor(R.color.vscode_line_number));
        scheme.setColor(EditorColorScheme.CURRENT_LINE, getResources().getColor(R.color.vscode_current_line));
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, getResources().getColor(R.color.vscode_selection));
        scheme.setColor(EditorColorScheme.SELECTION_HANDLE, getResources().getColor(R.color.vscode_selection));
        scheme.setColor(EditorColorScheme.ANNOTATION, getResources().getColor(R.color.vscode_variable));
        scheme.setColor(EditorColorScheme.IDENTIFIER_NAME, getResources().getColor(R.color.vscode_variable));

        // Apply the scheme to the editor
        editor.setColorScheme(scheme);
        editor.setEditorLanguage(new JavaLanguage());
        editor.setTypefaceText(Typeface.MONOSPACE);

        // Initialize UI components
        userInput = findViewById(R.id.userInput);

        // Get selected language from intent (passed from previous activity)
        selectedLanguage = getIntent().getStringExtra("selectedLanguage");

        // Handle code input and user input persistence using SharedPreferences
        SharedPreferences prefs = getSharedPreferences("CompilerPrefs", MODE_PRIVATE);
        userInput.setText(prefs.getString("input", "")); // Load saved input if exists

        // TextWatcher to save user input changes automatically
        TextWatcher saver = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                // Save input whenever it changes
                prefs.edit().putString("input", userInput.getText().toString()).apply();
            }
        };
        userInput.addTextChangedListener(saver);

        apiService = ApiClient.getApiService(); // Initialize Retrofit API service

        // Pre-fill default code snippets for the selected language
        prefillCodeForLanguage(selectedLanguage);
    }

    private void showOutputPopup(String output) {
        // Create and inflate the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.OutputDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.output_popup, null);
        
        // Get views
        TextView popupOutputText = view.findViewById(R.id.popupOutputText);
        Button popupCopyButton = view.findViewById(R.id.popupCopyButton);
        
        // Set output text
        popupOutputText.setText(output);
        
        // Make output text scrollable
        popupOutputText.setMovementMethod(new ScrollingMovementMethod());
        
        // Create dialog
        AlertDialog dialog = builder.setView(view)
                .setCancelable(true)
                .create();
        
        // Set up copy button
        popupCopyButton.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Output", output));
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        // Show dialog with window animations
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        dialog.show();
    }

    // Handle file upload result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (checkStoragePermission()) {
                // Permission granted, save the file
                String code = editor.getText().toString();
                saveCodeToFile(code);
            } else {
                Toast.makeText(this, "Storage permission is required to save files", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PICK_CODE_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            // Check file size and extension
            String ext = getFileExtension(uri);
            long fileSize = getFileSize(uri);
            if (fileSize > 2 * 1024 * 1024) { // 2MB limit
                showOutputPopup("File too large. Max 2 MB allowed.");
                return;
            }

            if (!isAllowedExtension(ext)) {
                showOutputPopup("Invalid file type. Allowed: .c, .cpp, .py, .java, .sh");
                return;
            }

            try {
                // Create temp file and upload
                File file = createTempFileFromUri(uri);
                uploadCodeFile(file);
            } catch (IOException e) {
                showOutputPopup("File read error: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_compile) {
            String code = editor.getText().toString().trim();
            String input = userInput.getText().toString().trim();

            if (code.isEmpty()) {
                Toast.makeText(this, "Code cannot be empty", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Show loading indicator
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Compiling...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Create compile request and send to API
            CompileRequest request = new CompileRequest(selectedLanguage, code, input);
            apiService.compileCode(request).enqueue(new Callback<CompileResponse>() {
                @Override
                public void onResponse(Call<CompileResponse> call, Response<CompileResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        showOutputPopup(response.body().getOutput());
                    } else {
                        showOutputPopup("Error: Response code " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<CompileResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    showOutputPopup("Connection error: " + t.getMessage());
                }
            });
            return true;
        }
        else if (id == R.id.action_upload_file) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Accept all file types (filtered later)
            startActivityForResult(intent, PICK_CODE_FILE);
            return true;
        }
        else if (id == R.id.action_save_code) {
            String code = editor.getText().toString();
            if (code.isEmpty()) {
                Toast.makeText(this, "Code is empty. Nothing to save.", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (checkStoragePermission()) {
                saveCodeToFile(code);
            } else {
                requestStoragePermission();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, check for MANAGE_EXTERNAL_STORAGE
            return Environment.isExternalStorageManager();
        } else {
            // For Android 10 and below, check for WRITE_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, request MANAGE_EXTERNAL_STORAGE
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            // For Android 10 and below, request WRITE_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    // Returns appropriate file extension for given programming language
    private String getExtensionForLanguage(String language) {
        switch (language.toLowerCase()) {
            case "c": return ".c";
            case "c++": return ".cpp";
            case "java": return ".java";
            case "python": return ".py";
            case "kotlin": return ".kt";
            case "javascript": return ".js";
            case "typescript": return ".ts";
            case "go": return ".go";
            case "rust": return ".rs";
            case "bash":
            case "shell": return ".sh";
            case "ruby": return ".rb";
            case "php": return ".php";
            case "swift": return ".swift";
            case "dart": return ".dart";
            case "scala": return ".scala";
            case "perl": return ".pl";
            case "haskell": return ".hs";
            case "lua": return ".lua";
            case "r": return ".r";
            case "sql": return ".sql";
            case "html": return ".html";
            case "css": return ".css";
            default: return ".txt"; // fallback for unknown languages
        }
    }

    // Pre-fill editor with language-specific code snippets
    private void prefillCodeForLanguage(String language) {
        String defaultCode = "";

        switch (language) {
            case "C":
                defaultCode = "#include <stdio.h>\nint main() {\n    printf(\"Heylo C\");\n    return 0;\n}";
                break;
            case "C++":
                defaultCode = "#include <iostream>\nint main() {\n    std::cout << \"Heylo C++\" << std::endl;\n    return 0;\n}";
                break;
            case "Java":
                defaultCode = "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Heylo Java\");\n    }\n}";
                break;
            case "Python":
                defaultCode = "print('Heylo Python')";
                break;
            case "Bash":
                defaultCode = "#!/bin/bash\necho \"Heylo Bash\"";
                break;
            case "Dart":
                defaultCode = "void main() {\n  print('Heylo Dart');\n}";
                break;
            case "Go":
                defaultCode = "package main\nimport \"fmt\"\nfunc main() {\n fmt.Println(\"Heylo Go\")\n}";
                break;
            case "JavaScript":
                defaultCode = "console.log('Heylo JavaScript');";
                break;
            case "Lua":
                defaultCode = "print('Heylo Lua')";
                break;
            case "Perl":
                defaultCode = "print \"Heylo Perl\\n\";";
                break;
            case "PHP":
                defaultCode = "<?php\necho 'Heylo PHP';";
                break;
            case "R":
                defaultCode = "cat('Heylo R\\n')";
                break;
            case "Ruby":
                defaultCode = "puts 'Heylo Ruby'";
                break;
            case "Swift":
                defaultCode = "print(\"Heylo Swift\")";
                break;
            case "TypeScript":
                defaultCode = "console.log('Heylo TypeScript');";
                break;
        }

        editor.setText(defaultCode); // Set the default code in the editor

        getSupportActionBar().setTitle(language);
    }

    // Get file extension from URI
    private String getFileExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
        // Fallback to URL parsing
        String path = uri.getPath();
        if (path != null) {
            int lastDot = path.lastIndexOf('.');
            if (lastDot != -1) {
                return path.substring(lastDot + 1).toLowerCase();
            }
        }
        return "";
    }

    // Get file size from URI
    private long getFileSize(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            long size = cursor.getLong(sizeIndex);
            cursor.close();
            return size;
        }
        return 0;
    }

    // Check if file extension is allowed for compilation
    private boolean isAllowedExtension(String ext) {
        if (ext == null) return false;
        ext = ext.toLowerCase();
        return ext.equals("c") || ext.equals("cpp") || ext.equals("java") || ext.equals("py") ||
                ext.equals("sh") || ext.equals("dart") || ext.equals("go") || ext.equals("js") ||
                ext.equals("lua") || ext.equals("pl") || ext.equals("php") || ext.equals("r") ||
                ext.equals("rb") || ext.equals("swift") || ext.equals("ts");
    }

    // Create temporary file from URI content
    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        String ext = getFileExtension(uri);
        if (ext.isEmpty()) {
            throw new IOException("Could not determine file extension");
        }
        
        // For Java files, ensure the extension is .java
        if (selectedLanguage.equalsIgnoreCase("Java")) {
            ext = "java";
        }
        
        File tempFile = File.createTempFile("uploaded_code", "." + ext, getCacheDir());
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        return tempFile;
    }

    // Upload code file to backend API
    private void uploadCodeFile(File file) {
        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Prepare multipart request
        RequestBody filePart = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), filePart);
        RequestBody languagePart = RequestBody.create(MultipartBody.FORM, selectedLanguage);
        RequestBody stdinPart = RequestBody.create(MultipartBody.FORM, userInput.getText().toString());

        // Make API call
        apiService.uploadCode(body, languagePart, stdinPart).enqueue(new Callback<CompileResponse>() {
            @Override
            public void onResponse(Call<CompileResponse> call, Response<CompileResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    showOutputPopup(response.body().getOutput());
                } else {
                    String errorMessage = "Error: Upload failed. ";
                    if (response.code() == 400) {
                        errorMessage += "Invalid file format or content.";
                    } else if (response.code() == 413) {
                        errorMessage += "File too large.";
                    } else {
                        errorMessage += "Response code: " + response.code();
                    }
                    showOutputPopup(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CompileResponse> call, Throwable t) {
                progressDialog.dismiss();
                showOutputPopup("Upload failed: " + t.getMessage());
            }
        });
    }

    private void saveCodeToFile(String code) {
        // For Java files, extract class name and validate
        if (selectedLanguage.equalsIgnoreCase("Java")) {
            String className = extractJavaClassName(code);
            if (className == null) {
                Toast.makeText(this, "Java file must contain a public class", Toast.LENGTH_LONG).show();
                return;
            }
            showSaveDialog(code, className + ".java");
        } else {
            showSaveDialog(code, null);
        }
    }

    private String extractJavaClassName(String code) {
        // Simple regex to find public class name
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("public\\s+class\\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void showSaveDialog(String code, String defaultName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.OutputDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.save_file_dialog, null);
        EditText fileNameInput = view.findViewById(R.id.fileNameInput);
        
        if (defaultName != null) {
            fileNameInput.setText(defaultName);
            fileNameInput.setEnabled(false); // Disable editing for Java files
        } else {
            String extension = getExtensionForLanguage(selectedLanguage);
            fileNameInput.setText("code_" + System.currentTimeMillis() + extension);
        }

        AlertDialog dialog = builder.setView(view)
               .setTitle("Save File")
               .setPositiveButton("Save", null) // Set to null initially
               .setNegativeButton("Cancel", null) // Set to null initially
               .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            
            // Set VS Code theme colors
            positiveButton.setTextColor(getResources().getColor(R.color.vscode_text));
            negativeButton.setTextColor(getResources().getColor(R.color.vscode_text));
            
            positiveButton.setOnClickListener(v -> {
                String fileName = fileNameInput.getText().toString().trim();
                if (fileName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a file name", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveFileWithName(code, fileName);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void saveFileWithName(String code, String fileName) {
        try {
            // Always use root directory
            File rootDir = Environment.getExternalStorageDirectory();
            File dir = new File(rootDir, "Compile - 1O(n)");
            
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    Toast.makeText(this, "Failed to create directory. Please check storage permissions.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            File file = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(code.getBytes());
            fos.close();

            // Show success message with file path
            String message = "Saved as: " + file.getAbsolutePath();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
            // Open the file location
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(dir), "resource/folder");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "File saved but could not open location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}