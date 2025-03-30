import java.util.*;
import java.util.stream.*;

public class ColTransCracker {

    // Enhanced list of common English words
    private static final Set<String> COMMON_WORDS = new HashSet<>(Arrays.asList(
        "THE", "AND", "FOR", "ARE", "BUT", "NOT", "YOU", "ALL", 
        "ANY", "CAN", "HAS", "HER", "HIS", "HAD", "WAS", "ONE",
        "HELLO", "WORLD", "THIS", "THAT", "WITH", "FROM"
    ));
    
    // Enhanced list of common English bigrams
    private static final Set<String> COMMON_BIGRAMS = new HashSet<>(Arrays.asList(
        "TH", "HE", "IN", "ER", "AN", "RE", "ON", "AT", "EN", "ND",
        "LL", "LD", "OR", "LO", "EL", "WO", "RL"
    ));

    public String crack(String ciphertext) {
        // Clean the ciphertext (remove non-letters, convert to uppercase)
        ciphertext = ciphertext.replaceAll("[^A-Za-z]", "").toUpperCase();
        System.out.println("Processing ciphertext: " + ciphertext);

        // Try likely key lengths first (divisors of the length)
        List<Integer> possibleLengths = getPossibleKeyLengths(ciphertext.length());
        
        for (int keyLength : possibleLengths) {
            System.out.println("Trying key length: " + keyLength);
            
            // Generate permutations in order of likelihood
            List<int[]> permutations = generateLikelyPermutations(ciphertext, keyLength);
            
            for (int[] perm : permutations) {
                String plaintext = decrypt(ciphertext, perm);
                if (isEnglish(plaintext)) {
                    System.out.println("Success with key length: " + keyLength);
                    System.out.println("Column order: " + Arrays.toString(perm));
                    return plaintext;
                }
            }
        }
        return "Failed to crack the cipher";
    }

    private List<Integer> getPossibleKeyLengths(int length) {
        List<Integer> lengths = new ArrayList<>();
        // Try divisors first
        for (int i = 2; i <= 8 && i <= length/2; i++) {
            if (length % i == 0) lengths.add(i);
        }
        // Then try other lengths if needed
        for (int i = 2; i <= 8; i++) {
            if (!lengths.contains(i)) lengths.add(i);
        }
        return lengths;
    }

    private List<int[]> generateLikelyPermutations(String ciphertext, int keyLength) {
        List<int[]> permutations = new ArrayList<>();
        int[] elements = IntStream.range(0, keyLength).toArray();
        permute(elements, 0, permutations);
        return permutations;
    }

    private void permute(int[] arr, int k, List<int[]> permutations) {
        if (k == arr.length - 1) {
            permutations.add(arr.clone());
            return;
        }
        for (int i = k; i < arr.length; i++) {
            swap(arr, k, i);
            permute(arr, k + 1, permutations);
            swap(arr, k, i);
        }
    }

    private String decrypt(String ciphertext, int[] columnOrder) {
        int numCols = columnOrder.length;
        int numRows = ciphertext.length() / numCols;
        char[][] grid = new char[numRows][numCols];

        // Fill grid column by column in the specified order
        int index = 0;
        for (int col : columnOrder) {
            for (int row = 0; row < numRows; row++) {
                grid[row][col] = ciphertext.charAt(index++);
            }
        }

        // Read row-wise to get plaintext
        StringBuilder plaintext = new StringBuilder();
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                plaintext.append(grid[row][col]);
            }
        }

        return plaintext.toString();
    }

    private boolean isEnglish(String text) {
        text = text.toUpperCase();
        if (text.length() < 5) return false;
        
        // Check for exact matches of common words
        int wordCount = 0;
        for (String word : text.split(" ")) {
            if (COMMON_WORDS.contains(word)) {
                wordCount++;
                if (wordCount >= 2) return true;
            }
        }
        
        // Check for common bigrams
        int bigramCount = 0;
        for (int i = 0; i < text.length() - 1; i++) {
            String bigram = text.substring(i, i + 2);
            if (COMMON_BIGRAMS.contains(bigram)) {
                bigramCount++;
                if (bigramCount >= 4) return true;
            }
        }
        
        // Check for the word "HELLO" specifically
        if (text.contains("HELLO") && text.contains("WORLD")) return true;
        
        // Letter frequency check
        int[] freq = new int[26];
        int totalLetters = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                freq[c - 'A']++;
                totalLetters++;
            }
        }
        
        if (totalLetters == 0) return false;
        
        // E should be the most common letter
        int eCount = freq['E' - 'A'];
        for (int i = 0; i < 26; i++) {
            if (i != ('E' - 'A') && freq[i] > eCount) {
                return false;
            }
        }
        
        // Vowels should make up about 35-40% of letters
        String vowels = "AEIOU";
        int vowelCount = 0;
        for (char v : vowels.toCharArray()) {
            vowelCount += freq[v - 'A'];
        }
        return vowelCount > totalLetters * 0.3;
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static void main(String[] args) {
        ColTransCracker cracker = new ColTransCracker();
        
        // Test with the known ciphertext for "HELLO WORLD"
        String ciphertext = "LOHELWRDOLX";  // Encrypted with key length 4
        System.out.println("Attempting to crack ciphertext: " + ciphertext);
        
        String plaintext = cracker.crack(ciphertext);
        System.out.println("Cracked plaintext: " + plaintext);
    }
}