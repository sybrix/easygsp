import java.io.FileInputStream;
import java.io.OutputStream;


        
       String filename = "IMG_0089.jpg"


       // Set content type
       response.setContentType("image/jpeg");

       // Set content size
       File file = new File(application.appPath + "\\" + filename);
        System.out.println file.path

       response.setContentLength((int)file.length());
       response.flushBuffer();

       // Open the file and output streams
       FileInputStream inp = new FileInputStream(file);
       OutputStream out = response.getBufferedOutputStream();

        long x = System.currentTimeMillis()
        System.out.println x
       // Copy the contents of the file to the output stream
       byte[] buf = new byte[1024];
       int count = 0;
       while ((count = inp.read(buf)) >= 0) {
           out.write(buf, 0, count);
       }

        System.out.println " time taken:" + (System.currentTimeMillis()-x)

       inp.close();
       out.close();
