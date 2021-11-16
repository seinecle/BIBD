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

    /**
     * It specifies the number of items.
     */
    public static int v = 7;
    /**
     * It specifies the number of blocks.
     */
    public static int b = 7;
    /**
     * It specifies the number of times an item should appear, in total.
     */
    public static int r = 3;
    /**
     * The size of the blocks.
     */
    public static int k = 3;
    /**
     * It specifies the number of times any two item can cooccur in blocks.
     */
    public static int lambda = 1;

    public static void main(String[] args) {

        List<Integer> items = IntStream.range(1, v + 1).boxed().collect(Collectors.toList());
        new BIBDCustom().run(items, v, b, r, k, lambda);

    }

    public void run(List<T> items, int nbItems, int nbOfBlocks, int numberOfAppearances, int nbItemsPerBlock, int maxNumberOfSamePairsInComparisons) {

        int countEfforts = 0;
        int countMaxEfforts = 5;
        List<Block> blocksSeries;
        List<List<Block>> goodSeriesOfBlocks = new ArrayList();
//        long startTime = System.currentTimeMillis();
//        long timeElapsed = 0;

        float theoreticalNbRounds = items.size() / nbItemsPerBlock * numberOfAppearances;
        System.out.println();
        System.out.println();
        System.out.println("theoeretical nb of rounds: " + theoreticalNbRounds);
        System.out.println();
        System.out.println();

        Multiset<UnDirectedPair> pairsAlreadyMade = new Multiset();
        Multiset<T> globalCountOfItems = new Multiset();

        // this map helps track which pairs have not been included in blocks yet.
        // at the beginning, all pairs are available: so we fill up the reservoir
        Map<T, Set<T>> pairingReservoir = new HashMap();
        for (T item : items) {
            Set<T> toBePairedWith = new HashSet();
            toBePairedWith.addAll(items);
            toBePairedWith.remove(item);
            pairingReservoir.put(item, toBePairedWith);
        }

        // a series of block is a list of blocks which cover the whole dataset (hopefully, not guaranteed).
        int seriesOfBlockCompleted = 0;
        double seriesToComplete = Math.ceil(b/Math.floor((float)v/(float)k));
        while (seriesOfBlockCompleted < seriesToComplete) {
            Collections.shuffle(items, new Random());
            blocksSeries = new ArrayList();
            int blocksPerSeries = items.size() / nbItemsPerBlock;
            Multiset<T> itemInASeriesAndTheirCount = new Multiset();
            Iterator<T> iteratorItems = items.iterator();
            Block block = new Block(nbItemsPerBlock);

            while (blocksSeries.size() < blocksPerSeries) {
                if (block.isComplete()) {
                    block = new Block(nbItemsPerBlock);
                }
                if (!iteratorItems.hasNext()) {
                    Collections.shuffle(items);
                    countEfforts++;
                    if (countEfforts > countMaxEfforts) {
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
                    iteratorItems = pairingReservoir.keySet().iterator();
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
                            if (!block.isComplete() && countForThisPair < maxNumberOfSamePairsInComparisons & globalCountOfItems.getCount(next) < numberOfAppearances) {
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
                            for (T itemsInBlock : existingItemsInBlock) {
                                pairingReservoir.get(itemsInBlock).remove(itemToAdd);
                                pairingReservoir.get(itemToAdd).remove(itemsInBlock);
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
                    Set<T> toBePaired = pairingReservoir.get(next);
                    if (!toBePaired.isEmpty() & !block.isComplete()) {
                        T itemToPair = pairingReservoir.get(next).iterator().next();
                        if (itemInASeriesAndTheirCount.getCount(itemToPair) == 1 & itemInASeriesAndTheirCount.getElementSet().size() < items.size()) {
                            continue;
                        }
                        if (!existingItemsInBlock.contains(itemToPair)) {
                            boolean addItemToPair = false;
                            T itemToAdd = null;
                            for (T itemInBlock : existingItemsInBlock) {
                                UnDirectedPair pair = new UnDirectedPair(itemInBlock, itemToPair);
                                Integer countForThisPair = pairsAlreadyMade.getCount(pair);
                                if (!block.isComplete() && countForThisPair < maxNumberOfSamePairsInComparisons & globalCountOfItems.getCount(itemToPair) < numberOfAppearances) {
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
                                    pairingReservoir.get(itemToAdd).remove(itemsInBlock);
                                    pairingReservoir.get(itemsInBlock).remove(itemToAdd);
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
        for (List<Block> oneSeriesOfBlocks : goodSeriesOfBlocks) {
            for (Block block : oneSeriesOfBlocks) {
                itemFreqs.addAllFromListOrSet(block.getItems());
            }
            System.out.println("series " + seriesCounter++ + ":");
            System.out.println(oneSeriesOfBlocks.toString());
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
        System.out.println("average occurrence of items: " + averageOccValue + " (to be compared with a target r of: " + r);

        
        
        // creating a first set of blocks from the list of items.
        // just going through the list one item per item, and creating blocks of the size set by the parameter
//        while (j <= items.size() - nbItemsPerBlock) {
//            block = new Block(nbItemsPerBlock);
//            for (int i = 0; i < nbItemsPerBlock; i++) {
//                T item = items.get(j);
//                j++;
//                block.addItem(item);
//            }
//            blocksSeries.add(block);
//        }
//        goodSeriesOfBlocks.add(blocksSeries);
//
//        // now, we use this initial list of blocks and navigate it
//        // with a round robin method
//        // this creates new lists of blocks that include completely unique pairs of items
//        // this is guaranteed by picking items from different blocks to create a new block
//        toBePairedWith.r List
//        <Block > newSeriesOfBlocks = new ArrayList();
//
//        int roundRobinIndex = 0;
//        int extraRoundsOfBlockSeriesDefinitions = 0;
//
//        while (newSeriesOfBlocks.size() < blocksSeries.size()) {
//            block = new Block(nbItemsPerBlock);
//            for (int i = 0; i < nbItemsPerBlock; i++) {
//                int indexNextItem = roundRobinIndex + i * nbItemsPerBlock;
//                if (indexNextItem > items.size() - 1) {
//                    System.out.println("the end of the blocks has been reached");
//                    System.out.println("we get to the beginning of the block series");
//                    roundRobinIndex = 0;
//                    indexNextItem = extraRoundsOfBlockSeriesDefinitions + (roundRobinIndex + i) * nbItemsPerBlock;
//                }
//                T item = items.get(indexNextItem);
//                block.addItem(item);
//            }
//            roundRobinIndex++;
//            newSeriesOfBlocks.add(block);
//        }
//        goodSeriesOfBlocks.add(newSeriesOfBlocks);
//
//        extraRoundsOfBlockSeriesDefinitions++;
//        System.out.println("finished round " + extraRoundsOfBlockSeriesDefinitions);
//
//        int seriesNumber = 1;
//        for (List<Block> oneSeriesOfBlocks : goodSeriesOfBlocks) {
//            System.out.println("series " + seriesNumber++ + ":");
//            System.out.println(oneSeriesOfBlocks.toString());
//        }
    }

    public void countNbOfCoAppearances(Set<Set<T>> comparisons) {
        Set<UnDirectedPair> allUnDirectedPairs = new HashSet();
        int theoreticalNumberOfPairs = 0;
        for (Set<T> comp : comparisons) {
            FindAllPairs find = new FindAllPairs();
            Set<UnDirectedPair> allUnDirectedPairsFromList = find.getAllUndirectedPairs(comp);
            theoreticalNumberOfPairs = theoreticalNumberOfPairs + allUnDirectedPairsFromList.size();
            allUnDirectedPairs.addAll(allUnDirectedPairsFromList);
        }
        System.out.println("there are " + theoreticalNumberOfPairs + " pairs in the comparisons");
        System.out.println("when we substract the duplicates, there are only " + allUnDirectedPairs.size() + " unique pairs left");
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
