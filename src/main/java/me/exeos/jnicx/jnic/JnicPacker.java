package me.exeos.jnicx.jnic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class JnicPacker {

    public static Optional<File> pack(File[] binaries) {
        File outputFile = new File("repacked.dat");

        try {
            if (!outputFile.exists() && !outputFile.createNewFile()) {
                System.out.println("Failed to create output file.");
                return Optional.empty();
            }

            JnicOutputStream outputStream = new JnicOutputStream(new FileOutputStream(outputFile));
            byte[] buffer = new byte[32 * 1024];
            long prevOffset = 0;

            for (File binary : binaries) {
                try (FileInputStream fis = new FileInputStream(binary)) {
                    int n;
                    while ((n = fis.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, n);
                    }
                    outputStream.flush();

                    long currentOffset = outputStream.currentSize();
                    System.out.println(binary.getName() + ": " + prevOffset + " - " + currentOffset);
                    prevOffset = currentOffset;
                } catch (IOException e) {
                    System.out.println("Failed to create input stream for: " + binary.getAbsolutePath());
                    System.out.println("Check if the file exists and can be read.");
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to create output file / stream.");
            return Optional.empty();
        }

        return Optional.of(outputFile);
    }
}
