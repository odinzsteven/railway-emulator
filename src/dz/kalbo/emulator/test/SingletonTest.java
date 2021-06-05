package dz.kalbo.emulator.test;

public class SingletonTest {
    public static void main(String[] args) {
        System.out.println("print classes names: " + Singleton1.NAME + " and " + Singleton2.NAME);
        System.out.println("other stuff");
        System.out.println("instances: " + Singleton1.getInstance() + " and " + Singleton2.getInstance());
    }
}
