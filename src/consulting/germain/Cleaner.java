package consulting.germain;

import java.io.*;

/**
 * Created by mark_local on 13/11/2015.
 * runnable that does the work on a separate thread
 */
public class Cleaner implements Runnable {
    ContactCleaner contactCleaner;
    private File inFile;
    private File outFile;

    BufferedReader reader;
    BufferedWriter writer;

    public Cleaner(ContactCleaner cc) {
        contactCleaner = cc;
    }

    @Override
    public void run() {
        inFile = contactCleaner.getInFile();
        outFile = contactCleaner.getOutFile();
        if (openFiles()) {
            try {
                String line = reader.readLine();
                while (line != null) {
                    writer.append(line).append("\n");
                    line = reader.readLine();
                }
            } catch (IOException e) {
                contactCleaner.getBuffer().append(e.toString());
            }
            finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                } catch (IOException e) {
                    contactCleaner.getBuffer().append(e.toString());
                }
            }
        }
    }

    private boolean openFiles() {
        try {
            if (inFile != null) {
                reader = new BufferedReader(new FileReader(inFile));
            } else {
                log("null inFile");
            }
        } catch (FileNotFoundException e) {
            log(e.toString());
        }
        try {
            if (outFile != null) {
                writer = new BufferedWriter(new FileWriter(outFile));
            } else {
                log("null outfile");
            }
        } catch (IOException e) {
            log(e.toString());
        }
        boolean ret = reader != null && writer != null;
        if (!ret) {
            contactCleaner.updateText();
        }
        return ret;
    }

    private void log(String msg) {
        contactCleaner.getBuffer().append(msg);
    }
}
