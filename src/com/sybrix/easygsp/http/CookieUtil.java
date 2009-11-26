/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sybrix.easygsp.http;

import javax.servlet.http.Cookie;
import java.util.Calendar;


/**
 * CookieUtil <br/>
 * Description :
 */
public class CookieUtil {
        
        /**
         * This will give the correct string value of this cookie. This
         * will generate the cookie text with only the values that were
         * given with this cookie. If there are no optional attributes
         * like $Path or $Domain these are left blank. This returns the
         * encoding as it would be for the HTTP Cookie header.
         *
         * @return this returns the Cookie header encoding of this
         */
        public String toClientString(Cookie cookie) {
                return "$Version=" + cookie.getVersion() + "; " + cookie.getName() + "=" +
                        cookie.getValue() + (cookie.getPath() == null ? "" : "; $Path=" +
                        cookie.getPath()) + (cookie.getDomain() == null ? "" : "; $Domain=" +
                        cookie.getDomain());
        }

//        public static String formatCookieHeaders(List<Cookie> cookies){
//                StringBuffer sb = new StringBuffer();
//
//
//                for(Cookie cookie: cookies){
//                        sb.append(ResponseHeaders.SET_COOKIE);
//                        sb.append(": ");
//                        sb.append(format(cookie));
//                        sb.append(ResponseHeaders.END_OF_LINE);
//                }
//
//                return sb.toString().trim();
//        }

        public static String format(Cookie cookie){
                if (cookie.getVersion()==0)
                        return formatVersion0(cookie);
                else
                        return formatVersion1(cookie);
        }

        private static String formatVersion0(Cookie cookie) {
                StringBuffer sb = new StringBuffer();
                sb.append(cookie.getName());
                sb.append("=");
                sb.append(cookie.getValue());

                if (cookie.getDomain() != null){
                        sb.append("; Domain=");
                        sb.append(cookie.getDomain());
                }

                if (cookie.getMaxAge() > -1){
                        sb.append("; Expires=");
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.SECOND, cookie.getMaxAge());
                        sb.append(ResponseImpl.cookieDateFormatter.format(cal.getTime()));
                }

                if (cookie.getPath() != null){
                        sb.append("; Path=");
                        sb.append(cookie.getPath());
                }

                if (cookie.getSecure()){
                        sb.append("; Secure");
                }

                return sb.toString();
        }

        private static String formatVersion1(Cookie cookie) {
                return "";
        }
}
