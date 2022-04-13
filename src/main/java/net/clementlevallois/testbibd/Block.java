/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.testbibd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author LEVALLOIS
 * @param <T>
 */
public class Block<T extends Comparable<? super T>> {
    
    private List<T> items;
    private final int blockSize;
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
            Collections.sort(items);
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
    public int hashCode() {
        int hash = 5;
        for (T item: items){
            hash = hash+item.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Block<?> other = (Block<?>) obj;
        return Objects.equals(this.toString(), other.toString());
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
