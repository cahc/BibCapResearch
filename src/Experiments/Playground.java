package Experiments;

public class Playground {

    public static void main(String[] arg) {

        SimChecker simChecker = new SimChecker();
        SimChecker.simVectorSimple vec = simChecker.new simVectorSimple("9 0.33 2 0.1 5 0.99 1 1 ");

        System.out.println(vec);

    }
}
