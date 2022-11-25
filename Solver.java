public class Solver {
    String algorithm;
    String varOrder;
    String valOrder;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Solver <*.csp> <fc|mac> <asc|sdf> <asc>") ;
            return ;
        }
        for (String string : args) {
            System.out.println(string);
        }
    }
}
