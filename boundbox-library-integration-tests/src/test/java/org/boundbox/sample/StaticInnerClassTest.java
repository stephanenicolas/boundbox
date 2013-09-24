package org.boundbox.sample;

import static org.junit.Assert.*;

import org.boundbox.BoundBox;
import org.junit.Test;

@BoundBox(boundClass = StaticInnerClassTestClass.class)
public class StaticInnerClassTest {

    @Test
    public void test_access_static_inner_class() {
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassTestClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass();
        assertTrue(instanceofInnerClass instanceof StaticInnerClassTestClass.InnerClass);
        assertNotNull( new BoundBoxOfStaticInnerClassTestClass.BoundBox_inner_InnerClass(instanceofInnerClass) );
    }
}
