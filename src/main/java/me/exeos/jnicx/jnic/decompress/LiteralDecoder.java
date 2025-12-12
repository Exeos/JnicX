package me.exeos.jnicx.jnic.decompress;

public class LiteralDecoder extends LiteralCoderBase {
    final LiteralSubCoder[] subCoders;
    final LZMADecoder decoder;

    LiteralDecoder(LZMADecoder LZMADecoder, int lc, int lp) {
        super(lc, lp);
        this.decoder = LZMADecoder;
        this.subCoders = new LiteralSubCoder[1 << lc + lp];
        for (int i = 0; i < this.subCoders.length; ++i) {
            this.subCoders[i] = new LiteralSubCoder(this, (byte) 0);
        }
    }

    final void reset() {
        for (int i = 0; i < this.subCoders.length; ++i) {
            this.subCoders[i].reset();
        }
    }
}

