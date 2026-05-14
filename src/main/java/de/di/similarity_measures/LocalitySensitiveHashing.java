package de.di.similarity_measures;

import de.di.similarity_measures.helper.MinHash;
import de.di.similarity_measures.helper.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocalitySensitiveHashing implements SimilarityMeasure {

    private final Tokenizer tokenizer;
    private final List<MinHash> minHashFunctions;

    public LocalitySensitiveHashing(final Tokenizer tokenizer, final int numHashFunctions) {
        this.tokenizer = tokenizer;
        this.minHashFunctions = new ArrayList<>(numHashFunctions);
        for (int i = 0; i < numHashFunctions; i++)
            this.minHashFunctions.add(new MinHash(i));
    }

    @Override
    public double calculate(final String string1, final String string2) {
        String[] strings1 = (string1 == null) ? new String[0] : this.tokenizer.tokenize(string1);
        String[] strings2 = (string2 == null) ? new String[0] : this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }

    @Override
    public double calculate(final String[] strings1, final String[] strings2) {

        if (strings1 == null || strings2 == null) {
            return (strings1 == null && strings2 == null) ? 1.0 : 0.0;
        }


        if (strings1.length == 0 && strings2.length == 0) return 1.0;
        if (strings1.length == 0 || strings2.length == 0) return 0.0;

        int k = this.minHashFunctions.size();
        int matches = 0;

        for (int i = 0; i < k; i++) {
            MinHash minHash = this.minHashFunctions.get(i);

            String signature1 = minHash.hash(strings1);
            String signature2 = minHash.hash(strings2);

            if (Objects.equals(signature1, signature2)) {
                matches++;
            }
        }


        return (double) matches / k;
    }
}