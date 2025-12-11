package me.exeos.jnicx;

import me.exeos.jnicx.jnic.JnicExtractor;
import me.exeos.jnicx.jnic.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        if (args.length < 4 || (args.length - 1) % 3 != 0) {
            System.out.println("Usage: jnicx file platform(s):<name, startOffset, endOffset>");
            System.exit(1);
        }

        File input = new File(args[0]);
        if (!input.exists()) {
            System.out.println("Input file does not exist.");
            System.exit(1);
        }
        if (!input.canRead()) {
            System.out.println("Input file can not be read.");
            System.exit(1);
        }


        ArrayList<Platform> platforms = new ArrayList<>();
        for (int i = 1; i < args.length; i += 3) {
            String name = args[i];
            long startOffset;
            long endOffset;

            try {
                startOffset = Long.parseLong(args[i + 1]);
                endOffset = Long.parseLong(args[i + 2]);
            } catch (Exception e) {
                System.out.println("Failed to parse command line options [" + (i + 1) + "], [" + (i + 2) + "]");
                System.exit(0);
                return;
            }

            platforms.add(new Platform(name, startOffset, endOffset));
        }

        System.out.println("Extracting.");

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(input);
        } catch (IOException e) {
            System.out.println("Failed create InputStream from File Object.");
            System.exit(0);
            return;
        }

        Optional<HashMap<Platform, byte[]>> result = JnicExtractor.extractPlatformBinaries(inputStream, platforms);
        if (result.isPresent()) {
            HashMap<Platform, byte[]> platformBinaries = result.get();

            if (result.get().size() == 0) {
                System.out.println("Extractor returned no output");
                System.exit(0);
            }

            for (Platform platform : platformBinaries.keySet()) {
                File outputFile = new File(platform.name() + "-extracted.bin");
                try {
                    if (!outputFile.exists() && !outputFile.createNewFile()) {
                        System.out.println("Failed to create file: " + outputFile.getAbsolutePath());
                        continue;
                    }

                    Files.write(outputFile.toPath(), platformBinaries.get(platform));
                    System.out.println("Extracted: " + outputFile.getAbsolutePath());
                } catch (IOException e) {
                    System.out.println("Failed to create or write file: " + outputFile.getAbsolutePath());
                }
            }
        } else {
            System.out.println("Extractor returned no output.");
        }
    }
}
