package com.iamxgw.secskill.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
    public static final String salt = "1a2b3c4d";

    public static String md5(String pass) {
        return DigestUtils.md5Hex(pass);
    }

    public static String inputPassToFormPass(String inputPass) {
        String pass = salt.charAt(0) + salt.charAt(3) + inputPass + salt.charAt(5) + salt.charAt(1);

        return md5(pass);
    }

    public static String formPassToDbPass(String formPass, String salt) {
        String pass = salt.charAt(0) + salt.charAt(3) + formPass + salt.charAt(5) + salt.charAt(1);

        return md5(pass);
    }

    public static String inputPassToDbPass(String inputPass, String saltDB) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDbPass(formPass, saltDB);

        return dbPass;
    }

    public static void main(String[] args) {
        //        psd: 123456
        //        formPass : d3b1294a61a07da9b49b6e22b2cbd7f9
        //        saltDB : 1a2b3c4d
        //        calcPass : 49138ae911808055cb19a77ee8ce0a12
        //        dbPass : 546a5ec90201d2bc2d07f36d344cce11

        String formPass = inputPassToFormPass("123456");
        System.out.println(formPass);
        String dbPass = formPassToDbPass(formPass, salt);
        System.out.println(dbPass);
        System.out.println(inputPassToDbPass("123456", "1a2b3c4d"));
    }
}
