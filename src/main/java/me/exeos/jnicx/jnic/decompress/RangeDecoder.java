package me.exeos.jnicx.jnic.decompress;

import java.io.IOException;

public abstract class RangeDecoder {
    public int range = 0;
    public int code = 0;

    public abstract void normalize() throws IOException;

    public final int decodeBit(short[] probs, int index) throws IOException {
        int n2;
        this.normalize();
        short s = probs[index];
        int n3 = (this.range >>> 11) * s;
        if ((this.code ^ Integer.MIN_VALUE) < (n3 ^ Integer.MIN_VALUE)) {
            this.range = n3;
            probs[index] = (short)(s + (2048 - s >>> 5));
            n2 = 0;
        } else {
            this.range -= n3;
            this.code -= n3;
            short s2 = s;
            probs[index] = (short)(s2 - (s2 >>> 5));
            n2 = 1;
        }
        return n2;
    }

    public final int decodeBitTree(short[] probs) throws IOException {
        int n = 1;
        while ((n = n << 1 | this.decodeBit(probs, n)) < probs.length) {
        }
        return n - probs.length;
    }

    public final int decodeBitTreeReverse(short[] probs) throws IOException {
        int n = 1;
        int n2 = 0;
        int n3 = 0;
        do {
            int n4 = this.decodeBit(probs, n);
            n = n << 1 | n4;
            n3 |= n4 << n2++;
        } while (n < probs.length);
        return n3;
    }
}
