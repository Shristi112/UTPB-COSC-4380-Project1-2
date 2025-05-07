import java.math.BigInteger;

public class DHE {
    private BigInteger generator;
    private BigInteger prime;

    /**
     * Constructor for DHE. Generates a secure generator and prime.
     * @param gBits Number of bits for generator
     * @param pBits Number of bits for prime
     */
    public DHE(int gBits, int pBits) {
        this.generator = Crypto.getSecureRandom(gBits);
        this.prime = Crypto.getSecurePrime(pBits);
    }

    /**
     * Returns a random base value of specified bit length.
     * @param bits Number of bits
     * @return Random base
     */
    public BigInteger getBase(int bits) {
        return Crypto.getSecureRandom(bits);
    }

    /**
     * Computes g^base mod p using fast modular exponentiation.
     * @param base The base
     * @return Result of g^base mod p
     */
    public BigInteger getExponent(BigInteger base) {
        return Crypto.fastModExp(generator, base, prime);
    }

    /**
     * Computes base^exponent mod p using fast modular exponentiation.
     * @param base The base
     * @param exponent The exponent
     * @return Result of base^exponent mod p
     */
    public BigInteger getKey(BigInteger base, BigInteger exponent) {
        return Crypto.fastModExp(base, exponent, prime);
    }

    /**
     * Main method for testing.
     */
    public static void main(String[] args) {
        DHE d = new DHE(8, 13); // instead of (512, 2048)
        System.out.printf("g = %s%np = %s%n%n", d.generator, d.prime);
        BigInteger a = d.getBase(512);
        BigInteger b = d.getBase(512);
        System.out.printf("a = %s%nb = %s%n%n", a, b);
        BigInteger A = d.getExponent(a);
        BigInteger B = d.getExponent(b);
        System.out.printf("A = %s%nB = %s%n%n", A, B);
        BigInteger aKey = d.getKey(A, b);  // base = A (from g^a), exponent = b
        BigInteger bKey = d.getKey(B, a);  // base = B (from g^b), exponent = a
        System.out.printf("keys = %s%n%s%n%n", aKey, bKey);

        DHE e = new DHE(8, 13);
        System.out.printf("g = %s%np = %s%n%n", e.generator, e.prime);
        BigInteger x = e.getBase(512);
        BigInteger y = e.getBase(512);
        BigInteger z = e.getBase(512);
        System.out.printf("x = %s%ny = %s%nz = %s%n%n", x, y, z);
        BigInteger X = e.getExponent(x);
        BigInteger Y = e.getExponent(y);
        BigInteger Z = e.getExponent(z);
        System.out.printf("X = %s%nY = %s%nZ = %s%n%n", X, Y, Z);
        BigInteger xKey = e.getKey(X, e.getKey(Y, z));
        BigInteger yKey = e.getKey(Y, e.getKey(Z, x));
        BigInteger zKey = e.getKey(Z, e.getKey(X, y));
        System.out.printf("keys = %s%n%s%n%s%n", xKey, yKey, zKey);
    }
}
