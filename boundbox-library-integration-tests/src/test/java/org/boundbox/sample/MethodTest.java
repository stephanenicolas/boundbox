package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

public class MethodTest {

    private MethodTestClassB methodTestClassB;
    private MethodTestClassC methodTestClassC;
    @BoundBox(boundClass = MethodTestClassB.class)
    private BoundBoxOfMethodTestClassB boundBoxOfB;
    @BoundBox(boundClass = MethodTestClassC.class)
    private BoundBoxOfMethodTestClassC boundBoxOfC;

    @Before
    public void setup() {
        methodTestClassB = new MethodTestClassB();
        methodTestClassC = new MethodTestClassC();
        boundBoxOfB = new BoundBoxOfMethodTestClassB(methodTestClassB);
        boundBoxOfC = new BoundBoxOfMethodTestClassC(methodTestClassC);
    }

    @Test
    public void test__access_to_method() {
        assertEquals("b", boundBoxOfB.foo());
    }

    @Test
    public void test__access_to_hidden_method() {
        assertEquals("a", boundBoxOfB.boundBox_super_MethodTestClassA_foo());
    }

    @Test
    public void test__access_to_inherited_method() {
        assertEquals("b", boundBoxOfB.bar());
    }

    @Test
    public void test__access_to_inherited_method2() {
        assertEquals("a", boundBoxOfC.boundBox_super_MethodTestClassA_foo());
    }

}
