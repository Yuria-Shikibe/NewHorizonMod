package newhorizon;

public class Runner{
	public static void main(String[] args){
		String s = "123\n\n123";
		
		System.out.println(s);
		
		System.out.println(":\\");
		
		System.out.println(s.replaceAll("\n", "\\\\n"));
	}
}