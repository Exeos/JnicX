package me.exeos.jnicx.jnic.is;

import java.util.Arrays;

public abstract class LZMADecoderState {
    final int positionMask;
    final int[] repDistances = new int[4];
    final LZMAState state = new LZMAState();
    final short[][] isMatch = new short[12][16];
    final short[] isRep = new short[12];
    final short[] isRep0 = new short[12];
    final short[] isRep1 = new short[12];
    final short[] isRep2 = new short[12];
    final short[][] isRep0Long = new short[12][16];
    final short[][] distSlot = new short[4][64];
    final short[][] distSpecial = new short[][]{new short[2], new short[2], new short[4], new short[4], new short[8], new short[8], new short[16], new short[16], new short[32], new short[32]};
    final short[] distAlign = new short[16];

    LZMADecoderState(int n) {
        this.positionMask = (1 << n) - 1;
    }

    void reset() {
        int n;
        this.repDistances[0] = 0;
        this.repDistances[1] = 0;
        this.repDistances[2] = 0;
        this.repDistances[3] = 0;
        this.state.value = 0;
        for (n = 0; n < this.isMatch.length; ++n) {
            Arrays.fill(this.isMatch[n], (short)1024);
        }
        Arrays.fill(this.isRep, (short)1024);
        Arrays.fill(this.isRep0, (short)1024);
        Arrays.fill(this.isRep1, (short)1024);
        Arrays.fill(this.isRep2, (short)1024);
        for (n = 0; n < this.isRep0Long.length; ++n) {
            Arrays.fill(this.isRep0Long[n], (short)1024);
        }
        for (n = 0; n < this.distSlot.length; ++n) {
            Arrays.fill(this.distSlot[n], (short)1024);
        }
        for (n = 0; n < this.distSpecial.length; ++n) {
            Arrays.fill(this.distSpecial[n], (short)1024);
        }
        Arrays.fill(this.distAlign, (short)1024);
    }
}
