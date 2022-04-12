/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.testbibd;

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
import net.clementlevallois.utils.FindAllPairs;
import net.clementlevallois.utils.Multiset;
import net.clementlevallois.utils.UnDirectedPair;

/**
 *
 * @author LEVALLOIS
 * @param <T>
 */
public class BIBDCustom<T extends Comparable<? super T>> {

    int theoreticalNumberOfDistinctPairs = 0;
    int actualNumberOfDistinctPairs = 0;
    float averageTimeADistinctPairAppears = 0;

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
         * number of times an item should appear, in total. Can be null if parameter b is non null 
         */
        Integer r = 7;
        /**
         * size of the blocks.
         */
        Integer k = 4;
        /**
         * number of times any two items should cooccur in blocks. Can be left null if r is set.
         */
        Integer lambda = null;

        /**
         * number of annotators that will do the task.
         */
        Integer nbAnnotators = null;

        List<Integer> items = IntStream.range(1, v + 1).boxed().collect(Collectors.toList());
        new BIBDCustom().run(items, v, b, nbAnnotators, r, k, lambda);

    }
    
    public Results run(List<T> items, Integer nbItems_v, Integer nbOfBlocks_b, Integer nbAnnotators, Integer numberOfAppearances_r, Integer blockSize_k, Integer maxNumberOfSamePairsInComparisons_lambda) {

        int countTries = 0;
        int countMaxTries = 5;
        List<Block> blocksSeries;
        List<List<Block>> goodSeriesOfBlocks = new ArrayList();

        if (nbOfBlocks_b == null & numberOfAppearances_r == null) {
            System.out.println("parameter b or r can be null but not both at the same time");
            System.exit(-1);
        }
        
        

//        long startTime = System.currentTimeMillis();
//        long timeElapsed = 0;
        float theoreticalNbRounds = items.size() / blockSize_k * numberOfAppearances_r;
        System.out.println();
        System.out.println();
        System.out.println("theoeretical nb of rounds: " + theoreticalNbRounds);
        System.out.println();
        System.out.println();

        Multiset<UnDirectedPair> pairsAlreadyMade = new Multiset();
        Multiset<T> globalCountOfItems = new Multiset();

        // this map helps track which pairs have been included in blocks.
        // keys: each item
        // value for a key: the set of all items the key (item) has been paired with
        // at the beginning, no pairs have been formed: so the values are empty sets
        Map<T, Set<T>> pairingReservoir = new HashMap();

        for (T item : items) {
            pairingReservoir.put(item, new HashSet());
        }

        // a series of block is a list of blocks which cover the whole dataset (hopefully, but not guaranteed).
        // we create as many series as there are annotators
        // this diverges from the traditional BIBD problem
        // to keep with the canonical BIBD, the nb of Annotators should be left null
        // it will set the nb of Series to 
        int seriesOfBlockCompleted = 0;
        double seriesToComplete;
        if (nbAnnotators != null) {
            seriesToComplete = nbAnnotators;
        } else {

            // if the nb of Annotators
            seriesToComplete = Math.ceil(nbOfBlocks_b / Math.floor((float) nbItems_v / (float) blockSize_k));
        }
        int loopsCounter = 0;
        while (seriesOfBlockCompleted < seriesToComplete) {
            System.out.println("loops counter: " + loopsCounter++);
            Collections.shuffle(items, new Random());
            blocksSeries = new ArrayList();
            Integer blocksPerSeries;
            if (nbAnnotators != null) {
                blocksPerSeries = ((Double) Math.ceil((double) nbOfBlocks_b / nbAnnotators)).intValue();
            } else {
                blocksPerSeries = items.size() / blockSize_k;
            }
            Multiset<T> itemInASeriesAndTheirCount = new Multiset();
            Iterator<T> iteratorItems = items.iterator();
            Block block = new Block(blockSize_k);

            while (blocksSeries.size() < blocksPerSeries) {
                if (block.isComplete()) {
                    block = new Block(blockSize_k);
                }
                if (!iteratorItems.hasNext()) {
                    Collections.shuffle(items);
                    countTries++;
                    if (countTries > countMaxTries) {
                        // no solution has been found. We should relax the constraint and fill in the reminding items.
                        block.setRegular(false);
                        while (!block.isComplete()) {
                            List<Map.Entry<T, Integer>> asc = globalCountOfItems.sortAsc(globalCountOfItems);
                            T lowFreqItem = asc.get(0).getKey();
                            if (!block.getItems().contains(lowFreqItem)) {
                                block.addItem(lowFreqItem);
                                globalCountOfItems.addOne(lowFreqItem);
                            }
                        }
                    }
                    iteratorItems = items.iterator();
                }
                while (iteratorItems.hasNext() & !block.isComplete()) {
                    T next = iteratorItems.next();
                    if (itemInASeriesAndTheirCount.getCount(next) == 1 & itemInASeriesAndTheirCount.getElementSet().size() < items.size()) {
                        continue;
                    }
                    List<T> existingItemsInBlock = block.getItems();
                    if (!existingItemsInBlock.contains(next)) {
                        boolean addNext = false;
                        T itemToAdd = null;
                        for (T itemInBlock : existingItemsInBlock) {
                            UnDirectedPair pair = new UnDirectedPair(itemInBlock, next);
                            Integer countForThisPair = pairsAlreadyMade.getCount(pair);
                            if (!block.isComplete() && countForThisPair < maxNumberOfSamePairsInComparisons_lambda & globalCountOfItems.getCount(next) < numberOfAppearances_r) {
                                addNext = true;
                                itemToAdd = next;
                            } else {
                                addNext = false;
                                break;
                            }
                        }
                        if (addNext) {
                            List<T> existingItemsInBlockANew = new ArrayList();
                            existingItemsInBlockANew.add(itemToAdd);
                            existingItemsInBlockANew.addAll(existingItemsInBlock);
                            FindAllPairs finder = new FindAllPairs();
                            Set<UnDirectedPair> allUndirectedPairs = finder.getAllUndirectedPairsFromList(existingItemsInBlockANew);
                            pairsAlreadyMade.addAllFromListOrSet(allUndirectedPairs);
                            for (T itemAlreadyInBlock : existingItemsInBlock) {
//                                pairingReservoir.get(itemsInBlock).remove(itemToAdd);
//                                pairingReservoir.get(itemToAdd).remove(itemsInBlock);
                                pairingReservoir.get(itemAlreadyInBlock).add(itemToAdd);
                                pairingReservoir.get(itemToAdd).add(itemAlreadyInBlock);
                            }
                            itemInASeriesAndTheirCount.addOne(next);
                            globalCountOfItems.addOne(next);
                            block.addItem(next);
                        }
                        if (existingItemsInBlock.isEmpty()) {
                            block.addItem(next);
                            itemInASeriesAndTheirCount.addOne(next);
                            globalCountOfItems.addOne(next);
                        }
                    }

                    // the items to be paired with the current item are:
                    // the list of all items
                    // minus the items already paired with the item
                    Set<T> toBePaired = new HashSet(items);
                    toBePaired.removeAll(pairingReservoir.get(next));

                    if (!toBePaired.isEmpty() & !block.isComplete()) {
                        T itemToPair = toBePaired.iterator().next();
                        if (itemInASeriesAndTheirCount.getCount(itemToPair) == 1 & itemInASeriesAndTheirCount.getElementSet().size() < items.size()) {
                            continue;
                        }
                        if (!existingItemsInBlock.contains(itemToPair)) {
                            boolean addItemToPair = false;
                            T itemToAdd = null;
                            for (T itemInBlock : existingItemsInBlock) {
                                UnDirectedPair pair = new UnDirectedPair(itemInBlock, itemToPair);
                                Integer countForThisPair = pairsAlreadyMade.getCount(pair);
                                if (!block.isComplete() && countForThisPair < maxNumberOfSamePairsInComparisons_lambda & globalCountOfItems.getCount(itemToPair) < numberOfAppearances_r) {
                                    addItemToPair = true;
                                    itemToAdd = itemToPair;
                                } else {
                                    addItemToPair = false;
                                    break;
                                }

                            }
                            if (addItemToPair) {
                                List<T> existingItemsInBlockANew = new ArrayList();
                                existingItemsInBlockANew.add(itemToAdd);
                                existingItemsInBlockANew.addAll(existingItemsInBlock);
                                FindAllPairs finder = new FindAllPairs();
                                Set<UnDirectedPair> allUndirectedPairs = finder.getAllUndirectedPairsFromList(existingItemsInBlockANew);
                                pairsAlreadyMade.addAllFromListOrSet(allUndirectedPairs);
                                for (T itemsInBlock : existingItemsInBlock) {
                                    pairingReservoir.get(itemToAdd).add(itemsInBlock);
                                    pairingReservoir.get(itemsInBlock).add(itemToAdd);
                                }
                                itemInASeriesAndTheirCount.addOne(itemToPair);
                                globalCountOfItems.addOne(itemToPair);
                                block.addItem(itemToPair);
                            }

                        }
                    }
                }
                if (block.isComplete()) {
                    blocksSeries.add(block);
                }
            }
            goodSeriesOfBlocks.add(blocksSeries);
//            System.out.println(blocksSeries.toString());
            seriesOfBlockCompleted++;
        }
        int seriesCounter = 1;
        Multiset<T> itemFreqs = new Multiset();
        Set<Set<T>> allBlocksAsSets = new HashSet();
        Set<T> oneBlockAsSet;
        for (List<Block> oneSeriesOfBlocks : goodSeriesOfBlocks) {
            for (Block block : oneSeriesOfBlocks) {
                itemFreqs.addAllFromListOrSet(block.getItems());
                oneBlockAsSet = new HashSet(block.getItems());
                allBlocksAsSets.add(oneBlockAsSet);
            }
            System.out.println("series " + seriesCounter++ + ":");
//            System.out.println(oneSeriesOfBlocks.toString());
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
        results.setSeriesOfBlocks(goodSeriesOfBlocks);
        results.setNbAnnotators(nbAnnotators);
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
}
