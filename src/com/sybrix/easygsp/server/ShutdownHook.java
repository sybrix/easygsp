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

package com.sybrix.easygsp.server;

import java.util.logging.Logger;

/**
 * ShutdownHookThread <br/>
 *
 * @author David Lee
 */
public class ShutdownHook extends Thread {
        private static final Logger logger = Logger.getLogger(ShutdownHook.class.getName());
        private EasyGServer server;

        public ShutdownHook(EasyGServer server) {
                this.server = server;
        }


        @Override
        public void run() {
                try {
                        logger.info("ShutdownHook invoked...");
                        server.stopServer();
                        //Thread.sleep(5000);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
