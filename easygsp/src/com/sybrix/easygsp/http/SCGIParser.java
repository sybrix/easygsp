package com.sybrix.easygsp.http;

import com.sybrix.easygsp.util.CaseInsensitiveMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * SCGI connector.<br>
 * Version: 1.0<br>
 * Home page: http://snippets.dzone.com/posts/show/4304
 */
public class SCGIParser {
    public static class SCGIException extends IOException {
        private static final long serialVersionUID = 1L;

        public SCGIException(String message) {
            super(message);
        }
    }

    /**
     * Used to decode the headers.
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * Read the <a href="http://python.ca/scgi/protocol.txt">SCGI</a> request headers.<br>
     * After the headers had been loaded,
     * you can read the body of the request manually from the same {@code input} stream:<pre>
     *   // Load the SCGI headers.
     *   Socket clientSocket = socket.accept();
     *   BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream(), 4096);
     *   HashMap<String, String> env = SCGI.parse(bis);
     *   // Read the body of the request.
     *   bis.read(new byte[Integer.parseInt(env.get("CONTENT_LENGTH"))]);
     * </pre>
     *
     * @param input an efficient (buffered) input stream.
     * @return strings passed via the SCGI request.
     */
    public static HashMap<String, String> parse(InputStream input) throws IOException {
        StringBuilder lengthString = new StringBuilder(12);
        String headers = "";
        for (; ;) {
            int c = 0;
//                        Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
//                        while((c = input.read()) != -1){
//                                byte x[] = new byte[1];
//                                if (c == 0)
//                                        System.out.print("<00>");
//                                else
//                                        System.out.print((char)c);
//                        }

            char ch = (char) input.read();

//            if (ch == '0')
//                System.out.print("<00>");
//            else
                //System.out.print(ch);


            if (ch >= '0' && ch <= '9') {
                lengthString.append(ch);

            } else if (ch == ':') {
                int length = Integer.parseInt(lengthString.toString());
                byte[] headersBuf = new byte[length];
                int read = input.read(headersBuf);
                //System.out.println(new String(headersBuf).replaceAll("\u0000","<00>"));


                if (read != headersBuf.length)
                    throw new SCGIException("Couldn't read all the headers (" + length + ").");
                headers = ISO_8859_1.decode(ByteBuffer.wrap(headersBuf)).toString();

                if (input.read() != ',')
                    throw new SCGIException("Wrong SCGI header length: " + lengthString);

                break;
            } else if(((int)ch) >= 65335 ){

            } else {
                lengthString.append(ch);
                 throw new SCGIException("Wrong SCGI header length: " + lengthString);
            }
        }

        HashMap<String, String> env = new CaseInsensitiveMap();
        while (headers.length() != 0) {
            int sep1 = headers.indexOf(0);
            int sep2 = headers.indexOf(0, sep1 + 1);

            if (headers.substring(0, sep1).startsWith("HTTP_")) {
                env.put(headers.substring(0, sep1).substring(5).replaceAll("_", "-"), headers.substring(sep1 + 1, sep2));
            } else {
                env.put(headers.substring(0, sep1).replaceAll("_", "-"), headers.substring(sep1 + 1, sep2));
            }
            headers = headers.substring(sep2 + 1);
        }
        return env;
    }
}
