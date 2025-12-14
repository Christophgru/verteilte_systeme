package uulm.in.vs.ex5.task2;

import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Optional;

public class VectorClock {
        int own_id;
        long time[];

    @Contract(pure = true)
    public VectorClock(Collection<Long> C, int id) {
        int size =C.size();
        time=new long[size];
        int i=0;
        for (long c :C) {
            time[i]=c;
            i++;
        }
        own_id=id;
    }

    public VectorClock(int size, int id){
        own_id=id;
        time=new long[size];
    }

    /**
    * Returns all times in the vector
    */
    public long[] getTime() {
        return time;
    }

    /**
    * Also returns incremented time for own processID
    */
    public long increment() {
        // TODO

        return time[own_id]++ +1;
    }

    /**
    * Returns time of given id
    */
    public long getTime(int id) {
        return time[id];
    }

    public long merge(VectorClock b) throws IllegalArgumentException{
        if( this.size()!=b.size())throw new IllegalArgumentException("");
        for (int i = 0; i < b.size(); i++) {
            if(b.getTime(i)>getTime(i)){
                time[i]=b.getTime(i);
            }
        }
        return increment();
    }

    public long size() {
        return time.length;
    }

    /**
    * Greater-or-Equals comparison
    * IllegalArgumentException is thrown when vectors are of different size.
    */
    public boolean geq(VectorClock b) throws IllegalArgumentException {
        // ∀i : A[i] ≤ B[i] ret true-> if E A[i]>B[i] ret false
        if(this.size()!=b.size())throw new IllegalArgumentException();
        for (int i = 0; i < size(); i++) {
            if(time[i]<b.getTime(i))return false;
        }
        return true;
    }

    /**
     *
     * @return Positive if a>b, Negative if a<b, 0 if a==b, empty Optional if not ordered
     * @throws IllegalArgumentException If Vectors are of different size
     */
    public static Optional<Integer> compare(VectorClock a, VectorClock b) throws IllegalArgumentException {
        if(a.size()!=b.size())throw new IllegalArgumentException();
        if(a.geq(b)&&b.geq(a))return Optional.of(0);
        if(a.geq(b)) return Optional.of(1);
        if(b.geq(a)) return Optional.of(-1);
        return Optional.empty();
    }

    public boolean equals(VectorClock b) {
        long size=size();
        if(b.size()>size)size=b.size();
        for (int i = 0; i < size; i++) {
            if(time[i]!=b.getTime(i))return false;
        }
        return true;
    }
}
