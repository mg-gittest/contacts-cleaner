package consulting.germain;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by mark_local on 13/11/2015.
 *  ActionListener to collect a file specification
 */
public class StartAction implements ActionListener {

    private StringBuffer buffer;
    private ContactCleaner contactCleaner;

    public StartAction(ContactCleaner cCleaner) {
        contactCleaner = cCleaner;
        buffer = contactCleaner.getBuffer();
    }

    @Override
    public void actionPerformed(final ActionEvent aEvent) {
        buffer.append("\nStart clicked...");
        contactCleaner.startCleaning();
        contactCleaner.updateText();
    }

}
