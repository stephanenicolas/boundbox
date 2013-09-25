package org.boundbox.sample;

import static org.junit.Assert.*;

import org.boundbox.BoundBox;
import org.junit.Test;

public class InnerClassTest {

    @BoundBox(boundClass = InnerClassTestClass.class)
    @Test
    public void test_access_static_inner_class() {
        InnerClassTestClass instanceofOuterClass = BoundBoxOfInnerClassTestClass.boundBox_new();
        Object instanceofInnerClass = new BoundBoxOfInnerClassTestClass(instanceofOuterClass).boundBox_new_InnerClass();
        assertTrue(instanceofInnerClass instanceof InnerClassTestClass.InnerClass);
        assertNotNull( new BoundBoxOfInnerClassTestClass(instanceofOuterClass).new BoundBox_inner_InnerClass(instanceofInnerClass) );
    }
    
}
