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
package com.sybrix.easygsp.util;


import com.sybrix.easygsp.exception.PropertyNotFoundException;

import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 * General purpose properties file reader class.
 */
public class PropertiesFile {
        private Properties prop;
        private String fileName;


        /**
         * The fileName is the classpath location of the file relative to the root '/'  of the classpath.<br/>
         * The root of the classpath starts in the classes dir <br/><br/>
         * For this path:<br/>
         * /WEB-INF/classes/env.properties <br/>
         * <p/>
         * fileName = "env.properties"
         * <p/>
         * <br/><br/>
         * For this path:<br/>
         * /WEB-INF/classes/resources/env.properties <br/>
         * fileName = "resources/env.propertiesn"
         * <br/>
         * <br/>
         * <p/>
         * To reload the file after a change has been made to it called the load method.
         *
         * @param fileName
         */
        public PropertiesFile(String fileName) {
                this.fileName = fileName;
                load();
        }

        public PropertiesFile() {

        }

        public PropertiesFile(InputStream inputStream) {
                prop = new Properties();
                try {
                        prop.load(inputStream);
                } catch (Exception e) {
                        throw new RuntimeException("Properties file load for inputstream failed", e);
                } finally {
                        try {
                                inputStream.close();
                        } catch (IOException e) {
                        }
                }
        }

        /**
         * Reads the property file from the file system.
         */
        public void load() {
                prop = new Properties();

                try {
                        if (fileName.startsWith("classPath:")) {
                                URL url = getClass().getClassLoader().getResource(fileName.substring(10));
                                FileInputStream fis = new FileInputStream(url.getFile());
                                prop.load(fis);
                                fis.close();
                        } else {

                                Reader fis = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1"));
                                prop.load(fis);
                                fis.close();
                        }

                } catch (Throwable e) {
                        throw new RuntimeException("Properties file load failed, fileName:" + fileName, e);
                } finally {
//                        try {
//
//                        } catch (IOException e) {
//                        }
                }
        }

        /**
         * @param key -  properties file key
         * @return Returns string value mapped to this key in the properties file.
         */
        public String getString(String key, Object... obj) {

                try {
                        return prop.get(key).toString();

                } catch (Exception e) {
                        if (obj.length > 0) {
                                return obj[0].toString();
                        }
                }

                return null;
        }

        /**
         * @param key -  properties file key
         * @return Returns the value mapped to this key in the properties file.
         */
        public Object get(String key) {
                try {
                        return prop.get(key);
                } catch (NullPointerException e) {
                        throw new PropertyNotFoundException("property: " + key, e);
                }
        }

        public String getString(String key, String defaultValue) {
                try {
                        return prop.get(key).toString();
                } catch (NullPointerException e) {
                        return defaultValue;
                }
        }


        /**
         * Returns null if value can not be parsed as an integer.
         *
         * @param key -  properties file key
         * @return Returns the Integer value mapped to this key in the properties file.
         */
        public Integer getInt(String key, Object... obj) {
                try {
                        return Integer.parseInt(getString(key, obj));
                } catch (NumberFormatException e) {
                        throw e;
                } catch (Exception e) {
                        throw new PropertyNotFoundException("property: " + key + " not found in properties file");
                }
        }

        public Long getLong(String key, Object... obj) {
                try {
                        return Long.parseLong(getString(key, obj));
                } catch (NumberFormatException e) {
                        throw e;
                } catch (Exception e) {
                        throw new PropertyNotFoundException("property: " + key + " not found in properties file");
                }
        }

        public boolean getBoolean(String key, Boolean... obj) {
                try {

                        return getString(key, obj.toString()).equalsIgnoreCase("true");
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        public Set getKeySet() {
                return prop.keySet();
        }
}
