import java.io.IOException;

import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithManyConstructors.class)
public class TestClassWithManyConstructors {

    public TestClassWithManyConstructors() {
    }

    private TestClassWithManyConstructors(int a) {
    }

    private TestClassWithManyConstructors(Object a) {
    }

    private TestClassWithManyConstructors(int a, Object b) {
    }

    private TestClassWithManyConstructors(int a, Object b, Object c) throws IOException, RuntimeException {
    }
}
