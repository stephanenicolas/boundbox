package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import org.boundbox.BoundBox;
import org.junit.Test;

@BoundBox(boundClass = StaticMethodTestClass.class)
public class StaticMethodTest {

    @Test
    public void test_access_static_method() {
        assertEquals("a", BoundBoxOfStaticMethodTestClass.foo());
    }
}
