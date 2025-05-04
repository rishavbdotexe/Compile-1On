#!/bin/bash

# Base URL
BASE_URL="http://localhost:5050/api/compiler/execute"

# Test function
test_language() {
    local language=$1
    local code=$2
    local stdin=$3
    local test_name=$4

    echo "Testing $language - $test_name"
    echo "Code: $code"
    echo "Stdin: $stdin"
    
    curl -X POST "$BASE_URL" \
        -H "Content-Type: application/json" \
        -d "{
            \"language\": \"$language\",
            \"code\": \"$code\",
            \"stdin\": \"$stdin\"
        }"
    echo -e "\n\n"
}

# Test cases for each language
test_cases() {
    # Java tests
    test_language "java" "public class Main { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }" "" "Basic print"
    test_language "java" "public class Main { public static void main(String[] args) { System.out.println(new java.util.Scanner(System.in).nextLine()); } }" "Test Input" "Input handling"

    # C tests
    test_language "c" "#include <stdio.h>\nint main() { printf(\"Hello, World!\\n\"); return 0; }" "" "Basic print"
    test_language "c" "#include <stdio.h>\nint main() { char str[100]; scanf(\"%s\", str); printf(\"%s\\n\", str); return 0; }" "Test Input" "Input handling"

    # C++ tests
    test_language "cpp" "#include <iostream>\nint main() { std::cout << \"Hello, World!\" << std::endl; return 0; }" "" "Basic print"
    test_language "cpp" "#include <iostream>\n#include <string>\nint main() { std::string str; std::cin >> str; std::cout << str << std::endl; return 0; }" "Test Input" "Input handling"

    # Python tests
    test_language "python" "print('Hello, World!')" "" "Basic print"
    test_language "python" "print(input())" "Test Input" "Input handling"

    # JavaScript tests
    test_language "javascript" "console.log('Hello, World!')" "" "Basic print"
    test_language "javascript" "console.log(require('fs').readFileSync(0, 'utf-8'))" "Test Input" "Input handling"

    # TypeScript tests
    test_language "typescript" "console.log('Hello, World!')" "" "Basic print"
    test_language "typescript" "console.log(require('fs').readFileSync(0, 'utf-8'))" "Test Input" "Input handling"

    # Bash tests
    test_language "bash" "echo 'Hello, World!'" "" "Basic print"
    test_language "bash" "read input; echo $input" "Test Input" "Input handling"

    # PHP tests
    test_language "php" "<?php echo 'Hello, World!'; ?>" "" "Basic print"
    test_language "php" "<?php echo fgets(STDIN); ?>" "Test Input" "Input handling"

    # Ruby tests
    test_language "ruby" "puts 'Hello, World!'" "" "Basic print"
    test_language "ruby" "puts gets" "Test Input" "Input handling"

    # Go tests
    test_language "go" "package main\nimport \"fmt\"\nfunc main() { fmt.Println(\"Hello, World!\") }" "" "Basic print"
    test_language "go" "package main\nimport \"fmt\"\nfunc main() { var input string\nfmt.Scan(&input)\nfmt.Println(input) }" "Test Input" "Input handling"

    # Perl tests
    test_language "perl" "print \"Hello, World!\\n\";" "" "Basic print"
    test_language "perl" "my $input = <STDIN>; print $input;" "Test Input" "Input handling"

    # Lua tests
    test_language "lua" "print('Hello, World!')" "" "Basic print"
    test_language "lua" "print(io.read())" "Test Input" "Input handling"

    # R tests
    test_language "r" "cat('Hello, World!\\n')" "" "Basic print"
    test_language "r" "input <- readLines('stdin', n=1); cat(input, '\\n')" "Test Input" "Input handling"

    # Dart tests
    test_language "dart" "void main() { print('Hello, World!'); }" "" "Basic print"
    test_language "dart" "import 'dart:io';\nvoid main() { print(stdin.readLineSync()); }" "Test Input" "Input handling"

    # Swift tests
    test_language "swift" "print(\"Hello, World!\")" "" "Basic print"
    test_language "swift" "if let input = readLine() { print(input) }" "Test Input" "Input handling"
}

# Run all tests
test_cases 