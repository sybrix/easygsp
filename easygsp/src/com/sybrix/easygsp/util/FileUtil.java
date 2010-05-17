package com.sybrix.easygsp.util;

import java.io.*;

/**
 * FileUtil <br/>
 *
 * @author David Lee
 */
public class FileUtil {
        public static void copy(String sourceLocation, String targetLocation) throws IOException {
                copyDirectory(new File(sourceLocation), new File(targetLocation));

        }
        
        public static boolean deleteDirectory(String path) {
                return deleteDirectory(new File(path));
        }

        public static boolean deleteDirectory(File path) {
                if (path.exists()) {
                        File[] files = path.listFiles();
                        for (int i = 0; i < files.length; i++) {
                                if (files[i].isDirectory()) {
                                        deleteDirectory(files[i]);
                                } else {
                                        files[i].delete();
                                }
                        }
                }
                return (path.delete());
        }

        public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

                if (sourceLocation.isDirectory()) {
                        if (!targetLocation.exists()) {
                                targetLocation.mkdir();
                        }

                        String[] children = sourceLocation.list();
                        for (int i = 0; i < children.length; i++) {
                                copyDirectory(new File(sourceLocation, children[i]),
                                        new File(targetLocation, children[i]));
                        }
                } else {
                        InputStream in = new FileInputStream(sourceLocation);
                        OutputStream out = new FileOutputStream(targetLocation);

                        // Copy the bits from instream to outstream
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();
                }
        }
}
