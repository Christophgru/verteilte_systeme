package uulm.in.vs.ex5.task2;

import java.util.Collection;
import java.util.Optional;

public class VectorClock {
        // TODO

    public VectorClock(Collection<Long> C, int id) {
        // TODO
    }

    public VectorClock(int size, int id){
        // TODO
    }

    /**
    * Returns all times in the vector
    */
    public long[] getTime() {
        // TODO
    }

    /**
    * Also returns incremented time for own processID
    */
    public long increment() {
        // TODO
    }

    /**
    * Returns time of given id
    */
    public long getTime(int id) {
        // TODO
    }

    public long merge(VectorClock b) throws IllegalArgumentException{
        // TODO
    }

    public long size() {
        // TODO
    }

    /**
    * Greater-or-Equals comparison
    * IllegalArgumentException is thrown when vectors are of different size.
    */
    public boolean geq(VectorClock b) throws IllegalArgumentException {
        // TODO
    }

    /**
     *
     * @return Positive if a>b, Negative if a<b, 0 if a==b, empty Optional if not ordered
     * @throws IllegalArgumentException If Vectors are of different size
     */
    public static Optional<Integer> compare(VectorClock a, VectorClock b) throws IllegalArgumentException {
        // TODO
    }

    public boolean equals(VectorClock b) {
        // TODO
    }
}
