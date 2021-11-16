/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.testbibd;

import java.util.ArrayList;
import java.util.List;
import org.jacop.constraints.AndBool;
import org.jacop.constraints.SumInt;
import org.jacop.core.BooleanVar;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.examples.fd.ExampleFD;

/**
 *
 * @author JACOP:
 * https://github.com/radsz/jacop/blob/develop/src/main/java/org/jacop/examples/fd/BIBD.java
 * 
 * Other useful source: https://www.csplib.org/Problems/prob028/
 */
public class BIBD extends ExampleFD {

    /**
     * It specifies the number of items.
     */
    public int v = 4;
    /**
     * It specifies the number of blocks.
     */
    public int b = 4;
    /**
     * It specifies the number of times an item should appear, in total.
     */
    public int r = 3;
    /**
     * The size of the blocks.
     */
    public int k = 3;
    /**
     * It specifies the number of times any two item can cooccur in blocks.
     */
    public int lambda = 2;

    IntVar[][] x;

    @Override
    public void model() {

        store = new Store();
        vars = new ArrayList<IntVar>();

        // Get problem size n from second program argument.
        x = new IntVar[v][b];

        for (int i = 0; i < v; i++) {
            for (int j = 0; j < b; j++) {
                x[i][j] = new BooleanVar(store, "x" + i + "_" + j);
                vars.add(x[i][j]);
            }
        }

        IntVar rVar = new IntVar(store, "r", r, r);
        IntVar kVar = new IntVar(store, "k", k, k);
        IntVar lambdaVar = new IntVar(store, "lambda", lambda, lambda);

        for (int i = 0; i < v; i++) {
            store.impose(new SumInt(x[i], "==", rVar), 1);
        }

        for (int j = 0; j < b; j++) {
            IntVar[] column = new IntVar[v];
            for (int i = 0; i < v; i++) {
                column[i] = x[i][j];
            }
            store.impose(new SumInt(column, "==", kVar), 1);
        }

        for (int i = 0; i - 1 < v; i++) {
            for (int j = i + 1; j < v; j++) {

                List<IntVar> result = new ArrayList();

                for (int m = 0; m < b; m++) {
                    BooleanVar product = new BooleanVar(store, "p" + i + "_" + j + "_" + m);
                    BooleanVar[] array = {(BooleanVar) x[i][m], (BooleanVar) x[j][m]};
                    store.imposeDecomposition(new AndBool(array, product), 0);
                    result.add(product);
                }

                store.impose(new SumInt(result, "==", lambdaVar), 1);
            }
        }

    }

    public static void main(String[] args) {
        BIBD example = new BIBD();
        //v : the distinct objects
        example.v = 7;

        //b: the number of blocks, one block being an evaluation item for a human coder
        // so b is the number of evaluations that will be done. If we think that one coder can do 100 evaluations, then b = 100 x nb of coders
        example.b = 7;

        //k : the number of objects per block (per evaluation). For Best Worst scaling, 4 is often selected
        example.k = 3;

        //r: the number of times an object should appear. 4 is an arbitrary but good number, no?
        example.r = 3;

        //lambda: the number of times a pair of items should appear together in the entire set of blocks. Ideally, twice I think? A good way to measure the consistency of the coder. 
        example.lambda = 1;

        example.model();

        if (example.searchAllAtOnce()) {
            System.out.println("Solution(s) found");

            ExampleFD.printMatrix(example.x, example.v, example.b);

        }
    }

}
