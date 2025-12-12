package me.exeos.jnicx.jnic.decompress;

import java.io.IOException;

public class BufferedRangeDecoder extends RangeDecoder {
    public final byte[] inputBuffer = new byte[65531];
    public int readPosition = this.inputBuffer.length;

    @Override
    public final void normalize() throws IOException {
        if ((this.range & 0xFF000000) == 0) {
            try {
                this.code = this.code << 8 | this.inputBuffer[this.readPosition++] & 0xFF;
                this.range <<= 8;
                return;
            }
            catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                throw new IOException();
            }
        }
    }
}