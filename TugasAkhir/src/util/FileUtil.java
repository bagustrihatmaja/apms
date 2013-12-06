/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CannotRealizeException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.protocol.DataSource;
import javax.swing.JFileChooser;
import javax.swing.JPanel;


/**
 *
 * @author tquekudo
 */
public class FileUtil extends JPanel{

    DataSource ds;
    JPanel parentPanel;
    URL fileURL;
    Player thePlayer;

    public File OpenSaja()
    {
        File theFile = null;
        JFileChooser chooser = new JFileChooser();
	if (chooser.showOpenDialog(parentPanel) == JFileChooser.APPROVE_OPTION)
	{
            theFile = chooser.getSelectedFile();
	}
        return theFile;
    }


    public File OpenTransmit()
    {
          File theFile = null;
        JFileChooser chooser = new JFileChooser();
	if (chooser.showOpenDialog(parentPanel) == JFileChooser.APPROVE_OPTION)
	{
            theFile = chooser.getSelectedFile();
	}
        return theFile;
    }

}
