/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stegano;

import java.io.IOException;
import java.util.BitSet;
import algo.LFSR;
import algo.pool;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Bagus Trihatmaja
 */
public class stegano {

    private static final int N1 = 3; // threshold minimal
    private static final int N2 = 5; // threshold maksimal
    private BitSet bitSetFlag = new BitSet();
    private int flagIndex = 0;


    public stegano() {
        
    }
    
    public void clearAllFlag()
    {
        bitSetFlag.clear();
    }
    
    public boolean dataContained(byte _data)
    {
        BitSet data = BitSet.valueOf(new byte[]{_data});
        if(data.length() == 0)
            return false;
        else return true;
    }

    public synchronized byte[] getFlag() {
        //byte[] temp = new byte[3];
        byte[] temp1;
        if(bitSetFlag.length()!=0)
        {
            pool p = new pool(45678);
            LFSR lfsr3 = new LFSR(p.IS[3]);
            temp1 = toByteArray(bitSetFlag);
            for(int i=0;i<temp1.length;i++){
                temp1[i] ^= lfsr3.nextInt();
            }
            //System.arraycopy(temp1, 0, temp, 0, temp1.length);
            bitSetFlag.clear();
            return temp1;
        }
        else return null;
    }
    // Returns a bitSet containing the values in bytes.

    public boolean countAPMS(byte M, byte B) throws IOException {
       BitSet M_BitSet = BitSet.valueOf(new byte[]{M});
       BitSet B_BitSet = BitSet.valueOf(new byte[]{B});
       int counter = 0;
       for(int i=0;i<8;i++)
       {
           if(!(M_BitSet.get(i)^B_BitSet.get(i)))
                   counter++;
       }
       if(decision(counter)) {
           bitSetFlag.set(flagIndex);
           flagIndex++;
           System.out.println(bitSetFlag);
          return true;
       }
       else 
       {
           flagIndex++;
           System.out.println(bitSetFlag);
           return false;
       }
       
       
    }

    private static boolean decision(int E) {

        pool p = new pool(45678);
        LFSR lfsr = new LFSR(p.IS[2]);
        if (E < N1) {
            return false;
        } else if (N1 >= E && E < N2) {
            if (lfsr.nextBoolean()) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        //System.out.println("panjang bytes :" + bytes.length);
        return bytes;
    }
    
    public static BitSet fromByteArray(byte[] bytes) {

        BitSet bits = new BitSet();

        for (int i = 0; i < bytes.length * 8; i++) {

            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {

                bits.set(i);

            }

        }

        return bits;

    }
}
