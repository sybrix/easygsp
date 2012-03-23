package easygsp.image;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.*;

import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.io.*;


/**
 * Thumbnail.java (requires Java 1.2+)
 * Load an image, scale it down and save it as a JPEG file.
 *
 * @author Marco Schmidt
 */
public class ImageResizer {

        public static void resize(String inputFile, String outputFile, int thumbWidth, int thumbHeight, int quality) {
                try {

                        // load image from INFILE
                        Image image = Toolkit.getDefaultToolkit().getImage(inputFile);

                        MediaTracker mediaTracker = new MediaTracker(new Container());
                        mediaTracker.addImage(image, 0);
                        mediaTracker.waitForID(0);
                        // determine thumbnail size from WIDTH and HEIGHT

//                        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
//                        int imageWidth = image.getWidth(null);
//                        int imageHeight = image.getHeight(null);
//                        double imageRatio = (double) imageWidth / (double) imageHeight;
//
//                        if (thumbRatio < imageRatio) {
//                                thumbHeight = (int) (thumbWidth / imageRatio);
//                        } else {
//                                thumbWidth = (int) (thumbHeight * imageRatio);
//                        }
                        // draw original image to thumbnail image object and
                        // scale it to the new size on-the-fly
                        BufferedImage thumbImage = new BufferedImage(thumbWidth,
                                thumbHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics2D graphics2D = thumbImage.createGraphics();
                        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
                        // save thumbnail image to OUTFILE
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
                        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                        JPEGEncodeParam param = encoder.
                                getDefaultJPEGEncodeParam(thumbImage);

                        quality = Math.max(0, Math.min(quality, 100));
                        param.setQuality((float) quality / 100.0f, false);

                        encoder.setJPEGEncodeParam(param);
                        encoder.encode(thumbImage);
                        out.close();

                } catch (Exception e) {
                        throw new RuntimeException("Thumbnail.convert() failed. message:" + e.getMessage(), e);
                }

        }

        /**
         * Resizes the specified image to the specified scale.  1.0
         *
         * @param img    the image
         * @param scaleW the width scale
         * @param scaleH the height scale
         */
        public static BufferedImage resize(Image img, double scaleW, double scaleH, ImageObserver obs) {
                int w = 1024;//img.getWidth(obs);
                int h = 728;//img.getHeight(obs);

                // Determine size of new image.
                // One of them should equal maxDim.
                int sw = (int) (scaleW * w);
                int sh = (int) (scaleH * h);

                // Create an image buffer bin which to paint on.
                BufferedImage bi = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);

                // Set the scale.
                AffineTransform tx = new AffineTransform();
                tx.scale(scaleW, scaleH);

                // Paint image.
                Graphics2D g2d = bi.createGraphics();
                g2d.drawImage(img, tx, obs);
                g2d.dispose();

                return bi;
        }

        /**
         * Writes the specified image to the specified file bin the specified
         * format.  For GIF files, if the format is not supported by the
         * standard Image I/O classes, the class
         * <code>Acme.JPM.Encoders.GifEncoder</code> is used, if it is bin the
         * classpath.
         *
         * @param img    the image object to write
         * @param out    the output stream to write to
         * @param format image format to save as
         * @throws java.io.IOException on I/O errors
         */
        public static void writeImage(BufferedImage img, OutputStream out, String format)
                throws IOException, IllegalArgumentException {
                format = format.toLowerCase();
                Iterator writers = ImageIO.getImageWritersByFormatName(format);
                if (writers == null || !writers.hasNext()) {
                        throw new IllegalArgumentException("Unsupported format:  " + format + "");
                }

                ImageWriter writer = (ImageWriter) writers.next();
                IIOImage iioImg = new IIOImage(img, null, null);
                ImageWriteParam iwparam = writer.getDefaultWriteParam();
                // for JPEGs... set image quality parameters
                if ("jpg".equals(format)) {
//			iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//			iwparam.setCompressionQuality(1.0f);
                }
                writer.setOutput(ImageIO.createImageOutputStream(out));
                writer.write(null, iioImg, iwparam);
        }

        public static ImageDimension getImageDimension(String tempPath) throws Exception {
                ImageDimension point = new ImageDimension();

                ImageInfo ii = new ImageInfo();
                ii.setInput(new FileInputStream(tempPath)); // bin can be InputStream or RandomAccessFile

                if (!ii.check()) {

                        throw new RuntimeException("BadImageException occurred. \"sybrix.image.ImageInfo failed. Image file format on upload not supported. file:\" + " +
                                "tempPath");

//                                Image image = Toolkit.getDefaultToolkit().getImage(tempPath);
//                                int x = 0;
//                                // wait until the image dimensions are determined
//                                while (image.getHeight(null) == -1 || image.getWidth(null) == -1 || x >= 10) {
//                                        Thread.sleep(500);
//                                        x++;
//
//                                        if (x == 10)
//                                                break;
//                                }
//
//                                point.width = image.getWidth(null);
//                                point.height = image.getHeight(null);

                } else {
                        point.width = ii.getWidth();
                        point.height = ii.getHeight();
                }

                return point;
        }

        public static ImageDimension getResizedDimensionsByWidth(int desiredWidth, int maxHeight, ImageDimension original, boolean checkHeight) {
                double newWidth = desiredWidth;
                double originalWidth = original.width;
                double ratio = newWidth / originalWidth;
                //double newHeight = Math.floor(original.height * ratio);
                double newHeight = new BigDecimal(original.height * ratio).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

                // this code block checks to see if the image is smaller than the thumbNail size, if so use the original dimensions
                if (original.width <= desiredWidth && original.height <= maxHeight) {
                        ImageDimension x = new ImageDimension();
                        x.height = original.height;
                        x.width = original.width;
                        return x;
                }

                // if original width is less than standard thumbnail width, then go with original
                if (original.width < desiredWidth)
                        newWidth = original.width;
                // do this only for thumbNails
                if (checkHeight) {
                        // for thumbnails height is especially important
                        //int maxHeight = (Integer) Context.getInstance().getAttribute(Constants.PHOTO_ALBUM_THUMBNAIL_MAX_HEIGHT);

                        if (newHeight > maxHeight) {
                                return getResizedDimensionsByHeight(maxHeight, original);
                        }
                }

                ImageDimension x = new ImageDimension();
                x.width = (int)Math.ceil(newWidth);
                x.height = (int)Math.ceil(newHeight);

                return x;
        }


        public static ImageDimension getResizedDimensionsByHeight(int desiredHeight, ImageDimension original) {

                double newHeight = desiredHeight;
                double originalHeight = original.height;
                double ratio = newHeight / originalHeight;

                double newWidth = new BigDecimal(original.width * ratio).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

                // if original width is less than standard thumbnail width, then go with original
                if (original.height < desiredHeight)
                        newHeight = original.height;

                ImageDimension x = new ImageDimension();
                x.width = (int)Math.ceil(newWidth);
                x.height = (int)Math.ceil(newHeight);

                return x;
        }




        public static String downloadFile(String url) {
                FileOutputStream file = null;
                String filePath = "";
                try {
                        URL p = new URL(url);

                        BufferedInputStream bin = new BufferedInputStream(p.openStream());

                        filePath = System.getProperty("java.io.tmpdir") + p.getFile().substring(1);
                        String _filePath = filePath.replaceAll("/", File.separator + File.separator);
                        File f = new File(_filePath);
                        f.getParentFile().mkdirs();

                        file = new FileOutputStream(f);
                        BufferedOutputStream out = new BufferedOutputStream(file);

                        for (int b; (b = bin.read()) != -1; ) {
                                out.write(b);
                        }

                        out.flush();
                        file.close();
                        bin.close();

                        return _filePath;
                } catch (Exception e) {
                        throw new RuntimeException("Download file failed.  " + e.getMessage(), e);
                }
        }
}
