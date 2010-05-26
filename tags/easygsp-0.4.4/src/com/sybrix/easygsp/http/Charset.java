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

/**
 * Charset <br/>
 * Description :
 */
public class Charset implements Comparable {
        private double order = 1.0;
        private String charset;

        public Charset(String charset) {
                this.charset = charset;

                 String l[] = charset.split(";");
                if (l.length > 1) {
                        this.charset = l[0];
                        order = Double.parseDouble(l[1].substring(2));
                } else {
                        this.charset = l[0];
                }
        }

        public String getCharset() {
                return charset;
        }

        public void setCharset(String charset) {
                this.charset = charset;
        }

        public double getOrder() {
                return order;
        }

        public void setOrder(double order) {
                this.order = order;
        }

        public int compareTo(Object o) {
                Charset l = (Charset) o;
                if (order < l.order)
                        return 1;
                else if (order > l.order)
                        return -1;
                else
                        return 0;
        }
}
