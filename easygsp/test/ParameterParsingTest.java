import junit.framework.TestCase;
import org.apache.http.entity.InputStreamEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.sybrix.easygsp.http.ParsingUtil;

/**
 * ParameterParsingTest <br/>
 * Description :
 */
public class ParameterParsingTest extends TestCase {

        public void testForLoop(){
                Map<String, Integer> data = new HashMap();
                data.put("a", 1);
                data.put("b", 2);
                data.put("c", 3);

                for(String key: data.keySet()){
                          if (data.get(key) instanceof Integer){
                                  data.put(key, 5);
                          }
                }

        }
        public void testQueryString() {
                try {
                        Map parameters = ParsingUtil.parse("a=1&b=2&c=3&c=4", "utf-8");

                        assertEquals(3, parameters.size());
                        assertEquals("1", parameters.get("a"));

                        assertEquals(2, ((List)parameters.get("c")).size());


                } catch (Exception e) {
                        e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
                }

        }

        public void testParseFromInputStream() {
                try {
                        String data = "a=1&b=2&c=3";
                        InputStreamEntity ise = new InputStreamEntity(new ByteArrayInputStream(data.getBytes()), data.getBytes().length);
                        ise.setContentType("application/x-www-form-urlencoded");
                        Map parameters = ParsingUtil.parse(ise);

                        assertEquals(3, parameters.size());
                        assertEquals(parameters.get("a"), "1");


                } catch (IOException e) {
                        e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
                }

        }
}
