/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algo;

import algo.MTRandom;

/**
 *
 * @author BagusTrihatmaja
 */

public class pool {
    private int _seed;
    private final static int MAX = 3;
    private int randomIS;
    private MTRandom mtr;
    public int[] IS = new int[MAX+1];
    
    public pool(int seed)
    {
        _seed = seed;
        mtr = new MTRandom(_seed);
        for(int i = 1;i<=MAX;i++) IS[i] = setIS();
    }
    
    private int setIS()
    {
        return mtr.next(_seed);
    }
    
    public int getIS(int i)
    {
        return IS[i];
    }
    
}
