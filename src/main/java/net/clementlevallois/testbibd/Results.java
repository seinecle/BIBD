/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.testbibd;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LEVALLOIS
 */
public class Results<T extends Comparable<? super T>> {

    private List<List<T>> seriesOfBlocks = new ArrayList();
    private int minItemOccurrence;
    private int maxItemOccurrence;
    private float averageItemOccurrence;
    private int theoreticalNbOfDistinctPairs;
    private int actualNbOfDuplicatePairs;
    private float averageNbOfDistinctPairs;
    private float lambdaIdeal;
    private int nbItems_v;
    private int nbOfBlocks_b;
    private int actualNbOfBlocks;
    private Integer nbAnnotators;
    private int numberOfAppearances_r;
    private int nbItemsPerBlock_k;

    public List<List<T>> getSeriesOfBlocks() {
        return seriesOfBlocks;
    }

    public void setSeriesOfBlocks(List<List<T>> seriesOfBlocks) {
        this.seriesOfBlocks = seriesOfBlocks;
    }

    public int getMinItemOccurrence() {
        return minItemOccurrence;
    }

    public void setMinItemOccurrence(int minItemOccurrence) {
        this.minItemOccurrence = minItemOccurrence;
    }

    public int getMaxItemOccurrence() {
        return maxItemOccurrence;
    }

    public void setMaxItemOccurrence(int maxItemOccurrence) {
        this.maxItemOccurrence = maxItemOccurrence;
    }

    public int getTheoreticalNbOfDistinctPairs() {
        return theoreticalNbOfDistinctPairs;
    }

    public void setTheoreticalNbOfDistinctPairs(int theoreticalNbOfDistinctPairs) {
        this.theoreticalNbOfDistinctPairs = theoreticalNbOfDistinctPairs;
    }

    public int getActualNbOfDuplicatePairs() {
        return actualNbOfDuplicatePairs;
    }

    public void setActualNbOfDuplicatePairs(int actualNbOfDuplicatePairs) {
        this.actualNbOfDuplicatePairs = actualNbOfDuplicatePairs;
    }

    public float getLambdaIdeal() {
        return lambdaIdeal;
    }

    public void setLambdaIdeal(float lambdaIdeal) {
        this.lambdaIdeal = lambdaIdeal;
    }

    public int getNbItems_v() {
        return nbItems_v;
    }

    public void setNbItems_v(int nbItems_v) {
        this.nbItems_v = nbItems_v;
    }

    public int getNbOfBlocks_b() {
        return nbOfBlocks_b;
    }

    public void setNbOfBlocks_b(int nbOfBlocks_b) {
        this.nbOfBlocks_b = nbOfBlocks_b;
    }

    public Integer getNbAnnotators() {
        return nbAnnotators;
    }

    public void setNbAnnotators(Integer nbAnnotators) {
        this.nbAnnotators = nbAnnotators;
    }

    public int getNumberOfAppearances_r() {
        return numberOfAppearances_r;
    }

    public void setNumberOfAppearances_r(int numberOfAppearances_r) {
        this.numberOfAppearances_r = numberOfAppearances_r;
    }

    public int getNbItemsPerBlock_k() {
        return nbItemsPerBlock_k;
    }

    public void setNbItemsPerBlock_k(int nbItemsPerBlock_k) {
        this.nbItemsPerBlock_k = nbItemsPerBlock_k;
    }

    public float getAverageNbOfDistinctPairs() {
        return averageNbOfDistinctPairs;
    }

    public void setAverageNbOfDistinctPairs(float averageNbOfDistinctPairs) {
        this.averageNbOfDistinctPairs = averageNbOfDistinctPairs;
    }

    public int getActualNbOfBlocks() {
        return actualNbOfBlocks;
    }

    public void setActualNbOfBlocks(int actualNbOfBlocks) {
        this.actualNbOfBlocks = actualNbOfBlocks;
    }

    public float getAverageItemOccurrence() {
        return averageItemOccurrence;
    }

    public void setAverageItemOccurrence(float averageItemOccurrence) {
        this.averageItemOccurrence = averageItemOccurrence;
    }

}
