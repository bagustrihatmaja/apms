/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rtp;

import algo.LFSR;
import algo.encrypt;
import algo.pool;

import com.sun.media.rtp.RTPSinkStream;
import com.sun.media.rtp.RTPTransmitter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import javax.media.rtp.RTPHeader;
import stegano.ekstract;
import stegano.stegano;
import util.Converter;

/**
 *
 * @author
 */
public class NewRTPSocket extends MulticastSocket {

    private boolean isRTP = true;
    private String Message = null;
    private Converter hex = new Converter();
    private encrypt Encrypt = null;
    private stegano Stegano = null;
    private int byteEmbedded = 0;
    private String[] temp = null;
    private int words = 0;
    private ekstract Ekstract = null;
    private int mLength = 2;
    //private byte[] result = new byte[49];
    private String mode;
    private byte[][] encryptedMessage = new byte[10][];
    private ByteArrayOutputStream result = new ByteArrayOutputStream();
    private boolean isCompleted = false;

    public NewRTPSocket(int port, String message, String _mode) throws IOException {
        super(port);
        isRTP = ((port % 2) == 0);
        Message = message;
        mode = _mode;
        if (message != "") {
            temp = message.split(" ");
            for (int i = 0; i < temp.length; i++) {
                Encrypt = new encrypt(temp[i]);
                encryptedMessage[i] = Encrypt.doEncrypt();
            }
        }
    }

    public NewRTPSocket(InetAddress addr, int port, boolean isRTP, String message, String _mode) throws IOException {
        super(new InetSocketAddress(addr, port));
        this.isRTP = isRTP;
        Message = message;
        mode = _mode;
        if (message != "") {
            temp = message.split(" ");
            for (int i = 0; i < temp.length; i++) {
                Encrypt = new encrypt(temp[i]);
                encryptedMessage[i] = Encrypt.doEncrypt();
            }
        }
    }

    public NewRTPSocket(InetAddress addr, int port, String message, String _mode) throws IOException {
        super(new InetSocketAddress(addr, port));
        isRTP = ((port % 2) == 0);
        Message = message;
        mode = _mode;
       if (message != "") {
            temp = message.split(" ");
            for (int i = 0; i < temp.length; i++) {
                Encrypt = new encrypt(temp[i]);
                encryptedMessage[i] = Encrypt.doEncrypt();
            }
        }
    }

    public NewRTPSocket(int port, boolean isRTP, String message, String _mode) throws IOException {
        super(port);
        this.isRTP = isRTP;
        Message = message;
        mode = _mode;
        if (message != "") {
            temp = message.split(" ");
            for (int i = 0; i < temp.length; i++) {
                Encrypt = new encrypt(temp[i]);
                encryptedMessage[i] = Encrypt.doEncrypt();
            }
        }
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void send(DatagramPacket dp) throws IOException {

        
        byte[] in = dp.getData();
        byte[] data;
        int flagPerPacket = 0;
        int x = 0;
        System.out.println("length initial : " + in.length);
        if (Message != "") {

            if (isRTP && words < temp.length) {

                Stegano = new stegano();

                for (int i = 50; i < 100; i++) {
                    if (byteEmbedded < encryptedMessage[words].length && flagPerPacket <= 24) {
                        if (Stegano.dataContained(in[i])) {
                            if (Stegano.countAPMS(encryptedMessage[words][byteEmbedded], in[i]) == true) {
                                in[i] = encryptedMessage[words][byteEmbedded];

                                byteEmbedded++;
                            }
                        }
                        flagPerPacket++;
                    } else {
                        break;

                    }

                }

                byte[] flag;
                byte[] combined = null;
                byte[] flagLength = null;
                byte[] length_message = new byte[]{(byte) byteEmbedded};
                byte[] length_AllMessage = new byte[]{(byte)temp.length};
                System.out.println("byte embededd: " + (byte) byteEmbedded);
                //System.out.println("testing: " + (int)length[0]); 
                if ((flag = Stegano.getFlag()) != null) {
                    //flag = Stegano.getFlag();
                    flagLength = new byte[]{(byte) flag.length};
                    combined = new byte[in.length + flag.length + flagLength.length + length_message.length + length_AllMessage.length];
                    System.arraycopy(in, 0, combined, 0, in.length);
                    System.arraycopy(flag, 0, combined, in.length, flag.length);
                    System.arraycopy(flagLength, 0, combined, in.length + flag.length, flagLength.length);
                    System.arraycopy(length_message, 0, combined, in.length + flag.length + flagLength.length, length_message.length);
                    System.arraycopy(length_AllMessage, 0, combined, in.length + flag.length + flagLength.length + length_message.length, length_AllMessage.length);
                    dp.setData(combined);
                    dp.setLength(combined.length);
                    byteEmbedded = 0;
                    words += 1;

                } else {
                    flag = new byte[]{0};
                    combined = new byte[in.length + flag.length];
                    System.arraycopy(in, 0, combined, 0, in.length);
                    System.arraycopy(flag, 0, combined, in.length, flag.length);
                    dp.setData(combined);
                    dp.setLength(combined.length);
                }
                //System.out.println("Times : " + System.currentTimeMillis());

            }
            System.out.println("Times : " + System.currentTimeMillis());
        }
        
        //dp.setData(in);
        super.send(dp);

    }

    @Override
    public void receive(DatagramPacket dp) throws IOException {
        super.receive(dp);
        
    }

    class test extends RTPSinkStream {
    }
}
