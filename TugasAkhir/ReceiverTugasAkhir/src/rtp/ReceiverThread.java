/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rtp;

/**
 *
 * @author BagusTrihatmaja
 */
public class ReceiverThread extends Thread {
    UI.uiReceiver ui;
    String[] session = new String[2];
    Receiver avReceive;
    
    public ReceiverThread(UI.uiReceiver ui, String[] session) {
        super();
        this.ui = ui;
        this.session = session;
       avReceive = new Receiver(session, ui);

    }
    
    @Override
    public void run() {
      
        if (!avReceive.initialize()) {
            System.err.println("Failed to initialize the sessions.");
            System.exit(-1);
        }
        try {
            while (!avReceive.isDone()) {
                //Thread.sleep(1000);
            }
        } catch (Exception e) {
        }
    }
}
