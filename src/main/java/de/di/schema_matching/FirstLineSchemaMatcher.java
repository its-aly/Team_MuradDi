package de.di.schema_matching;

import de.di.Relation;
import de.di.schema_matching.structures.SimilarityMatrix;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.helper.Tokenizer;

public class FirstLineSchemaMatcher {

    /**
     * Compares the attributes of the source relation with the target relation
     * to construct a comprehensive matrix of similarity scores.
     */
    public SimilarityMatrix match(Relation sourceRelation, Relation targetRelation) {
        // Retrieve underlying column datasets
        String[][] srcDataset = sourceRelation.getColumns();
        String[][] tgtDataset = targetRelation.getColumns();

        int srcLen = srcDataset.length;
        int tgtLen = tgtDataset.length;

        double[][] matrixScores = new double[srcLen][tgtLen];

        // Initialize the token-based Jaccard similarity metric
        Tokenizer wordTokenizer = new Tokenizer(1, true);
        Jaccard jaccardMetric = new Jaccard(wordTokenizer, false);

        // Compute similarity scores across all attribute combinations
        for (int i = 0; i < srcLen; i++) {
            String[] srcCol = srcDataset[i];

            for (int j = 0; j < tgtLen; j++) {
                String[] tgtCol = tgtDataset[j];

                // Evaluate and store the mapping strength
                matrixScores[i][j] = jaccardMetric.calculate(srcCol, tgtCol);
            }
        }

        return new SimilarityMatrix(matrixScores, sourceRelation, targetRelation);
    }
}