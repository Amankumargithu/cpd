/**
 * Copyright 2000 Tacpoint Technologies, Inc.
 * All rights resebundleed.
 *
 * @author  Tayze MacKenzie
 * @author tmackenzie@tacpoint.com
 * @version 1.0
 * Date created: 10/30/2000
 * Date modified:
 * - 10/30/2000 Initial version
 *
 *
 */

package com.tacpoint.util;

import java.util.*;
import com.tacpoint.exception.*;

public class Bundle extends Vector
{
    
    public Bundle() {
        
        
    }

    public Object clone() {
        
        Bundle tempBundle = new Bundle();
        
        for (int i=0; i<this.size(); i++) {
            
            Bundleable element = (Bundleable)this.elementAt(i);
            
            tempBundle.addElement(element.clone());       
            
        }   
        
        return tempBundle;
    }

// sort

    public void sortAscending(Comparator comp) {

        boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
            
        Bundleable newArray[] = new Bundleable[this.size()];
            
        // build array
        for (int i=0; i<this.size(); i++) {
            
            newArray[i] = (Bundleable)this.elementAt(i);            
            
        }   
        
        // sort array
        java.util.Arrays.sort(newArray, comp);
        
        this.clear();
            
        // build bundle from sorted array
        for (int i=newArray.length-1; i>=0; i--) {
            
            this.add(newArray[i]);

        }
  
    }

    public void sortDecending(Comparator comp) {

        boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
            
        Bundleable newArray[] = new Bundleable[this.size()];
            
        // build array
        for (int i=0; i<this.size(); i++) {
            
            newArray[i] = (Bundleable)this.elementAt(i);            
            
        }   
        
        // sort array
        java.util.Arrays.sort(newArray, comp);
        
        this.clear();
            
        // build bundle from sorted array
        for (int i=0; i<newArray.length; i++) {
            
            this.add(newArray[i]);

        }
  
    }

// sort aggregate (based on some other key)

    public void sortLike(Bundle sortedBundle) {

        Bundle tempBundle = (Bundle)this.clone(); 
        
        this.clear();
        
        for (int i=0; i<sortedBundle.size(); i++) {
            
            if (tempBundle.contains((Bundleable)sortedBundle.elementAt(i))) {
                
                this.addElement(tempBundle.findElement((Bundleable)sortedBundle.elementAt(i)));
                
            }
            
        }   
        
    }

    public Bundleable findElement(Bundleable bundleableElement) {
        
        for (int i=0; i<this.size(); i++) {
            
            if (this.elementAt(i).equals(bundleableElement))       
                return (Bundleable)this.elementAt(i);
        }
        
        return null;
        
    }


// find top x elements

    public Bundle limitToTopX(int x, Comparator comp) {

        boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
    
        Bundle tempBundle = (Bundle)this.clone();

        Logger.debug("sortDecending", doLog);
        
        tempBundle.sortDecending(comp);
        
        Bundle truncatedBundle = new Bundle();
            
//        if (tempBundle.size() < x)
//            x = tempBundle.size();
        

        Logger.debug("rebuild", doLog);

        float previousPercent = 0f;
        int numResults = 0;
                  
        for (int i=0; i<tempBundle.size(); i++) {
            
            Bundleable element = (Bundleable)tempBundle.elementAt(i);


            if (element.isOkToAdd()) {

                if (numResults >= x) {
                    if (previousPercent == element.getRank()) {
                        truncatedBundle.addElement(element);   
                        numResults++;
                    }
                }
                else {
                    truncatedBundle.addElement(element);   
                    previousPercent = element.getRank();
                    numResults++;
                }

        
            }
            
                   
        }

/*
        if (x < tempBundle.size())
        
            Bundleable element1 = (Bundleable)tempBundle.elementAt(x-1);
            Bundleable element2 = (Bundleable)tempBundle.elementAt(x);
            
            while (element1.isEquivalentTo(element2))
        
        for (int i=x; i<x; i++) {
            
            Bundleable element = (Bundleable)tempBundle.elementAt(i);

            if (!element.isOkToAdd())
                truncatedBundle.addElement(element);   
        
        
        }
*/             
            
        return truncatedBundle;
        
    }

// create aggregate from x Bundles

    public static Bundle createAggregate(Vector bundleVector) {
        
        Bundle newBundle = new Bundle();
            
        for (int i=0; i<bundleVector.size(); i++) {
            
            Logger.log("-- Adding from vector " + i);
            
            Bundle tempBundle = (Bundle)bundleVector.elementAt(i);
            
            for (int j=0; j<tempBundle.size(); j++) {
            
                Bundleable newBundleable = (Bundleable)tempBundle.elementAt(j);
                
                if (!newBundle.contains(newBundleable)) {

                    newBundleable.print();
                    
                    newBundle.addElement(newBundleable);   
                    
                }   
            
            }
            
        }    
            
        return newBundle;
        
    }
    
    public void appendOther(float total, Bundleable bundleable) {
        
        float sum = 0f;

        Bundleable newBundleable = (Bundleable)bundleable.clone();

        for (int i=0; i<this.size(); i++)
            sum = sum + ((Bundleable)this.elementAt(i)).getRank();
        
        Logger.log("total = " + total + ", sum = " + sum);
        
        newBundleable.setRank((float)total - (float)sum);
        
        this.addElement(newBundleable);
                
    }
    
    public float getTotal() {
        
        float sum = 0f;

        for (int i=0; i<this.size(); i++)
            sum = sum + ((Bundleable)this.elementAt(i)).getRank();
        
        return (float)sum;      
          
    }
    
    
    public void print() {
        
        for (int i=0; i<this.size(); i++)
            ((Bundleable)elementAt(i)).print();
    
    }

}
