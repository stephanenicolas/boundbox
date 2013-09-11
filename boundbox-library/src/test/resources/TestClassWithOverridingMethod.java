import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassWithOverridingMethod.class)
public class TestClassWithOverridingMethod extends TestClassWithSingleMethod {
    @Override
    protected void foo() {
    }
}
