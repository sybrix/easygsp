package com.sybrix.easygsp.http;

import java.util.Locale;

/**
 * Languages <br/>
 * Description :
 */
public class Language implements Comparable {

        private double order = 1.0;
        private String language;
        private String languageCode;
        private String countryCode;
        private Locale locale;

        public Language(String language){
                this(language,  1.0d);
        }
        
        public Language(String language, double order) {
                String l[] = language.split(";");
                if (l.length > 1) {
                        this.language = l[0];
                        String[] parts = l[0].split("-");
                        languageCode = parts[0];

                        if (parts.length > 1)
                                countryCode = parts[1];

                        order = Double.parseDouble(l[1].substring(2));

                } else {
                        this.language = l[0];
                        String parts[] = l[0].split("-");
                        this.languageCode = parts[0];
                        if (parts.length>1)
                                this.countryCode = parts[1];

                        this.order = order;
                }

                if (countryCode == null)
                        locale = new Locale(languageCode);
                else
                        locale = new Locale(languageCode, countryCode);

                this.language = language.replace('-', '_');

        }

        public String getLanguage() {
                return language;
        }

        public String getLanguageCode() {
                return languageCode;
        }

        public String getCountryCode() {
                return countryCode;
        }

        public double getOrder() {
                return order;
        }

        public void setOrder(double order) {
                this.order = order;
        }

        public int compareTo(Object o) {
                Language l = (Language) o;
                if (order < l.order)
                        return 1;
                else if (order > l.order)
                        return -1;
                else
                        return 0;
        }

        public Locale getLocale() {
                return locale;
        }
}
