package me.exeos.jnicx.jnic.is;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JnicInputStream extends InputStream {

    private DataInputStream dataInputStream;
    private SlidingWindowBuffer slidingWinBuffer;
    private BufferedRangeDecoder rangeDecoder;
    private LZMADecoder lzmaDecoder;
    private int remainingBlockBytes = 0;
    private boolean isCompressedBlock = false;
    private boolean needsReset = true;
    private boolean needsNewDecoder = true;
    private boolean endOfStream = false;
    private IOException previousException = null;
    private final byte[] singleByteBuf = new byte[1];

    public JnicInputStream(InputStream inputStream) {
        this(inputStream, 0x4000000, null);
    }

    public JnicInputStream(InputStream inputStream, int dictSize, byte[] preset) {
        this(inputStream, dictSize, preset, (byte) 0);
    }

    private JnicInputStream(InputStream inputStream, int dictSize, byte[] presetDictionary, byte dummy) {
        if (inputStream == null) {
            throw new NullPointerException();
        }
        this.dataInputStream = new DataInputStream(inputStream);
        this.rangeDecoder = new BufferedRangeDecoder();
        if (dictSize < 4096 || dictSize > 0x7FFFFFF0) {
            throw new IllegalArgumentException();
        }
        this.slidingWinBuffer = new SlidingWindowBuffer(dictSize + 15 & 0xFFFFFFF0, presetDictionary);
        if (presetDictionary != null && presetDictionary.length > 0) {
            this.needsReset = false;
        }
    }

    @Override
    public int read() throws IOException {
        if (read(singleByteBuf, 0, 1) == -1) {
            return -1;
        }
        return this.singleByteBuf[0] & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset + length < 0 || offset + length > bytes.length) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        if (this.dataInputStream == null) {
            throw new IOException();
        }
        if (this.previousException != null) {
            throw this.previousException;
        }
        if (this.endOfStream) {
            return -1;
        }
        try {
            int totalBytesRead = 0;
            while (length > 0) {
                SlidingWindowBuffer slidingWindowBuffer2;
                int compressedSize;
                int blockHeader;
                if (this.remainingBlockBytes == 0) {
                    blockHeader = this.dataInputStream.readUnsignedByte();
                    if (blockHeader == 0) {
                        this.endOfStream = true;
                        this.releaseResources();
                    } else {
                        if (blockHeader >= 224 || blockHeader == 1) {
                            this.needsNewDecoder = true;
                            this.needsReset = false;
                            this.slidingWinBuffer.totalBytesWritten = 0;
                            this.slidingWinBuffer.writePosition = 0;
                            this.slidingWinBuffer.availableBytes = 0;
                            this.slidingWinBuffer.inputLimit = 0;
                            this.slidingWinBuffer.buffer[this.slidingWinBuffer.bufferSize - 1] = 0;
                        } else if (this.needsReset) {
                            throw new IOException();
                        }
                        if (blockHeader >= 128) {
                            int n6;
                            this.isCompressedBlock = true;
                            this.remainingBlockBytes = (blockHeader & 0x1F) << 16;
                            this.remainingBlockBytes += this.dataInputStream.readUnsignedShort() + 1;
                            compressedSize = this.dataInputStream.readUnsignedShort() + 1;

                            if (blockHeader >= 192) {
                                int n7;
                                this.needsNewDecoder = false;
                                blockHeader = this.dataInputStream.readUnsignedByte();
                                if (blockHeader > 224) {
                                    throw new IOException();
                                }
                                n6 = blockHeader / 45;
                                blockHeader -= n6 * 9 * 5;
                                if ((blockHeader -= (n7 = blockHeader / 9) * 9) + n7 > 4) {
                                    throw new IOException();
                                }
                                this.lzmaDecoder = new LZMADecoder(this.slidingWinBuffer, this.rangeDecoder, blockHeader, n7, n6);
                            } else {
                                if (this.needsNewDecoder) {
                                    throw new IOException();
                                }
                                if (blockHeader >= 160) {
                                    this.lzmaDecoder.reset();
                                }
                            }
                            n6 = compressedSize;
                            DataInputStream dataInputStream = this.dataInputStream;
                            if (n6 < 5) {
                                throw new IOException();
                            }
                            if (dataInputStream.readUnsignedByte() != 0) {
                                throw new IOException();
                            }
                            this.rangeDecoder.code = dataInputStream.readInt();
                            this.rangeDecoder.range = -1;
                            this.rangeDecoder.readPosition = this.rangeDecoder.inputBuffer.length - (n6 -= 5);
                            dataInputStream.readFully(this.rangeDecoder.inputBuffer, this.rangeDecoder.readPosition, n6);
                        } else {
                            if (blockHeader > 2) {
                                throw new IOException();
                            }
                            this.isCompressedBlock = false;
                            this.remainingBlockBytes = this.dataInputStream.readUnsignedShort() + 1;
                        }
                    }
                    if (this.endOfStream) {
                        if (totalBytesRead == 0) {
                            return -1;
                        }
                        return totalBytesRead;
                    }
                }
                int n8 = Math.min(this.remainingBlockBytes, length);
                if (!this.isCompressedBlock) {
                    compressedSize = n8;
                    DataInputStream dataInputStream = this.dataInputStream;
                    slidingWindowBuffer2 = this.slidingWinBuffer;
                    int n9 = Math.min(slidingWindowBuffer2.bufferSize - slidingWindowBuffer2.writePosition, compressedSize);
                    dataInputStream.readFully(slidingWindowBuffer2.buffer, slidingWindowBuffer2.writePosition, n9);
                    slidingWindowBuffer2.writePosition += n9;
                    if (slidingWindowBuffer2.availableBytes < slidingWindowBuffer2.writePosition) {
                        slidingWindowBuffer2.availableBytes = slidingWindowBuffer2.writePosition;
                    }
                } else {
                    blockHeader = n8;
                    slidingWindowBuffer2 = this.slidingWinBuffer;
                    slidingWindowBuffer2.inputLimit = slidingWindowBuffer2.bufferSize - slidingWindowBuffer2.writePosition <= blockHeader ? slidingWindowBuffer2.bufferSize : slidingWindowBuffer2.writePosition + blockHeader;
                    this.lzmaDecoder.decode();
                }
                compressedSize = offset;
                byte[] byArray2 = bytes;
                slidingWindowBuffer2 = this.slidingWinBuffer;
                int n10 = slidingWindowBuffer2.writePosition - slidingWindowBuffer2.totalBytesWritten;
                if (slidingWindowBuffer2.writePosition == slidingWindowBuffer2.bufferSize) {
                    slidingWindowBuffer2.writePosition = 0;
                }
                System.arraycopy(slidingWindowBuffer2.buffer, slidingWindowBuffer2.totalBytesWritten, byArray2, compressedSize, n10);
                slidingWindowBuffer2.totalBytesWritten = slidingWindowBuffer2.writePosition;
                int n11 = n10;
                offset += n11;
                length -= n11;
                totalBytesRead += n11;
                this.remainingBlockBytes -= n11;
                if (this.remainingBlockBytes != 0) continue;
                BufferedRangeDecoder bufferedRangeDecoder2 = this.rangeDecoder;
                if (bufferedRangeDecoder2.readPosition == bufferedRangeDecoder2.inputBuffer.length && bufferedRangeDecoder2.code == 0 && !(this.slidingWinBuffer.pendingLength > 0)) continue;
                throw new IOException();
            }
            return totalBytesRead;
        }
        catch (IOException iOException) {
            this.previousException = iOException;
            throw iOException;
        }
    }

    @Override
    public int available() throws IOException {
        if (this.dataInputStream == null) {
            throw new IOException("closed");
        }
        if (this.previousException != null) {
            throw this.previousException;
        }
        if (this.isCompressedBlock) {
            return this.remainingBlockBytes;
        }
        return Math.min(this.remainingBlockBytes, this.dataInputStream.available());
    }

    private void releaseResources() {
        if (this.slidingWinBuffer != null) {
            this.slidingWinBuffer = null;
            this.rangeDecoder = null;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.dataInputStream != null) {
            this.releaseResources();
            try {
                this.dataInputStream.close();
                return;
            }
            finally {
                this.dataInputStream = null;
            }
        }
    }
}
