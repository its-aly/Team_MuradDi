package de.di.data_profiling.structures;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class PositionListIndex {

    private final AttributeList attributes;
    private final List<IntArrayList> clusters;
    private final int[] invertedClusters;

    public PositionListIndex(final AttributeList attributes, final String[] values) {
        this.attributes = attributes;
        this.clusters = this.calculateClusters(values);
        this.invertedClusters = this.calculateInverted(this.clusters, values.length);
    }

    public PositionListIndex(final AttributeList attributes, final List<IntArrayList> clusters, int relationLength) {
        this.attributes = attributes;
        this.clusters = clusters;
        this.invertedClusters = this.calculateInverted(this.clusters, relationLength);
    }

    private List<IntArrayList> calculateClusters(final String[] values) {
        Map<String, IntArrayList> invertedIndex = new HashMap<>(values.length);
        for (int i = 0; i < values.length; i++) {
            invertedIndex.putIfAbsent(values[i], new IntArrayList());
            invertedIndex.get(values[i]).add(i);
        }
        return invertedIndex.values().stream().filter(c -> c.size() > 1).collect(Collectors.toList());
    }

    private int[] calculateInverted(List<IntArrayList> clusters, int length) {
        int[] inv = new int[length];
        Arrays.fill(inv, -1);
        for (int i = 0; i < clusters.size(); i++)
            for (int record : clusters.get(i))
                inv[record] = i;
        return inv;
    }

    public boolean isUnique() {
        return this.clusters.isEmpty();
    }

    public int relationLength() {
        return this.invertedClusters.length;
    }

    public PositionListIndex intersect(PositionListIndex other) {
        List<IntArrayList> intersection = this.intersect(this.clusters, other.getInvertedClusters());
        return new PositionListIndex(this.attributes.union(other.getAttributes()), intersection, this.relationLength());
    }

    private List<IntArrayList> intersect(List<IntArrayList> clusters, int[] inv) {
        List<IntArrayList> res = new ArrayList<>();
        for (IntArrayList cluster : clusters) {
            Int2ObjectMap<IntArrayList> sub = new Int2ObjectArrayMap<>();
            for (int i = 0; i < cluster.size(); i++) {
                int rec = cluster.getInt(i);
                int id = inv[rec];
                if (id != -1) {
                    if (!sub.containsKey(id)) sub.put(id, new IntArrayList());
                    sub.get(id).add(rec);
                }
            }
            for (IntArrayList sc : sub.values()) {
                if (sc.size() > 1) res.add(sc);
            }
        }
        return res;
    }
}