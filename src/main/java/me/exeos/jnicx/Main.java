package me.exeos.jnicx;

import me.exeos.jnicx.jnic.JnicExtractor;
import me.exeos.jnicx.jnic.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        if (args.length < 5 || (args.length - 2) % 3 != 0) {
            System.out.println("Usage:");
            System.out.println("jnicx extract <filePath> platform(s):<name, startOffset, endOffset>");
            System.out.println("or");
            System.out.println("jnicx pack <pathToBinaries> platform(s):<platform-string, startOffset, endOffset>");
            return;
        }

        String option = args[0];
        String[] params = new String[args.length - 1];

        // copy all args except option
        System.arraycopy(args, 1, params, 0, args.length - 1);

        if (option.equals("extract")) {
            extract(params);
        } else if (option.equals("pack")) {
            pack(params);
        }
    }

    private static void extract(String[] args) {
        File input = new File(args[0]);
        if (!input.exists()) {
            System.out.println("Input file does not exist.");
            return;
        }
        if (!input.canRead()) {
            System.out.println("Input file can not be read.");
            return;
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
                System.exit(1);
                return;
            }

            platforms.add(new Platform(name, startOffset, endOffset));
        }

        HashMap<Platform, byte[]> result = JnicExtractor.extractPlatformBinaries(input, platforms);
        if (result.isEmpty()) {
            System.out.println("Extractor returned no output.");
            return;
        }

        for (Platform platform : result.keySet()) {
            File outputFile = new File(platform.name() + "-extracted.bin");
            try {
                if (!outputFile.exists() && !outputFile.createNewFile()) {
                    System.out.println("Failed to create file: " + outputFile.getAbsolutePath());
                    System.out.println("Skipping: " + platform.name());
                    continue;
                }

                Files.write(outputFile.toPath(), result.get(platform));

                System.out.println("Extracted: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("Failed to create or write file: " + outputFile.getAbsolutePath());
            }
        }
    }

    private static void pack(String[] args) {

    }
}
