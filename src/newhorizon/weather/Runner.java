package newhorizon.weather;

public class Runner{
	public static void main(String[] args){
		StringBuilder builder = new StringBuilder();
		
		for(int i = 1; i < 250; i++){
			builder.append("-").append(i).append("-").append(2);
		}
		
		System.out.println(builder.toString().replaceFirst("-", ""));
	}
}
