/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

/**
 *
 * @author BagusTrihatmaja
 */
public class Converter {

    public Converter() {
    }

    public String hexToString(String txtInHex) {
        byte[] txtInByte = new byte[txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2) {
            txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }

    public String toHex(String arg) {
        return String.format("%02x", new BigInteger(arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    public byte[] toBinary(String arg) {
        byte[] bytes = arg.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }

        }
        return bytes;
    }

    public String recoverBynary(byte[] arg) {
       
        return String.format("%02x", new BigInteger(arg));

    }
}
