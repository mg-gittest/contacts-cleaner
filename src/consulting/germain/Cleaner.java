package consulting.germain;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import ezvcard.property.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mark_local on 13/11/2015.
 * runnable that does the work on a separate thread
 */
public class Cleaner implements Runnable {
    ContactCleaner contactCleaner;
    private File inFile;
    private File outFile;

    private VCardReader vCardReader;
    private VCardWriter vCardWriter;

    private VcardFromStringMap emailMap = new VcardFromStringMap();
    private VcardFromStringMap nameMap = new VcardFromStringMap();
    private VcardFromStringMap phoneMap = new VcardFromStringMap();

    public Cleaner(ContactCleaner cc) {
        contactCleaner = cc;

    }

    /**
     * combine all the maps, using the VCard hashcode to elimiinate duplicates
     * @return list of unique VCard (by hashcode)
     */
    private List<VCard> combineCards() {
        VcardFromStringMap combined = new VcardFromStringMap();

        for (VCard vcard: emailMap.values()) {
            String hashStr = String.valueOf(vcard.hashCode());
            combined.put(hashStr, vcard);
        }

        for (VCard vcard: nameMap.values()) {
            String hashStr = String.valueOf(vcard.hashCode());
            combined.put(hashStr, vcard);
        }

        for (VCard vcard: phoneMap.values()) {
            String hashStr = String.valueOf(vcard.hashCode());
            combined.put(hashStr, vcard);
        }

        List<VCard> list = new ArrayList<>(combined.size());

        list.addAll(combined.values());

        return list;
    }

    @Override
    public void run() {
        inFile = contactCleaner.getInFile();
        outFile = contactCleaner.getOutFile();
        if (openFiles()) {
            try {
                int numCards = 0;
                List<VCard> vcardIn = vCardReader.readAll();
                for (VCard vcard: vcardIn) {
                    addCard(vcard);
                    ++numCards;
                }

                log("\nAdded cards: " + numCards);
                contactCleaner.updateText();

                List<VCard> vcardOut = combineCards();
                log("\nCombined cards: " + vcardOut.size());
                contactCleaner.updateText();

                for(VCard vcard: vcardOut) {
                    vCardWriter.write(vcard);
                }
                vCardWriter.close();
                log("\nWritten cards: " + vcardOut.size());
                contactCleaner.updateText();

            } catch (IOException e) {
                contactCleaner.getBuffer().append(e.toString());
                contactCleaner.updateText();
            }
        }
    }

    /**
     * add a new card to the maps, merging as needed
     * @param vcard what ot add
     */
    private void addCard(VCard vcard) {
        // merge by email
        List<Email> emails = vcard.getEmails();
        for (Email email: emails) {
            String emailStr = email.getValue();
            VCard exist= addEmailToMap(emailStr, vcard);
            if (exist != null){
                mergeCards(exist, vcard);
            }
        }

        // merge by phone number
        List<Telephone> tels = vcard.getTelephoneNumbers();
        for (Telephone tel: tels) {
            if (tel == null) { continue; }
            String telStr = tel.getText();
            VCard exist = phoneMap.get(telStr);
            if (exist == null ) {
                phoneMap.put(telStr, vcard);
            } else {
                mergeCards(exist, vcard);
            }
        }

        // merge by name
        StructuredName name = vcard.getStructuredName();
        if (name != null ) {
            String nameStr = name.getFamily() + "." + name.getGiven();
            VCard exist = nameMap.get(nameStr);
            if (exist == null) {
                nameMap.put(nameStr, vcard);
            } else {
                mergeCards(exist, vcard);
            }
        }
    }

    /**
     * merge a new card into an existing, updtintg maps as needed
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeCards(VCard existCard, final VCard mergeCard) {
        mergeAddresses(existCard, mergeCard);
        mergeEmails(existCard, mergeCard);
        mergeFN(existCard, mergeCard);
        mergeLogo(existCard, mergeCard);
        mergeStructuredName(existCard, mergeCard);
        mergeName(existCard, mergeCard);
        mergePhotos(existCard, mergeCard);
        mergeRole(existCard, mergeCard);
        mergeTel(existCard, mergeCard);
        mergeTitle(existCard, mergeCard);
    }

    /**
     *  merge addresses
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeAddresses(VCard existCard, final VCard mergeCard) {
        List<Address> exist = existCard.getAddresses();
        List<Address> merges = mergeCard.getAddresses();

        for (Address address: merges) {
            if (exist == null || !exist.contains(address)) {
                existCard.addAddress(address);
            }
        }
    }

    /**
     *  merge emails, updating email map if needed
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeEmails(VCard existCard, final VCard mergeCard) {
        List<Email> exist = existCard.getEmails();
        List<Email> merges = mergeCard.getEmails();

        Set<String> existStr = new HashSet<>();
        for (Email exEm : exist) {
            String val = exEm.getValue();
            existStr.add(val);
        }

        for(Email email: merges) {
            String mergeStr = email.getValue();
            if (!existStr.contains(mergeStr)) {
                existCard.addEmail(email);
                addEmailToMap(mergeStr, existCard);
            }
        }
    }

    /**
     * merge Family and Given names
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeStructuredName(VCard existCard, final VCard mergeCard) {
        StructuredName exist = existCard.getStructuredName();
        StructuredName merge = mergeCard.getStructuredName();

        if (exist == null) {
            if (merge != null) {
                existCard.setStructuredName(merge);
            }
            return;
        }
        if (merge == null) {
            return;
        }
        String mergeStr = merge.getFamily();
        if (needToMerge(mergeStr, exist.getFamily())){
            exist.setFamily(mergeStr);
        }
        mergeStr = merge.getGiven();
        if (needToMerge(mergeStr, exist.getGiven())){
            exist.setGiven(mergeStr);
        }
    }

    /**
     * evaluate whether we have empty exist String and non-empty merge string
     * @param mergeStr string ot merge
     * @param existStr strin gmerging to
     * @return true if merge is helpful
     */
    private boolean needToMerge(String mergeStr, String existStr) {
        return mergeStr != null && !mergeStr.isEmpty()
                && (existStr == null || existStr.isEmpty() );
    }

    /**
     * helper to add email to a vcard
     * @param emailStr email to search for
     * @param existCard existing card
     * @return existing VCard if the emailStr already in use
     */
    private VCard addEmailToMap(String emailStr, VCard existCard) {
        if (emailStr == null ) { return null; }
        VCard exist = emailMap.get(emailStr);
        if (exist == null ) {
            emailMap.put(emailStr, existCard);
        }
        return exist;
    }

    /**
     * merge
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeFN(VCard existCard, final VCard mergeCard) {

    }

    /**
     * merge
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeLogo(VCard existCard, final VCard mergeCard) {

    }

    /**
     * merge
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeName(VCard existCard, final VCard mergeCard) {

    }

    /**
     * merge
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergePhotos(VCard existCard, final VCard mergeCard) {

    }

    /**
     * merge
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeRole(VCard existCard, final VCard mergeCard) {

    }

    /**
     * merge
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeTel(VCard existCard, final VCard mergeCard) {

    }

    /**
     * merge
     * @param existCard existing card
     * @param mergeCard card to merge
     */
    private void mergeTitle(VCard existCard, final VCard mergeCard) {

    }

    /**
     * helpr to open files or report problems
     * @return true on success
     */
    private boolean openFiles() {
        try {
            if (inFile != null) {
                vCardReader = new VCardReader(inFile);
            } else {
                log("null inFile");
            }
        } catch (FileNotFoundException e) {
            log(e.toString());
        }
        try {
            if (outFile != null) {
                vCardWriter = new VCardWriter(outFile, VCardVersion.V2_1);
            } else {
                log("null outfile");
            }
        } catch (IOException e) {
            log(e.toString());
        }
        boolean ret = vCardReader != null && vCardWriter != null;
        if (!ret) {
            contactCleaner.updateText();
        }
        return ret;
    }

    /**
     * helper to log message to cleaner buffer
     * @param msg string to send, will not update until call udpateText
     */
    private void log(String msg) {
        contactCleaner.getBuffer().append(msg);
    }
}
