package newhorizon;

public class Runner{
	public static final double MIN = 0, MAX = 2 * Math.E;
	
	public static final double STEP = 0.05;
	
	public static void main(String[] args){
		for(double i = MIN; i < MAX; i += STEP){
			print("ln(" + i + ")", Math.log(i));
			print("exp(" + i + ")", Math.exp(i));
		}
	}
	
	static void print(String expression, double output){
		System.out.println(expression + " = " + output);
	}
}