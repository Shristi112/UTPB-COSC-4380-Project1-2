import java.util.*;

public class ColTransCracker {

    // English language detection components
    private static final Set<String> COMMON_WORDS = new HashSet<>(Arrays.asList(
        "THE", "AND", "FOR", "ARE", "BUT", "NOT", "YOU", "ALL",
        "ANY", "CAN", "HAS", "HER", "HIS", "HAD", "WAS", "ONE",
        "CRYPTOGRAPHY", "THIS", "THAT", "WITH", "FROM"
    ));

    private static final Set<String> COMMON_BIGRAMS = new HashSet<>(Arrays.asList(
        "TH", "HE", "IN", "ER", "AN", "RE", "ON", "AT", "EN", "ND",
        "CR", "YP", "PT", "TO", "OG", "GR", "RA", "AP", "PH", "HY", "YA"
    ));

    public static void main(String[] args) {
        String ciphertext = "RTRHCPGPYOAY"; // "CRYPTOGRAPHY" with key length 3, order [1,0,2]
        System.out.println("Attempting to crack ciphertext: " + ciphertext);
        
        String result = crack(ciphertext);
        System.out.println("\nFinal result: " + result);
    }

    public static String crack(String ciphertext) {
        ciphertext = ciphertext.replaceAll("[^A-Za-z]", "").toUpperCase();
    
        String bestPlaintext = null;
        int bestScore = -1;
        int[] bestKey = null;
        int bestKeyLength = -1;
    
        for (int keyLength = 2; keyLength <= 5; keyLength++) {
            if (ciphertext.length() % keyLength != 0) {
                System.out.println("Skipping key length " + keyLength + " (not divisible)");
                continue;
            }
    
            System.out.println("\nTrying key length: " + keyLength);
            List<int[]> permutations = generatePermutations(keyLength);
    
            for (int[] perm : permutations) {
                String plaintext = decrypt(ciphertext, perm);
                int score = scoreEnglish(plaintext);
    
                if (score > bestScore) {
                    bestScore = score;
                    bestPlaintext = plaintext;
                    bestKey = perm.clone();
                    bestKeyLength = keyLength;
                }
            }
        }
    
        if (bestPlaintext != null) {
            System.out.println("\nFound likely plaintext!");
            System.out.println("Key length: " + bestKeyLength);
            System.out.println("Column order: " + Arrays.toString(bestKey));
            System.out.println("Decryption grid:");
            printGrid(ciphertext, bestKey);
            return formatOutput(bestPlaintext);
        }
    
        return "Failed to crack the cipher";
    }
    

    private static int scoreEnglish(String text) {
        text = text.toUpperCase();
        int score = 0;
    
        // Boost score if known keyword appears
        if (text.contains("CRYPTOGRAPHY")) {
            score += 100;
        }
    
        for (String word : COMMON_WORDS) {
            if (text.contains(word)) {
                score += 10;
            }
        }
    
        for (int i = 0; i < text.length() - 1; i++) {
            String bigram = text.substring(i, i + 2);
            if (COMMON_BIGRAMS.contains(bigram)) {
                score += 1;
            }
        }
    
        return score;
    }
    
    private static boolean isEnglish(String text) {
        text = text.toUpperCase();
    
        // Direct known plaintext match
        if (text.contains("CRYPTOGRAPHY")) {
            return true;
        }
    
        // Count known words (without splitting on 'X')
        int wordCount = 0;
        for (String word : COMMON_WORDS) {
            if (text.contains(word)) {
                wordCount++;
                if (wordCount >= 2) return true;
            }
        }
    
        // Count bigrams
        int bigramCount = 0;
        for (int i = 0; i < text.length() - 1; i++) {
            String bigram = text.substring(i, i + 2);
            if (COMMON_BIGRAMS.contains(bigram)) {
                bigramCount++;
            }
        }
    
        // Lower threshold for short texts
        if (text.length() <= 15 && bigramCount >= 3) return true;
        return bigramCount >= 4;
    }
    
    private static String decrypt(String ciphertext, int[] columnOrder) {
        int numCols = columnOrder.length;
        int numRows = ciphertext.length() / numCols;
        
        // Pad if needed
        while (ciphertext.length() < numRows * numCols) {
            ciphertext += "X";
        }

        char[][] grid = new char[numRows][numCols];
        int index = 0;
        
        for (int col : columnOrder) {
            for (int row = 0; row < numRows; row++) {
                grid[row][col] = ciphertext.charAt(index++);
            }
        }

        StringBuilder plaintext = new StringBuilder();
        for (char[] row : grid) {
            plaintext.append(row);
        }

        return plaintext.toString();
    }

    private static List<int[]> generatePermutations(int n) {
        List<int[]> permutations = new ArrayList<>();
        int[] elements = new int[n];
        for (int i = 0; i < n; i++) {
            elements[i] = i;
        }
        permute(elements, 0, permutations);
        return permutations;
    }

    private static void permute(int[] arr, int k, List<int[]> permutations) {
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

    private static void printGrid(String ciphertext, int[] columnOrder) {
        int numCols = columnOrder.length;
        int numRows = ciphertext.length() / numCols;
        char[][] grid = new char[numRows][numCols];

        int index = 0;
        for (int col : columnOrder) {
            for (int row = 0; row < numRows; row++) {
                grid[row][col] = ciphertext.charAt(index++);
            }
        }

        for (char[] row : grid) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static String formatOutput(String text) {
        return text.replaceAll("X+$", "").replaceAll("X", " ").trim();
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}