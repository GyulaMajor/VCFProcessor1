package org.example;

public class Result {
    String transcript, hgvsc, variantType;
    int count = 1;

    Result(String transcript, String hgvsc, String variant) {
        this.transcript = transcript;
        this.hgvsc = hgvsc;
        this.variantType = variant;
    }

    Result increment() {
        count++;
        return this;
    }

    public String toString() {
        return String.format("%s;%s;%s;%d%n", transcript, hgvsc, variantType, count);
    }
}
