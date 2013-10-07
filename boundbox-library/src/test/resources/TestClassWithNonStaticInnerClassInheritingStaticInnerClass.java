import org.boundbox.BoundBox;

//TDD for issue #18
@BoundBox(boundClass = TestClassWithNonStaticInnerClassInheritingStaticInnerClass.class)
public class TestClassWithNonStaticInnerClassInheritingStaticInnerClass {
    public class SubInnerClass extends TestClassWithStaticInnerClass.InnerClass {
    }
}

