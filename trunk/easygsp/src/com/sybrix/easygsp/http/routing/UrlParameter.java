package com.sybrix.easygsp.http.routing;

import jregex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 3/14/12
 * Time: 8:17 PM
 */
public class UrlParameter {
        String name;
        Pattern regexPattern;
        String value;

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public Pattern getRegexPattern() {
                return regexPattern;
        }

        public void setRegexPattern(Pattern regexPattern) {
                this.regexPattern = regexPattern;
        }

        public String getValue() {
                return value;
        }

        public void setValue(String value) {
                this.value = value;
        }
}
