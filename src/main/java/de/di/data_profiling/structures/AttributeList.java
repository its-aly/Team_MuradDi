package de.di.data_profiling.structures;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * An AttributeList is an ordered list of attribute indexes.
 */
@Getter
@AllArgsConstructor
public class AttributeList {

    private int[] attributes;

    public AttributeList(final int singleAttribute) {
        this.attributes = new int[]{singleAttribute};
    } // Fixed: Properly closed the constructor

    // Note: 'isSupersetOf' was removed because 'supersetOf' already exists below.
    // Use 'candidate.supersetOf(other)' in your UCCProfiler.

    public IntSet getAttributeSet() {
        return new IntArraySet(this.attributes);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.attributes);
    }

    public int size() {
        return this.attributes.length;
    }

    public AttributeList union(AttributeList other) {
        int[] attributes1 = this.attributes.clone();
        int[] attributes2 = other.getAttributes().clone();
        Arrays.sort(attributes1);
        Arrays.sort(attributes2);

        IntArrayList attributesUnion = new IntArrayList(attributes1.length + 1);
        int i = 0;
        int j = 0;
        while (i < attributes1.length || j < attributes2.length) {
            if (i >= attributes1.length) {
                attributesUnion.add(attributes2[j]);
                j++;
            } else if (j >= attributes2.length) {
                attributesUnion.add(attributes1[i]);
                i++;
            } else if (attributes1[i] == attributes2[j]) {
                attributesUnion.add(attributes1[i]);
                i++;
                j++;
            } else if (attributes1[i] < attributes2[j]) {
                attributesUnion.add(attributes1[i]);
                i++;
            } else {
                attributesUnion.add(attributes2[j]);
                j++;
            }
        }
        return new AttributeList(attributesUnion.toArray(new int[0]));
    }

    public boolean samePrefixAs(AttributeList other) {
        if (this.attributes.length != other.getAttributes().length)
            return false;
        for (int i = 0; i < this.attributes.length - 1; i++)
            if (this.attributes[i] != other.getAttributes()[i])
                return false;
        return true;
    }

    public boolean superlistOf(AttributeList other) {
        if (this.attributes.length <= other.getAttributes().length)
            return false;
        int i = 0;
        int j = 0;
        while (true) {
            if (j == other.getAttributes().length)
                return true;
            if (i == this.attributes.length)
                return false;

            if (this.attributes[i] > other.getAttributes()[j])
                return false;
            if (this.attributes[i] < other.getAttributes()[j])
                i++;
            else if (this.attributes[i] == other.getAttributes()[j]) {
                i++;
                j++;
            }
        }
    }

    public boolean sublistOf(AttributeList other) {
        return other.superlistOf(this);
    }

    public boolean supersetOf(AttributeList other) {
        IntSet attributeSet1 = new IntArraySet(this.attributes);
        IntSet attributeSet2 = new IntArraySet(other.getAttributes());
        return attributeSet1.containsAll(attributeSet2);
    }

    public boolean subsetOf(AttributeList other) {
        return other.superlistOf(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        AttributeList that = (AttributeList) o;
        return Arrays.equals(this.attributes, that.getAttributes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.attributes);
    }
}