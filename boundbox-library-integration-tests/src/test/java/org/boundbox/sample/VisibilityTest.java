package org.boundbox.sample;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

public class VisibilityTest {

    private VisibilityTestClass visibilityTestClassA;
    @BoundBox(boundClass = VisibilityTestClass.class)
    private BoundBoxOfVisibilityTestClass boundBoxOfA;

    @Before
    public void setup() {
        visibilityTestClassA = new VisibilityTestClass();
        boundBoxOfA = new BoundBoxOfVisibilityTestClass(visibilityTestClassA);
    }

    @Test
    public void test_read_access_to_field() {
        System.out.println("Inner classes of VisibilityTestClass");
        for(Class<?> c : VisibilityTestClass.class.getDeclaredClasses()) {
            System.out.println(c.getName());
        }
        //given
        Object innerInstance = boundBoxOfA.boundBox_new_Inner();
        Object cInstance = BoundBoxOfVisibilityTestClass.boundBox_new_C();
        
        //when
        BoundBoxOfVisibilityTestClass.BoundBoxOfInner boundBoxOfInner = boundBoxOfA.new BoundBoxOfInner(innerInstance);
        Object innerInnerInstance = boundBoxOfInner.boundBox_new_InnerInner();
        BoundBoxOfVisibilityTestClass.BoundBoxOfInner.BoundBoxOfInnerInner boundBoxOfInnerInner = boundBoxOfInner.new BoundBoxOfInnerInner(innerInnerInstance);
                
        //then
        assertNotEquals(innerInstance, cInstance);
        assertNotEquals(innerInstance.getClass(), cInstance.getClass());
        assertNotSame(null, boundBoxOfInnerInner.boundBox_getFoo());
    }
}
