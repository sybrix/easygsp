package com.sybrix.easygsp.server;

/**
 * ShutdownHookThread <br/>
 *
 * @author David Lee
 */
public class ShutdownHook extends Thread {
        private EasyGServer server;

        public ShutdownHook(EasyGServer server) {
                this.server = server;
        }


        @Override
        public void run() {
                try {
                        server.getServerSocket().close();
                        Thread.sleep(5000);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
