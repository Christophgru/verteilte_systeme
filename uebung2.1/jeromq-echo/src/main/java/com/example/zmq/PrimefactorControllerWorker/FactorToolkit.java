package com.example.zmq.PrimefactorControllerWorker;

import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.util.*;

public class FactorToolkit {

  /*** =================== Prime-Table (Cache) =================== ***/
  static final class PrimeTable {
    static final int LIMIT = 65_536; // √(2^32-1)
    static final Path PRIME_FILE = Paths.get("primes_upto_65536.bin");

    static int[] getPrimes(boolean recompute) throws IOException {
      if (recompute) {
        int[] p = sieve(LIMIT);
        savePrimesBinary(p, PRIME_FILE);
        return p;
      }
      if (Files.exists(PRIME_FILE)) {
        return loadPrimesBinary(PRIME_FILE);
      }
      int[] p = sieve(LIMIT);
      savePrimesBinary(p, PRIME_FILE);
      return p;
    }

    /** Sieve of Eratosthenes */
    static int[] sieve(int n) {
      boolean[] comp = new boolean[n + 1];
      for (int i = 2; i * i <= n; i++) {
        if (!comp[i]) for (int j = i * i; j <= n; j += i) comp[j] = true;
      }
      int cnt = 0; for (int i = 2; i <= n; i++) if (!comp[i]) cnt++;
      int[] primes = new int[cnt];
      for (int i = 2, k = 0; i <= n; i++) if (!comp[i]) primes[k++] = i;
      return primes;
    }

    static void savePrimesBinary(int[] primes, Path file) throws IOException {
      try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
        out.writeInt(primes.length);
        for (int p : primes) out.writeInt(p);
      }
    }

    static int[] loadPrimesBinary(Path file) throws IOException {
      try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
        int len = in.readInt();
        int[] primes = new int[len];
        for (int i = 0; i < len; i++) primes[i] = in.readInt();
        return primes;
      }
    }
  }

  /*** =================== Trial-Division (mit Prime-Cache) =================== ***/
  public static BigInteger[] factorTrialDivisionBig(BigInteger x, boolean recomputePrimes) throws IOException {
  if (x.compareTo(BigInteger.ONE) <= 0) {
    return new BigInteger[]{x};
  }

  int[] primes = PrimeTable.getPrimes(recomputePrimes);
  List<BigInteger> res = new ArrayList<>();
  BigInteger n = x;

  for (int p : primes) {
    long sq = (long) p * (long) p;
    if (BigInteger.valueOf(sq).compareTo(n) > 0) break;

    BigInteger bp = BigInteger.valueOf(p);
    while (n.mod(bp).signum() == 0) {
      res.add(bp);
      n = n.divide(bp);
    }
  }

  if (n.compareTo(BigInteger.ONE) > 0) {
    res.add(n);
  }
  return res.toArray(new BigInteger[0]);
}
}
