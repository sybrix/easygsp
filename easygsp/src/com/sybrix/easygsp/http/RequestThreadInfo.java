package com.sybrix.easygsp.http;

/**
 * RequestThreadInfo <br/>
 *
 * @author David Lee
 */
public class RequestThreadInfo {
        private static final ThreadLocal<RequestInfo> _id = new ThreadLocal<RequestInfo>(){
                protected RequestInfo initialValue() {
                        return new RequestInfo();
                }
        };

        public static RequestInfo get(){                       
                return _id.get();
        }
        protected static void set(RequestInfo id){
                _id.set(id);
        }
}
