/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algo;

import util.Converter;
import java.util.BitSet;

/**
 *
 * @author BagusTrihatmaja
 */
public class encrypt {

    private LFSR lfsr;
    public final static pool p = new pool(45678);
    private Converter hex = new Converter();
    private byte[] fromHexa;
    private String Message;
    // private boolean[] encrypted ;

    public encrypt(String _Message) {
        Message = _Message;
    }

    public final int getMessageLength() {
        if (fromHexa.length != 0) {
            return fromHexa.length * 8;
        } else {
            return 0;
        }
    }

    public final byte[] doEncrypt() {

        String hexString = hex.toHex(Message);
        int i = 0;
        fromHexa = hex.toBinary(hexString);
        
       
        lfsr = new LFSR(p.IS[1]);
        //BitSet bitSetMessage = new BitSet();
        byte[] bitSetEncrypted = new byte[fromHexa.length];
        //bitSetMessage = fromByteArray(fromHexa);
        
        int next;
        for (i = 0; i < fromHexa.length; i++) {
            next = lfsr.nextInt(); 
            bitSetEncrypted[i] = (byte) (fromHexa[i] ^ next);
        }        
        return bitSetEncrypted;

        /*
        for(int i=0;i<s.length;i++)
        {
        System.out.println((char)(j[i] ^ save[i]));
        }
         * */

    }
    
    public final byte[] doDecrypt(byte[] data)
    {
        lfsr = new LFSR(p.IS[1]);
        //BitSet bitSetMessage = new BitSet();
        byte[] bitSetEncrypted = data;
        //bitSetMessage = fromByteArray(fromHexa);
        int i=0;
        int next;
        for (i = 0; i < data.length; i++) {
            next = lfsr.nextInt(); 
            bitSetEncrypted[i] = (byte) (bitSetEncrypted[i] ^ next);
        }        
        return bitSetEncrypted;
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

    private static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }
}
//for decrypt
//hexString = hex.recoverBynary(hex.toBinary(hexString));
// System.out.println("Hexa from binary: " + hexString);
// System.out.println("Recover text:" + hex.hexToString(hex.hexToString(hexString)));