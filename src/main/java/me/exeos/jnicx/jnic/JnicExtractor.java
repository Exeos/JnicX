package me.exeos.jnicx.jnic;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class JnicExtractor {

    public static HashMap<Platform, byte[]> extractPlatformBinaries(File input, ArrayList<Platform> platforms) {
        HashMap<Platform, byte[]> result = new HashMap<>();

        for (Platform platform : platforms) {
            long skipOffset = platform.startOffset();
            long endOffset = platform.endOffset();

            byte[] readBuffer = new byte[2048];

            try {
                JnicInputStream jnicInputStream = new JnicInputStream(new DataInputStream(new FileInputStream(input)));
                ByteArrayOutputStream binaryOutputStream = new ByteArrayOutputStream();

                long currentPosition = 0;
                long bytesSkipped = 0;

                for (currentPosition = 0L; currentPosition < skipOffset; currentPosition += bytesSkipped) {
                    bytesSkipped = jnicInputStream.skip(skipOffset - currentPosition);
                    if (bytesSkipped > 0L) continue;
                    throw new IOException("Failed to skip to start offset.");
                }

                while (currentPosition < endOffset) {
                    int bytesRead = jnicInputStream.read(readBuffer, 0, (int) Math.min(readBuffer.length, endOffset - currentPosition));
                    binaryOutputStream.write(readBuffer, 0, bytesRead);
                    currentPosition += bytesRead;
                }

                result.put(platform, binaryOutputStream.toByteArray());
            } catch (Exception e) {
                System.out.println("Failed to extract platform: " + platform.name());
            }
        }

        return result;
    }
}
