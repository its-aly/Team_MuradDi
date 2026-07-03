package de.di.duplicate_detection;

import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.similarity_measures.SimilarityMeasure;

import java.util.List;
import java.util.stream.Collectors;

public class RecordComparator {

    private List<AttrSimWeight> attrSimWeights;
    private double threshold;

    private Integer keyAttribute = null;
    private SimilarityMeasure keySimilarityMeasure = null;
    private double keyThreshold = 0.6;

    public RecordComparator(List<AttrSimWeight> attrSimWeights, double threshold) {
        this.attrSimWeights = this.normalize(attrSimWeights);
        this.threshold = threshold;
    }

    public RecordComparator(List<AttrSimWeight> attrSimWeights, double threshold,
                            int keyAttribute, SimilarityMeasure keySimilarityMeasure, double keyThreshold) {
        this.attrSimWeights = this.normalize(attrSimWeights);
        this.threshold = threshold;
        this.keyAttribute = keyAttribute;
        this.keySimilarityMeasure = keySimilarityMeasure;
        this.keyThreshold = keyThreshold;
    }

    private List<AttrSimWeight> normalize(List<AttrSimWeight> attrSimWeights) {
        double correction = 1 / attrSimWeights.stream()
                .map(AttrSimWeight::getWeight)
                .mapToDouble(Double::doubleValue)
                .sum();

        return attrSimWeights.stream()
                .map(a -> new AttrSimWeight(
                        a.getAttribute(),
                        a.getSimilarityMeasure(),
                        a.getWeight() * correction))
                .collect(Collectors.toList());
    }

    public double compare(String[] tuple1, String[] tuple2) {
        double recordSimilarity = 0.0;

        for (AttrSimWeight attrSimWeight : attrSimWeights) {
            int attribute = attrSimWeight.getAttribute();
            SimilarityMeasure similarityMeasure = attrSimWeight.getSimilarityMeasure();
            double weight = attrSimWeight.getWeight();

            double similarity = similarityMeasure.calculate(
                    tuple1[attribute],
                    tuple2[attribute]
            );

            recordSimilarity += similarity * weight;
        }

        return recordSimilarity;
    }

    public boolean isDuplicate(double similarity, String[] tuple1, String[] tuple2) {
        if (similarity <= this.threshold) {
            return false;
        }
        if (keyAttribute != null) {
            double keySim = keySimilarityMeasure.calculate(tuple1[keyAttribute], tuple2[keyAttribute]);
            return keySim > keyThreshold;
        }
        return true;
    }

    public boolean isDuplicate(double similarity) {
        return similarity > this.threshold;
    }
}