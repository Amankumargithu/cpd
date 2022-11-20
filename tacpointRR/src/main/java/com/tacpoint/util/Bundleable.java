package com.tacpoint.util;

import java.util.*;

public interface Bundleable {


    public Comparator comparator();
    
    public void print();

    public Object clone();
    
//    public boolean isEquivalentTo(Bundleable b);    

    public boolean isOkToAdd();
    
    public float getRank();
    
    public void setRank(float rank);

}