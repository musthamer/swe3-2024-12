import java.io.* ; 

public class Copy {
    public static void main(String... args) throws IOException {
       // System.in.transferTo(System.out);
       
      FileInputStream input = new FileInputStream("1G");
      FileOutputStream output = new FileOutputStream ("1G2");
      copyFast(input,output);

    }

/*    static void copySlow(InputStream in, OutputStream out) throws IOException {
        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }
        out.flush();
    }
*/
    static void copyFast(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[1024 * 4];
        while ((in.read(b)) != -1) {
            out.write(b);
        }
        out.flush();
    } 
}

