/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tccoder.j2me.rms;

import gr.fire.util.Log;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 *
 * @author Dimas
 */
public class TokenStorage {
    private RecordStore rs;
    private final static String STORE_NAME = "APP-twitter-oauth";

    private static TokenStorage instance;

    public static TokenStorage getInstance() {
        if (instance == null) {
            instance = new TokenStorage();
        }

        return instance;
    }

    private void openRms() throws RecordStoreException {
        rs = RecordStore.openRecordStore(STORE_NAME, true);
    }

    private void closeRms() {
        try {
            rs.closeRecordStore();
        } catch (Throwable ex) { }
    }

    public void saveToken(final String token, final String secret) {
        try {
            openRms();

            byte[] temp = token.getBytes();
            System.out.println("BYTE TOKEN: " + new String(temp));
            rs.addRecord(temp, 0, temp.length);

            temp = secret.getBytes();
            rs.addRecord(temp, 0, temp.length);
        } catch (Throwable ex) {
            System.out.println("RMS ERROR: " + ex.getMessage());
        } finally {
            closeRms();
        }
    }

    /**
     * 
     * @return String[] (token, secret)
     */
    public String[] getAccessToken() {
        String token = null;
        String secret = null;

        try {
            openRms();

            token = new String(rs.getRecord(1));
            secret = new String(rs.getRecord(2));

            System.out.println("STORED TOKEN: " + token + "::" + secret);
        } catch (Throwable ex) {
            token = null;
            secret = null;
        } finally {
            closeRms();
        }

        return new String[] { token, secret };
    }

    public boolean isAuthorized() {
        String[] tokens = getAccessToken();

        if (tokens[0] == null || tokens[1] == null) {
            return false;
        } else {
            return true;
        }
    }

    public void resetToken() {
        try {
            rs.deleteRecordStore(STORE_NAME);
        } catch (Throwable ex) {
        }
    }
}
