package easygsp.image;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 1/28/12
 * Time: 7:50 PM
 */
public class ImageDimension {

        ImageDimension() {
        }

        ImageDimension(Integer width, Integer height) {
                this.width = width;
                this.height = height;
        }

        public Integer getWidth() {
                return width;
        }

        public void setWidth(Integer width) {
                this.width = width;
        }

        public Integer getHeight() {
                return height;
        }

        public void setHeight(Integer height) {
                this.height = height;
        }

        Integer width;
        Integer height;
}
