package com.iamxgw.secskill.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
    public static Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }

        Matcher mobile_matcher = mobile_pattern.matcher(mobile);
        return mobile_matcher.matches();
    }
}
