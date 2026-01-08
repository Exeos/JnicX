package me.exeos.jnicx.jnic;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JnicOutputStream extends OutputStream {

    private final DataOutputStream dataOutputStream;
    private long logicalSize = 0;

    public JnicOutputStream(OutputStream out) {
        this.dataOutputStream = new DataOutputStream(out);
    }

    @Override
    public void write(int b) throws IOException {
        byte[] buf = new byte[]{(byte) b};
        write(buf, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int chunk = Math.min(len, 0xFFFF + 1);
            writeUncompressedBlock(b, off, chunk);
            off += chunk;
            len -= chunk;
        }
    }

    private void writeUncompressedBlock(byte[] buf, int off, int len) throws IOException {
        dataOutputStream.writeByte(1); // block header: 1 = reset, uncompressed
        dataOutputStream.writeShort(len - 1); // size-1 as unsigned short
        dataOutputStream.write(buf, off, len); // raw data

        logicalSize += len;
    }

    @Override
    public void flush() throws IOException {
        dataOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        dataOutputStream.writeByte(0);
        dataOutputStream.flush();
        dataOutputStream.close();
    }

    public long currentSize() {
        return logicalSize;
    }
}