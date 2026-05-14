package de.di.similarity_measures;

import lombok.AllArgsConstructor;
import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
public class Levenshtein implements SimilarityMeasure {

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    private final boolean withDamerau;

    @Override
    public double calculate(final String string1, final String string2) {
        if (string1 == null || string2 == null) return 0.0;
        if (string1.isEmpty() && string2.isEmpty()) return 1.0;

        int n = string1.length();
        int m = string2.length();
        if (n == 0 || m == 0) return 0.0;

        int[] upperupperLine = new int[n + 1];
        int[] upperLine = new int[n + 1];
        int[] lowerLine = new int[n + 1];

        for (int i = 0; i <= n; i++) upperLine[i] = i;

        for (int j = 1; j <= m; j++) {
            lowerLine[0] = j;
            for (int i = 1; i <= n; i++) {
                int cost = (string1.charAt(i - 1) == string2.charAt(j - 1)) ? 0 : 1;

                // Standard Levenshtein: Min of (Deletion, Insertion, Substitution)
                lowerLine[i] = min(upperLine[i] + 1, lowerLine[i - 1] + 1, upperLine[i - 1] + cost);

                // Damerau Extension: Check for adjacent swaps
                if (withDamerau && i > 1 && j > 1
                        && string1.charAt(i - 1) == string2.charAt(j - 2)
                        && string1.charAt(i - 2) == string2.charAt(j - 1)) {
                    lowerLine[i] = Math.min(lowerLine[i], upperupperLine[i - 2] + 1);
                }
            }
            System.arraycopy(upperLine, 0, upperupperLine, 0, n + 1);
            System.arraycopy(lowerLine, 0, upperLine, 0, n + 1);
        }

        return 1.0 - ((double) upperLine[n] / Math.max(n, m));
    }

    @Override
    public double calculate(final String[] strings1, final String[] strings2) {
        if (strings1 == null || strings2 == null) return 0.0;
        if (strings1.length == 0 && strings2.length == 0) return 1.0;

        int n = strings1.length;
        int m = strings2.length;
        if (n == 0 || m == 0) return 0.0;

        int[] upperupperLine = new int[n + 1];
        int[] upperLine = new int[n + 1];
        int[] lowerLine = new int[n + 1];

        for (int i = 0; i <= n; i++) upperLine[i] = i;

        for (int j = 1; j <= m; j++) {
            lowerLine[0] = j;
            for (int i = 1; i <= n; i++) {
                int cost = (Objects.equals(strings1[i - 1], strings2[j - 1])) ? 0 : 1;
                lowerLine[i] = min(upperLine[i] + 1, lowerLine[i - 1] + 1, upperLine[i - 1] + cost);

                if (withDamerau && i > 1 && j > 1
                        && Objects.equals(strings1[i - 1], strings2[j - 2])
                        && Objects.equals(strings1[i - 2], strings2[j - 1])) {
                    lowerLine[i] = Math.min(lowerLine[i], upperupperLine[i - 2] + 1);
                }
            }
            System.arraycopy(upperLine, 0, upperupperLine, 0, n + 1);
            System.arraycopy(lowerLine, 0, upperLine, 0, n + 1);
        }

        return 1.0 - ((double) upperLine[n] / Math.max(n, m));
    }
}