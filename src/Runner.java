public class Runner{
	public static void main(String[] args){
		System.out.println(B.class.isAssignableFrom(A.class));
	}
}

class A implements B{

}

interface B{

}