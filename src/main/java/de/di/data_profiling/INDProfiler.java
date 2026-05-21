package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.IND;
import java.util.*;

public class INDProfiler {

    public List<IND> profile(List<Relation> relations, boolean discoverNary) {
        List<IND> allInds = discoverUnaryINDs(relations);
        if (!discoverNary) return allInds;
        return allInds;
    }

    private List<IND> discoverUnaryINDs(List<Relation> relations) {
        List<IND> inds = new ArrayList<>();
        List<List<Set<String>>> relColumnSets = new ArrayList<>();
        for (Relation r : relations) {
            List<Set<String>> colSets = new ArrayList<>();
            for (int i = 0; i < r.getColumns().length; i++) {
                colSets.add(new HashSet<>(Arrays.asList(r.getColumns()[i])));
            }
            relColumnSets.add(colSets);
        }

        for (int i = 0; i < relations.size(); i++) {
            for (int j = 0; j < relations.size(); j++) {
                List<Set<String>> lhsCols = relColumnSets.get(i);
                List<Set<String>> rhsCols = relColumnSets.get(j);

                for (int colI = 0; colI < lhsCols.size(); colI++) {
                    for (int colJ = 0; colJ < rhsCols.size(); colJ++) {
                        if (i == j && colI == colJ) continue;

                        if (rhsCols.get(colJ).containsAll(lhsCols.get(colI))) {
                            inds.add(new IND(relations.get(i), new AttributeList(colI),
                                    relations.get(j), new AttributeList(colJ)));
                        }
                    }
                }
            }
        }
        return inds;
    }
}