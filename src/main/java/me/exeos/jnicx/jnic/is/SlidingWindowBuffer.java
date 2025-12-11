package me.exeos.jnicx.jnic.is;

import java.io.IOException;

public class SlidingWindowBuffer {

    public final byte[] buffer;           // The circular buffer
    public final int bufferSize;          // Maximum buffer capacity
    public int totalBytesWritten = 0;     // Total bytes ever written (smallerLengthO)
    public int writePosition = 0;         // Current write position (smallerLengthP)
    public int availableBytes = 0;        // Bytes available for back-referencing (smallerLengthQ)
    public int inputLimit = 0;            // Limit from input stream (r)
    public int pendingLength = 0;         // Remaining bytes to copy (s)
    public int pendingDistance = 0;       // Distance for pending copy (t)

    /**
     * Constructor - initializes buffer with optional seed data
     * Copies the END of inputArray into the buffer (dictionary initialization)
     */
    public SlidingWindowBuffer(int size, byte[] inputArray) {
        this.bufferSize = size;
        this.buffer = new byte[this.bufferSize];
        if (inputArray != null) {
            // Copy at most 'size' bytes from the END of inputArray
            this.availableBytes = this.writePosition = Math.min(inputArray.length, size);
            this.totalBytesWritten = this. writePosition;
            System.arraycopy(inputArray, inputArray. length - this.writePosition,
                    this.buffer, 0, this.writePosition);
        }
    }

    /**
     * Get byte at distance 'n' back from current position
     * This is the "lookback" operation for LZ77 decompression
     * @param distance - how far back to look (0 = most recent byte)
     * @return unsigned byte value (0-255)
     */
    public final int getByte(int distance) {
        int index = this.writePosition - distance - 1;
        if (distance >= this.writePosition) {
            index += this.bufferSize;  // Wrap around circular buffer
        }
        return this.buffer[index] & 0xFF;
    }

    /**
     * Copy 'length' bytes from 'distance' back to current position
     * This is the core LZ77 "match copy" operation
     * @param distance - how far back to start copying from
     * @param length - how many bytes to copy
     */
    public final void copyFromHistory(int distance, int length) throws IOException {
        int copyCount;

        // Validate distance is within available history
        if (distance < 0 || distance >= this.availableBytes) {
            throw new IOException();  // Invalid back-reference
        }

        // Calculate how much we can copy now vs later
        int canCopyNow = Math.min(this.inputLimit - this.writePosition, length);
        this.pendingLength = length - canCopyNow;  // Save remainder for later
        this.pendingDistance = distance;

        // Calculate source index (with wraparound)
        int sourceIndex = this.writePosition - distance - 1;

        if (sourceIndex < 0) {
            // Handle wraparound case
            assert (this.availableBytes == this.bufferSize);
            sourceIndex += this.bufferSize;
            copyCount = Math.min(this. bufferSize - sourceIndex, canCopyNow);
            assert (copyCount <= distance + 1);

            System.arraycopy(this.buffer, sourceIndex, this.buffer, this.writePosition, copyCount);
            this.writePosition += copyCount;
            sourceIndex = 0;

            if ((canCopyNow -= copyCount) == 0) {
                return;
            }
        }

        assert (sourceIndex < this.writePosition);
        assert (canCopyNow > 0);

        // Copy bytes (may overlap - intentional for run-length encoding)
        do {
            copyCount = Math.min(canCopyNow, this.writePosition - sourceIndex);
            System.arraycopy(this.buffer, sourceIndex, this.buffer, this.writePosition, copyCount);
            this.writePosition += copyCount;
        } while ((canCopyNow -= copyCount) > 0);

        // Update available history
        if (this.availableBytes < this.writePosition) {
            this.availableBytes = this.writePosition;
        }
    }
}
