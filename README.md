# Polyalphabetic Cipher (Project 1 UTPB-COSC-4380)

## Overview
This project is a Java implementation of a **Polyalphabetic Cipher**, a type of encryption that uses multiple substitution alphabets to encode messages securely. The cipher allows encryption and decryption of text using a given key and a modified alphabet that includes **uppercase, lowercase letters, and spaces**.

## Features
- Implements a **polyalphabetic substitution cipher**.
- Supports **both uppercase and lowercase letters**, as well as **spaces**.
- Provides an **instantiable class** with easy-to-use methods.
- Ensures security by making the encryption key **private and final**.

## Class Implementation
### `PolyalphabeticCipher` Class
This class is responsible for encoding and decoding messages using a polyalphabetic cipher.

### Constructor
```java
public PolyalphabeticCipher(String alphabet, String key)
```
- Initializes the cipher with a given **alphabet** and **key**.
- Generates the **beta matrix**, which is used for character substitution.

### Methods
#### `getBeta()`
```java
public char[][] getBeta()
```
- Returns the generated **beta matrix** used for encryption and decryption.

#### `encrypt()`
```java
public String encrypt(String plaintext)
```
- Accepts a plaintext string and applies the **internally stored key** to generate a ciphertext.
- **Returns** the encrypted string.

#### `decrypt()`
```java
public String decrypt(String ciphertext)
```
- Accepts a ciphertext string and applies the **inverse encryption process** to retrieve the original message.
- **Returns** the decrypted string.

## Usage
### Example
```java
public static void main(String[] args) {
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String key = "Key";
    
    PolyalphabeticCipher cipher = new PolyalphabeticCipher(alphabet, key);
    String plaintext = "Hello World";
    String encrypted = cipher.encrypt(plaintext);
    String decrypted = cipher.decrypt(encrypted);
    
    System.out.println("Plaintext: " + plaintext);
    System.out.println("Encrypted: " + encrypted);
    System.out.println("Decrypted: " + decrypted);
}
```
### Expected Output
```
Plaintext: Hello World
Encrypted: (Ciphered Text)
Decrypted: Hello World
```

## Security Considerations
- The **key** is declared as `private final`, preventing modification after initialization.
- The **beta matrix** must remain consistent for both encryption and decryption.

## Notes
- Ensure the key and alphabet remain consistent when decrypting messages.
- Only characters present in the **defined alphabet** are encrypted; all others remain unchanged.


