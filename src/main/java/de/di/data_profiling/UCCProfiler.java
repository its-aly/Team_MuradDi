package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;
import java.util.ArrayList;
import java.util.List;

public class UCCProfiler {

    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();

        for (int i = 0; i < numAttributes; i++) {
            AttributeList attributes = new AttributeList(i);
            PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[i]);
            if (pli.isUnique()) {
                uniques.add(new UCC(relation, attributes));
            } else {
                currentNonUniques.add(pli);
            }
        }

        for (int level = 2; level <= numAttributes; level++) {
            List<PositionListIndex> nextNonUniques = new ArrayList<>();
            for (int i = 0; i < currentNonUniques.size(); i++) {
                for (int j = i + 1; j < currentNonUniques.size(); j++) {
                    PositionListIndex pli1 = currentNonUniques.get(i);
                    PositionListIndex pli2 = currentNonUniques.get(j);

                    if (pli1.getAttributes().samePrefixAs(pli2.getAttributes())) {
                        AttributeList combined = pli1.getAttributes().union(pli2.getAttributes());
                        if (combined.size() == level && isMinimal(combined, uniques)) {
                            PositionListIndex intersected = pli1.intersect(pli2);
                            if (intersected.isUnique()) {
                                uniques.add(new UCC(relation, combined));
                            } else {
                                nextNonUniques.add(intersected);
                            }
                        }
                    }
                }
            }
            currentNonUniques = nextNonUniques;
            if (currentNonUniques.isEmpty()) break;
        }
        return uniques;
    }

    private boolean isMinimal(AttributeList candidate, List<UCC> discoveredUCCs) {
        for (UCC ucc : discoveredUCCs) {
            if (candidate.supersetOf(ucc.getAttributeList())) return false;
        }
        return true;
    }
}