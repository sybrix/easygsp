package easygsp;

import javax.activation.DataSource;
import java.io.*;

public class ByteArrayDataSource implements DataSource {
        private ByteArrayInputStream stream;
        private String contentType;
        private String name;
        private ByteArrayOutputStream outstream;


        public ByteArrayDataSource(String name, byte[] content, String contentType) {
                stream = new ByteArrayInputStream(content);
                outstream = new ByteArrayOutputStream();
                this.contentType = contentType;
                this.name = name;

                try {
                        outstream.write(content);
                } catch (IOException e) {

                }
        }

        public void setStream(ByteArrayInputStream stream) {
                this.stream = stream;
        }

        public void setContentType(String contentType) {
                this.contentType = contentType;
        }

        public void setName(String name) {
                this.name = name;
        }

        public ByteArrayInputStream getStream() {
                return stream;
        }

        public String getContentType() {
                return contentType;
        }

        public InputStream getInputStream() {
                return stream;
        }

        public String getName() {
                return name;
        }

        public OutputStream getOutputStream() {
                return outstream;
        }
}