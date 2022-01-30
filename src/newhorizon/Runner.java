package newhorizon;

import java.util.function.Function;

public class Runner{
	public static void main(String[] args){
		System.out.println(1 / 2);
		System.out.println(3 / 2);
		
		Function<String, String> s = String::toLowerCase;
		
		System.out.println(1.0E10f);
		
		
	}
}

class Tester{
	static{
		System.out.print("Inited");
	}
}