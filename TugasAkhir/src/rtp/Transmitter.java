/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rtp;

import UI.mainUI;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPHeader;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.SessionEvent;

/**
 *
 * @author Bagus Trihatmaja
 */
public class Transmitter implements ReceiveStreamListener, SessionListener, ControllerListener {

    private DataSource source = null;
    private String Message = null;
    private DataSource OutputData = null;
    private boolean failed = false;
    private String ipAddress;
    private int port;
    private RTPManager RTPMgrs[];
    private RTPManager RTPMgr[];
    private mainUI mainui;
    /**
     * Output locator - this is the broadcast address for the media.
     */
    private MediaLocator mediaLocator = null;
    /**
     * The data sink object used to broadcast the results from the processor to
     * the network.
     */
    private DataSink dataSink = null;
    /**
     * The processor used to read the media from a local file, and produce an
     * output stream which will be handed to the data sink object for broadcast.
     */
    private Processor mediaProcessor = null;
    /**
     * The track formats used for all data sources in this transmitter. It is
     * assumed that this transmitter will always be associated with the same RTP
     * stream format, so this is made static.
     */
    private static final Format[] FORMATS = new Format[]{
        new AudioFormat(AudioFormat.ULAW_RTP)};
    /**
     * The content descriptor for this transmitter. It is assumed that this
     * transmitter always handles the same type of RTP content, so this is made
     * static.
     */
    private static final ContentDescriptor CONTENT_DESCRIPTOR =
            new ContentDescriptor(ContentDescriptor.RAW_RTP);

    public Transmitter(File mediaFile, MediaLocator locator, String _ipAddress, int _port, String _message, mainUI aThis) throws MalformedURLException, IOException, NoDataSourceException {
        mediaLocator = locator;
        ipAddress = _ipAddress;
        Message = _message;
        port = _port;
        mainui = aThis;
        source = Manager.createDataSource(
                new MediaLocator(mediaFile.toURL()));
        System.out.println("-> Created data source: '"
                + mediaFile.getAbsolutePath() + "'");
    }

    /**
     * Starts transmitting the media.
     */
    public void startTransmitting() throws IOException, UnsupportedFormatException {
        String result = null;
        // Create an RTP session to transmit the output of the processor to the specified IP address and port no.

        result = createTransmitter();


        if (result != null) {
            mediaProcessor.close();
            mediaProcessor = null;

        }

        // Start the transmission
        mediaProcessor.start();
    }

    /**
     * Stops transmitting the media.
     */
    public void stopTransmitting() throws IOException {
        synchronized (this) {
            if (mediaProcessor != null) {
                mediaProcessor.stop();
                mediaProcessor.close();
                mediaProcessor = null;
                for (int i = 0; i < RTPMgrs.length; i++) {
                    RTPMgrs[i].removeTargets("Session ended.");
                    RTPMgrs[i].dispose();
                }
            }

        }
    }

    /**
     * Sets the data source. This is where the transmitter will get the media to
     * transmit.
     */
    public void setDataSource(DataSource ds) throws IOException,
            NoProcessorException, CannotRealizeException, NoDataSinkException {

        /* Create the realized processor.  By calling the 
         createRealizedProcessor() method on the manager, we are guaranteed 
         that the processor is both configured and realized already.  
         For this reason, this method will block until both of these 
         conditions are true.  In general, the processor is responsible 
         for reading the file from a file and converting it to
         an RTP stream.
         */
        mediaProcessor = Manager.createProcessor(ds);
        boolean result = waiting(mediaProcessor, Processor.Configured);
        if (result == false) {
            System.out.println("ada yang salah pada configuring processor");
        }

        TrackControl[] tracks = mediaProcessor.getTrackControls();
        if (tracks == null || tracks.length < 1) {
            System.out.println("tidak menemukan track");
        }

        boolean formated = false;

        System.out.println("panjang format track " + tracks.length);
        for (int i = 0; i < tracks.length; i++) {
            Format format = tracks[i].getFormat();
            System.out.println("Format track : " + format);


            if (tracks[i].isEnabled() && format instanceof AudioFormat && !formated) {
                int isBigEndian = ((AudioFormat) format).getEndian();
                int jmlKanal = ((AudioFormat) format).getChannels();
                double audioFrameRate = ((AudioFormat) format).getFrameRate();
                int audioFrameSizeBit = ((AudioFormat) format).getFrameSizeInBits();
                double audioSampleRate = ((AudioFormat) format).getSampleRate();
                int audioSampleSizeBit = ((AudioFormat) format).getSampleSizeInBits();
                System.out.println("BigEndian: " + isBigEndian);
                System.out.println("Jumlah Channel: " + jmlKanal);
                System.out.println("Audio Frame Rate: " + audioFrameRate);
                System.out.println("Audio Frame Size (bit): " + audioFrameSizeBit);
                System.out.println("Audio Sample Rate: " + audioSampleRate);
                System.out.println("Audio Sample Size (bit): " + audioSampleSizeBit);
            } else {
                tracks[i].setEnabled(false);

                System.out.println("track disable");
            }

        }

        mediaProcessor.setContentDescriptor(CONTENT_DESCRIPTOR);

        result = waiting(mediaProcessor, Controller.Realized);
        if (result == false) {
            System.out.println("tidak dapat mengenali processor");;
        }

        OutputData = mediaProcessor.getDataOutput();
        //OutputData.connect();
        //Buffer buf = new Buffer();
        //buf.setTimeStamp(1000);
        //((PushBufferDataSource)OutputData).getStreams()[0].read(buf);

        /* Create the data sink.  The data sink is used to do the actual work 
         of broadcasting the RTP data over a network.
         */
        //dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(),
        //        mediaLocator);
    }

    private String createTransmitter() throws IOException, UnsupportedFormatException {

        PushBufferDataSource pbds = (PushBufferDataSource) OutputData;

        PushBufferStream pbss[] = pbds.getStreams();


        System.out.println("hello :" + pbss.length);
        RTPMgrs = new RTPManager[pbss.length];
        SendStream sendStream;

        int portAddr;

        for (int i = 0; i < pbss.length; i++) {

            RTPMgrs[i] = RTPManager.newInstance();
            // System.out.println("hello: " + pbss[i].buf.getTimeStamp());
            portAddr = port + 2 * i;
            RTPMgrs[i].initialize(new RTPSocketAdapter(InetAddress.getByName(ipAddress), port, 10, Message, ""));
            System.out.println("berhasil membuat RTP session : " + ipAddress + ":" + port);
            sendStream = RTPMgrs[i].createSendStream(OutputData, i);


            sendStream.start();

        }
        return null;
    }

    /*
     * Contructor untuk class Trasmitter
     * @mediaFile : File yang akan ditransmisikan
     */
    @Override
    public void update(ReceiveStreamEvent rse) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(SessionEvent se) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void setFailed() {
        failed = true;
    }

    Integer getStateLock() {
        return 0;
    }

    @Override
    public void controllerUpdate(ControllerEvent ce) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class stateListener implements ControllerListener {

        public void controllerUpdate(ControllerEvent ce) {
            if (ce instanceof ControllerClosedEvent) {
                setFailed();
            }

            if (ce instanceof ControllerEvent) {
                synchronized (getStateLock()) {
                    getStateLock().notifyAll();
                }
            }
        }
    }

    public void setDS() throws IOException, NoProcessorException, CannotRealizeException, NoDataSinkException {
        this.setDataSource(source);
    }

    private synchronized boolean waiting(Processor proc, int state) {
        proc.addControllerListener(new stateListener());
        failed = false;

        if (state == Processor.Configured) {
            proc.configure();
        } else if (state == Processor.Realized) {
            proc.realize();
        }

        while (proc.getState() < state && !failed) {
            synchronized (getStateLock()) {
                try {
                    getStateLock().wait();
                } catch (Exception e) {
                    System.out.println("error pada :" + e);
                    return false;
                }
            }
        }

        if (failed) {
            return false;
        } else {
            return true;
        }
    }
}
