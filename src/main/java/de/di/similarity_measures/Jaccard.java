package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;

import java.util.*;

@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {


    private final Tokenizer tokenizer;


    private final boolean bagSemantics;

    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;

        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }


    @Override
    public double calculate(String[] strings1, String[] strings2) {
        if (strings1.length == 0 && strings2.length == 0) {
            return 1.0;
        }

        if (!this.bagSemantics) {
            // --- SET SEMANTICS ---
            Set<String> set1 = new HashSet<>(Arrays.asList(strings1));
            Set<String> set2 = new HashSet<>(Arrays.asList(strings2));

            // Intersection
            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            // Union
            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);

            return (double) intersection.size() / union.size();
        } else {
            // --- BAG SEMANTICS ---
            // Count frequencies in both bags
            Map<String, Integer> counts1 = new HashMap<>();
            for (String s : strings1) counts1.put(s, counts1.getOrDefault(s, 0) + 1);

            Map<String, Integer> counts2 = new HashMap<>();
            for (String s : strings2) counts2.put(s, counts2.getOrDefault(s, 0) + 1);

            // Calculate Intersection (sum of minimum counts)
            int intersectionSize = 0;
            Set<String> allTokens = new HashSet<>(counts1.keySet());
            allTokens.addAll(counts2.keySet());

            for (String token : allTokens) {
                intersectionSize += Math.min(counts1.getOrDefault(token, 0), counts2.getOrDefault(token, 0));
            }

            // Calculate Union (sum of total counts)
            int unionSize = strings1.length + strings2.length;

            return (double) intersectionSize / unionSize;
        }
    }
}