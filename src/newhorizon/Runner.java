package newhorizon;

import java.io.IOException;

public class Runner{
	public static void main(String[] args){
		
		
		try{
			Runtime.getRuntime().exec("cmd /c cd C:\\Users\\Administrator");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}

class Tester{
	static{
		System.out.print("Inited");
	}
}