package com.sybrix.easygsp.http.routing;

import com.sybrix.easygsp.http.ParsedRequest;
import com.sybrix.easygsp.http.RequestImpl;
import com.sybrix.easygsp.http.ServletContextImpl;
import jregex.Matcher;
import jregex.Pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 3/14/12
 * Time: 8:20 PM
 */
public class Router {
        private Logger logger = Logger.getLogger(Route.class.getName());
        Pattern methodPattern = new Pattern("(GET|POST|PUT|DELETE|OPTIONS|HEAD|WS|\\*)");
        //Pattern routePattern = new Pattern("^({method}GET|POST|PUT|DELETE|OPTIONS|HEAD|WS|\\*)[(]?({headers}[^)]*)(\\))?\\s+({path}.*/[^\\s]*)\\s+" +
        //        "({action}[^\\s(]+)({params}.+)?(\\s*)$");

        List<Route> routes = new ArrayList<Route>();

        public void addRoute(String method, String path, String action) {
                routes.add(new Route(method, path, action));
        }

        public List<Route> getRoutes() {
                return routes;
        }

        public void addRoute(Route route) {
                routes.add(route);
        }

//        public Route findRequest(RequestImpl request, String requestUri) {
//                Route matchingRoute = null;
//
//                for (Route route : routes) {
//                        if (route.matches(requestUri)) {
//                                boolean allParamsMatched = true;
//                                for (UrlParameter param : route.getParameters().values()) {
//                                        if (!param.getRegexPattern().matches(param.getValue())) {
//                                                allParamsMatched = false;
//                                        }
//                                }
//
//                                if (allParamsMatched) {
//                                        addParameters(request, route.getParameters().values());
//                                        matchingRoute = route;
//                                        break;
//                                }
//                        }
//                }
//
//                //parsedRequest.init();
//
//                return matchingRoute;
//        }

        public void routeRequest(RequestImpl request, ParsedRequest parsedRequest) {
                Route matchingRoute = null;

                for (Route route : routes) {
                        String uri = "/"  + parsedRequest.getRequestURI();
                        if (route.matches(uri) && (route.getMethod().equals("*") || route.getMethod().equalsIgnoreCase((request.getMethod().toUpperCase())))) {
                                logger.finer("routing match found.  pattern: " + route.getPattern().toString()+ ", uri: " + uri);
                                if (route.getController().startsWith("/")) {
                                        parsedRequest.setRequestURI(route.getController().substring(1));
                                } else {
                                        parsedRequest.setRequestURI(route.getController());
                                }

                                parsedRequest.setRequestFilePath(parsedRequest.getAppPath() + route.getController());

                                //parsedRequest.setRequestFilePath(filePath.replace(uri, route.getController()));

                                addParameters(request, route.getParameters().values());
                                break;
                        }
                }
        }

        private void addParameters(RequestImpl request, Collection<UrlParameter> parameters) {
                for (UrlParameter parameter : parameters) {
                        Object obj = request.getParameterMap().get(parameter.getName());

                        if (obj == null) {
                                request.getParameterMap().put(parameter.getName(), parameter.getValue());
                                return;
                        }

                        if (obj instanceof String) {
                                request.getParameterMap().put(parameter.getName(), parameter.getValue());
                        } else {
                                List params = (List) obj;
                                params.add(parameter.getValue());
                        }
                }
        }
}
