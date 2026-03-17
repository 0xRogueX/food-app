package com.fooddeliveryapp.util;

import java.util.List;

public class TablePrinter {
    public static void print(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return;

        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) colWidths[i] = headers[i].length();

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null && row[i].length() > colWidths[i]) {
                    colWidths[i] = row[i].length();
                }
            }
        }

        StringBuilder formatBuilder = new StringBuilder();
        for (int width : colWidths) formatBuilder.append("| %-").append(width).append("s ");
        formatBuilder.append("|\n");
        String format = formatBuilder.toString();

        System.out.println();
        printSeparator(colWidths);
        System.out.printf(format, (Object[]) headers);
        printSeparator(colWidths);

        for (String[] row : rows) {
            String[] safeRow = new String[headers.length];
            for (int i = 0; i < headers.length; i++) safeRow[i] = (i < row.length && row[i] != null) ? row[i] : "";
            System.out.printf(format, (Object[]) safeRow);
        }
        printSeparator(colWidths);
        System.out.println();
    }

    private static void printSeparator(int[] colWidths) {
        for (int width : colWidths) {
            System.out.print("+");
            for (int i = 0; i < width + 2; i++) System.out.print("-");
        }
        System.out.println("+");
    }
}