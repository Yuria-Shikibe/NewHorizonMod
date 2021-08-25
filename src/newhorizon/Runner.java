package newhorizon;

public class Runner{
	public static void main(String[] args){
		int[] arr = new int[10];
		
		for(int i = 1; i <= 166; i++){
			char[] num = String.valueOf(i).toCharArray();
			for(char c : num){
				arr[Integer.parseInt(String.valueOf(c))]++;
			}
		}
		
		for(int i = 0; i < arr.length; i++){
			System.out.println("Number of " + i + ": " + arr[i]);
		}
	}
}