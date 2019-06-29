import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class Environment {
    /*
    // for JNI:
    public static class LibC {
        public native int setenv(String name, String value, int overwrite);
        public native int unsetenv(String name);

        LibC() {
            System.loadLibrary("Environment_LibC");
        }
    }
    static LibC libc = new LibC();
    */

    public interface WinLibC extends Library {
        public int _putenv(String name);
    }
    public interface LinuxLibC extends Library {
        public int setenv(String name, String value, int overwrite);
        public int unsetenv(String name);
    }

    static public class POSIX {
        static Object libc;
        static {
            if (System.getProperty("os.name").equals("Linux")) {
                libc = Native.loadLibrary("c", LinuxLibC.class);
            } else {
                libc = Native.loadLibrary("msvcrt", WinLibC.class);
            }
        }

        public int setenv(String name, String value, int overwrite) {
            if (libc instanceof LinuxLibC) {
                return ((LinuxLibC)libc).setenv(name, value, overwrite);
            }
            else {
                return ((WinLibC)libc)._putenv(name + "=" + value);
            }
        }

        public int unsetenv(String name) {
            if (libc instanceof LinuxLibC) {
                return ((LinuxLibC)libc).unsetenv(name);
            }
            else {
                return ((WinLibC)libc)._putenv(name + "=");
            }
        }
    }

    static POSIX libc = new POSIX();

    public static int unsetenv(String name) {
        Map<String, String> map = getenv();
        map.remove(name);
        Map<String, String> env2 = getwinenv();
        env2.remove(name);
        return libc.unsetenv(name);
    }

    public static int setenv(String name, String value, boolean overwrite) {
        if (name.lastIndexOf("=") != -1) {
            throw new IllegalArgumentException("Environment variable cannot contain '='");
        }
        Map<String, String> map = getenv();
        boolean contains = map.containsKey(name);
        if (!contains || overwrite) {
            map.put(name, value);
            Map<String, String> env2 = getwinenv();
            env2.put(name, value);
        }
        return libc.setenv(name, value, overwrite?1:0);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getwinenv() {
        try {
            Class<?> sc = Class.forName("java.lang.ProcessEnvironment");
            Field caseinsensitive = sc.getDeclaredField("theCaseInsensitiveEnvironment");
            caseinsensitive.setAccessible(true);
            return (Map<String, String>)caseinsensitive.get(null);
        }
        catch (Exception e) {
        }
        return new HashMap<String, String>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getenv() {
        try {
            Map<String, String> theUnmodifiableEnvironment = System.getenv();
            Class<?> cu = theUnmodifiableEnvironment.getClass();
            Field m = cu.getDeclaredField("m");
            m.setAccessible(true);
            return (Map<String, String>)m.get(theUnmodifiableEnvironment);
        }
        catch (Exception ex2) {
        }
        return new HashMap<String, String>();
    }

}
