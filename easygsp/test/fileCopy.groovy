def APPDIR = System.getenv("EASYGSP_HOME")
static long copyCount = 0;

Properties p = new Properties();
FileInputStream fis = new FileInputStream(APPDIR + File.separator + "conf" + File.separator + "server.properties")
p.load(fis)
fis.close()

def home =  p.get('groovy.webapp.dir').replace("/",File.separator)

def newDir = home + File.separator + args[1]

new File(newDir).mkdirs()

println newDir

copyDir home + File.separator + "template", home + File.separator + arg[2], false

public class FileCopy {
        static long copyCount = 0;

 	public static void copyFile(String sourceFile, String destFile) throws IOException {
                copyFile(new File(sourceFile), new File(destFile), false, true);
        }


        public static void copyFile(String sourceFile, String destFile, boolean overwrite, boolean preserveLastModified) throws IOException {
                copyFile(new File(sourceFile), new File(destFile), overwrite, preserveLastModified);
        }

        public static void copyFile(File sourceFile, File destFile,
                boolean overwrite, boolean preserveLastModified)  throws IOException {

                if (overwrite || !destFile.exists() || destFile.lastModified() < sourceFile.lastModified()) {

                        if (destFile.exists() && destFile.isFile()) {
                                destFile.delete();
                        }

                        // ensure that parent dir of dest file exists!
                        // not using getParentFile method to stay 1.1 compat
                        File parent = new File(destFile.getParent());
                        if (!parent.exists()) {
                                parent.mkdirs();
                        }


                        FileInputStream fis = new FileInputStream(sourceFile);
                        FileOutputStream fos = new FileOutputStream(destFile);

                        byte[] buffer = new byte[8 * 1024];
			 int count = 0;

                        while (count != -1) {
				count = fis.read(buffer, 0, buffer.length);
                                fos.write(buffer, 0, count);
                        } ;

                        fis.close();
                        fos.close();


                        if (preserveLastModified) {
                                destFile.setLastModified(sourceFile.lastModified());
                        }
                }

        }


        public static void copyDir(String source, String dest, boolean showOutput) throws IOException{
                copyCount = 0;
                getFiles(source, dest, source.length(), showOutput);
                if (showOutput)
                        System.out.println(copyCount + " file(s) copied");
        }

        public static void getFiles(String source, String dest, int srcLenIndex, boolean showOutput) throws IOException {
                File sourceDir = new File(source);
                File[] files = sourceDir.listFiles();

                for(int x=0;x<sourceDir.list().length;x++){
                        if(files[x].isDirectory()){
                                new File(dest + files[x].getAbsolutePath().substring(srcLenIndex)).mkdirs();
                                getFiles(files[x].getAbsolutePath(), dest, srcLenIndex, showOutput);
                        } else {
                                if (showOutput)
                                        System.out.println(dest + files[x].getAbsolutePath().substring(srcLenIndex));

                                FileCopy.copyFile(files[x].getAbsolutePath(), dest + files[x].getAbsolutePath().substring(srcLenIndex), true, true);
                                copyCount++;
                        }
                }
        }

}
