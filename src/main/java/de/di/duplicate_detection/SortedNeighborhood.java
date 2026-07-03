package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.duplicate_detection.structures.Duplicate;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.Levenshtein;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class SortedNeighborhood {

    @Data
    @AllArgsConstructor
    private static class DataEntry {
        private int rowId;
        private String[] fields;
    }

    public Set<Duplicate> detectDuplicates(Relation relation, int[] sortingKeys, int windowSize, RecordComparator recordComparator) {
        Set<Duplicate> identifiedDuplicates = new HashSet<>();
        int totalRecords = relation.getRecords().length;

        DataEntry[] wrappedEntries = new DataEntry[totalRecords];
        for (int i = 0; i < totalRecords; i++) {
            wrappedEntries[i] = new DataEntry(i, relation.getRecords()[i]);
        }

        for (int keyIndex : sortingKeys) {
            Arrays.sort(wrappedEntries, Comparator.comparing(entry -> entry.getFields()[keyIndex]));

            for (int outerIdx = 0; outerIdx < totalRecords; outerIdx++) {
                int boundaryLimit = Math.min(outerIdx + windowSize, totalRecords);

                for (int innerIdx = outerIdx + 1; innerIdx < boundaryLimit; innerIdx++) {
                    DataEntry firstItem = wrappedEntries[outerIdx];
                    DataEntry secondItem = wrappedEntries[innerIdx];

                    double similarityScore = recordComparator.compare(firstItem.getFields(), secondItem.getFields());

                    if (recordComparator.isDuplicate(similarityScore, firstItem.getFields(), secondItem.getFields())) {
                        identifiedDuplicates.add(new Duplicate(
                                firstItem.getRowId(),
                                secondItem.getRowId(),
                                similarityScore,
                                relation
                        ));
                    }
                }
            }
        }

        return identifiedDuplicates;
    }

    public static RecordComparator suggestRecordComparatorFor(Relation relation) {
        List<AttrSimWeight> weightConfigurations = new ArrayList<>();

        double baseThreshold = 0.75;
        Tokenizer nGramTokenizer = new Tokenizer(3, true);
        boolean useDamerauVariation = true;

        Integer keyAttribute = null;

        for (int idx = 0; idx < relation.getAttributes().length; idx++) {
            String label = relation.getAttributes()[idx].toLowerCase();

            if (label.startsWith("track") || label.equals("pk") || label.equals("id")
                    || label.equals("cdextra") || label.equals("category")) {
                continue;
            }

            AttrSimWeight evaluationWeight;

            if (label.equals("title")) {
                evaluationWeight = new AttrSimWeight(idx, new Levenshtein(useDamerauVariation), 0.5);
                keyAttribute = idx;
            } else if (label.equals("artist")) {
                evaluationWeight = new AttrSimWeight(idx, new Levenshtein(useDamerauVariation), 0.4);
            } else if (label.equals("genre")) {
                evaluationWeight = new AttrSimWeight(idx, new Jaccard(nGramTokenizer, true), 0.05);
            } else if (label.equals("year")) {
                evaluationWeight = new AttrSimWeight(idx, new Levenshtein(useDamerauVariation), 0.05);
            } else {
                continue;
            }

            weightConfigurations.add(evaluationWeight);
        }

        if (keyAttribute != null) {
            return new RecordComparator(
                    weightConfigurations,
                    baseThreshold,
                    keyAttribute,
                    new Levenshtein(useDamerauVariation),
                    0.6
            );
        }

        return new RecordComparator(weightConfigurations, baseThreshold);
    }
}