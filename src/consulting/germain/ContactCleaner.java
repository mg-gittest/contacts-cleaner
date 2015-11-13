package consulting.germain;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mark_local on 12/11/2015.
 * main class for contact cleaning app
 */
public final class ContactCleaner {


    public static void main(final String[] args) {
        final ContactCleaner app = new ContactCleaner();
        app.buildAndDisplayGui();
    }

    private JFrame frame = new JFrame("Contact Cleaner");
    private JPanel panel = new JPanel();
    private StringBuffer buffer = new StringBuffer();
    private TextArea textArea = new TextArea();

    private JButton btnInputFile = new JButton("Input");
    private JButton  btnOutputFile = new JButton("Output");
    private JButton btnStart = new JButton("Start");

    private ShowFileDialog inShowFD;
    private ShowFileDialog outShowFD;
    private StartAction startAction;
    private ExecutorService executor =  Executors.newFixedThreadPool(1);

    public void startCleaning() {
        Cleaner cleaner = new Cleaner(this);
        executor.execute(cleaner);

        buffer.append("\nExecutor started");
    }

    public JFrame getFrame() {
        return frame;
    }

    public StringBuffer getBuffer() {
        return buffer;
    }

    public File getInFile() {
        if (inShowFD == null) { return null; }
        return inShowFD.getFile();
    }

    public File getOutFile() {
        if (outShowFD == null) { return null; }
        return outShowFD.getFile();
    }

    public void updateText() {
        textArea.setText(buffer.toString());
    }

    private void buildAndDisplayGui() {
        buildContent();

        frame.setVisible(true);
    }

    private void buildContent() {

        panel.add(textArea);

        panel.add(new JLabel("Select Files:"));

        inShowFD = new ShowFileDialog(this);
        btnInputFile.addActionListener(inShowFD);
        panel.add(btnInputFile);

        outShowFD = new ShowFileDialog(this);
        btnOutputFile.addActionListener(outShowFD);
        panel.add(btnOutputFile);

        panel.add(new JLabel("Run Cleaner:"));

        startAction = new StartAction(this);
        btnStart.addActionListener(startAction);
        panel.add(btnStart);

        frame.getContentPane().add(panel);

        frame.setMinimumSize(new Dimension(1200, 600));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();

        frame.setLocation(200, 200);
    }


}

