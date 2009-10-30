
response.setHeader("Transfer-Encoding", "chunked")
response.flushBuffer()

String word = "What"
String len = word.length();

OutputStream ot = response.getOutputStream();
ot.write((word.length() + "\r\n").getBytes());
ot.write(word.getBytes(),0,word.length());
ot.write("\r\n".getBytes());
ot.flush();

java.lang.Thread.sleep(5000)

word = "hello"
ot.write((word.length() + "\r\n").getBytes());
ot.write(word.getBytes(),0,word.length());
ot.write("\r\n".getBytes());
ot.flush();

java.lang.Thread.sleep(5000)

word = "take that"
ot.write((word.length() + "\r\n").getBytes());
ot.write(word.getBytes(),0,word.length());
ot.write("\r\n".getBytes());
ot.flush();


ot.write("0".getBytes());
ot.write("\r\n".getBytes());
ot.flush();




