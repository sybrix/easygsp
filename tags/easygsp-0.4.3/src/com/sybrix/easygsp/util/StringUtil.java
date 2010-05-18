package com.sybrix.easygsp.util;

import com.sybrix.easygsp.server.EasyGServer;

/**
 * StringUtil <br/>
 *
 * @author David Lee
 */
public class StringUtil {
        public static String capDriveLetter(String s) {
                if (EasyGServer.isWindows) {
                        return s.substring(0, 1).toUpperCase() + s.substring(1);
                } else {
                        return s;
                }
        }

        public static String capFirstLetter(String s) {
                return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
}
