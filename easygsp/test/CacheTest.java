import junit.framework.TestCase;
import com.sybrix.easygsp.http.ApplicationCache;
import org.apache.jcs.access.exception.CacheException;

/**
 * CacheTest <br/>
 * Description :
 */
public class CacheTest extends TestCase {
        public void testAppCache() throws CacheException {
               // ApplicationCache.getInstance().put("test", new Long("1"));

                Long l = (Long) ApplicationCache.getInstance().get("1", "test2");
                assertTrue(l == 1);
        }

//        public void testDisk() throws CacheException {
//                for (int i = 0; i < 100; i++) {
//                        ApplicationCache.getInstance().put("test"+i, new Long(i));
//                }
//
//                Long l = (Long) ApplicationCache.getInstance().get("test1");
//                assertTrue(l == 0);
//        }
}
