package dz.kalbo.emulator.test;

public class Singleton2 {

    private String data;
    public static String NAME = "test2";

    public Singleton2(String data) {
        this.data = data;
        System.out.println("new test2 created");
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Test2{" +
                "data='" + data + '\'' +
                '}';
    }

    private static class Wrapper {
        private static Singleton2 test2 = new Singleton2("data2");
    }

    public static Singleton2 getInstance() {
        return Wrapper.test2;
    }
}
