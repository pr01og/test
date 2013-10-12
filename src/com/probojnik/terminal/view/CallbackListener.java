package com.probojnik.terminal.view;

import java.util.List;
import java.util.Map;

/**
 * @author Stanislav Shamji
 */
public interface CallbackListener {
    public short addButtons(MainActivity mainActivity, List<Map<String, String>> responseArrayList);
    public short fillLayout(MainActivity mainActivity, List<Map<String, String>> responseArrayList);
}
