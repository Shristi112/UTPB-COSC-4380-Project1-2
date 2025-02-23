import java.util.Arrays;

public class PolyalphabeticCipher {
    private final char[][] beta;
    private final String alphabet;
    private final String key;

    public PolyalphabeticCipher(String alphabet, String key) {
        this.alphabet = alphabet + alphabet.toLowerCase() + " "; // Include both uppercase, lowercase, and space in alphabet
        this.key = key;
        this.beta = generateBetaMatrix(this.alphabet);
    }

    private char[][] generateBetaMatrix(String alphabet) {
        int length = alphabet.length();
        char[][] matrix = new char[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                matrix[i][j] = alphabet.charAt((i + j) % length);
            }
        }
        return matrix;
    }

    public char[][] getBeta() {
        return beta;
    }

    public String encrypt(String plaintext) {
        StringBuilder ciphertext = new StringBuilder();
        int keyLength = key.length();
        
        for (int i = 0; i < plaintext.length(); i++) {
            char plainChar = plaintext.charAt(i);
            char keyChar = key.charAt(i % keyLength);
            int row = alphabet.indexOf(keyChar);
            int col = alphabet.indexOf(plainChar);
            if (col == -1 || row == -1) {
                ciphertext.append(plainChar);
            } else {
                ciphertext.append(beta[row][col]);
            }
        }
        return ciphertext.toString();
    }

    public String decrypt(String ciphertext) {
        StringBuilder plaintext = new StringBuilder();
        int keyLength = key.length();
        
        for (int i = 0; i < ciphertext.length(); i++) {
            char cipherChar = ciphertext.charAt(i);
            char keyChar = key.charAt(i % keyLength);
            int row = alphabet.indexOf(keyChar);
            
            if (row == -1) {
                plaintext.append(cipherChar);
            } else {
                int col = new String(beta[row]).indexOf(cipherChar);
                plaintext.append(alphabet.charAt(col));
            }
        }
        return plaintext.toString();
    }

    public static void main(String[] args) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String key = "Key";
        
        PolyalphabeticCipher cipher = new PolyalphabeticCipher(alphabet, key);
        String plaintext = "Cryptography is Fun";
        String encrypted = cipher.encrypt(plaintext);
        String decrypted = cipher.decrypt(encrypted);
        
        System.out.println("Plaintext: " + plaintext);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
    }
}
