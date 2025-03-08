import java.io.*;
//damit ich das String in einem Datei schreibn kann


public class Aufgabe04anderung {
    public static void main(String... args) throws IOException {
      OutputStream os = new FileOutputStream("output.txt");
        writeSomething(System.out);
    }

    public static void writeSomething(OutputStream out) {
        // Korrekte Großschreibung der Klassen
      Writer osw = new OutputStreamWriter(out);  
      PrintWriter pw = new PrintWriter(osw);

        // Verwenden von PrintWriter für die Ausgabe
        for (int i = 0; i < 10; ++i) {
            pw.println("hallole");
        }

        // Nur flush verwenden, um sicherzustellen, dass alle Daten geschrieben werden
        pw.flush();
        // pw.close(); // Nicht schließen, da wir System.out nicht schließen wollen
        pw.close();   
    }
}
