package me.exeos.jnicx.jnic.is;

import java.util.Arrays;

public abstract class LiteralSubCoderBase {
    final short[] probs = new short[768];

    LiteralSubCoderBase() {
    }

    final void reset() {
        Arrays.fill(this.probs, (short)1024);
    }
}
