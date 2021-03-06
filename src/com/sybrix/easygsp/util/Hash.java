/*
 * Copyright 2012. the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sybrix.easygsp.util;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.io.UnsupportedEncodingException;

/**
 * MD5 <br/>
 * Description :
 */
public class Hash {
        public static String convertToHex(byte[] data) {

                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < data.length; i++) {
                        int halfbyte = (data[i] >>> 4) & 0x0F;
                        int two_halfs = 0;
                        do {
                                if ((0 <= halfbyte) && (halfbyte <= 9))
                                        buf.append((char) ('0' + halfbyte));
                                else
                                        buf.append((char) ('a' + (halfbyte - 10)));
                                halfbyte = data[i] & 0x0F;
                        } while (two_halfs++ < 1);
                }
                return buf.toString();
        }

        public static String MD5(String text) {
                try {
                        MessageDigest md;
                        md = MessageDigest.getInstance("MD5");
                        byte[] md5hash = new byte[32];
                        md.update(text.getBytes("iso-8859-1"), 0, text.length());
                        md5hash = md.digest();
                        return convertToHex(md5hash);
                } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                }
        }

        public static String SHA1(String text) {
                try {
                        MessageDigest md;
                        md = MessageDigest.getInstance("SHA-1");
                        byte[] md5hash = new byte[32];
                        md.update(text.getBytes("iso-8859-1"), 0, text.length());
                        md5hash = md.digest();
                        return convertToHex(md5hash);
                } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                }

        }
}
