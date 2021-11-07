/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.testbibd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.clementlevallois.utils.FindAllPairs;
import net.clementlevallois.utils.UnDirectedPair;

/**
 *
 * @author LEVALLOIS
 */
public class BIBDCustom<T extends Comparable<? super T>> {

    /**
     * @param args the command line arguments
     */
    private Integer maxNumberOfCoAppearances = 2;

    public static void main(String[] args) {
        Integer minNumberOfAppearances = 3;
        int nbItems = 100;
        Integer nbPerComparison = 4;
        Integer nbOfComparisons = 100;
        List<Integer> items = IntStream.range(0, nbItems).boxed().collect(Collectors.toList());
        new BIBDCustom().run(items, minNumberOfAppearances, nbPerComparison);

    }

    public List<List<T>> run(List<T> items, int minNumberOfAppearances, int nbItemsPerComparison) {
        List<T> comparison;
        List<List<T>> comparisons = new ArrayList();

        for (int n = 0; n < minNumberOfAppearances; n++) {

            Collections.shuffle(items);

            int j = 0;
            while (j <= items.size() - nbItemsPerComparison) {
                comparison = new ArrayList();
                for (int i = 0; i < nbItemsPerComparison; i++) {
                    T item = items.get(j);
                    j++;
                    comparison.add(item);
                }
                comparisons.add(comparison);
            }
//            countNbOfCoAppearances();
//            System.out.println("number of comparisons after round " + (n + 1) + ": " + comparisons.size());
        }
//            System.out.println("total number of comparisons: " + comparisons.size());

        return comparisons;
    }

    public void countNbOfCoAppearances(List<List<T>> comparisons) {
        Set<UnDirectedPair> allUnDirectedPairs = new HashSet();
        int theoreticalNumberOfPairs = 0;
        for (List<T> comp : comparisons) {
            FindAllPairs find = new FindAllPairs();
            Set<UnDirectedPair> allUnDirectedPairsFromList = find.getAllUndirectedPairsFromList(comp);
            theoreticalNumberOfPairs = theoreticalNumberOfPairs + allUnDirectedPairsFromList.size();
            allUnDirectedPairs.addAll(allUnDirectedPairsFromList);
        }
        System.out.println("there are " + theoreticalNumberOfPairs + " pairs in the comparisons");
        System.out.println("when we substract the duplicates, there are only " + allUnDirectedPairs.size() + " unique pairs left");
    }
}
