/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stegano;

import algo.LFSR;
import util.Converter;
import java.util.BitSet;
import algo.pool;

/**
 *
 * @author Bagus Trihatmaja
 */
public class ekstract {

    private int Length = 0;
    private BitSet StegFlag = new BitSet();
    private BitSet Message = new BitSet();
    private BitSet Audio = new BitSet();
    private pool p = new pool(45678);
    private LFSR lfsr;
    private Converter hex = new Converter();
    private byte[] messageByte;
    private static int mLength = 16; //anggep aja gitu dulu sebelum ada header

    public ekstract(byte[] flag) {
        StegFlag = fromByteArray(flag);

        // Length = read.getNumBytes();      
        // _steganoSound = new byte[Length];

        lfsr = new LFSR(p.IS[1]);
        //Audio = fromByteArray(data);
        //getMessage();

    }

    public boolean getMessage(int i) {
       // System.out.println(i + " : " + StegFlag);
        if (StegFlag.length()!=0) {
            if (StegFlag.get(i)) {
              
                return true;
            } else {
                
                return false;
            }
        }
        else return false;
    }

    private static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
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
