import java.util.Scanner;

public class Runner{
	public static void main(String[] args){
		while(true){
			Scanner in = new Scanner(System.in);
			String s = in.next();
			if(s.equals("e"))break;
			
			String[] num = s.split(",");
			for(String i : num){
				System.out.print(Integer.toHexString(Integer.parseInt(i)));
			}
			
			System.out.println();
		}
	}
}
