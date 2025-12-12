package me.exeos.jnicx.jnic.decompress;

public abstract class LiteralCoderBase {
    final int literalContextBits;
    final int literalPositionMask;

    LiteralCoderBase(int lc, int lp) {
        this.literalContextBits = lc;
        this.literalPositionMask = (1 << lp) - 1;
    }
}
