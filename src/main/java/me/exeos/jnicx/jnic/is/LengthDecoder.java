package me.exeos.jnicx.jnic.is;

import java.io.IOException;

public class LengthDecoder extends LengthDecoderBase {
    private LZMADecoder decoder;

    private LengthDecoder(LZMADecoder LZMADecoder) {
        this.decoder = LZMADecoder;
    }

    final int decodeLength(int posState) throws IOException {
        if (this.decoder.rangeDecoder.decodeBit(this.choice, 0) == 0) {
            return this.decoder.rangeDecoder.decodeBitTree(this.lowProbs[posState]) + 2;
        }
        if (this.decoder.rangeDecoder.decodeBit(this.choice, 1) == 0) {
            return this.decoder.rangeDecoder.decodeBitTree(this.midProbs[posState]) + 2 + 8;
        }
        return this.decoder.rangeDecoder.decodeBitTree(this.highProbs) + 2 + 8 + 8;
    }

    LengthDecoder(LZMADecoder LZMADecoder, byte by) {
        this(LZMADecoder);
    }
}
