package istat.android.network.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

public class Security {
    final static String[] PASSWORD_PROPOSITION_CHAR = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    @SuppressLint("SimpleDateFormat")
    public static String generateXWSSEToken(String userName, String secret) {
        return generateXWSSEToken(userName, secret, System.currentTimeMillis());
    }

    @SuppressLint("SimpleDateFormat")
    public static String generateXWSSEToken(String userName, String secret,
                                            long time) {
        try {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            df.setTimeZone(tz);
            String nowAsISO = df.format(new Date(time));
            String nonce = "";
            Random random = new Random();
            for (int i = 0; i < 16; i++) {
                int index = random
                        .nextInt(PASSWORD_PROPOSITION_CHAR.length - 1);
                nonce += PASSWORD_PROPOSITION_CHAR[index];
            }
            Log.i("Security", "generateXWSSEToken::nonce=" + nonce);
            String createAt = nowAsISO;
            String password = nonce + createAt + secret;
            String sha1 = SHA1.toSHA1(password.getBytes());
            String passwordDigest = Base64.encodeToString(SHA1.toByte(sha1),
                    Base64.NO_WRAP);
            String header = "UsernameToken Username=\"" + userName
                    + "\", PasswordDigest=\"" + passwordDigest + "\", Nonce=\""
                    + Base64.encodeToString(nonce.getBytes(), Base64.NO_WRAP)
                    + "\", Created=\"" + createAt + "\"";
            return header;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
