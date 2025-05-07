import java.util.HashMap;
import java.util.Arrays;

public class AES {
    private static final boolean DEBUG = true; // Set to false to disable debug output
    
    private int[][] roundKey;
    private static final int N = 10; // Number of rounds for AES-128
    private static final int BLOCK_SIZE = 16; // 16 bytes = 128 bits
    
    private static final HashMap<Integer, Integer> RC = new HashMap<>();
    static {
        RC.put(1, 0x01);
        RC.put(2, 0x02);
        RC.put(3, 0x04);
        RC.put(4, 0x08);
        RC.put(5, 0x10);
        RC.put(6, 0x20);
        RC.put(7, 0x40);
        RC.put(8, 0x80);
        RC.put(9, 0x1B);
        RC.put(10, 0x36);
    }

    public AES(String key) {
        if (key.length() != 16) {
            throw new IllegalArgumentException("Key must be 16 characters long");
        }
        keyExpansion(key);
    }

    public String encrypt(String plaintext, boolean cbcMode) {
        if (plaintext.length() % 16 != 0) {
            // Pad plaintext to be multiple of block size
            plaintext = padString(plaintext);
        }
        
        int blockCount = plaintext.length() / 16;
        int[][] iv = null;
        if (cbcMode) {
            iv = generateIV();
            if (DEBUG) {
                System.out.println("Initialization Vector:");
                printBlock(iv);
            }
        }
        
        StringBuilder ciphertext = new StringBuilder();
        
        for (int i = 0; i < blockCount; i++) {
            int[][] block = getBlock(plaintext, i);
            
            if (DEBUG) {
                System.out.println("\nEncrypting Block " + (i + 1) + ":");
                printBlock(block);
            }
            
            if (cbcMode) {
                if (i == 0) {
                    // XOR with IV for first block
                    addRoundKey(block, iv);
                } else {
                    // XOR with previous ciphertext block
                    int[][] prevBlock = getBlock(ciphertext.toString(), i - 1);
                    addRoundKey(block, prevBlock);
                }
            }
            
            cipher(block, true);
            
            ciphertext.append(blockToString(block));
        }
        
        return ciphertext.toString();
    }

    public String decrypt(String ciphertext, boolean cbcMode) {
        if (ciphertext.length() % 16 != 0) {
            throw new IllegalArgumentException("Ciphertext length must be multiple of block size (16)");
        }
        
        int blockCount = ciphertext.length() / 16;
        int[][] iv = null;
        if (cbcMode) {
            iv = generateIV();
            if (DEBUG) {
                System.out.println("Initialization Vector:");
                printBlock(iv);
            }
        }
        
        StringBuilder plaintext = new StringBuilder();
        int[][] prevBlock = null;
        
        for (int i = 0; i < blockCount; i++) {
            int[][] block = getBlock(ciphertext, i);
            
            if (DEBUG) {
                System.out.println("\nDecrypting Block " + (i + 1) + ":");
                printBlock(block);
            }
            
            int[][] tempBlock = new int[4][4];
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    tempBlock[r][c] = block[r][c];
                }
            }
            
            cipher(block, false);
            
            if (cbcMode) {
                if (i == 0) {
                    // XOR with IV for first block
                    addRoundKey(block, iv);
                } else {
                    // XOR with previous ciphertext block
                    addRoundKey(block, prevBlock);
                }
            }
            
            prevBlock = tempBlock;
            plaintext.append(blockToString(block));
        }
        
        // Remove padding if any
        return unpadString(plaintext.toString());
    }

    private void cipher(int[][] block, boolean encryptMode) {
        if (encryptMode) {
            if (DEBUG) System.out.println("\nInitial AddRoundKey:");
            addRoundKey(block, Arrays.copyOfRange(roundKey, 0, 4));

            for (int round = 1; round < N; round++) {
                if (DEBUG) System.out.println("\nRound " + round + ":");
                
                if (DEBUG) System.out.println("SubBytes:");
                subBytes(block, encryptMode);
                
                if (DEBUG) System.out.println("ShiftRows:");
                shiftRows(block, encryptMode);
                
                if (DEBUG) System.out.println("MixColumns:");
                mixColumns(block, encryptMode);
                
                if (DEBUG) System.out.println("AddRoundKey:");
                addRoundKey(block, Arrays.copyOfRange(roundKey, round * 4, round * 4 + 4));
            }

            if (DEBUG) System.out.println("\nFinal Round:");
            if (DEBUG) System.out.println("SubBytes:");
            subBytes(block, encryptMode);
            
            if (DEBUG) System.out.println("ShiftRows:");
            shiftRows(block, encryptMode);
            
            if (DEBUG) System.out.println("AddRoundKey:");
            addRoundKey(block, Arrays.copyOfRange(roundKey, N * 4, N * 4 + 4));
        } else {
            if (DEBUG) System.out.println("\nInitial AddRoundKey:");
            addRoundKey(block, Arrays.copyOfRange(roundKey, N * 4, N * 4 + 4));

            for (int round = N - 1; round > 0; round--) {
                if (DEBUG) System.out.println("\nRound " + (N - round) + ":");
                
                if (DEBUG) System.out.println("ShiftRows:");
                shiftRows(block, encryptMode);
                
                if (DEBUG) System.out.println("SubBytes:");
                subBytes(block, encryptMode);
                
                if (DEBUG) System.out.println("AddRoundKey:");
                addRoundKey(block, Arrays.copyOfRange(roundKey, round * 4, round * 4 + 4));
                
                if (DEBUG) System.out.println("MixColumns:");
                mixColumns(block, encryptMode);
            }

            if (DEBUG) System.out.println("\nFinal Round:");
            if (DEBUG) System.out.println("ShiftRows:");
            shiftRows(block, encryptMode);
            
            if (DEBUG) System.out.println("SubBytes:");
            subBytes(block, encryptMode);
            
            if (DEBUG) System.out.println("AddRoundKey:");
            addRoundKey(block, Arrays.copyOfRange(roundKey, 0, 4));
        }
    }

    private int[][] getBlock(String text, int blockIdx) {
        int[][] block = new int[4][4];
        int start = blockIdx * BLOCK_SIZE;
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int pos = start + i + j * 4;
                if (pos < text.length()) {
                    block[i][j] = text.charAt(pos) & 0xFF;
                } else {
                    block[i][j] = 0;
                }
            }
        }
        return block;
    }

    private String blockToString(int[][] block) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                sb.append((char) block[i][j]);
            }
        }
        return sb.toString();
    }

    private void keyExpansion(String key) {
        roundKey = new int[44][4];
        
        // First 4 words are the key itself
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                roundKey[i][j] = key.charAt(i * 4 + j) & 0xFF;
            }
        }
        
        // Generate the rest of the key schedule
        for (int i = 4; i < 44; i++) {
            int[] temp = Arrays.copyOf(roundKey[i - 1], 4);
            
            if (i % 4 == 0) {
                temp = rotWord(temp);
                for (int j = 0; j < 4; j++) {
                    temp[j] = SBox.sbox(temp[j]);
                }
                temp[0] ^= RC.get(i / 4);
            }
            
            for (int j = 0; j < 4; j++) {
                roundKey[i][j] = roundKey[i - 4][j] ^ temp[j];
            }
        }
        
        if (DEBUG) {
            System.out.println("Key Schedule:");
            for (int i = 0; i < 44; i += 4) {
                System.out.print("Round " + (i / 4) + ": ");
                for (int j = 0; j < 4; j++) {
                    for (int k = 0; k < 4; k++) {
                        System.out.printf("%02x", roundKey[i + j][k]);
                    }
                }
                System.out.println();
            }
        }
    }

    private int[] rotWord(int[] word) {
        int[] rot = new int[4];
        rot[0] = word[1];
        rot[1] = word[2];
        rot[2] = word[3];
        rot[3] = word[0];
        return rot;
    }

    private void subBytes(int[][] block, boolean mode) {
        if (DEBUG) printBlock(block, "Input to SubBytes:");
        
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                block[r][c] = mode ? SBox.sbox(block[r][c]) : SBox.invSbox(block[r][c]);
            }
        }
        
        if (DEBUG) printBlock(block, "Output from SubBytes:");
    }

    private void shiftRows(int[][] block, boolean mode) {
        if (DEBUG) printBlock(block, "Input to ShiftRows:");
        
        for (int r = 1; r < 4; r++) {
            int[] temp = new int[4];
            for (int c = 0; c < 4; c++) {
                int newC = mode ? (c + r) % 4 : (c - r + 4) % 4;
                temp[c] = block[r][newC];
            }
            System.arraycopy(temp, 0, block[r], 0, 4);
        }
        
        if (DEBUG) printBlock(block, "Output from ShiftRows:");
    }

    private void mixColumns(int[][] block, boolean mode) {
        if (DEBUG) printBlock(block, "Input to MixColumns:");
        
        for (int c = 0; c < 4; c++) {
            int[] column = new int[4];
            for (int r = 0; r < 4; r++) {
                column[r] = block[r][c];
            }
            
            if (mode) {
                block[0][c] = mul(0x02, column[0]) ^ mul(0x03, column[1]) ^ column[2] ^ column[3];
                block[1][c] = column[0] ^ mul(0x02, column[1]) ^ mul(0x03, column[2]) ^ column[3];
                block[2][c] = column[0] ^ column[1] ^ mul(0x02, column[2]) ^ mul(0x03, column[3]);
                block[3][c] = mul(0x03, column[0]) ^ column[1] ^ column[2] ^ mul(0x02, column[3]);
            } else {
                block[0][c] = mul(0x0e, column[0]) ^ mul(0x0b, column[1]) ^ mul(0x0d, column[2]) ^ mul(0x09, column[3]);
                block[1][c] = mul(0x09, column[0]) ^ mul(0x0e, column[1]) ^ mul(0x0b, column[2]) ^ mul(0x0d, column[3]);
                block[2][c] = mul(0x0d, column[0]) ^ mul(0x09, column[1]) ^ mul(0x0e, column[2]) ^ mul(0x0b, column[3]);
                block[3][c] = mul(0x0b, column[0]) ^ mul(0x0d, column[1]) ^ mul(0x09, column[2]) ^ mul(0x0e, column[3]);
            }
        }
        
        if (DEBUG) printBlock(block, "Output from MixColumns:");
    }

    private void addRoundKey(int[][] block, int[][] roundKey) {
        if (DEBUG) {
            printBlock(block, "Input to AddRoundKey:");
            printBlock(roundKey, "RoundKey:");
        }
        
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                block[r][c] ^= roundKey[r][c];
            }
        }
        
        if (DEBUG) printBlock(block, "Output from AddRoundKey:");
    }

    private int mul(int a, int b) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0) {
                result ^= a;
            }
            boolean hiBit = (a & 0x80) != 0;
            a <<= 1;
            if (hiBit) {
                a ^= 0x1b; // x^8 + x^4 + x^3 + x + 1
            }
            b >>= 1;
        }
        return result;
    }

    private void printBlock(int[][] block, String label) {
        System.out.println(label);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                System.out.printf("%02x ", block[r][c]);
            }
            System.out.println();
        }
    }

    private void printBlock(int[][] block) {
        printBlock(block, "");
    }

    private String padString(String input) {
        int padLength = BLOCK_SIZE - (input.length() % BLOCK_SIZE);
        char padChar = (char) padLength;
        return input + String.valueOf(padChar).repeat(padLength);
    }

    private String unpadString(String input) {
        int padLength = input.charAt(input.length() - 1);
        if (padLength > 0 && padLength <= BLOCK_SIZE) {
            return input.substring(0, input.length() - padLength);
        }
        return input;
    }

    private int[][] generateIV() {
        int[][] iv = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                iv[i][j] = (int) (Math.random() * 256);
            }
        }
        return iv;
    }

    public static void main(String[] args) {
        String plaintext = "Two One Nine Two";
        String key = "Thats my Kung Fu";
        
        System.out.println("Original: " + plaintext);
        System.out.println("Key: " + key);
        
        AES aes = new AES(key);
        
        // Test ECB mode
        String ciphertext = aes.encrypt(plaintext, false);
        System.out.println("\nECB Ciphertext: " + bytesToHex(ciphertext.getBytes()));
        
        String decrypted = aes.decrypt(ciphertext, false);
        System.out.println("ECB Decrypted: " + decrypted);
        
        // Test CBC mode
        ciphertext = aes.encrypt(plaintext, true);
        System.out.println("\nCBC Ciphertext: " + bytesToHex(ciphertext.getBytes()));
        
        decrypted = aes.decrypt(ciphertext, true);
        System.out.println("CBC Decrypted: " + decrypted);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

class SBox {
    private static final int[] sbox = {
        0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
        0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
        0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
        0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
        0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
        0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
        0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
        0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
        0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
        0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
        0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
        0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
        0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
        0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
        0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
        0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    };

    private static final int[] invSbox = {
        0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
        0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
        0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
        0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
        0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
        0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
        0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
        0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
        0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
        0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
        0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
        0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
        0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
        0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
        0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
        0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
    };

    public static int sbox(int value) {
        return sbox[value & 0xFF];
    }

    public static int invSbox(int value) {
        return invSbox[value & 0xFF];
    }
}