import java.io.*;
public class datei{
  public static void main (String...args) throws IOException {
    OutputStream os = new FileOutputStream8("output.txt");
    PrintStream ps = new PrintStream(os);
    ps.println("");

  }
}
