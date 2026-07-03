package de.di.schema_matching;

import de.di.schema_matching.structures.CorrespondenceMatrix;
import de.di.schema_matching.structures.SimilarityMatrix;

import java.util.Arrays;

public class SecondLineSchemaMatcher {

    /**
     * Converts a continuous similarity score matrix into a binary matching grid
     * by framing the task as a linear assignment optimization problem.
     */
    public CorrespondenceMatrix match(SimilarityMatrix similarityMatrix) {
        double[][] weights = similarityMatrix.getMatrix();
        int rows = weights.length;
        int cols = weights[0].length;

        // Map similarity metrics into minimization costs (Cost = 1.0 - Similarity)
        double[][] distanceGrid = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                distanceGrid[r][c] = 1.0 - weights[r][c];
            }
        }

        int[] optimalMappings = executeKuhnMunkres(distanceGrid);
        int[][] binaryMatches = buildSelectionGrid(optimalMappings, weights);

        return new CorrespondenceMatrix(
                binaryMatches,
                similarityMatrix.getSourceRelation(),
                similarityMatrix.getTargetRelation()
        );
    }

    /**
     * Linear assignment optimizer utilizing a modified Hungarian/Kuhn-Munkres routine.
     */
    private int[] executeKuhnMunkres(double[][] baseCosts) {
        int originalRows = baseCosts.length;
        int originalCols = baseCosts[0].length;
        int scale = Math.max(originalRows, originalCols);

        double[][] balancedCosts = new double[scale][scale];
        for (int r = 0; r < scale; r++) {
            for (int c = 0; c < scale; c++) {
                balancedCosts[r][c] = (r < originalRows && c < originalCols)
                        ? baseCosts[r][c]
                        : 1e9; // Large penalty for padding
            }
        }

        double[] potentialRow = new double[scale];
        double[] potentialCol = new double[scale];
        int[] assignments = new int[scale];
        Arrays.fill(assignments, -1);

        for (int activeRow = 0; activeRow < scale; activeRow++) {
            int[] parentLinks = new int[scale];
            double[] slackValues = new double[scale];
            boolean[] columnSeen = new boolean[scale];

            Arrays.fill(parentLinks, -1);
            Arrays.fill(slackValues, Double.POSITIVE_INFINITY);

            int currentElementRow = activeRow;
            int currentElementCol = -1;
            int targetedCol;

            do {
                targetedCol = -1;

                for (int nextCol = 0; nextCol < scale; nextCol++) {
                    if (!columnSeen[nextCol]) {
                        double residual = balancedCosts[currentElementRow][nextCol] - potentialRow[currentElementRow] - potentialCol[nextCol];

                        if (residual < slackValues[nextCol]) {
                            slackValues[nextCol] = residual;
                            parentLinks[nextCol] = currentElementCol;
                        }

                        if (targetedCol == -1 || slackValues[nextCol] < slackValues[targetedCol]) {
                            targetedCol = nextCol;
                        }
                    }
                }

                double stepDelta = slackValues[targetedCol];

                for (int colIdx = 0; colIdx < scale; colIdx++) {
                    if (columnSeen[colIdx]) {
                        potentialRow[assignments[colIdx]] += stepDelta;
                        potentialCol[colIdx] -= stepDelta;
                    } else {
                        slackValues[colIdx] -= stepDelta;
                    }
                }

                potentialRow[activeRow] += stepDelta;
                columnSeen[targetedCol] = true;
                currentElementCol = targetedCol;
                currentElementRow = assignments[targetedCol];

            } while (currentElementRow != -1);

            // Alternating path updating sequence
            int tracer = targetedCol;
            while (parentLinks[tracer] != -1) {
                assignments[tracer] = assignments[parentLinks[tracer]];
                tracer = parentLinks[tracer];
            }
            assignments[tracer] = activeRow;
        }

        int[] structuralMapping = new int[originalRows];
        Arrays.fill(structuralMapping, -1);

        for (int c = 0; c < originalCols; c++) {
            if (assignments[c] < originalRows) {
                structuralMapping[assignments[c]] = c;
            }
        }

        return structuralMapping;
    }

    private int[][] buildSelectionGrid(int[] associations, double[][] matrixBounds) {
        int height = matrixBounds.length;
        int[][] outputGrid = new int[height][];

        for (int i = 0; i < height; i++) {
            outputGrid[i] = new int[matrixBounds[i].length];
        }

        for (int srcIdx = 0; srcIdx < associations.length; srcIdx++) {
            int tgtIdx = associations[srcIdx];
            if (tgtIdx >= 0) {
                outputGrid[srcIdx][tgtIdx] = 1;
            }
        }

        return outputGrid;
    }
}