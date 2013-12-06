/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rtp;

import algo.LFSR;
import algo.encrypt;
import algo.pool;
import UI.uiReceiver;

import com.sun.media.rtp.RTPSinkStream;
import com.sun.media.rtp.RTPTransmitter;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
    private ekstract Ekstract = null;
    private ByteArrayOutputStream result = new ByteArrayOutputStream();
    private String mode;
    private int flag = 0;
    private boolean isCompleted = false;
    private byte[] encryptedMessage = null;
    private uiReceiver ui = new uiReceiver();
    private BufferedWriter out = new BufferedWriter(new FileWriter("file.txt",true));
    private String word_message="";

    public NewRTPSocket(int port, String message, String _mode) throws IOException {
        super(port);
        isRTP = ((port % 2) == 0);
        Message = message;
        mode = _mode;
        if (message != "") {

            Encrypt = new encrypt(Message);
            encryptedMessage = Encrypt.doEncrypt();
        }
    }

    public NewRTPSocket(InetAddress addr, int port, boolean isRTP, String message, String _mode) throws IOException {
        super(new InetSocketAddress(addr, port));

        this.isRTP = isRTP;
        Message = message;
        mode = _mode;
        if (message != "") {
            Encrypt = new encrypt(Message);
            encryptedMessage = Encrypt.doEncrypt();
        }
    }

    public NewRTPSocket(InetAddress addr, int port, String message, String _mode) throws IOException {
        super(new InetSocketAddress(addr, port));
        isRTP = ((port % 2) == 0);
        Message = message;
        mode = _mode;
        if (message != "") {
            Encrypt = new encrypt(Message);
            encryptedMessage = Encrypt.doEncrypt();
        }
    }

    public NewRTPSocket(int port, boolean isRTP, String message, String _mode) throws IOException {
        super(port);
        this.isRTP = isRTP;
        Message = message;
        mode = _mode;
        if (message != "") {
            Encrypt = new encrypt(Message);
            encryptedMessage = Encrypt.doEncrypt();
        }
    }

    @Override
    public void send(DatagramPacket dp) throws IOException {


        //dp.setData(in);
        super.send(dp);

    }

    @Override
    public void receive(DatagramPacket dp) throws IOException {
        super.receive(dp);
        /*
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        byte[] in = dp.getData();
        int last = 0;
        
        pool Pool = new pool(45678);
        LFSR lfsr = new LFSR(Pool.IS[3]);
        int counter = 0;
        if (in.length == 2048) {
            last = dp.getLength() - 1;
        }

        if (in[last] != 0 && isRTP && last > 100 && mode == "receive" && isCompleted == false) {
            byte sentece_length_byte = in[last];
            byte word_length_byte = in[last - 1];
            int flagLength = (int) in[last - 2];
            for (int x = flagLength; x > 0; x--) {
                temp.write(in[last - 2 - x] ^ lfsr.nextInt());
            }
            int sentence_length = (int) sentece_length_byte;
            int word_length = (int) word_length_byte;
            //System.out.println("length of message : " + word_length);
            Ekstract = new ekstract(temp.toByteArray());
            Stegano = new stegano();
            try {
                for (int i = 50, j = 0; i < 100; i++) {
                    if (Stegano.dataContained(in[i])) {
                        if (Ekstract.getMessage(counter) == true && j != word_length) {
                            result.write(in[i]);
                            j++;
                        } else if (j == word_length) {
                            break;
                        }
                        counter++;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result.size() == word_length && isCompleted == false) {
                try {
                    Encrypt = new encrypt(" ");
                    String m = hex.recoverBynary(Encrypt.doDecrypt(result.toByteArray()));
                   word_message += hex.hexToString(hex.hexToString(m)) + " ";
                   //out.write("halo ini maja");
                   //out.newLine();
                    
                    
                    
                    result = new ByteArrayOutputStream();
                    temp = new ByteArrayOutputStream();
                    counter = 0;
                   // System.out.println(System.currentTimeMillis());
                    if (flag == sentence_length - 1) {
                        System.out.println(word_message);
                        File file = new File("test.txt");
                        FileOutputStream fop = new FileOutputStream(file);
                        if (!file.exists()) {
				file.createNewFile();
			}
                        
 
			// get the content in bytes
			byte[] contentInBytes = word_message.getBytes();
 
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
                        isCompleted = true;
                        
                        //out.close();
                    }
                } catch (Exception ex) {
                    System.out.println("Message couldn't be generated. Either there is a bug or a packet loss during transmission");
                }

                flag++;
                
            }
            
            */
        }
        

    

    class test extends RTPSinkStream {
    }
}
