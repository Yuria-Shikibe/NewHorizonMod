package newhorizon.util.generator;

public class BulletGen {
    public static int indentation = 0;
    public static StringBuilder code = new StringBuilder();



    public static void forward(){
        indentation++;
    }

    public static void backward(){
        indentation--;
    }

    public static void lineBreak(){
        code.append('\n');
    }
}
