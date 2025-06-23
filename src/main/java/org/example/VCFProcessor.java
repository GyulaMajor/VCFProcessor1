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
    public static void main(String[] args) throws IOException {
        if(args.length < 2){
            System.out.println("Adjon meg egy bemeneti és egy kimeneti könyvtárat");
            return;
        }
        File bemenetiKonyvtar = new File(args[0]);
        File kimenet = new File(args[1]);

        if (!bemenetiKonyvtar.isDirectory()) {
            System.out.println("Az input könyvtár nem található: " + bemenetiKonyvtar.getAbsolutePath());
            return;
        }

        Map<String, Eredmeny> map = new HashMap<>();

        for (File file : Objects.requireNonNull(bemenetiKonyvtar.listFiles((d, name) -> name.endsWith(".vcf")))) {
            processFile(file, map);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(kimenet))) {
            writer.write("TRANSCRIPT;HGVS.C;VARIANT_TYPE;TOTAL_COUNT\n");
            for (Eredmeny e : map.values())
                writer.write(e.toString());
        }
        catch (IOException e){
            System.err.println("Kimeneti fájl hiba");
        }
    }

   static void processFile(File file, Map<String, Eredmeny> map) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) continue;
                String[] cols = line.split("\t");
                if (cols.length < 8) continue;

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
                if (annFields.length < 10) continue;

                String transcript = annFields[6], hgvsc = annFields[9], variantType = annFields[1];

                String eredmeny = transcript + "|" + hgvsc + "|" + variantType;

                map.compute(eredmeny, (k, v) -> v == null
                        ? new Eredmeny(transcript, hgvsc, variantType)
                        : v.inc());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}


