import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithStaticField.class)
public class TestClassWithStaticField {
    private static String foo = "test";
    
    //TDD for Issue #2 proposed by Flavien Laurent
    @BoundBox(boundClass = TestClassWithStaticInitializer.class)
    private static int a = 1;
}
