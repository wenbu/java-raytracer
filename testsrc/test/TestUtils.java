package test;

import java.util.Random;

public class TestUtils
{
    public static final double EPSILON = 1e-6;
    public static final double EPSILON_LENIENT = 0.1;
    
    private static final Random random = new Random();
    
    public static double getRandomDouble()
    {
//        if (random.nextInt(11) <= 1) // return 0 10% of the time
//            return 0;
        double mantissa = -10 + random.nextDouble() * 20; // (-10, 10)
        int exponent = random.nextInt(21) - 10; // (-10, 10)
        return mantissa * Math.pow(10.0, exponent);
    }
}
