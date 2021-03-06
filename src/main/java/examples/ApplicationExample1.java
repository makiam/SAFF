/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package examples;

import org.jdesktop.application.Application;
import org.jdesktop.application.Launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A "Hello World" application.   A simpler way to write an application
 * like this would be to use the {@code SingleFrameApplication} base class.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationExample1 extends Application {
    JFrame mainFrame = null;

    @Override
    protected void startup() {
        JLabel label = new JLabel("Hello World", JLabel.CENTER);
        label.setFont(new Font("LucidaSans", Font.PLAIN, 32));
        mainFrame = new JFrame(" Hello World ");
        mainFrame.add(label, BorderLayout.CENTER);
        mainFrame.addWindowListener(new MainFrameListener());
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);  // center the window
        mainFrame.setVisible(true);
    }

    @Override
    protected void shutdown() {
        mainFrame.setVisible(false);
    }

    private class MainFrameListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            exit();
        }
    }

    public static void main(String[] args) {
        Launcher.getInstance().launch(ApplicationExample1.class, args);
    }
}

