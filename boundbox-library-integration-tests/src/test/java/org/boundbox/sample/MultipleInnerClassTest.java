package org.boundbox.sample;

import static org.junit.Assert.*;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

public class MultipleInnerClassTest {

    private MultipleInnerClassTestClass multipleInnerClassTestClassInstance;
    @BoundBox(boundClass = MultipleInnerClassTestClass.class)
    private BoundBoxOfMultipleInnerClassTestClass boundBoxOfA;

    @Before
    public void setup() {
        multipleInnerClassTestClassInstance = new MultipleInnerClassTestClass();
        boundBoxOfA = new BoundBoxOfMultipleInnerClassTestClass(multipleInnerClassTestClassInstance);
    }

    @Test
    public void test_read_access_to_field() {
        //given
        Object innerInstance = boundBoxOfA.boundBox_new_InnerClass();
        Object innerInstance2 = boundBoxOfA.boundBox_new_InnerClass2();
        Object staticInnerInstance = BoundBoxOfMultipleInnerClassTestClass.boundBox_new_StaticInnerClass();
        Object staticInnerInstance2 = BoundBoxOfMultipleInnerClassTestClass.boundBox_new_StaticInnerClass2();
        
        //when
        BoundBoxOfMultipleInnerClassTestClass.BoundBoxOfInnerClass boundBoxOfInner = boundBoxOfA.new BoundBoxOfInnerClass(innerInstance);
        BoundBoxOfMultipleInnerClassTestClass.BoundBoxOfInnerClass2 boundBoxOfInner2 = boundBoxOfA.new BoundBoxOfInnerClass2(innerInstance2);
        BoundBoxOfMultipleInnerClassTestClass.BoundBoxOfStaticInnerClass boundBoxOfStaticInner = new BoundBoxOfMultipleInnerClassTestClass.BoundBoxOfStaticInnerClass(staticInnerInstance);
        BoundBoxOfMultipleInnerClassTestClass.BoundBoxOfStaticInnerClass2 boundBoxOfStaticInner2 = new BoundBoxOfMultipleInnerClassTestClass.BoundBoxOfStaticInnerClass2(staticInnerInstance2);
        
        //then
        assertEquals("org.boundbox.sample.MultipleInnerClassTestClass$InnerClass", innerInstance.getClass().getName());
        assertEquals("org.boundbox.sample.MultipleInnerClassTestClass$InnerClass2", innerInstance2.getClass().getName());
        assertEquals("org.boundbox.sample.MultipleInnerClassTestClass$StaticInnerClass", staticInnerInstance.getClass().getName());
        assertEquals("org.boundbox.sample.MultipleInnerClassTestClass$StaticInnerClass2", staticInnerInstance2.getClass().getName());
        
        assertEquals(0,boundBoxOfInner.boundBox_getA());
        assertEquals(1,boundBoxOfInner2.boundBox_getA());
        assertEquals(2,boundBoxOfStaticInner.boundBox_getA());
        assertEquals(3,boundBoxOfStaticInner2.boundBox_getA());
    }
}
