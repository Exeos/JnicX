package me.exeos.jnicx.jnic.decompress;

import java.io.IOException;

public class LZMADecoder extends LZMADecoderState {
    private SlidingWindowBuffer outputBuffer;
    final RangeDecoder rangeDecoder;
    private final LiteralDecoder literalDecoder;
    private final LengthDecoder matchLengthDecoder = new LengthDecoder(this, (byte) 0);
    private final LengthDecoder repLengthDecoder = new LengthDecoder(this, (byte) 0);

    public LZMADecoder(SlidingWindowBuffer buffer, RangeDecoder rangeDecoder, int lc, int lp, int pb) {
        super(pb);
        this.outputBuffer = buffer;
        this.rangeDecoder = rangeDecoder;
        this.literalDecoder = new LiteralDecoder(this, lc, lp);
        this.reset();
    }

    @Override
    public final void reset() {
        super.reset();
        this.literalDecoder.reset();
        this.matchLengthDecoder.reset();
        this.repLengthDecoder.reset();
    }

    public final void decode() throws IOException {
        SlidingWindowBuffer slidingWindowBuffer2 = this.outputBuffer;
        if (slidingWindowBuffer2.pendingLength > 0) {
            SlidingWindowBuffer slidingWindowBuffer3 = slidingWindowBuffer2;
            slidingWindowBuffer3.copyFromHistory(slidingWindowBuffer3.pendingDistance, slidingWindowBuffer2.pendingLength);
        }
        while (true) {
            int n2;
            block20: {
                LZMADecoder LZMADecoder;
                int n3;
                block22: {
                    int n4;
                    block21: {
                        int n5;
                        block19: {
                            int n6;
                            int n7;
                            slidingWindowBuffer2 = this.outputBuffer;
                            if (!(slidingWindowBuffer2.writePosition < slidingWindowBuffer2.inputLimit)) break;
                            n5 = this.outputBuffer.writePosition & this.positionMask;
                            if (this.rangeDecoder.decodeBit(this.isMatch[this.state.value], n5) == 0) {
                                LiteralDecoder literalDecoder2 = this.literalDecoder;
                                int n8 = literalDecoder2.decoder.outputBuffer.writePosition;
                                int n9 = literalDecoder2.decoder.outputBuffer.getByte(0);
                                Object object = literalDecoder2;
                                int n10 = n9 >> 8 - ((LiteralCoderBase)object).literalContextBits;
                                n7 = (n8 & ((LiteralCoderBase)object).literalPositionMask) << ((LiteralCoderBase)object).literalContextBits;
                                n9 = n10 + n7;
                                object = literalDecoder2.subCoders[n9];
                                n9 = 1;
                                if (((LiteralSubCoder)object).parent.decoder.state.value < 7) {
                                    while ((n9 = n9 << 1 | ((LiteralSubCoder)object).parent.decoder.rangeDecoder.decodeBit(((LiteralSubCoder)object).probs, n9)) < 256) {
                                    }
                                } else {
                                    n8 = ((LiteralSubCoder)object).parent.decoder.outputBuffer.getByte(((LiteralSubCoder)object).parent.decoder.repDistances[0]);
                                    n10 = 256;
                                    do {
                                        n7 = (n8 <<= 1) & n10;
                                        n6 = ((LiteralSubCoder)object).parent.decoder.rangeDecoder.decodeBit(((LiteralSubCoder)object).probs, n10 + n7 + n9);
                                        n9 = n9 << 1 | n6;
                                        n10 &= 0 - n6 ^ ~n7;
                                    } while (n9 < 256);
                                }
                                byte by = (byte)n9;
                                SlidingWindowBuffer slidingWindowBuffer4 = ((LiteralSubCoder)object).parent.decoder.outputBuffer;
                                slidingWindowBuffer4.buffer[slidingWindowBuffer4.writePosition++] = by;
                                if (slidingWindowBuffer4.availableBytes < slidingWindowBuffer4.writePosition) {
                                    slidingWindowBuffer4.availableBytes = slidingWindowBuffer4.writePosition;
                                }
                                LZMAState LZMAState2 = ((LiteralSubCoder)object).parent.decoder.state;
                                if (LZMAState2.value <= 3) {
                                    LZMAState2.value = 0;
                                    continue;
                                }
                                if (LZMAState2.value <= 9) {
                                    LZMAState2.value -= 3;
                                    continue;
                                }
                                LZMAState2.value -= 6;
                                continue;
                            }
                            if (this.rangeDecoder.decodeBit(this.isRep, this.state.value) != 0) break block19;
                            n3 = n5;
                            LZMADecoder LZMADecoder2 = this;
                            LZMADecoder2.state.value = LZMADecoder2.state.value < 7 ? 7 : 10;
                            LZMADecoder2.repDistances[3] = LZMADecoder2.repDistances[2];
                            LZMADecoder2.repDistances[2] = LZMADecoder2.repDistances[1];
                            LZMADecoder2.repDistances[1] = LZMADecoder2.repDistances[0];
                            int n11 = LZMADecoder2.matchLengthDecoder.decodeLength(n3);
                            int n12 = n11;
                            if ((n3 = LZMADecoder2.rangeDecoder.decodeBitTree(LZMADecoder2.distSlot[n12 < 6 ? n12 - 2 : 3])) < 4) {
                                LZMADecoder2.repDistances[0] = n3;
                            } else {
                                int n13 = (n3 >> 1) - 1;
                                LZMADecoder2.repDistances[0] = (2 | n3 & 1) << n13;
                                if (n3 < 14) {
                                    LZMADecoder2.repDistances[0] = LZMADecoder2.repDistances[0] | LZMADecoder2.rangeDecoder.decodeBitTreeReverse(LZMADecoder2.distSpecial[n3 - 4]);
                                } else {
                                    int n14 = LZMADecoder2.repDistances[0];
                                    n7 = n13 - 4;
                                    RangeDecoder rangeDecoder2 = LZMADecoder2.rangeDecoder;
                                    n6 = 0;
                                    do {
                                        rangeDecoder2.normalize();
                                        rangeDecoder2.range >>>= 1;
                                        n3 = rangeDecoder2.code - rangeDecoder2.range >>> 31;
                                        rangeDecoder2.code -= rangeDecoder2.range & n3 - 1;
                                        n6 = n6 << 1 | 1 - n3;
                                    } while (--n7 != 0);
                                    LZMADecoder2.repDistances[0] = n14 | n6 << 4;
                                    LZMADecoder2.repDistances[0] = LZMADecoder2.repDistances[0] | LZMADecoder2.rangeDecoder.decodeBitTreeReverse(LZMADecoder2.distAlign);
                                }
                            }
                            n2 = n11;
                            break block20;
                        }
                        n3 = n5;
                        LZMADecoder = this;
                        if (LZMADecoder.rangeDecoder.decodeBit(LZMADecoder.isRep0, LZMADecoder.state.value) != 0) break block21;
                        if (LZMADecoder.rangeDecoder.decodeBit(LZMADecoder.isRep0Long[LZMADecoder.state.value], n3) != 0) break block22;
                        LZMADecoder.state.value = LZMADecoder.state.value < 7 ? 9 : 11;
                        n2 = 1;
                        break block20;
                    }
                    if (LZMADecoder.rangeDecoder.decodeBit(LZMADecoder.isRep1, LZMADecoder.state.value) == 0) {
                        n4 = LZMADecoder.repDistances[1];
                    } else {
                        if (LZMADecoder.rangeDecoder.decodeBit(LZMADecoder.isRep2, LZMADecoder.state.value) == 0) {
                            n4 = LZMADecoder.repDistances[2];
                        } else {
                            n4 = LZMADecoder.repDistances[3];
                            LZMADecoder.repDistances[3] = LZMADecoder.repDistances[2];
                        }
                        LZMADecoder.repDistances[2] = LZMADecoder.repDistances[1];
                    }
                    LZMADecoder.repDistances[1] = LZMADecoder.repDistances[0];
                    LZMADecoder.repDistances[0] = n4;
                }
                LZMADecoder.state.value = LZMADecoder.state.value < 7 ? 8 : 11;
                n2 = LZMADecoder.repLengthDecoder.decodeLength(n3);
            }
            int n15 = n2;
            this.outputBuffer.copyFromHistory(this.repDistances[0], n15);
        }
        this.rangeDecoder.normalize();
    }
}