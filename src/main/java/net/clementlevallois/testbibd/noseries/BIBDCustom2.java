/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.testbibd.noseries;

import java.io.Serializable;
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
import net.clementlevallois.functions.model.bibd.Block;
import net.clementlevallois.functions.model.bibd.BIBDResults;
import net.clementlevallois.utils.FindAllPairs;
import net.clementlevallois.utils.Multiset;
import net.clementlevallois.utils.UnDirectedPair;

/**
 *
 * @author LEVALLOIS
 * @param <T>
 */
public class BIBDCustom2 <T extends Comparable<? super T>> implements Serializable {

    boolean printBlocks = true;

    int theoreticalNumberOfDistinctPairs = 0;
    int actualNumberOfDistinctPairs = 0;
    float averageTimeADistinctPairAppears = 0;

    Multiset pairsAlreadyMade = new Multiset();
    Multiset globalCountOfItems = new Multiset();

    // this map helps track which pairs have been included in blocks.
    // keys: each item
    // value for a key: the set of all items the key (item) has been paired with
    // at the beginning, no pairs have been formed: so the values are empty sets
    Map<T, Set<T>> pairingReservoir = new HashMap();
    int lambdaRelaxationFactor = 0;
    int rRelaxationFactor = 0;

    public static void main(String[] args) {

        /**
         * number of items.
         */
        Integer v = 8;
        /**
         * number of blocks. Can be null if parameter r and lambda are non null
         */
        Integer b = 70;
        /**
         * number of times an item should appear, in total. Can be null if
         * parameter b is non null
         */
        Integer r = 35;
        /**
         * size of the blocks.
         */
        Integer k = 4;
        /**
         * number of times any two items should cooccur in blocks. Can be left
         * null if r is set.
         */
        Integer lambda = 15;

        /**
         * number of annotators that will do the task.
         */
        Integer nbAnnotators = null;

        List<Integer> items = IntStream.range(1, v + 1).boxed().collect(Collectors.toList());
        new BIBDCustom2().run(GOAL.R_KNOWN, items, v, b, r, k, lambda);

    }

    public enum GOAL {
        CANONICAL_BIBD, LAMBDA_KNOWN, R_KNOWN
    }

    public BIBDResults run(GOAL goal, List<T> items, Integer nbItems_v, Integer nbOfBlocks_b, Integer numberOfAppearances_r, Integer blockSize_k, Integer maxNumberOfSamePairsInComparisons_lambda) {

        int globalTries = 0;

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
        int countMaxTries = Math.round(nbOfBlocks_b * blockSize_k / nbItems_v) * 50;

        float theoretical_lambda = (float) numberOfAppearances_r * (blockSize_k - 1) / (nbItems_v - 1);
        System.out.println("theoretical lambda: " + theoretical_lambda);

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

                // exceptional regime: verify that we have not tried too many times iterating on items
                if (countTries > countMaxTries) {
                    // no solution has been found. We should relax the constraint and fill in the remaining items.
                    blockToFill.setRegular(false);
                    T candidateItemToAdd = findCandidateFromLowFrequencyItems(blockToFill, globalCountOfItems, numberOfAppearances_r);
                    if (candidateItemToAdd == null) {
                        blockToFill = new Block(blockSize_k);
                        blocks = new ArrayList();
                        pairingReservoir = new HashMap();
                        for (T item : items) {
                            pairingReservoir.put(item, new HashSet());
                        }

                        pairsAlreadyMade = new Multiset();
                        globalCountOfItems = new Multiset();

                        System.out.println("REMOVED ALL BLOCKS TO FIND A BETTER SOLUTION");

                    } else {
                        int blockSize = blockToFill.getItems().size();
                        blockToFill = addItemToBlock(candidateItemToAdd, blockToFill, maxNumberOfSamePairsInComparisons_lambda, numberOfAppearances_r, blocks);

                        // if no item was added to the block, this means we are stuck... we should start again.
                        if (blockToFill.getItems().size() == blockSize) {
                            blockToFill = new Block(blockSize_k);
                            blocks = new ArrayList();
                            pairingReservoir = new HashMap();
                            for (T item : items) {
                                pairingReservoir.put(item, new HashSet());
                            }

                            pairsAlreadyMade = new Multiset();
                            globalCountOfItems = new Multiset();

                            System.out.println("REMOVED ALL BLOCKS TO FIND A BETTER SOLUTION");
                            globalTries++;
                            if (globalTries % 100 == 0) {
                                lambdaRelaxationFactor++;
                            }
                            if (globalTries % 5_000 == 0) {
                                rRelaxationFactor++;
                            }
                        }
                    }
                    countTries = 0;
                }
                Collections.shuffle(items, new Random());
                iteratorItems = items.iterator();
            }

            // normal regime: we examine each item to see if we can add it to the current block
            while (iteratorItems.hasNext() & !blockToFill.isComplete()) {
                T candidateItem = iteratorItems.next();

                List<T> existingItemsInBlock = blockToFill.getItems();

                // if the item is not in the block yet, consider the item
                if (!existingItemsInBlock.contains(candidateItem)) {
                    // if a number of conditions are met, add the item to the block
                    blockToFill = addItemToBlock(candidateItem, blockToFill, maxNumberOfSamePairsInComparisons_lambda, numberOfAppearances_r, blocks);
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
            if (printBlocks) {
                System.out.println(completedBlock.toString());
            }
            itemFreqs.addAllFromListOrSet(completedBlock.getItems());
            oneBlockAsSet = new HashSet(completedBlock.getItems());
            allBlocksAsSets.add(oneBlockAsSet);
        }

        System.out.println("----------------");
        System.out.println("pairsAlreadyMade: " + pairsAlreadyMade.getSize());
        System.out.println("pairsAlreadyMade element count: " + pairsAlreadyMade.getElementSet().size());
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

        BIBDResults results = new BIBDResults();

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
        System.out.println("there are " + theoreticalNumberOfDistinctPairs + " pairs in the blocks");
        System.out.println("when we substract the duplicates, there are only " + allUnDirectedPairs.size() + " unique pairs left");
        actualNumberOfDistinctPairs = allUnDirectedPairs.size();
        averageTimeADistinctPairAppears = (float) theoreticalNumberOfDistinctPairs / allUnDirectedPairs.size();
        System.out.println("which means that on average, a pair appears " + averageTimeADistinctPairAppears + " times.");
    }

    public T findCandidateFromLowFrequencyItems(Block blockToFill, Multiset globalCountOfItems, Integer numberOfAppearances_r) {
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

    public Block addItemToBlock(T itemToAdd, Block blockToFill, Integer maxNumberOfSamePairsInComparisons_lambda, Integer numberOfAppearances_r, List<Block> blocks) {

        List<T> existingItemsInBlock = new ArrayList();
        existingItemsInBlock.addAll(blockToFill.getItems());

//        List<T> existingItemsInBlockANew = new ArrayList();
//        existingItemsInBlockANew.add(itemToAdd);
//        existingItemsInBlockANew.addAll(existingItemsInBlock);
//        FindAllPairs finder = new FindAllPairs();
//        Set<UnDirectedPair> allUndirectedPairs = finder.getAllUndirectedPairsFromList(existingItemsInBlockANew);
//        pairsAlreadyMade.addAllFromListOrSet(allUndirectedPairs);
        if (existingItemsInBlock.isEmpty()) {
            if (!blockToFill.isComplete() & globalCountOfItems.getCount(itemToAdd) < (numberOfAppearances_r + rRelaxationFactor)) {
                globalCountOfItems.addOne(itemToAdd);
                blockToFill.addItem(itemToAdd);
            }

        } else if (!blockToFill.isComplete() & globalCountOfItems.getCount(itemToAdd) < (numberOfAppearances_r + rRelaxationFactor)) {

            FindAllPairs find = new FindAllPairs();
            Set<T> blockIfItemAdded = new HashSet();
            blockIfItemAdded.addAll(blockToFill.getItems());
            blockIfItemAdded.add(itemToAdd);
            Set<UnDirectedPair> allUnDirectedPairsIfItemAdded = find.getAllUndirectedPairs(blockIfItemAdded);
            boolean adequate = true;

            for (UnDirectedPair blockPair : allUnDirectedPairsIfItemAdded) {
                Comparable<T> other = blockPair.getOther(itemToAdd);
                if (other != null) {
                    Integer countForThisPair = pairsAlreadyMade.getCount(blockPair);
                    if (countForThisPair >= (maxNumberOfSamePairsInComparisons_lambda + lambdaRelaxationFactor)) {
                        adequate = false;
                    }
                }
            }
            if (adequate) {
                blockToFill.addItem(itemToAdd);
                globalCountOfItems.addOne(itemToAdd);
                for (UnDirectedPair blockPair : allUnDirectedPairsIfItemAdded) {
                    Comparable<T> other = blockPair.getOther(itemToAdd);
                    if (other != null) {
                        pairingReservoir.get((T) other).add(itemToAdd);
                        pairingReservoir.get(itemToAdd).add((T) other);
                        pairsAlreadyMade.addOne(blockPair);
                    }
                }

            }

//            boolean addedToBlock = false;
//            for (T itemAlreadyInBlock : existingItemsInBlock) {
//                UnDirectedPair pair = new UnDirectedPair(itemAlreadyInBlock, itemToAdd);
//                Integer countForThisPair = pairsAlreadyMade.getCount(pair);
//                if (!blockToFill.isComplete()
//                        & countForThisPair < maxNumberOfSamePairsInComparisons_lambda
//                        & globalCountOfItems.getCount(itemToAdd) < numberOfAppearances_r) {
//                    System.out.println("----------------");
//                    System.out.println("count for this pair (" + itemAlreadyInBlock + ", " + itemToAdd + "): " + countForThisPair);
//
//                    System.out.println("all blocks so far:");
//                    for (Block completedBlock : blocks) {
//                        System.out.println(completedBlock.toString());
//                    }
//
//                    System.out.println("current block before addition: " + blockToFill.toString());
//                    System.out.println("candidate: " + itemToAdd);
//                    System.out.println("pairs already made: " + pairsAlreadyMade.getInternalMap().toString());
//                    System.out.println("----------------");
//                    blockToFill.addItem(itemToAdd);
//                    globalCountOfItems.addOne(itemToAdd);
//                    addedToBlock = true;
//                    System.out.println("pair count: " + pairsAlreadyMade.getCount(pair));
//                    for (UnDirectedPair pairCheck : pairsAlreadyMade.getElementSet()) {
//                        if (pairsAlreadyMade.getCount(pairCheck) > maxNumberOfSamePairsInComparisons_lambda) {
//                            System.out.println("stop");
//                        }
//                    }
//                    break;
//
//                }
//            }
//            if (addedToBlock) {
//                FindAllPairs find = new FindAllPairs();
//                Set<UnDirectedPair> allUnDirectedPairsFromList = find.getAllUndirectedPairsFromList(blockToFill.getItems());
//                for (UnDirectedPair blockPair : allUnDirectedPairsFromList) {
//                    Comparable<T> other = blockPair.getOther(itemToAdd);
//                    if (other != null) {
//                        pairingReservoir.get((T) other).add(itemToAdd);
//                        pairingReservoir.get(itemToAdd).add((T) other);
//                        pairsAlreadyMade.addOne(blockPair);
//                    }
//                }
//            }
//
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
