import java.io.*;

public class wasistinput {
    public static void main(String... args) throws IOException {
        InputStream in = System.in;
        int data = in.read(); // قراءة أول بايت من المدخلات
        System.out.println("Data read: " + data); // طباعة القيمة المقروءة (كقيمة عددية)
    }
}

