package Index;

import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by crco0001 on 11/15/2017.
 */
public class SparseDoc implements Comparable<SparseDoc>,Serializable {
    static final DecimalFormat df = new DecimalFormat("0.00000"); // for printing the simvalues in the neighbourhood

    public int[] dimensions;
    public float[] values;
    final int UT;
    static int instanceCounter = 0; // internal id starts at 0 (not one!)
    final int ID;
    boolean isSortedByValue = false;
    boolean isSortedByDimension = false;


    // ************EXPERIMENTAL************* //

    //neighbour is used in the enhancement step
    SparseDoc[] docNeighbours; // save neighbours here, lazy initialize (maybe never)

    //neighbour is used in the enhancement step
    float[] docSimilarity; // save similarity here, lazy initialize (maybe never)

    //which dimensions for this doc is prefixes?
    IntArrayList prefixDims;  // lazy initialize (maybe never)

    // ***************EXPERIMENTAL ***********//


    public SparseDoc(Int2FloatOpenHashMap m, int UT) {

        this.ID = instanceCounter;
        int i = 0;
        this.dimensions = new int[m.size()];
        this.values = new float[m.size()];
        this.UT = UT;

        for (Int2FloatOpenHashMap.Entry e : m.int2FloatEntrySet()) {

            dimensions[i] = e.getIntKey();
            values[i] = e.getFloatValue();
            //System.out.println( e.getIntKey() + " " + e.getValue() );
            i++;
        }

        instanceCounter++;
    }


    // ***************EXPERIMENTAL ***********//


    public void addDocNeighbours(TopNCollector c) {

        //manual array copy is slower than the native System.arraycopy()


        this.docNeighbours = new SparseDoc[c.currentObjectsAdded + 1];

        System.arraycopy(c.docs, 0, docNeighbours, 0, docNeighbours.length);


        //for(int i=0; i<= c.currentObjectsAdded; i++) { // as it might docs[i] might be null

        //    this.docNeighbours[i] = c.docs[i];
        //}

    }

    public void addDocSimilarity(TopNCollector c) {

        //manual array copy is slower than the native System.arraycopy()

        this.docSimilarity = new float[c.currentObjectsAdded + 1];

        System.arraycopy(c.sim, 0, docSimilarity, 0, docSimilarity.length);

        //for(int i=0;i<=c.currentObjectsAdded;i++) {

        //   this.docSimilarity[i] = c.sim[i];

        //}

    }


    public void addPrefix(int i) {

        if (prefixDims == null) {

            prefixDims = new IntArrayList(5); //initial capacity
        }

        prefixDims.add(i);
    }

    public int prefixLength() {

        if (prefixDims == null) return 0;

        return prefixDims.size();

    }

    // ***************EXPERIMENTAL ***********//


    public String printNeighbourhood() {

        StringBuilder s = new StringBuilder(10);
        //print docID with start of 1 not 0!
        for (int i = 0; i < docNeighbours.length; i++) { // as it might docs[i] might be null

            if (i == 0) {
                s.append((this.docNeighbours[i].ID + 1));
                s.append(" ");
                s.append(df.format(this.docSimilarity[i]));
            } else {

                s.append(" ");
                s.append((this.docNeighbours[i].ID + 1));
                s.append(" ");
                s.append(df.format(this.docSimilarity[i]));
            }

        }

        return s.toString();


    }


    public int getLength() {

        return dimensions.length;

    }

    public int[] getDimensions() {

        return dimensions;
    }

    public float[] getValues() {

        return values;
    }


    //todo BROKEN!!! take care of isPrefix, overload..

    public void sortByDimensions() {

        final int perm[] = new int[this.values.length];

        for (int i = 0; i < values.length; i++) perm[i] = i;

        //maybe parallelRadixSortIndirect
        IntArrays.radixSortIndirect(perm, this.dimensions, false);

        //ok ok.. maybe this should be done "in place"
        final int[] tmpDimensions = IntArrays.copy(this.dimensions);
        final float[] tmpValues = FloatArrays.copy(this.values);


        //Smallest to largest
        for (int i = 0; i < this.values.length; i++) {


            this.dimensions[i] = tmpDimensions[perm[i]];
            this.values[i] = tmpValues[perm[i]];


        }

        isSortedByDimension = true;
        isSortedByValue = false;

    }

    public void sortByValues() {


        final int perm[] = new int[this.values.length];

        for (int i = 0; i < this.values.length; i++) perm[i] = i;

        //maybe parallelRadixSortIndirect
        FloatArrays.radixSortIndirect(perm, this.values, false);

        //ok ok.. maybe this should be done "in place"
        final int[] tmpDimensions = IntArrays.copy(this.dimensions);
        final float[] tmpValues = FloatArrays.copy(this.values);

        //Largest to smallest
        for (int i = 0; i < this.values.length; i++) {


            this.dimensions[(this.values.length - 1) - i] = tmpDimensions[perm[i]];
            this.values[(this.values.length - 1) - i] = tmpValues[perm[i]];

        }

        isSortedByDimension = false;
        isSortedByValue = true;

    }

    public void normalize() {

        float norm = 0;

        for (int i = 0; i < this.values.length; i++) {


            norm = norm + (this.values[i] * this.values[i]);

        }

        norm = (float) Math.sqrt(norm);

        for (int i = 0; i < this.values.length; i++) {

            this.values[i] = this.values[i] / norm;

        }


    }

    public void idfWeight(Int2IntMap documentFrequency, float N) {

        for (int i = 0; i < dimensions.length; i++) {

            this.values[i] = (float) (values[i] * Math.log((N / documentFrequency.get(this.dimensions[i]))));

        }


    }

    public void weightNormalizeReduceAndCreatePrefixArray(Int2IntMap documentFrequency, double N) {

        boolean featuresWithMoreThanOneDoc[] = new boolean[dimensions.length];

        int DF;

        for (int i = 0; i < dimensions.length; i++) {

            DF = documentFrequency.get(this.dimensions[i]);
            if (DF > 1) featuresWithMoreThanOneDoc[i] = true;

            this.values[i] = values[i] * (float) Math.log((N / DF));

        }

        float norm = 0;

        for (int i = 0; i < this.values.length; i++) {

            norm = norm + (this.values[i] * this.values[i]);

        }

        norm = (float) Math.sqrt(norm);

        int newDims = 0;

        for (int j = 0; j < featuresWithMoreThanOneDoc.length; j++) {

            if (featuresWithMoreThanOneDoc[j]) newDims++;
        }

        int[] newDimensions = new int[newDims];
        float[] newValues = new float[newDims];


        int j = 0;
        for (int i = 0; i < featuresWithMoreThanOneDoc.length; i++) {

            if (featuresWithMoreThanOneDoc[i]) {
                newValues[j] = this.values[i] / norm;
                newDimensions[j] = this.dimensions[i];
                j++;
            }


        }

        this.values = newValues;
        this.dimensions = newDimensions;

    }

    public float dotProductDefault(SparseDoc other) {

        //arrays must be sorted by ascending dimensions

        if (this.UT == other.UT) return 1.0f; // check for equal UT

        if (!this.isSortedByDimension || !other.isSortedByDimension) {
            System.out.println("Vectors not sorted by dimension!");
            System.exit(1);
        }

        float dot = 0;

        if (other.values.length > this.values.length) { // this.SparseDoc is shortest

            int j; // index for the longer vector
            int jCounter = 0;

            for (int i = 0; i < this.values.length; i++) {

                j = jCounter;
                while (j < other.values.length) {

                    if (this.dimensions[i] == other.dimensions[j]) {
                        dot = dot + this.values[i] * other.values[j];
                        jCounter = ++j;
                        break;
                    }
                    if (this.dimensions[i] > other.dimensions[j]) {
                        jCounter = j;
                    } // never lookup j-indices lower than this again
                    if (this.dimensions[i] < other.dimensions[j]) {
                        jCounter = j;
                        break;
                    } //cannot hope to match to the right
                    j++;
                }


            }

        }


        if (this.values.length >= other.values.length) { // this.SparseDoc is longest or equal to other.SparseDoc

            int j; // index for the longer vector
            int jCounter = 0;

            for (int i = 0; i < other.values.length; i++) {

                j = jCounter;
                while (j < this.values.length) {


                    if (other.dimensions[i] == this.dimensions[j]) {
                        dot = dot + other.values[i] * this.values[j];
                        jCounter = ++j;
                        break;
                    }
                    if (other.dimensions[i] > this.dimensions[j]) {
                        jCounter = j;
                    } // never lookup j-indices lower than this again
                    if (other.dimensions[i] < this.dimensions[j]) {
                        jCounter = j;
                        break;
                    } //cannot hope to match to the right

                    j++;
                }


            }

        }


        return dot;

    }

    public float dotProductBinarySearch(SparseDoc other) {

        if (this.UT == other.UT) return 1.0f; // check for equal UT *reference* //todo move back

        //arrays must be sorted by ascending dimensions

        if (!this.isSortedByDimension || !other.isSortedByDimension) {
            System.out.println("Vectors not sorted by dimension!");
            System.exit(1);
        }

        float dot = 0;
        int index;
        if (other.values.length > this.values.length) { //the calling vector is shortest


            for (int i = 0; i < this.values.length; i++) {


                index = Arrays.binarySearch(other.dimensions, dimensions[i]);

                if (index >= 0) {
                    dot = dot + values[i] * other.values[index];
                }


            }

        }


        if (this.values.length >= other.values.length) {


            for (int i = 0; i < other.values.length; i++) {

                index = Arrays.binarySearch(dimensions, other.dimensions[i]);

                if (index >= 0) {
                    dot = dot + other.values[i] * values[index];
                }


            }


        }

        return dot;
    }


    //TODO FIX/CHECK EQUAL/HASHCODE

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) return false;

        return (this == o); //Identity!!

    }

    @Override
    public int hashCode() {

        return this.ID;

    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();

        for (int i = 0; i < dimensions.length; i++) {

            if (i == 0) {
                s.append(dimensions[i]);
            } else {
                s.append(" ");
                s.append(dimensions[i]);
            }

            s.append(" ");
            s.append(values[i]);
        }

        return s.toString();
        // return String.valueOf(this.UT);
    }

    @Override
    public int compareTo(SparseDoc other) {
        return this.UT - other.UT;
    }


}