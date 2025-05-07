import java.math.BigInteger;
import java.security.SecureRandom;
import java.io.*;
import java.util.*;

public class Crypto {
    private static final SecureRandom rnd = new SecureRandom();
    private static List<BigInteger> primeList = null;

    /**
     * Returns a secure random BigInteger with the specified number of bits.
     */
    public static BigInteger getSecureRandom(int bits) {
        return new BigInteger(bits, rnd);
    }

    /**
     * Returns a secure random prime from the primes.txt file.
     */
    public static BigInteger getSecurePrime(int bits) {
        if (primeList == null) {
            loadPrimesFromFile("primes.txt"); // adjust path if needed
        }

        // Filter primes by bit length
        List<BigInteger> candidates = new ArrayList<>();
        for (BigInteger p : primeList) {
            if (p.bitLength() == bits) {
                candidates.add(p);
            }
        }

        if (candidates.isEmpty()) {
            throw new RuntimeException("No primes of " + bits + " bits found in primes.txt");
        }

        return candidates.get(rnd.nextInt(candidates.size()));
    }

    private static void loadPrimesFromFile(String filename) {
        primeList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                primeList.add(new BigInteger(line.trim()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading primes.txt: " + e.getMessage());
        }
    }

    /**
     * Fast modular exponentiation.
     */
    public static BigInteger fastModExp(BigInteger base, BigInteger exp, BigInteger mod) {
        return base.modPow(exp, mod);
    }
}
