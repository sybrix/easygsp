package com.sybrix.easygsp.server;

import java.util.logging.Logger;

/**
 * ShutdownHookThread <br/>
 *
 * @author David Lee
 */
public class ShutdownHook extends Thread {
        private static final Logger log = Logger.getLogger(ShutdownHook.class.getName());
        private EasyGServer server;

        public ShutdownHook(EasyGServer server) {
                this.server = server;
        }


        @Override
        public void run() {
                try {
                        log.info("ShutdownHook invoked...");
                        server.stopServer();
                        //Thread.sleep(5000);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
