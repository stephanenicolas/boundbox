package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import org.boundbox.BoundBox;
import org.junit.Test;

@BoundBox(boundClass = StaticFieldTestClass.class)
public class StaticFieldTest {

    @Test
    public void test_access_static_method() {
        assertEquals("a", BoundBoxOfStaticFieldTestClass.boundBox_getFoo());
    }
}
