import junit.framework.TestCase;
import groovy.lang.GroovyObject;

import com.sybrix.easygsp.http.GSE3;

/**
 * ScriptTest <br/>
 * Description :
 */
public class ScriptTest extends TestCase {

//        public void testIncludeRegex() {
//                String template = "dasdas dasdas dasdsad dfadsad <%@ include file=\"temp\\fidsadsa\\das1le.gsp\" %>" +
//                        "ok ook fsdf sffd dasdas dasdas dasdsad dfadsad <%@ include file=\"temp\\fil$ e2.g?sp\"%>" +
//                        "ok ook fsdf sffd ";
//
//                //template = "<%@";
//
//                //\w|\\|\//|.
//                Pattern p = Pattern.compile("<%@\\s*include\\s*file\\s*=\\s*\"([\\w|\\\\|\\//|~|.|0-9|\\$|\\s|\\:|\\,|\\?])+\"\\s*%>");
//                Matcher m = p.matcher(template);
//
//                String s;
//                int i=0;
//                while(m.find()){
//                        System.out.println("found start: " + m.start() + " end: " + m.end());
//                        String include = template.substring(m.start(), m.end());
//                        String file[] = include.split("\"");
//                        System.out.println(file[1]);
//                }
//
//        }

        public void testLoadScript() {
                try {
                        GSE3 gse = new GSE3(new String[]{"C:/projects/scgi/webapps/app1/WEB-INF"});

                        //GroovyClassLoader gcl = new GroovyClassLoader();
                        //gcl.addClasspath("c:/projects/scgi/webapps/app1/WEB-INF");


                        //  GroovyCodeSource gcs = new GroovyCodeSource(new URL("file://c:/projects/scgi/webapps/app1/WEB-INF/web"));

                        //Class cls = gse.loadScriptByName("web");

                        //Class cls = gcl.parseClass("web");
                        //GroovyObject go = (GroovyObject) cls.newInstance();
                                                Class clazz = gse.loadScriptByName("web");
                                                GroovyObject go = (GroovyObject)clazz.newInstance();

                        go.invokeMethod("onApplicationStart", new Object[]{null});
                        go.invokeMethod("onSessionStart", new Object[]{null});


                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
