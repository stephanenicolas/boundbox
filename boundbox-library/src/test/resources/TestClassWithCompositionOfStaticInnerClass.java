import org.boundbox.BoundBox;

//TDD for issue #18
@BoundBox(boundClass = TestClassWithCompositionOfStaticInnerClass.class)
public class TestClassWithCompositionOfStaticInnerClass {
    TestClassWithStaticInnerClass.InnerClass a;
}

