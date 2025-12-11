package me.exeos.jnicx.jnic.is;

public abstract class LiteralCoderBase {
    final int literalContextBits;
    final int literalPositionMask;

    LiteralCoderBase(int lc, int lp) {
        this.literalContextBits = lc;
        this.literalPositionMask = (1 << lp) - 1;
    }
}
