package consulting.germain;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by mark_local on 13/11/2015.
 *  ActionListener to collect a file specification
 */
public class ShowFileDialog implements ActionListener {

    private final ContactCleaner contactCleaner;
    private StringBuffer buffer;
    private File file;

    /**
     *  ctor
     * @param cCleaner context for dialog
     */
    ShowFileDialog(final ContactCleaner cCleaner) {
        contactCleaner = cCleaner;
        buffer = contactCleaner.getBuffer();
    }

    public File getFile() {
        return file;
    }

    @Override
    public void actionPerformed(final ActionEvent aEvent) {
        final JFileChooser fc = new JFileChooser();

        int rc = fc.showOpenDialog(contactCleaner.getFrame());
        if (JFileChooser.APPROVE_OPTION == rc) {
            file = fc.getSelectedFile();

            buffer.append("\nSelected File:\n");
            try {
                buffer.append(file.getCanonicalPath());
            } catch (IOException e) {
                buffer.append("Exception: detected");
            }
        }

        contactCleaner.updateText();
    }

}
