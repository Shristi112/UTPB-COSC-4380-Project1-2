import java.math.BigInteger;
import java.util.Base64;

public class RSA {
    private final BigInteger p;
    private final BigInteger q;
    private final BigInteger n;
    private final BigInteger phi;
    private final BigInteger e;
    private final BigInteger d;
    private static final BigInteger DEFAULT_E = BigInteger.valueOf(65537);
    private static final int MAX_BYTES = 1; // Reduced for small primes

    public RSA() {
        // Use 13-bit primes from your file
        this.p = Crypto.getSecurePrime(13);
        BigInteger tempQ;
        do {
            tempQ = Crypto.getSecurePrime(13);
        } while (tempQ.equals(this.p));
        this.q = tempQ;

        // Calculate modulus and totient
        this.n = this.p.multiply(this.q);
        this.phi = this.p.subtract(BigInteger.ONE)
                      .multiply(this.q.subtract(BigInteger.ONE));

        // Initialize e
        BigInteger tempE = DEFAULT_E;
        while (this.phi.gcd(tempE).compareTo(BigInteger.ONE) > 0) {
            tempE = tempE.add(BigInteger.ONE);
        }
        this.e = tempE;

        // Calculate private exponent
        this.d = this.e.modInverse(this.phi);
        
        System.out.println("Using primes p=" + p + " q=" + q);
        System.out.println("Modulus n=" + n + " (bits: " + n.bitLength() + ")");
    }

    private byte[] processBlock(byte[] input, BigInteger exponent, BigInteger modulus) {
        BigInteger inputInt = new BigInteger(1, input);
        if (inputInt.compareTo(modulus) >= 0) {
            throw new IllegalArgumentException("Input too large for modulus");
        }
        return inputInt.modPow(exponent, modulus).toByteArray();
    }

    public BigInteger[] getPubKey() {
        return new BigInteger[]{this.e, this.n};
    }

    public String encrypt(String message, BigInteger[] pubKey) {
        try {
            byte[] bytes = message.getBytes("UTF-8");
            if (bytes.length > MAX_BYTES) {
                throw new IllegalArgumentException("Message too long (" + bytes.length + " bytes)");
            }
            byte[] encrypted = processBlock(bytes, pubKey[0], pubKey[1]);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            System.err.println("Encryption error: " + ex.getMessage());
            return null;
        }
    }

    public String decrypt(String ciphertext) {
        try {
            byte[] bytes = Base64.getDecoder().decode(ciphertext);
            byte[] decrypted = processBlock(bytes, this.d, this.n);
            return new String(decrypted, "UTF-8").trim();
        } catch (Exception ex) {
            System.err.println("Decryption error: " + ex.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Testing RSA with small primes...");
            RSA rsa = new RSA();
            
            // Test with single character
            String testMsg = "A";
            System.out.println("\nOriginal message: " + testMsg);
            
            String encrypted = rsa.encrypt(testMsg, rsa.getPubKey());
            System.out.println("Encrypted: " + encrypted);
            
            String decrypted = rsa.decrypt(encrypted);
            System.out.println("Decrypted: " + decrypted);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}