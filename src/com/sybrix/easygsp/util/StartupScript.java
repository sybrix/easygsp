package com.sybrix.easygsp.util;

import java.io.File;
import java.io.IOException;

/**
 * StartupScript <br/>
 *
 * @author David Lee
 */
public class StartupScript {
        public static void main(String args[]){
                String appDir = System.getProperty("easygsp.home");
                try {
                        Runtime.getRuntime().exec(appDir + File.separator + "bin" + File.separator + "start.bat");
                } catch (IOException e) {
                        e.printStackTrace(); 
                }
        }
}
