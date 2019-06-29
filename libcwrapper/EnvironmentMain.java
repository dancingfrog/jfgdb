import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EnvironmentMain {
    public static void main(String[] args) {
        System.out.println("System.getenv(\"DISPLAY\") => " + System.getenv("DISPLAY"));
        Environment.unsetenv("DISPLAY");
        System.out.println("post unsetenv, System.getenv(\"DISPLAY\") => " + System.getenv("DISPLAY"));
        Environment.setenv("DISPLAY", ":0.0", false);
        Environment.setenv("FOOBAR", "wibble", true);
        try {
            Environment.setenv("INVALID=", ":0.0", false);
        }
        catch (IllegalArgumentException ex) {
            System.out.println("Did not set INVALID=");
        }

        try {
            Process p = Runtime.getRuntime().exec("./test FOOBAR");
            //Process p = Runtime.getRuntime().exec("env");
            InputStream s = p.getInputStream();
            BufferedReader d = new BufferedReader(new InputStreamReader(s));
            System.out.println("C environment:");
            for (String r = ""; r != null; r = d.readLine()) {
                if (r.startsWith("FOOBAR")) {
                    System.out.println(r);
                }
            }
            d.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
