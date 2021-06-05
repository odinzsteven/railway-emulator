package dz.kalbo.emulator.test;

public class Singleton1 {

    private String data;
    public static String NAME = "test1";

    public Singleton1(String data) {
        this.data = data;
        System.out.println("new test1 created");
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Test{" +
                "data='" + data + '\'' +
                '}';
    }

    private static Singleton1 test = new Singleton1("data1");

    public static Singleton1 getInstance() {
        return Singleton1.test;
    }
}
