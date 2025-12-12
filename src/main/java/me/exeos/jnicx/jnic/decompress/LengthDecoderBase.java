package me.exeos.jnicx.jnic.decompress;

import java.util.Arrays;

public abstract class LengthDecoderBase {
    final short[] choice = new short[2];
    final short[][] lowProbs = new short[16][8];
    final short[][] midProbs = new short[16][8];
    final short[] highProbs = new short[256];

    LengthDecoderBase() {
    }

    final void reset() {
        int n;
        Arrays.fill(this.choice, (short)1024);
        for (n = 0; n < this.lowProbs.length; ++n) {
            Arrays.fill(this.lowProbs[n], (short)1024);
        }
        for (n = 0; n < this.lowProbs.length; ++n) {
            Arrays.fill(this.midProbs[n], (short)1024);
        }
        Arrays.fill(this.highProbs, (short)1024);
    }
}
