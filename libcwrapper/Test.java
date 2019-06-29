import java.util.Map;
import java.util.HashMap;

public class Test
{
	public static void main (String [] args)
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put("FOO", "poo");
		map.remove("Heh");
		map.remove("Heh");
		map.remove("Heh");
		map.remove("Heh");
		map.remove("Heh");
		System.out.println("All Done");
	}
}
