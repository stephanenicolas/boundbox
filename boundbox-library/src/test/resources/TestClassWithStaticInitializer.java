
public class TestClassWithStaticInitializer {
    static {
        System.out.println(System.currentTimeMillis());
    }
    
    protected static String foo = "test";
}
