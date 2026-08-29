package org.dromara.ship.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class SignatureGenerator {
    private static final Logger LOGGER =
        Logger.getLogger(SignatureGenerator.class.getName());



    public static String generateSignature(String sk, String ak,
                                           long timestamp, String nonce) {
        try {
            if (nonce.length() > 64) {
                LOGGER.severe("The length of nonce is too long: "
                    + nonce.length() + ", limit is 64");
                return null;
            }
            String message = String.format("%s:%d:%s", ak,
                timestamp, nonce);
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new
                SecretKeySpec(sk.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
            hmacSHA256.init(secretKey);
            byte[] hash =
                hmacSHA256.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 *
                hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            LOGGER.severe("generate_signature: " +
                e.getMessage());
            return null;
        }
    }
    public static void main(String[] args) {

        //String sk = "8d70356d-9a79-44ef-a467-2a850b0fcae5";
        //fbe699b8-d176-4934-8feb-a70766453448
        String sk = "fbe699b8-d176-4934-8feb-a70766453448";
       // String ak = "677bf2bfc38062043af4e673";
        String ak = "67ae0700c380620380c05fc8";
        long timestamp = System.currentTimeMillis() / 1000;
        System.out.println(timestamp);
        String nonce = "random_string";
        String signature = generateSignature(sk, ak, timestamp,
            nonce);
        System.out.println("Signature: " + signature);
    }
}
