package com.sybrix.easygsp.http.routing;

import com.sybrix.easygsp.exception.RoutingException;
import jregex.Matcher;
import jregex.Pattern;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Route {
        // shameless taken from the play framework, these regex's are not mine
        public static final Pattern customRegexPattern = new Pattern("\\{([a-zA-Z_][a-zA-Z_0-9]*)\\}");
        public static final Pattern argsPattern = new Pattern("\\{<([^>]+)>([a-zA-Z_0-9]+)\\}");

        private Pattern pattern;
        private String method;
        private String path;
        private String controller;

        private Map<String, UrlParameter> parameters = new HashMap();

        public Route(String method, String path, String controller) throws RoutingException {
                try {
                        this.method = method;
                        this.path = path;
                        this.controller = controller;

                        String patternString = path;
                        patternString = customRegexPattern.replacer("\\{<[^/]+>$1\\}").replace(patternString);
                        Matcher matcher = argsPattern.matcher(patternString);

                        while (matcher.find()) {
                                UrlParameter param = new UrlParameter();
                                param.name = matcher.group(2);
                                param.regexPattern = new Pattern(matcher.group(1));
                                parameters.put(param.name, param);
                        }

                        patternString = argsPattern.replacer("({$2}$1)").replace(patternString);
                        this.pattern = new Pattern(patternString);
                } catch (Exception e) {
                        throw new RoutingException("Error initializing route method:" + method + ", path:" + path + ", " +
                                "controller:" + controller + ". " + e.getMessage(), e);
                }
        }

        public Pattern getPattern() {
                return pattern;
        }

        public String getMethod() {
                return method;
        }

        public void setMethod(String method) {
                this.method = method;
        }

        public String getPath() {
                return path;
        }

        public void setPath(String path) {
                this.path = path;
        }

        public String getController() {
                return controller;
        }

        public void setController(String controller) {
                this.controller = controller;
        }

        public Map<String, UrlParameter> getParameters() {
                return parameters;
        }

        public boolean matches(String path) {
                Matcher matcher = pattern.matcher(path);

                if (matcher.matches()) {
                        for (UrlParameter parameter : parameters.values()) {
                                String value = matcher.group(parameter.name);
                                if (!parameter.getRegexPattern().matches(value)) {
                                        return false;
                                } else {
                                        parameter.setValue(value);
                                }
                        }

                        return true;
                }

                return false;
        }

}