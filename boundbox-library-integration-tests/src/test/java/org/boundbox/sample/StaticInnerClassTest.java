package org.boundbox.sample;

import static org.junit.Assert.*;

import org.boundbox.BoundBox;
import org.junit.Test;

public class StaticInnerClassTest {

    @BoundBox(boundClass = StaticInnerClassTestClass.class)
    @Test
    public void test_access_static_inner_class() {
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassTestClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass();
        assertTrue(instanceofInnerClass instanceof StaticInnerClassTestClass.InnerClass);
        assertNotNull( new BoundBoxOfStaticInnerClassTestClass.BoundBox_inner_InnerClass(instanceofInnerClass) );
    }
    
    @BoundBox(boundClass = StaticPrivateInnerClassTestClass.class)
    @Test
    public void test_access_static_private_inner_class() {
        Object instanceofInnerClass = BoundBoxOfStaticPrivateInnerClassTestClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        assertNotNull( new BoundBoxOfStaticPrivateInnerClassTestClass.BoundBox_inner_InnerClass(instanceofInnerClass) );
    }
}
