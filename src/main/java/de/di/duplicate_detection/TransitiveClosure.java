package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.Duplicate;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.*;

public class TransitiveClosure {

    // Circuit breaker: clusters larger than this are skipped instead of
    // expanded into a full pairwise clique, which prevents O(n^2) blowups.
    private static final int MAX_CLUSTER_SIZE = 1000;

    /**
     * Pure primitive, auto-boxing-free Transitive Closure calculation.
     */
    public Set<Duplicate> calculate(Set<Duplicate> duplicates) {
        if (duplicates == null || duplicates.size() <= 1) {
            return duplicates;
        }

        Relation relation = duplicates.iterator().next().getRelation();
        int totalRecords = relation.getRecords().length;

        List<Integer>[] graphNeighbors = new ArrayList[totalRecords];
        LongOpenHashSet inputPairs = new LongOpenHashSet(duplicates.size());

        for (Duplicate match : duplicates) {
            int id1 = match.getIndex1();
            int id2 = match.getIndex2();

            long key = ((long) Math.min(id1, id2) << 32) | (Math.max(id1, id2) & 0xFFFFFFFFL);
            inputPairs.add(key);

            if (graphNeighbors[id1] == null) graphNeighbors[id1] = new ArrayList<>();
            if (graphNeighbors[id2] == null) graphNeighbors[id2] = new ArrayList<>();

            graphNeighbors[id1].add(id2);
            graphNeighbors[id2].add(id1);
        }

        Set<Duplicate> closedDuplicates = new HashSet<>(duplicates.size() * 2);
        boolean[] globallyVisited = new boolean[totalRecords];
        int[] traversalQueue = new int[totalRecords];

        for (int i = 0; i < totalRecords; i++) {
            if (globallyVisited[i] || graphNeighbors[i] == null) {
                continue;
            }

            IntArrayList clusterElements = new IntArrayList();
            int head = 0;
            int tail = 0;

            traversalQueue[tail++] = i;
            globallyVisited[i] = true;

            while (head < tail) {
                int currentElement = traversalQueue[head++];
                clusterElements.add(currentElement);

                List<Integer> neighbors = graphNeighbors[currentElement];
                if (neighbors != null) {
                    for (int n = 0; n < neighbors.size(); n++) {
                        int neighbor = neighbors.get(n);
                        if (!globallyVisited[neighbor]) {
                            globallyVisited[neighbor] = true;
                            traversalQueue[tail++] = neighbor;
                        }
                    }
                }
            }

            int clusterSize = clusterElements.size();
            if (clusterSize > 1) {

                long pairCount = (long) clusterSize * (clusterSize - 1) / 2;
                if (clusterSize > MAX_CLUSTER_SIZE) {
                    System.out.println("WARNING: large cluster detected, size=" + clusterSize
                            + " -> ~" + pairCount + " pairs. Skipping pairwise expansion for "
                            + "this cluster (likely caused by an overly loose similarity "
                            + "threshold or too many low-entropy fields chaining records "
                            + "together). Tighten RecordComparator before rerunning.");
                    continue; // do NOT materialize this cluster's pairs
                }

                int[] sortedElements = clusterElements.toIntArray();
                Arrays.sort(sortedElements);

                for (int x = 0; x < clusterSize; x++) {
                    int idx1 = sortedElements[x];
                    for (int y = x + 1; y < clusterSize; y++) {
                        int idx2 = sortedElements[y];
                        closedDuplicates.add(new Duplicate(idx1, idx2, 1.0, relation));
                    }
                }
            }
        }

        return closedDuplicates;
    }
}