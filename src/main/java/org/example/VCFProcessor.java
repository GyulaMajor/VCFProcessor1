package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VCFProcessor {
    public static void main(String[] args){
        if(args.length < 2){
            System.out.println("USAGE: VCFProcessor INPUT_DIRECTORY OUTPUT_FILE");
            System.exit(1);
        }
        File inputDirectory = new File(args[0]);
        File outputFile = new File(args[1]);

        if (!inputDirectory.isDirectory()) {
            System.out.println("Could not find the input directory " + inputDirectory.getAbsolutePath());
            System.exit(1);
        }

        Map<String, Result> resultMap = new HashMap<>();

        for (File file : Objects.requireNonNull(inputDirectory.listFiles((d, name) -> name.endsWith(".vcf")))) {
            processFile(file, resultMap);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("TRANSCRIPT;HGVS.C;VARIANT_TYPE;TOTAL_COUNT\n");
            for (Result e : resultMap.values())
                writer.write(e.toString());
        }
        catch (IOException e){
            System.err.println("Output file error" + e.getMessage());
            System.exit(1);
        }
    }

   static void processFile(File file, Map<String, Result> map) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) continue;
                String[] cols = line.split("\t");
                if (cols.length < 8){
                    System.out.println("Not enough data in line.");
                    continue;
                }


                String ref = cols[3], alt = cols[4], info = cols[7];
                if (ref.length() != 1 || alt.length() != 1 || alt.contains(",")) continue;

                String ann = null;
                for (String field : info.split(";")) {
                    if (field.startsWith("ANN=")) {
                        ann = field;
                        break;
                    }
                }

                if (ann == null) continue;

                String[] annFields = ann.split("\\|");
                if (annFields.length < 10) {
                    System.out.println("Not enough data in the annotation field.");
                    continue;
                }

                String transcript = annFields[6], hgvsc = annFields[9], variantType = annFields[1];

                String resultKey = transcript + "|" + hgvsc + "|" + variantType;

                map.compute(resultKey, (k, v) -> v == null
                        ? new Result(transcript, hgvsc, variantType)
                        : v.increment());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}


