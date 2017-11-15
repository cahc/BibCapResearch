package Index;

import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

import java.text.DecimalFormat;

/**
 * Created by crco0001 on 11/15/2017.
 */
public class TopNCollector {

    //for printing
    static final DecimalFormat df = new DecimalFormat("0.00000");


    double currentMinSim = Double.MIN_VALUE;

    //use both as a counter and as an index
    int currentObjectsAdded = -1;
    int currentMinIndex = Integer.MIN_VALUE;
    final int ID;
    final int capacity;
    final SparseDoc[] docs;
    final float[] sim;

    public TopNCollector(int capacity, int ID) {

        docs = new SparseDoc[capacity];

        sim = new float[capacity];
        this.ID = ID;
        this.capacity = capacity;
    }

    public boolean offer(SparseDoc d, float sim) {


        if (currentObjectsAdded < docs.length - 1) { //will add

            currentObjectsAdded++;
            docs[currentObjectsAdded] = d;
            this.sim[currentObjectsAdded] = sim;

            //first offered object
            if (currentObjectsAdded == 0) {
                currentMinSim = sim;
                currentMinIndex = currentObjectsAdded;
                return true;
            }

            //not first but capacity is not full
            if (sim < currentMinSim) {
                currentMinSim = sim;
                currentMinIndex = currentObjectsAdded;
            }
            return true;

        } else {

            //perhaps we add, perhaps we don't!

            if (sim <= currentMinSim) return false;

            //we do:

            docs[currentMinIndex] = d;
            this.sim[currentMinIndex] = sim;

            //expensive step, which is the min index / min value now?

            double tmpSimVal = Double.MAX_VALUE;
            int index = 0;

            for (int i = 0; i < docs.length; i++) {

                if (this.sim[i] < tmpSimVal) {
                    tmpSimVal = this.sim[i];
                    index = i;
                }

            }

            currentMinSim = tmpSimVal;
            currentMinIndex = index;

            return true;
        }


    }


    @Override
    public String toString() {

        StringBuilder s = new StringBuilder(10);

        for (int i = 0; i <= currentObjectsAdded; i++) { // as it might docs[i] might be null

            s.append((this.docs[i].ID + 1));
            s.append(" ");
            s.append(df.format(this.sim[i]));
            s.append(" "); //print docID with start of 1 not 0!
        }

        return s.toString();

    }

    public void sort() {


        final int perm[] = new int[this.sim.length];

        for (int i = 0; i < this.sim.length; i++) perm[i] = i;

        //maybe parallelRadixSortIndirect
        FloatArrays.radixSortIndirect(perm, this.sim, false);

        //ok ok.. maybe this should be done "in place"
        final SparseDoc[] tmpDocs = ObjectArrays.copy(this.docs);
        final float[] tmpValues = FloatArrays.copy(this.sim);

        //Largest to smallest
        for (int i = 0; i < this.sim.length; i++) {


            this.docs[(this.docs.length - 1) - i] = tmpDocs[perm[i]];
            this.sim[(this.sim.length - 1) - i] = tmpValues[perm[i]];

        }


    }

    public void clear() {


        currentObjectsAdded = -1;
        currentMinIndex = Integer.MIN_VALUE;
        currentMinSim = Double.MIN_VALUE;

        for (int i = 0; i < docs.length; i++) {
            docs[i] = null;
            sim[i] = 0;

        }
    }

}

