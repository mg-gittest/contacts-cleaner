package consulting.germain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by mark_local on 12/11/2015.
 * main class for contact cleaning app
 */
public final class ContactCleaner {

    public static void main(final String[] args) {
        final ContactCleaner app = new ContactCleaner();
        app.buildAndDisplayGui();
    }

    private JFrame frame;

    private ShowFileDialog showFileDialog;
    private JPanel panel;
    private JButton fileButton;
    private TextArea textArea;

    private void buildAndDisplayGui() {
        buildContent();

        frame.setMinimumSize(new Dimension(1200, 600));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation(200, 200);
        frame.setVisible(true);
    }

    private void buildContent() {
        frame = new JFrame("Contact Cleaner");

        panel = new JPanel();

        textArea = new TextArea();
        panel.add(textArea);

        panel.add(new JLabel("Select Input File:"));

        fileButton = new JButton("Input");
        showFileDialog = new ShowFileDialog(frame, textArea);
        fileButton.addActionListener(showFileDialog);
        panel.add(fileButton);

        frame.getContentPane().add(panel);
    }

    private static final class ShowFileDialog implements ActionListener {

        private final JFrame frame;
        private TextArea textArea;
        private File file;

        ShowFileDialog(final JFrame aFrame, TextArea text) {
            frame = aFrame;
            textArea = text;
        }

        public File getFile() {
            return file;
        }

        @Override
        public void actionPerformed(final ActionEvent aEvent) {
            final JFileChooser fc = new JFileChooser();

            int rc = fc.showOpenDialog(frame);
            if (JFileChooser.APPROVE_OPTION == rc) {
                file = fc.getSelectedFile();

                String str = textArea.getText() + "\nSelected File:\n";
                try {
                    str += file.getCanonicalPath();
                } catch (IOException e) {
                    str = "Exception: detected";
                }

                textArea.setText(str);


            }

        }

    }
}

