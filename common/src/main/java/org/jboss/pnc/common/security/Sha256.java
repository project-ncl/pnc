package org.jboss.pnc.common.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Sha256 {

    MessageDigest md;

    public Sha256() throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance("SHA-256");
    }

    public static String digest(String message)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(message.getBytes("UTF-8"));
        byte[] digest = md.digest();
        return format(digest);
    }

    public void add(String message) throws UnsupportedEncodingException {
        md.update(message.getBytes("UTF-8"));
    }

    public String digest() {
        byte[] digest = md.digest();
        return format(digest);
    }

    private static String format(byte[] digest) {
        return String.format("%064x", new java.math.BigInteger(1, digest));
    }

}
