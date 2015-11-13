package consulting.germain;

import ezvcard.VCard;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by mark_local on 13/11/2015.
 * a Map from String to VCard
 */
public class VcardFromStringMap  {
    
    private Map<String, VCard> map = new HashMap<>();
    
    public int size() {
        return map.size();
    }

    
    public boolean isEmpty() {
        return map.isEmpty();
    }

    
    public VCard get(String key) {
        return map.get(key);
    }

    
    public VCard put(String key, VCard value) {
        return map.put(key, value);
    }

    
    public VCard remove(String key) {
        return map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    
    public Set<String> keySet() {
        return map.keySet();
    }

    
    public Collection<VCard> values() {
        return map.values();
    }
}
