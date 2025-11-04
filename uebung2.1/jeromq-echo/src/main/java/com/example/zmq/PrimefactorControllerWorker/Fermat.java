package com.example.zmq.PrimefactorControllerWorker;

import java.math.BigInteger;

public class Fermat {
    public static BigInteger[] fermatFactorization(BigInteger N) {
        BigInteger[] sq = N.sqrtAndRemainder();

        BigInteger a = null;
        if(!sq[1].equals(BigInteger.ZERO)) // ceil the square root value
            a = sq[0].add(BigInteger.ONE);
        else                               // N equals a^2
            return new BigInteger[]{a, a};

        while((a.compareTo(N)) < 0) {
            BigInteger candidateB = a.pow(2).subtract(N);
            BigInteger[] sqrtRemainder = candidateB.sqrtAndRemainder();
            if(sqrtRemainder[1].equals(BigInteger.ZERO))
                return new BigInteger[]{a.add(sqrtRemainder[0]), a.subtract(sqrtRemainder[0])};

            a = a.add(BigInteger.ONE);
        }
        System.out.println("N is prime");
        return new BigInteger[0];
    }
}
