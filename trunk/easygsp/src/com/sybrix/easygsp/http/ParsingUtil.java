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

package com.sybrix.easygsp.http;

import org.apache.http.NameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.Header;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Collections;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This is not mine.....httpclient ?
 * Simple http server maybe
 *
 * ParsingUtil <br/>
 * Description :
 */
public class ParsingUtil {

        public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
        private static final String PARAMETER_SEPARATOR = "&";
        private static final String NAME_VALUE_SEPARATOR = "=";

        /**
         * Returns a list of {@link org.apache.http.NameValuePair NameValuePairs} as built from the
         * URI's query portion. For example, a URI of
         * http://example.org/path/to/file?a=1&b=2&c=3 would return a list of three
         * NameValuePairs, one for a=1, one for b=2, and one for c=3.
         * <p/>
         * This is typically useful while parsing an HTTP PUT.
         *
         * @param query    uri to parse
         * @param encoding encoding to use while parsing the query
         */
        public static Map<String, Object> parse(final String query, final String encoding) {
                Map<String, Object> result = Collections.emptyMap();
                if (query != null && query.length() > 0) {
                        result = new HashMap<String, Object>();
                        parse(result, new Scanner(query), encoding);
                }
                return result;
        }

        /**
         * Returns a list of {@link NameValuePair NameValuePairs} as parsed from an
         * {@link org.apache.http.HttpEntity}. The encoding is taken from the entity's
         * Content-Encoding header.
         * <p/>
         * This is typically used while parsing an HTTP POST.
         *
         * @param entity The entity to parse
         * @throws java.io.IOException If there was an exception getting the entity's data.
         */
        public static Map parse(
                final HttpEntity entity) throws IOException {
                Map<String, String> result = Collections.emptyMap();
                if (isEncoded(entity)) {
                        final String content = EntityUtils.toString(entity);
                        final Header encoding = entity.getContentEncoding();
                        if (content != null && content.length() > 0) {
                                result = new HashMap<String, String>();
                                parse(result, new Scanner(content),
                                        encoding != null ? encoding.getValue() : null);
                        }
                }
                return result;
        }

        /**
         * Returns true if the entity's Content-Type header is
         * <code>application/x-www-form-urlencoded</code>.
         */
        public static boolean isEncoded(final HttpEntity entity) {
                final Header contentType = entity.getContentType();
                return (contentType != null && contentType.getValue().equalsIgnoreCase(CONTENT_TYPE));
        }

        /**
         * Adds all parameters within the Scanner to the list of
         * <code>parameters</code>, as encoded by <code>encoding</code>. For
         * example, a scanner containing the string <code>a=1&b=2&c=3</code> would
         * add the {@link NameValuePair NameValuePairs} a=1, b=2, and c=3 to the
         * list of parameters.
         *
         * @param parameters List to add parameters to.
         * @param scanner    Input that contains the parameters to parse.
         * @param encoding   Encoding to use when decoding the parameters.
         */
        public static void parse(
                final Map parameters,
                final Scanner scanner,
                final String encoding) {
                scanner.useDelimiter(PARAMETER_SEPARATOR);
                while (scanner.hasNext()) {
                        final String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
                        if (nameValue.length == 0 || nameValue.length > 2)
                                throw new IllegalArgumentException("bad parameter");

                        final String name = decode(nameValue[0], encoding);
                        String value = "";
                        if (nameValue.length == 2)
                                value = decode(nameValue[1], encoding);
                        if (parameters.containsKey(name)) {
                                if (parameters.get(name) instanceof String) {
                                        String val = (String) parameters.get(name);
                                        List l = new ArrayList();
                                        l.add(val);
                                        l.add(value);
                                        parameters.put(name, l);
                                } else if (parameters.get(name) instanceof List) {
                                        List val = (List) parameters.get(name);
                                        val.add(value);
                                        parameters.put(name, val);
                                }
                        } else {
                                parameters.put(name, value);
                        }
                }
        }

        /**
         * Returns a String that is suitable for use as an <code>application/x-www-form-urlencoded</code>
         * list of parameters in an HTTP PUT or HTTP POST.
         *
         * @param parameters The parameters to include.
         * @param encoding   The encoding to use.
         */
        public static String format(
                final List<? extends NameValuePair> parameters,
                final String encoding) {
                final StringBuilder result = new StringBuilder();
                for (final NameValuePair parameter : parameters) {
                        final String encodedName = encode(parameter.getName(), encoding);
                        final String value = parameter.getValue();
                        final String encodedValue = value != null ? encode(value, encoding) : "";
                        if (result.length() > 0)
                                result.append(PARAMETER_SEPARATOR);
                        result.append(encodedName);
                        result.append(NAME_VALUE_SEPARATOR);
                        result.append(encodedValue);
                }
                return result.toString();
        }

        private static String decode(final String content, final String encoding) {
                try {
                        return URLDecoder.decode(content,
                                encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
                } catch (UnsupportedEncodingException problem) {
                        throw new IllegalArgumentException(problem);
                }
        }

        private static String encode(final String content, final String encoding) {
                try {
                        return URLEncoder.encode(content,
                                encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
                } catch (UnsupportedEncodingException problem) {
                        throw new IllegalArgumentException(problem);
                }
        }
}
