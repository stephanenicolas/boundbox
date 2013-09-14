import java.io.IOException;

import org.boundbox.BoundBox;

@SuppressWarnings("unused")
@BoundBox(boundClass = TestClassWithManyMethods.class)
public class TestClassWithManyMethods {
    private void simple() {
    }

    private void withPrimitiveArgument(int a) {
    }

    private void withObjectArgument(Object a) {
    }

    private void withManyArguments(int a, Object b) {
    }

    private int withPrimitiveIntReturnType() {
        return 0;
    }

    private double withPrimitiveDoubleReturnType() {
        return 0;
    }

    private boolean withPrimitiveBooleanReturnType() {
        return false;
    }

    private void withSingleThrownType() throws IOException {
    }

    private void withManyThrownType() throws IOException, RuntimeException {
    }

}
