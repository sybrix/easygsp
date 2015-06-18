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
