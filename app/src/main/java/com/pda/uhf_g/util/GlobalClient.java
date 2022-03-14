package com.pda.uhf_g.util;

import com.gg.reader.api.dal.GClient;

public class GlobalClient {
    private static GClient instance = null;

    static {
        try {
            instance = new GClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GClient getClient() {
        return instance;
    }
}
