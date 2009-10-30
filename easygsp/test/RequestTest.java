import junit.framework.TestCase;

import javax.servlet.http.Cookie;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * CookieTest <br/>
 * Description :
 */
public class RequestTest extends TestCase {
        //        String s = "HTTP_USER_AGENT = Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.4) Gecko/2008102920 Firefox/3.0.4\n" +
        //                "HTTP_ACCEPT_LANGUAGE = en-us,en;q=0.5\n" +
        //                "HTTP_ACCEPT = text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n" +
        //                "HTTP_KEEP_ALIVE = 300\n" +
        //                "REMOTE_PORT = 2996\n" +
        //                "HTTP_ACCEPT_ENCODING = gzip,deflate\n" +
        //                "SERVER_NAME = localhost:8085\n" +
        //                "SERVER_SOFTWARE = LightTPD/1.4.20-1 (Win32)\n" +
        //                "REDIRECT_STATUS = 200\n" +
        //                "SCRIPT_FILENAME = HTDOCS/test.groovy\n" +
        //                "HTTP_COOKIE = tracker=98C23CD6EE2C6B9C0902FDFB1B2D35F3\n" +
        //                "SERVER_ADDR = 127.0.0.1\n" +
        //                "SERVER_PROTOCOL = HTTP/1.1\n" +
        //                "REQUEST_METHOD = GET\n" +
        //                "SERVER_PORT = 8085\n" +
        //                "SCRIPT_NAME = /test.groovy\n" +
        //                "REMOTE_ADDR = 127.0.0.1\n" +
        //                "DOCUMENT_ROOT = HTDOCS/\n" +
        //                "HTTP_ACCEPT_CHARSET = ISO-8859-1,utf-8;q=0.7,*;q=0.7\n" +
        //                "HTTP_CONNECTION = keep-alive\n" +
        //                "HTTP_HOST = localhost:8085\n" +
        //                "PATH_INFO =\n" +
        //                "SCGI = 1\n" +
        //                "QUERY_STRING =\n" +
        //                "GATEWAY_INTERFACE = CGI/1.1\n" +
        //                "CONTENT_LENGTH = 01\n" +
        //                "COOKIE = JSESSIONID=abc123\n" +
        //                "REQUEST_URI = /test.groovy\n\n";

        public void testRequestObject() {
                ByteArrayInputStream bis = new ByteArrayInputStream("".getBytes());
                try {

                        try {              //http://www.bumblezee.com:8080/admin-toaster
                                Socket socket = new Socket("www.bumblezee.com", 80);

                                socket.getOutputStream().write("GET /index.jsp HTTP/1.1\r\nAccept-Language:zh-cn,en-us;q=0.7,en;q=0.3\r\nAccept-Charset:ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\nHost:bumblezee.com:80\r\n\r\n".getBytes());
                                socket.getOutputStream().flush();

                                InputStream is = socket.getInputStream();

                                try {
                                        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                                        String str;
                                        while ((str = rd.readLine()) != null) {
                                                System.out.println(str);
                                        }
                                        rd.close();
                                } catch (IOException e) {
                                }

                                is.close();
                        } catch (MalformedURLException me) {
                                System.out.println("MalformedURLException: " + me);
                        } catch (IOException ioe) {
                                System.out.println("IOException: " + ioe);
                        }


//                        Map headers = new HashMap();
//                        headers.put("COOKIE", "JSESSIONID=123abc");
//                        Request request = new Request(bis, headers, new Application("app1"));
//                        HttpSession session = request.getSession();

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public void testCookie() {
                Cookie cookie = new Cookie("lastname", "smith");
                Calendar dt = Calendar.getInstance();
                dt.set(Calendar.YEAR, 2010);
                cookie.setMaxAge(2 * 24 * 60 * 60);
                cookie.setVersion(0);
                cookie.setSecure(true);
                cookie.setDomain(".bumblezee.com");
                cookie.setComment("what is this");
                cookie.setPath("/ldc");

                List cookies = new ArrayList();
                cookies.add(cookie);
                cookies.add(cookie);

                //System.out.println(CookieUtil.formatCookieHeaders(cookies));

        }

        public void testDomainRegEx() {
                String s = "";

                String regex = "(.)+\\.frameworkfaceoff\\.com";
                String regex1 = "frameworkfaceoff\\.com";
                assertTrue("domain does not match 1", "l.www.frameworkfaceoff.com".matches(regex));
//                assertTrue("domain does not match 2", "frameworkfaceoff.com".matches(regex));
//                assertTrue("domain does not match 3", "w.frameworkfaceoff.com".matches(regex));
                assertTrue("domain does not match 4", "wframeworkfaceoff.com".matches(regex1) == false);

        }
}
