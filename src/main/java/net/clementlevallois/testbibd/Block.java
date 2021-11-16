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
 * @param <T>
 */
public class Block<T extends Comparable<? super T>> {
    
    private List<T> items;
    private int blockSize;
    private boolean regular;
    
    public Block(int blockSize){
        items = new ArrayList();
        this.blockSize = blockSize;
        regular = true;
    };
    
    public void addItem(T item){
        if (items.size() == blockSize){
            System.out.println("block was full");
        }else{
            items.add(item);
        }               
    }
    
    public List<T> getItems(){
        return items;
    }
    
    public boolean isComplete(){
        return items.size() == blockSize;
    }

    public boolean isRegular() {
        return regular;
    }

    public void setRegular(boolean regular) {
        this.regular = regular;
    }

    
    
    @Override
    public String toString() {
        String result = "Block{" + "items=" + items + '}';
        if (!regular){
            result = result + "[ <- irregular]";
        }
        return result;
    }

    
    
    
}
