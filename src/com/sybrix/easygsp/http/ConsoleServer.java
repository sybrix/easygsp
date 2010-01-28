package com.sybrix.easygsp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * ConsoleServer <br/>
 * Description :
 */
public class ConsoleServer extends Thread {
        private boolean stopRequested = false;
        private ServerSocket serverSocket;
        private List<OutputStream> outputStreams;
        private PrintStream consoleOutPrintStream, consoleErrPrintStream;


        public void run() {
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