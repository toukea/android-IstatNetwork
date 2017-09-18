package istat.android.network.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

public class Security {
    final static String[] PASSWORD_PROPOSITION_CHAR = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public static String generateDigestAuthenticationToken(String userName, String password, String realm, String HttpMethod, String uri, String nonce) {
        return generateDigestAuthenticationToken(userName, password, realm, HttpMethod, uri, nonce, null, "", "", "");
    }

    public static String generateDigestAuthenticationToken(String userName, String password, String realm, String HttpMethod, String uri, String nonce, String qop, String opaque) {
        return generateDigestAuthenticationToken(userName, password, realm, HttpMethod, uri, nonce, qop, opaque, "", "");
    }

    //A capturer-->www-authenticate →Digest realm="GAURAVBYTES.COM", qop="auth", nonce="MTUwNTY1MTcxODIxNDo2ZjgxYWQ2NDZlMWIyMDg4NThlODc2MmQ2NzY3M2I2NQ==", stale="true"
    //A Répondre-->Digest username="gaurav", realm="GAURAVBYTES.COM", nonce="MTUwNTY1MDY1MDU2NDo4YmNiYmQyYTVlNDBlNjk1MTlmMzI2NDk4ZGFhYzIxMA==", uri="/rest/my/find", response="54d7a82b4e591f4c7ba6743bdcb99f8c", opaque=""
    public static String generateDigestAuthenticationToken(String userName, String password, String realm, String HttpMethod, String uri, String nonce, String qop, String opaque, String nc, String cnonce) {
        StringBuilder sb = new StringBuilder(128);
        String HA3 = DigestAuthUtils.generateDigest(false, userName, realm, password, HttpMethod, uri, qop, nonce, nc, cnonce);
        sb.append("Digest ");
        sb.append("username").append("=\"").append(userName).append("\",");
        sb.append("realm").append("=\"").append(realm).append("\",");
        sb.append("nonce").append("=\"").append(nonce).append("\",");
        sb.append("uri").append("=\"").append(uri).append("\",");
        if (!TextUtils.isEmpty(qop)) {
            sb.append("qop").append('=').append(qop).append(",");
        }
        if (!TextUtils.isEmpty(opaque)) {
            sb.append("opaque").append('=').append(opaque).append(",");
        }
        sb.append("response").append("=\"").append(HA3).append("\"");
        return sb.toString();
    }

    //header:Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    public static String generateBasicAuthenticationToken(String userName, String secret) {
        String usernameAndPassword = userName + ":" + secret;
        return Base64.encodeToString(usernameAndPassword.getBytes(),
                Base64.NO_WRAP);
    }

    //header:X-WSSE
    @SuppressLint("SimpleDateFormat")
    public static String generateXWSSEToken(String userName, String secret) {
        return generateXWSSEToken(userName, secret, System.currentTimeMillis());
    }

    public static String generateXWSSEToken(String userName, String secret,
                                            long time) {
        String nonce = "";
        Random random = new Random();
        int index;
        for (int i = 0; i < 16; i++) {
            index = random
                    .nextInt(PASSWORD_PROPOSITION_CHAR.length - 1);
            nonce += PASSWORD_PROPOSITION_CHAR[index];
        }
        return generateXWSSEToken(userName, secret, nonce, time);
    }

    @SuppressLint("SimpleDateFormat")
    public static String generateXWSSEToken(String userName, String secret, String nonce,
                                            long time) {
        try {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
            df.setTimeZone(tz);
            String nowAsISO = df.format(new Date(time));
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
