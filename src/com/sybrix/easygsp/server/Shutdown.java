package com.sybrix.easygsp.server;

import com.sybrix.easygsp.util.PropertiesFile;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.File;

/**
 * Shutdown <br/>
 * Description :
 */
public class Shutdown implements Runnable {
        public Shutdown() {
        }

        public static void main(String args[]) {
                doIt(args);
        }

        private static EasyGServer server;

        public Shutdown(EasyGServer server) {
                this.server = server;
        }

        public static void doIt(String args[]) {
                String APP_DIR = System.getProperty("user.dir");

                if (args.length > 0) {
                        APP_DIR = args[0];
                        if (APP_DIR.endsWith(File.separator)) {
                                APP_DIR = APP_DIR.substring(0, APP_DIR.length() - 1);
                        }
                } else {
                        APP_DIR = System.getProperty("easygsp.home");
                        if (APP_DIR != null) {
                                if (APP_DIR.endsWith(File.separator)) {
                                        APP_DIR = APP_DIR.substring(0, APP_DIR.length() - 1);
                                }
                        }
                }

                PropertiesFile propertiesFile = new PropertiesFile(APP_DIR + File.separator + "conf" + File.separator + "server.properties");
                try {
                        Socket s = new Socket("localhost", propertiesFile.getInt("shutdown.port"));
                        s.getOutputStream().write("1".getBytes());
                        s.getOutputStream().flush();
                        s.close();


                } catch (IOException e) {
                        e.printStackTrace();
                }
                System.exit(0);
        }

        public void run() {
                int port = EasyGServer.propertiesFile.getInt("shutdown.port");
                try {
                        ServerSocket ss = new ServerSocket(port);

                        Socket s = ss.accept();
                        server.stopServer();

                        //
                        Socket stopSocket = new Socket("localhost", EasyGServer.propertiesFile.getInt("server.port"));
                        stopSocket.close();

                } catch (Exception e) {
                        e.printStackTrace();
                }

        }

}
//
//public class Shutdown implements Runnable {
//        private String args[];
//
//        public static void main(String args[]) {
//                doIt(args);
//        }
//
//        public Shutdown(String args[]) {
//                this.args = args;
//        }
//
//        public void run() {
//                doIt(args);
//        }
//
//        public static void doIt(String args[]) {
//                String APP_DIR = System.getProperty("user.dir");
//
//                if (args.length > 0) {
//                        APP_DIR = args[0];
//                        if (APP_DIR.endsWith(File.separator)) {
//                                APP_DIR = APP_DIR.substring(0, APP_DIR.length() - 1);
//                        }
//                } else {
//                        APP_DIR = System.getProperty("easygsp.home");
//                        if (APP_DIR != null) {
//                                if (APP_DIR.endsWith(File.separator)) {
//                                        APP_DIR = APP_DIR.substring(0, APP_DIR.length() - 1);
//                                }
//                        }
//                }
//
//                PropertiesFile propertiesFile = new PropertiesFile(APP_DIR + File.separator + "conf" + File.separator + "server.properties");
//                try {
//                        Socket s = new Socket("localhost", propertiesFile.getInt("shutdown.port"));
//                        s.getOutputStream().write("1".getBytes());
//                        s.getOutputStream().flush();
//                        s.close();
//
//
//                } catch (IOException e) {
//                        e.printStackTrace();
//                }
//                System.exit(0);
//        }
//
//}
