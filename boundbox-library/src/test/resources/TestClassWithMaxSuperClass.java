import org.boundbox.BoundBox;

@BoundBox(boundClass = TestClassWithMaxSuperClass.class, maxSuperClass=TestClassWithOverridingMethod.class)
public class TestClassWithMaxSuperClass extends TestClassWithOverridingMethod {
}
