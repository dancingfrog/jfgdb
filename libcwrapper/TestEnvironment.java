import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;


public class TestEnvironment {
	private String getCommandOutput(String variable) {
		String result = "";
		try {
			Process p = Runtime.getRuntime().exec("env");
			InputStream s = p.getInputStream();
			BufferedReader d = new BufferedReader(new InputStreamReader(s));
			for (String r = ""; r != null; r = d.readLine()) {
				if (r.startsWith(variable+"=")) {
					result = r;
					break;
				}
			}
			d.close();
		}
		catch (IOException ex) {
		}
		return result;
	}

	@Test
	public void testSetenvChangesJavaEnv() throws Exception {
		int result = Environment.setenv("DISPLAY", "notlikely", true);
		assertEquals(0, result);
		String display = System.getenv("DISPLAY");
		assertEquals("notlikely", display);
		display = System.getenv().get("DISPLAY");
		assertEquals("notlikely", display);
	}

	@Test
	public void testSetenvWithEqualsFail() throws Exception {
		boolean threw = false;
		try {
			int result = Environment.setenv("INVALID=", "someval", true);
			assertEquals(-1, result);
		}
		catch (IllegalArgumentException ex) {
			threw = true;
		}
		assertTrue("Should throw IllegalArgumentException", threw);
		String display = System.getenv("INVALID=");
		assertNull(display);
		String cmdout = getCommandOutput("INVALID");
		assertEquals("", cmdout);
	}

	@Test
	public void testSetenvChangesSubprocessEnv() throws Exception {
		int result = Environment.setenv("FOOBAR", "wibble", true);
		assertEquals(0, result);
		String cmdout = getCommandOutput("FOOBAR");
		assertEquals("FOOBAR=wibble", cmdout);
	}

	@Test
	public void testUnsetenv() throws Exception {
		String original = System.getenv("HOME");
		assertFalse("".equals(original));
		try {
			int result = Environment.unsetenv("HOME");
			assertEquals(0, result);
			assertFalse(Environment.getenv().containsKey("HOME"));
		}
		finally {
			Environment.setenv("HOME", original, true);
		}
	}
}
