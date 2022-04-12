/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.testbibd.noseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.clementlevallois.testbibd.Block;
import net.clementlevallois.testbibd.Results;
import net.clementlevallois.utils.FindAllPairs;
import net.clementlevallois.utils.Multiset;
import net.clementlevallois.utils.UnDirectedPair;

/**
 *
 * @author LEVALLOIS
 * @param <T>
 */
public class BIBDCustom2<T extends Comparable<? super T>> {

    int theoreticalNumberOfDistinctPairs = 0;
    int actualNumberOfDistinctPairs = 0;
    float averageTimeADistinctPairAppears = 0;

    Multiset<UnDirectedPair> pairsAlreadyMade = new Multiset();
    Multiset<T> globalCountOfItems = new Multiset();

    // this map helps track which pairs have been included in blocks.
    // keys: each item
    // value for a key: the set of all items the key (item) has been paired with
    // at the beginning, no pairs have been formed: so the values are empty sets
    Map<T, Set<T>> pairingReservoir = new HashMap();

    public static void main(String[] args) {

        /**
         * number of items.
         */
        Integer v = 8;
        /**
         * number of blocks. Can be null if parameter r and lambda are non null
         */
        Integer b = null;
        /**
         * number of times an item should appear, in total. Can be null if
         * parameter b is non null
         */
        Integer r = null;
        /**
         * size of the blocks.
         */
        Integer k = 4;
        /**
         * number of times any two items should cooccur in blocks. Can be left
         * null if r is set.
         */
        Integer lambda = null;

        /**
         * number of annotators that will do the task.
         */
        Integer nbAnnotators = null;

        List<Integer> items = IntStream.range(1, v + 1).boxed().collect(Collectors.toList());
        new BIBDCustom2().run(GOAL.CANONICAL_BIBD, items, v, b, r, k, lambda);

    }

    public enum GOAL {
        CANONICAL_BIBD, LAMBDA_KNOWN, R_KNOWN
    }

    public Results run(GOAL goal, List<T> items, Integer nbItems_v, Integer nbOfBlocks_b, Integer numberOfAppearances_r, Integer blockSize_k, Integer maxNumberOfSamePairsInComparisons_lambda) {

        int countTries = 0;
        
        List<Block> blocks;

//        if (nbOfBlocks_b == null & numberOfAppearances_r == null) {
//            System.out.println("parameter b or r can be null but not both at the same time");
//            System.exit(-1);
//        }
        if (goal == GOAL.CANONICAL_BIBD) {
            int potential_r;
            float theoretical_lambda;
            for (int i = 1; i < 100; i++) {
                potential_r = i;
                theoretical_lambda = (float) potential_r * (blockSize_k - 1) / (nbItems_v - 1);
                if (((int) theoretical_lambda) == theoretical_lambda) {
                    System.out.println("solution found:");
                    System.out.println("r: " + potential_r);
                    System.out.println("lambda: " + theoretical_lambda);
                    maxNumberOfSamePairsInComparisons_lambda = (int) theoretical_lambda;
                    numberOfAppearances_r = potential_r;
                    break;
                }
            }
        }

        if (nbOfBlocks_b == null) {
            // because BIBD imposes that bk = vr // see Louviere et al. (2015), p. 17
            nbOfBlocks_b = (nbItems_v * numberOfAppearances_r) / blockSize_k;
        }
        System.out.println("nbOfBlocks_b: " + nbOfBlocks_b);

        if (numberOfAppearances_r == null) {
            // because BIBD imposes that bk = vr // see Louviere et al. (2015), p. 17
            numberOfAppearances_r = (nbOfBlocks_b * blockSize_k) / nbItems_v;
        }
        System.out.println("numberOfAppearances_r: " + numberOfAppearances_r);

        // important
        // to fill all blocks, the algo will iterate on the items as many times as necessary
        // to fill nb blocks x their size
        // a count of max tries should take this number of iterations as a basis, and multiply it by
        // a factor allowing for retries - here: 5
        int countMaxTries = Math.round(nbOfBlocks_b * blockSize_k/ nbItems_v) * 5;
        
        
        
        float theoretical_lambda = (float) numberOfAppearances_r * (blockSize_k - 1) / (nbItems_v - 1);

        for (T item : items) {
            pairingReservoir.put(item, new HashSet());
        }

        Collections.shuffle(items, new Random());

        // create an empty series of blocks
        blocks = new ArrayList(nbOfBlocks_b);

        Iterator<T> iteratorItems = items.iterator();

        //create a new block
        Block blockToFill = new Block(blockSize_k);

        // loop as long as the block series is not complete
        while (blocks.size() < nbOfBlocks_b) {
            if (blockToFill.isComplete()) {
                blockToFill = new Block(blockSize_k);
            }

            // if no more items to iterate on:
            if (!iteratorItems.hasNext()) {
                countTries++;

                // verify that we have not tried too many times iterating on items
                if (countTries > countMaxTries) {
                    // no solution has been found. We should relax the constraint and fill in the remaining items.
                    blockToFill.setRegular(false);
                    T candidateItemToAdd = findCandidateFromLowFrequencyItems(blockToFill, globalCountOfItems, numberOfAppearances_r);
                    if (candidateItemToAdd == null) {
                        blockToFill = new Block(blockSize_k);
                        Collections.shuffle(blocks);
                        int blocksToDelete = Math.min(5, blocks.size());
                        int deleted = 0;
                        while (!blocks.isEmpty() & deleted < blocksToDelete) {
                            Block blockToRemove = blocks.get(0);
                            blocks.remove(0);
                            removeBlock(blockToRemove);
                            deleted++;
                        }
                        System.out.println("REMOVED " + blocksToDelete + " BLOCKS TO FIND A BETTER SOLUTION");

                    } else {
                        blockToFill = addItemToBlock(candidateItemToAdd, blockToFill, maxNumberOfSamePairsInComparisons_lambda, numberOfAppearances_r);
                    }
                    countTries = 0;
                }
                Collections.shuffle(items, new Random());
                iteratorItems = items.iterator();
            }

            while (iteratorItems.hasNext() & !blockToFill.isComplete()) {
                T candidateItem = iteratorItems.next();

                List<T> existingItemsInBlock = blockToFill.getItems();

                // if the item is not in the block yet, consider the item
                if (!existingItemsInBlock.contains(candidateItem)) {
                    // if a number of conditions are met, add the item to the block
                    blockToFill = addItemToBlock(candidateItem, blockToFill, maxNumberOfSamePairsInComparisons_lambda, numberOfAppearances_r);
                }
            }
            if (blockToFill.isComplete()) {
                blocks.add(blockToFill);
            }
        }
        Multiset<T> itemFreqs = new Multiset();
        Set<Set<T>> allBlocksAsSets = new HashSet();
        Set<T> oneBlockAsSet;
        for (Block completedBlock : blocks) {
            itemFreqs.addAllFromListOrSet(completedBlock.getItems());
            oneBlockAsSet = new HashSet(completedBlock.getItems());
            allBlocksAsSets.add(oneBlockAsSet);
        }

        System.out.println("----------------");
        System.out.println("freq of all items: " + itemFreqs.getInternalMap().toString());

        int minOccurrenceValue = itemFreqs.sortAsc(itemFreqs).get(0).getValue();
        int maxOccurrenceValue = itemFreqs.sortDesc(itemFreqs).get(0).getValue();

        System.out.println("min occurrence for any item: " + minOccurrenceValue);
        System.out.println("max occurrence for any item: " + maxOccurrenceValue);

        float averageOccValue = 0;
        for (Entry<T, Integer> entry : itemFreqs.getEntrySet()) {
            averageOccValue = averageOccValue + entry.getValue();
        }
        averageOccValue = averageOccValue / itemFreqs.getSize();

        System.out.println("average occurrence of items: " + averageOccValue + " (to be compared with a target r of: " + numberOfAppearances_r);

        float lambdaIdeal = Math.max(1, numberOfAppearances_r * (blockSize_k - 1) / (nbItems_v - 1));

        System.out.println("ideal lambda: " + lambdaIdeal);
        countNbOfCoAppearances(allBlocksAsSets);

        Results results = new Results();

        results.setNbItems_v(nbItems_v);

        results.setNbOfBlocks_b(nbOfBlocks_b);

        results.setActualNbOfBlocks(allBlocksAsSets.size());
        results.setNbItemsPerBlock_k(blockSize_k);

        results.setNumberOfAppearances_r(numberOfAppearances_r);

        results.setTheoreticalNbOfDistinctPairs(theoreticalNumberOfDistinctPairs);

        results.setActualNbOfDuplicatePairs(theoreticalNumberOfDistinctPairs - actualNumberOfDistinctPairs);
        results.setAverageNbOfDistinctPairs(averageTimeADistinctPairAppears);

        results.setMinItemOccurrence(minOccurrenceValue);

        results.setMaxItemOccurrence(maxOccurrenceValue);

        results.setAverageItemOccurrence(averageOccValue);

        results.setLambdaIdeal(lambdaIdeal);

        return results;
    }

    public void countNbOfCoAppearances(Set<Set<T>> comparisons) {
        Set<UnDirectedPair> allUnDirectedPairs = new HashSet();
        for (Set<T> comp : comparisons) {
            FindAllPairs find = new FindAllPairs();
            Set<UnDirectedPair> allUnDirectedPairsFromList = find.getAllUndirectedPairs(comp);
            theoreticalNumberOfDistinctPairs = theoreticalNumberOfDistinctPairs + allUnDirectedPairsFromList.size();
            allUnDirectedPairs.addAll(allUnDirectedPairsFromList);
        }
        System.out.println("there are " + theoreticalNumberOfDistinctPairs + " pairs in the comparisons");
        System.out.println("when we substract the duplicates, there are only " + allUnDirectedPairs.size() + " unique pairs left");
        actualNumberOfDistinctPairs = allUnDirectedPairs.size();
        averageTimeADistinctPairAppears = (float) theoreticalNumberOfDistinctPairs / allUnDirectedPairs.size();
        System.out.println("which means that on average, a pair appears " + averageTimeADistinctPairAppears + " times.");
    }

    public boolean checkGoodNeighbors(List<Set<Set<T>>> goodSetsOfComparisons, Set<Set<T>> candidateSetOfComparisons, int maxNbNeighbors) {
        int nbOfNeighbors = 0;
        for (Set<T> oneCandidateComparison : candidateSetOfComparisons) {
            for (Set<Set<T>> onePastSetOfComparisons : goodSetsOfComparisons) {
                for (Set<T> onePastComparison : onePastSetOfComparisons) {
                    Set<T> common = new HashSet(oneCandidateComparison);
                    common.retainAll(onePastComparison);
                    if (common.size() > 1) {
                        nbOfNeighbors++;
                    }
                }
            }
        }
        return nbOfNeighbors <= maxNbNeighbors;
    }

    public T findCandidateFromLowFrequencyItems(Block blockToFill, Multiset<T> globalCountOfItems, Integer numberOfAppearances_r) {
        if (!blockToFill.isComplete()) {
            List<Map.Entry<T, Integer>> asc = globalCountOfItems.sortAsc(globalCountOfItems);
            for (Map.Entry<T, Integer> entry : asc) {
                T lowFreqItem = entry.getKey();
                if (!blockToFill.isComplete() & !blockToFill.getItems().contains(lowFreqItem) & globalCountOfItems.getCount(lowFreqItem) < numberOfAppearances_r) {
                    return lowFreqItem;
                }
            }
        }
        return null;
    }

    public Block addItemToBlock(T itemToAdd, Block blockToFill, Integer maxNumberOfSamePairsInComparisons_lambda, Integer numberOfAppearances_r) {

        List<T> existingItemsInBlock = new ArrayList();
        existingItemsInBlock.addAll(blockToFill.getItems());

//        List<T> existingItemsInBlockANew = new ArrayList();
//        existingItemsInBlockANew.add(itemToAdd);
//        existingItemsInBlockANew.addAll(existingItemsInBlock);
//        FindAllPairs finder = new FindAllPairs();
//        Set<UnDirectedPair> allUndirectedPairs = finder.getAllUndirectedPairsFromList(existingItemsInBlockANew);
//        pairsAlreadyMade.addAllFromListOrSet(allUndirectedPairs);
        if (existingItemsInBlock.isEmpty()) {
            if (!blockToFill.isComplete() & globalCountOfItems.getCount(itemToAdd) < numberOfAppearances_r) {
                globalCountOfItems.addOne(itemToAdd);
                blockToFill.addItem(itemToAdd);
            }

        } else {

            for (T itemAlreadyInBlock : existingItemsInBlock) {
                UnDirectedPair pair = new UnDirectedPair(itemAlreadyInBlock, itemToAdd);
                Integer countForThisPair = pairsAlreadyMade.getCount(pair);
                if (!blockToFill.isComplete() & countForThisPair < maxNumberOfSamePairsInComparisons_lambda & globalCountOfItems.getCount(itemToAdd) < numberOfAppearances_r) {
                    pairingReservoir.get(itemAlreadyInBlock).add(itemToAdd);
                    pairingReservoir.get(itemToAdd).add(itemAlreadyInBlock);
                    pairsAlreadyMade.addOne(pair);
                    globalCountOfItems.addOne(itemToAdd);
                    blockToFill.addItem(itemToAdd);
                }
            }
        }
        return blockToFill;
    }

    public void removeBlock(Block blockToremove) {

        List<T> existingItemsInBlock = new ArrayList();
        existingItemsInBlock.addAll(blockToremove.getItems());
        FindAllPairs finder = new FindAllPairs();
        Set<UnDirectedPair> allUndirectedPairs = finder.getAllUndirectedPairsFromList(existingItemsInBlock);

        for (UnDirectedPair pair : allUndirectedPairs) {
            pairsAlreadyMade.removeOne(pair);
            pairingReservoir.get((T) pair.getLeft()).remove((T) pair.getRight());
            pairingReservoir.get((T) pair.getRight()).remove((T) pair.getLeft());
        }

        for (T item : existingItemsInBlock) {
            globalCountOfItems.removeOne(item);
        }
    }

}
