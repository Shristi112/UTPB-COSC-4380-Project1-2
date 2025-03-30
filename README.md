# Cryptographic Ciphers (UTPB-COSC-4380 Projects 1 & 2)

## Overview
This repository contains two Java implementations of classical cryptographic ciphers:
1. **Polyalphabetic Cipher** (Project 1)
2. **Columnar Transposition Cipher** (Project 2)

## Project 1: Polyalphabetic Cipher
### Features
- Implements a **polyalphabetic substitution cipher**
- Supports **uppercase, lowercase letters, and spaces**
- Provides an **instantiable class** with easy-to-use methods
- Ensures security by making the encryption key **private and final**

### Class Implementation
#### `PolyalphabeticCipher` Class
**Constructor**:
```java
public PolyalphabeticCipher(String alphabet, String key)
```

**Methods**:
- `getBeta()`: Returns the beta matrix
- `encrypt(String plaintext)`: Encrypts plaintext
- `decrypt(String ciphertext)`: Decrypts ciphertext

### Example Usage
```java
String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
String key = "Key";
PolyalphabeticCipher cipher = new PolyalphabeticCipher(alphabet, key);

String plaintext = "Hello World";
String encrypted = cipher.encrypt(plaintext);
String decrypted = cipher.decrypt(encrypted);
```

## Project 2: Columnar Transposition Cipher Cracker
### Features
- Implements a **columnar transposition cipher cracker**
- Uses **frequency analysis and brute-force techniques**
- Supports cracking without knowing the original key
- Includes **English language detection** for automated cracking

### Class Implementation
#### `ColumnarCracker` Class
**Methods**:
- `crack(String ciphertext)`: Attempts to crack the ciphertext
- `decrypt(String ciphertext, int[] columnOrder)`: Decrypts with known column order
- `isEnglish(String text)`: Validates potential plaintext

### Example Usage
```java
ColTransCracker = new ColTransCracker();
String ciphertext = "LOHELWRDOLX"; // Encrypted "HELLO WORLD"
String plaintext = cracker.crack(ciphertext);
```

## Security Considerations
- Both ciphers use **private final** keys for security
- Columnar cracker implements multiple validation checks:
  - Common English word detection
  - Bigram frequency analysis
  - Letter frequency validation

## Notes
- For Project 2, the cracker works best with **short to medium length texts**
- Only characters present in the defined alphabet are encrypted
- Both projects include **complete JavaDoc documentation**

## How to Run
1. Compile all Java files
2. Run the main method in either:
   - `PolyalphabeticCipher.java` (Project 1)
   - `ColTransCracker.java` (Project 2)

Sample test cases are provided in each main method.