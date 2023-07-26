import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class rpal20 {
    public static void main(String[] args) {
        try {
            final Process p = Runtime.getRuntime().exec("./rpal20 " + args[0]);

            new Thread(new Runnable() {
                public void run() {
                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = null;

                    try {
                        while ((line = input.readLine()) != null)
                            System.out.println(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            p.waitFor();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
