/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rtp_receiver;

import UI.uiReceiver;
import com.sun.media.rtp.RTPTransmitter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.RTPConnector;

/**
 *
 * @author Bagus Trihatmaja
 */
public class RTPSocketAdapterReceiver implements RTPConnector {

    private NewRTPSocketReceiver dataSock;
    private NewRTPSocketReceiver ctrlSock;
    private InetAddress addr;
    private int port;
    SockInputStream dataInStrm = null, ctrlInStrm = null;
    SockOutputStream dataOutStrm = null, ctrlOutStrm = null;

    public RTPSocketAdapterReceiver(InetAddress addr, int port, String Message, String _mode) throws IOException {
        this(addr, port, 1, Message, _mode );
    }

    public RTPSocketAdapterReceiver(InetAddress addr, int port, int ttl, String Message, String _mode) throws IOException {

        this.addr = addr;
        this.port = port;

        try {
            dataSock = new NewRTPSocketReceiver(port, Message,_mode);
            ctrlSock = new NewRTPSocketReceiver(port + 1, Message,_mode);

        } catch (SocketException e) {
            throw new IOException(e.getMessage());
        }

        dataSock.send(
                new DatagramPacket(new byte[100], 100, addr, port));
        ctrlSock.send(
                new DatagramPacket(new byte[100], 100, addr, port
                + 1));
    }

    @Override
    public PushSourceStream getDataInputStream() throws IOException {
        if (dataInStrm == null) {
            dataInStrm = new SockInputStream(dataSock, addr, port);
            dataInStrm.start();
        }
        return dataInStrm;
    }

    @Override
    public OutputDataStream getDataOutputStream() throws IOException {
        if (dataOutStrm == null) {
            dataOutStrm = new SockOutputStream(dataSock, addr, port);
            
        }
        
        return dataOutStrm;
    }

    @Override
    public PushSourceStream getControlInputStream() throws IOException {
        if (ctrlInStrm == null) {
            ctrlInStrm = new SockInputStream(ctrlSock, addr, port + 1);
            ctrlInStrm.start();
        }
        return ctrlInStrm;
    }

    @Override
    public OutputDataStream getControlOutputStream() throws IOException {
        if (ctrlOutStrm == null) {
            ctrlOutStrm = new SockOutputStream(ctrlSock, addr, port + 1);
        }
        return ctrlOutStrm;
    }

    @Override
    public void close() {
        if (dataInStrm != null) {
            dataInStrm.kill();
        }
        if (ctrlInStrm != null) {
            ctrlInStrm.kill();
        }
        dataSock.close();
        ctrlSock.close();
    }

    @Override
    public void setReceiveBufferSize(int i) throws IOException {
        dataSock.setReceiveBufferSize(i);
    }

    @Override
    public int getReceiveBufferSize() {
        try {
            return dataSock.getReceiveBufferSize();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void setSendBufferSize(int i) throws IOException {
        dataSock.setSendBufferSize(i);
    }

    @Override
    public int getSendBufferSize() {
        try {
            return dataSock.getSendBufferSize();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public double getRTCPBandwidthFraction() {
        return -1;
    }

    @Override
    public double getRTCPSenderBandwidthFraction() {
        return -1;


    }

    class SockOutputStream implements OutputDataStream {

        DatagramSocket sock;
        InetAddress addr;
        int port;

        public SockOutputStream(DatagramSocket sock, InetAddress addr, int port) {
            this.sock = sock;
            this.addr = addr;
            this.port = port;
        }

        
        /**
         * Write data to the underlying network. Data is copied from the buffer starting af offset. Number of bytes copied is length
         * @param buffer - The buffer from which data is to be sent out on the network.
         * @param offset - The offset at which data from buffer is copied over
         * @param length - The number of bytes of data copied over to the network.
         */
        public int write(byte data[], int offset, int len) {
            try {
                //System.out.println(data.length);
                sock.send(new DatagramPacket(data, offset, len, addr, port));
            } catch (Exception e) {
                return -1;
            }
            return len;
        }
    }

    /**
     * An inner class to implement an PushSourceStream based on UDP sockets.
     */
    class SockInputStream extends Thread implements PushSourceStream {

        DatagramSocket sock;
        InetAddress addr;
        int port;
        boolean done = false;
        boolean dataRead = false;
        SourceTransferHandler sth = null;
      
        public SockInputStream(DatagramSocket sock, InetAddress addr, int port) {
            this.sock = sock;
            this.addr = addr;
            this.port = port;
        }


        /**
         * Read from the stream without blocking. Returns -1 when the end of the media is reached.
         * @param buffer - The buffer to read bytes into.
         * @param offset - The offset into the buffer at which to begin writing data.
         * @param length - The number of bytes to read.
         * @return int: The number of bytes read or -1 when the end of stream is reached.
         */
        public int read(byte buffer[], int offset, int length) {
            DatagramPacket p = new DatagramPacket(buffer, offset, length, addr, port);
//            uiReceiver ui = new uiReceiver();
            try {
                sock.receive(p);
            } catch (IOException e) {
                return -1;
            }
            synchronized (this) {
                dataRead = true;
                notify();
            }
            return p.getLength();
        }

        public synchronized void start() {
            super.start();
            if (sth != null) {
                dataRead = true;
                notify();
            }
        }

        public synchronized void kill() {
            done = true;
            notify();
        }

        /**
         * Determine the size of the buffer needed for the data transfer. This method is provided so that a transfer handler can determine how much data, at a minimum, will be available to transfer from the source. Overflow and data loss is likely to occur if this much data isn't read at transfer time.
         * @return int: The size of the data transfer.
         */
        public int getMinimumTransferSize() {
            return 2 * 1024;	// twice the MTU size, just to be safe.
        }

        /**
         *     Register an object to service data transfers to this stream.
         * If a handler is already registered when setTransferHandler is called, the handler is replaced; there can only be one handler at a time.
         * @param sth - The handler to transfer data to.
         */
        public synchronized void setTransferHandler(SourceTransferHandler sth) {
            this.sth = sth;
           
            dataRead = true;
            notify();
        }

        // Not applicable.
        public ContentDescriptor getContentDescriptor() {
            return null;
        }

        // Not applicable.
        public long getContentLength() {
            return LENGTH_UNKNOWN;
        }

        // Not applicable.
        public boolean endOfStream() {
            return false;
        }

        // Not applicable.
        public Object[] getControls() {
            return new Object[0];
        }

        // Not applicable.
        public Object getControl(String type) {
            return null;
        }

        /**
         * Loop and notify the transfer handler of new data.
         */
        public void run() {
            while (!done) {

                synchronized (this) {
                    while (!dataRead && !done) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    dataRead = false;
                }

                if (sth != null && !done) {
                    sth.transferData(this);
                }
            }
        }
    }
}
