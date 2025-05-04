#!/bin/bash

# Test cases for different languages
declare -A test_cases=(
    ["python"]='print("Hello from Python")'
    ["c"]='#include <stdio.h>\nint main() { printf("Hello from C\\n"); return 0; }'
    ["cpp"]='#include <iostream>\nint main() { std::cout << "Hello from C++" << std::endl; return 0; }'
    ["javascript"]='console.log("Hello from JavaScript")'
    ["php"]='<?php echo "Hello from PHP"; ?>'
    ["ruby"]='puts "Hello from Ruby"'
    ["go"]='package main\nimport "fmt"\nfunc main() { fmt.Println("Hello from Go") }'
    ["perl"]='print "Hello from Perl\\n"'
    ["lua"]='print("Hello from Lua")'
    ["r"]='print("Hello from R")'
    ["dart"]='void main() { print("Hello from Dart"); }'
    ["typescript"]='console.log("Hello from TypeScript")'
    ["swift"]='print("Hello from Swift")'
    ["bash"]='echo "Hello from Bash"'
    ["java"]='public class Main { public static void main(String[] args) { System.out.println("Hello from Java"); } }'
)

# Test each language
for lang in "${!test_cases[@]}"; do
    echo "Testing $lang..."
    # Properly escape the code for JSON
    code=$(echo "${test_cases[$lang]}" | sed 's/\\/\\\\/g' | sed 's/"/\\"/g')
    curl -X POST -H "Content-Type: application/json" \
         -d "{\"language\":\"$lang\",\"code\":\"$code\"}" \
         http://localhost:5050/api/compiler/execute
    echo -e "\n"
done 