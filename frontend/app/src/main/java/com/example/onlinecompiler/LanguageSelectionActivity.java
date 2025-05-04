package com.example.onlinecompiler;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlinecompiler.adapter.LanguageAdapter;
import com.example.onlinecompiler.models.Language;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LanguageAdapter languageAdapter;
    private List<Language> languageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);


        View rootView = findViewById(R.id.rootView);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.languageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize language list
        languageList = new ArrayList<>();

        // Add languages to the list with their image resource and compiler version
        languageList.add(new Language("C", R.drawable.c, "GCC 10.2"));
        languageList.add(new Language("C++", R.drawable.cpp, "G++ 10.2"));
        languageList.add(new Language("Java", R.drawable.java, "OpenJDK 21"));
        languageList.add(new Language("Python", R.drawable.python, "Python 3.9.2"));
        languageList.add(new Language("Bash", R.drawable.bash, "Bash 5.1"));
        languageList.add(new Language("Dart", R.drawable.dart, "Dart 2.13"));
        languageList.add(new Language("Go", R.drawable.go, "Go 1.16"));
        languageList.add(new Language("JavaScript", R.drawable.javascript, "Node.js 14"));
        languageList.add(new Language("Lua", R.drawable.lua, "Lua 5.4"));
        languageList.add(new Language("Perl", R.drawable.perl, "Perl 5.32"));
        languageList.add(new Language("PHP", R.drawable.php, "PHP 7.4"));
        languageList.add(new Language("R", R.drawable.r, "R 4.0.5"));
        languageList.add(new Language("Ruby", R.drawable.ruby, "Ruby 2.7"));
        languageList.add(new Language("TypeScript", R.drawable.typescript, "TypeScript 4.2"));

        // Set up the adapter with the language list
        languageAdapter = new LanguageAdapter(languageList);
        recyclerView.setAdapter(languageAdapter);

        // Set click listener for language selection
        languageAdapter.setOnItemClickListener(new LanguageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Language language) {
                // Pass the selected language to MainActivity via Intent
                Intent intent = new Intent(LanguageSelectionActivity.this, MainActivity.class);
                intent.putExtra("selectedLanguage", language.getName());
                intent.putExtra("compilerVersion", language.getCompilerVersion());
                startActivity(intent);
            }
        });
    }
}
