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

import com.sybrix.easygsp.server.EasyGServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ConsoleServer <br/>
 * Description :
 */
public class ConsoleServer extends Thread {
        private static final Logger logger = Logger.getLogger(ConsoleServer.class.getName());
        private volatile boolean stopRequested = false;
        private ServerSocket serverSocket;
        private List<OutputStream> outputStreams;
        private PrintStream consoleOutPrintStream, consoleErrPrintStream;

        public ConsoleServer(){
               super("ConsoleServer Thread");
        }

        public void run() {
                try {
                        serverSocket = new ServerSocket(EasyGServer.propertiesFile.getInt("console.server.port", 4447));
                } catch (IOException e) {
                        logger.severe("IOException instantiating serverSocket in ConsoleServer.  " + e.getMessage());
                }
                outputStreams = new ArrayList();
                consoleOutPrintStream = System.out;
                consoleErrPrintStream = System.err;

                System.setOut(new PrintStream(new CustomStream(consoleErrPrintStream, true)));
                System.setErr(new PrintStream(new CustomStream(consoleOutPrintStream, false)));

                while (!stopRequested) {
                        try {
                                Socket socket = serverSocket.accept();
                                if (isStopped())
                                        break;

                                outputStreams.clear();
                                outputStreams.add(new BufferedOutputStream(socket.getOutputStream()));
                        } catch (Exception e) {

                        }
                }
        }

        private boolean isStopped() {
                return false;
        }

        public void stopThread() {
                stopRequested = true;
                try {
                        serverSocket.close();
                } catch (IOException e) {

                }
        }

        class CustomStream extends FilterOutputStream {
                private boolean errorStream = false;

                public CustomStream(OutputStream aStream, boolean isErrorStream) {
                        super(aStream);
                        errorStream = isErrorStream;
                }

                public void write(byte b[]) throws IOException {
                        String aString = new String(b);
                        if (errorStream)
                                consoleErrPrintStream.write(b);
                        else
                                consoleOutPrintStream.write(b);

                        try {
                                for (OutputStream outputStream : outputStreams) {
                                        outputStream.write(b);
                                }
                        } catch (Exception e) {
                                outputStreams.clear();
                        }
                }

                public void write(byte b[], int off, int len) throws IOException {
                        String aString = new String(b, off, len);

                        if (errorStream)
                                consoleErrPrintStream.write(b, off, len);
                        else
                                consoleOutPrintStream.write(b, off, len);
                        try {
                                for (OutputStream outputStream : outputStreams) {
                                        outputStream.write(b, off, len);
                                }
                        } catch (Exception e) {
                                outputStreams.clear();
                        }

                }


        }

}