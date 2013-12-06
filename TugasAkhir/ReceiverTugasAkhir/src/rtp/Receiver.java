/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rtp;

import UI.uiReceiver;
import rtp.RTPSocketAdapter;
import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.rtp.rtcp.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.AudioFormat;
import javax.media.Format;
import javax.media.format.FormatChangeEvent;
import javax.media.control.BufferControl;

/**
 *
 * @author tquekudo
 */
public class Receiver implements ReceiveStreamListener, SessionListener, ControllerListener{
    String sessions[] = null;
    RTPManager mgrs[] = null;
    Vector playerWindows = null;
    
    boolean dataReceived = false;
    Object dataSync = new Object();

    uiReceiver ui;

    public Receiver(String sessions[], uiReceiver ui) {
	this.sessions = sessions;
        this.ui = ui;
  
        //System.out.println("panjang sesi:"+sessions.length);
    }

    private String showWaktu()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyy/MM/dd HH:mm:ss.SSS");
        Date date = new Date();
        String a = new String(dateFormat.format(date));
        return a;
    }

    public boolean initialize() {

        try {
	    mgrs = new RTPManager[sessions.length];
            
	    playerWindows = new Vector();

            InetAddress ipAddr;
            SessionAddress localAddr = new SessionAddress();
            SessionAddress destAddr;
	    SessionLabel session;
          

	    // Open the RTP sessions.
	    for (int i = 0; i < sessions.length; i++) {

	 	// Parse the session addresses.
		try {
		    session = new SessionLabel(sessions[i]);
                    
		} catch (IllegalArgumentException e) {
		    System.err.println("Failed to parse the session address given: " + sessions[i]);
		    return false;
		}

		System.err.println("  - Open RTP session for: addr: " + session.addr + " port: " + session.port + " ttl: " + session.ttl);

		mgrs[i] = (RTPManager) RTPManager.newInstance();
		mgrs[i].addSessionListener(this);
                //PushBufferSource pb = (PushBufferSource)mgrs[i].getReceiveStreams();
		mgrs[i].addReceiveStreamListener(this);

                ipAddr = InetAddress.getByName(session.addr);


                 if( ipAddr.isMulticastAddress())
                 {
                     // local and remote address pairs are identical:
                     localAddr= new SessionAddress( ipAddr, session.port, session.ttl);
                     destAddr = new SessionAddress( ipAddr, session.port, session.ttl);
                 }
                 else
                 {
                     localAddr= new SessionAddress( InetAddress.getLocalHost(), session.port);
                     destAddr = new SessionAddress( ipAddr, session.port);
                 }
                
		// Initialize the RTPManager with the RTPSocketAdapter
		mgrs[i].initialize(new RTPSocketAdapter(ipAddr,session.port, 100,"","receive", ui));

		// You can try out some other buffer size to see
		// if you can get better smoothness.
		BufferControl bc = (BufferControl)mgrs[i].getControl("javax.media.control.BufferControl");
                
		if (bc != null)
		    bc.setBufferLength(350);
	    }

        } catch (Exception e){
            System.err.println("Cannot create the RTP Session: " + e.getMessage());
            return false;
        }

	// Wait for data to arrive before moving on.

	long then = System.currentTimeMillis();
	long waitingPeriod = 30000;  // wait for a maximum of 30 secs.

	try{
	    synchronized (dataSync) {
		while (!dataReceived &&
			System.currentTimeMillis() - then < waitingPeriod) {
		    if (!dataReceived)
			System.err.println("  - Waiting...");
		    dataSync.wait(1000);
		}
	    }
	} catch (Exception e) {}

	if (!dataReceived) {
	    System.err.println("No data received");
	    close();
	    return false;
	}

        return true;
    }


    public boolean isDone() {
	return playerWindows.size() == 0;
    }


    /**
     * Close the players and the session managers.
     */
    protected void close() {

	for (int i = 0; i < playerWindows.size(); i++) {
	    try {
		((PlayerWindow)playerWindows.elementAt(i)).close();
	    } catch (Exception e) {}
	}

	playerWindows.removeAllElements();

	// close the RTP session.
	for (int i = 0; i < mgrs.length; i++) {
	    if (mgrs[i] != null) {
                mgrs[i].removeTargets( "Closing session from AVReceive3");
                mgrs[i].dispose();
		mgrs[i] = null;
	    }
	}
    }


    PlayerWindow find(Player p) {
	for (int i = 0; i < playerWindows.size(); i++) {
	    PlayerWindow pw = (PlayerWindow)playerWindows.elementAt(i);
	    if (pw.player == p)
		return pw;
	}
	return null;
    }


    PlayerWindow find(ReceiveStream strm) {
	for (int i = 0; i < playerWindows.size(); i++) {
	    PlayerWindow pw = (PlayerWindow)playerWindows.elementAt(i);
	    if (pw.stream == strm)
		return pw;
	}
	return null;
    }


    /**
     * SessionListener.
     */
    public synchronized void update(SessionEvent evt) {
	if (evt instanceof NewParticipantEvent) {
	    Participant p = ((NewParticipantEvent)evt).getParticipant();
	    System.err.println("  A new participant: " + p.getCNAME()+"  on  "+showWaktu());
	}
    }


    /**
     * ReceiveStreamListener
     */
    @Override
    public synchronized void update( ReceiveStreamEvent evt) {

	RTPManager mgr = (RTPManager)evt.getSource();
	Participant participant = evt.getParticipant();	// could be null.
	ReceiveStream stream = evt.getReceiveStream();  // could be null.

	if (evt instanceof RemotePayloadChangeEvent) {

	    System.err.println("  - Received an RTP PayloadChangeEvent.");
	    System.err.println("Sorry, cannot handle payload change.");
	    System.exit(0);

	}

	else if (evt instanceof NewReceiveStreamEvent) {

	    try {
		stream = ((NewReceiveStreamEvent)evt).getReceiveStream();
		DataSource ds = stream.getDataSource();
               
		// Find out the formats.
		RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
                
		if (ctl != null){
		    System.err.println("  - Received new RTP stream: " + ctl.getFormat()+"  pada  "+showWaktu());
		} else
		    System.err.println("  - Received new RTP stream");

                System.out.println("data source size: ");

		if (participant == null)
		    System.err.println("      The sender of this stream had yet to be identified."+"  pada  "+showWaktu());
		else {
		    System.err.println("      The stream comes from: " + participant.getCNAME()+"  pada  "+showWaktu());
		}

		// create a player by passing datasource to the Media Manager
		Player p = javax.media.Manager.createPlayer(ds);
                System.out.println("data source: "+ds.getContentType());
		if (p == null) return;

		p.addControllerListener(this);
		p.realize();
		PlayerWindow pw = new PlayerWindow(p, stream);
		playerWindows.addElement(pw);




		// Notify intialize() that a new stream had arrived.
		synchronized (dataSync) {
		    dataReceived = true;
		    dataSync.notifyAll();
                   
		}

	    } catch (Exception e) {
		System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
		return;
	    }

	}

	else if (evt instanceof StreamMappedEvent) {

	     if (stream != null && stream.getDataSource() != null) {
		DataSource ds = stream.getDataSource();
		// Find out the formats.
              //  RTPHeader head = new RTPHeader();
              //  System.out.println("hello:" + head.isExtensionPresent());
              //  System.out.println("hello:" + head.getExtension());
		RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
		System.err.println("  - The previously unidentified stream ");
		if (ctl != null)
		    System.err.println("      " + ctl.getFormat());
		System.err.println("      had now been identified as sent by: " + participant.getCNAME()+"  pada  "+showWaktu());
	     }
	}

	else if (evt instanceof ByeEvent) {

	     System.err.println("  - Got \"bye\" from: " + participant.getCNAME()+"  pada  "+showWaktu());
	     PlayerWindow pw = find(stream);
	     if (pw != null) {
		pw.close();
		playerWindows.removeElement(pw);
	     }
	}

    }


    /**
     * ControllerListener for the Players.
     */
    public synchronized void controllerUpdate(ControllerEvent ce) {

	Player p = (Player)ce.getSourceController();

	if (p == null)
	    return;

	// Get this when the internal players are realized.
	if (ce instanceof RealizeCompleteEvent) {
	    PlayerWindow pw = find(p);
	    if (pw == null) {
		// Some strange happened.
		System.err.println("Internal error!");
		System.exit(-1);
	    }
	    pw.initialize();
	    pw.setVisible(true);
	    p.start();
            System.out.println("memulai memainkan data video "+"  pada  "+showWaktu());
	}

	if (ce instanceof ControllerErrorEvent) {
	    p.removeControllerListener(this);
	    PlayerWindow pw = find(p);
	    if (pw != null) {
		pw.close();
		playerWindows.removeElement(pw);
	    }
	    System.err.println("AVReceive3 internal error: " + ce);
	}

    }


    /**
     * A utility class to parse the session addresses.
     */
    class SessionLabel {

	public String addr = null;
	public int port;
	public int ttl = 1;

	SessionLabel(String session) throws IllegalArgumentException {

	    int off;
	    String portStr = null, ttlStr = null;

	    if (session != null && session.length() > 0) {
		while (session.length() > 1 && session.charAt(0) == '/')
		    session = session.substring(1);

		// Now see if there's a addr specified.
		off = session.indexOf('/');
		if (off == -1) {
		    if (!session.equals(""))
			addr = session;
		} else {
		    addr = session.substring(0, off);
		    session = session.substring(off + 1);
		    // Now see if there's a port specified
		    off = session.indexOf('/');
		    if (off == -1) {
			if (!session.equals(""))
			    portStr = session;
		    } else {
			portStr = session.substring(0, off);
			session = session.substring(off + 1);
			// Now see if there's a ttl specified
			off = session.indexOf('/');
			if (off == -1) {
			    if (!session.equals(""))
				ttlStr = session;
			} else {
			    ttlStr = session.substring(0, off);
			}
		    }
		}
	    }

	    if (addr == null)
		throw new IllegalArgumentException();

	    if (portStr != null) {
		try {
		    Integer integer = Integer.valueOf(portStr);
		    if (integer != null)
			port = integer.intValue();
		} catch (Throwable t) {
		    throw new IllegalArgumentException();
		}
	    } else
		throw new IllegalArgumentException();

	    if (ttlStr != null) {
		try {
		    Integer integer = Integer.valueOf(ttlStr);
		    if (integer != null)
			ttl = integer.intValue();
		} catch (Throwable t) {
		    throw new IllegalArgumentException();
		}
	    }
	}
    }


    /**
     * GUI classes for the Player.
     */
    class PlayerWindow extends Frame {

	Player player;
	ReceiveStream stream;

	PlayerWindow(Player p, ReceiveStream strm) {
	    player = p;
	    stream = strm;
	}

	public void initialize() {
	    add(new PlayerPanel(player));
	}

	public void close() {
	    player.close();
	    setVisible(false);
	    dispose();
	}

	public void addNotify() {
	    super.addNotify();
	    pack();
	}
    }


    /**
     * GUI classes for the Player.
     */
    class PlayerPanel extends Panel {

	Component vc, cc;

	PlayerPanel(Player p) {
	    setLayout(new BorderLayout());
	    if ((vc = p.getVisualComponent()) != null)
		add("Center", vc);
	    if ((cc = p.getControlPanelComponent()) != null)
		add("South", cc);
	}

	public Dimension getPreferredSize() {
	    int w = 0, h = 0;
	    if (vc != null) {
		Dimension size = vc.getPreferredSize();
		w = size.width;
		h = size.height;
	    }
	    if (cc != null) {
		Dimension size = cc.getPreferredSize();
		if (w == 0)
		    w = size.width;
		h += size.height;
	    }
	    if (w < 160)
		w = 160;
	    return new Dimension(w, h);
	}
    }

}
