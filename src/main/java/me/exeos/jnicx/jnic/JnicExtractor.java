package me.exeos.jnicx.jnic;

import me.exeos.jnicx.jnic.is.JnicInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class JnicExtractor {


    public static Optional<HashMap<Platform, byte[]>> extractPlatformBinaries(InputStream input, ArrayList<Platform> platforms) {
        HashMap<Platform, byte[]> result = new HashMap<>();

        for (Platform platform : platforms) {
            long skipOffset = platform.startOffset();
            long endOffset = platform.endOffset();

            byte[] readBuffer = new byte[2048];

            try {
                JnicInputStream jnicInputStream = new JnicInputStream(new DataInputStream(input));
                ByteArrayOutputStream binaryOutputStream = new ByteArrayOutputStream();

                long currentPosition;
                long bytesSkipped;

                for (currentPosition = 0L; currentPosition < skipOffset; currentPosition += bytesSkipped) {
                    bytesSkipped = jnicInputStream.skip(skipOffset - currentPosition);
                    if (bytesSkipped > 0L) continue;
                    return Optional.empty();
                }

                while (currentPosition < endOffset) {
                    int bytesRead = jnicInputStream.read(readBuffer, 0, (int) Math.min(readBuffer.length, endOffset - currentPosition));
                    binaryOutputStream.write(readBuffer, 0, bytesRead);
                    currentPosition += bytesRead;
                }

                result.put(platform, binaryOutputStream.toByteArray());
            } catch (IOException e) {
                System.out.println("Failed to extract platform: " + platform.name());
            }
        }

        return Optional.of(result);
    }
}
