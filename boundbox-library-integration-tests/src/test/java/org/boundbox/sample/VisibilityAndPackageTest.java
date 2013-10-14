package org.boundbox.sample;

import static org.junit.Assert.*;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

public class VisibilityAndPackageTest {

    private VisibilityAndPackageTestClass visibilityTestClassA;
    @BoundBox(boundClass = VisibilityAndPackageTestClass.class)
    private BoundBoxOfVisibilityAndPackageTestClass boundBoxOfA;
    @BoundBox(boundClass = VisibilityAndPackageTestClass.class, boundBoxPackage="foo")
    private foo.BoundBoxOfVisibilityAndPackageTestClass boundBoxOfAInFoo;

    @Before
    public void setup() {
        visibilityTestClassA = new VisibilityAndPackageTestClass();
        boundBoxOfA = new BoundBoxOfVisibilityAndPackageTestClass(visibilityTestClassA);
        boundBoxOfAInFoo = new foo.BoundBoxOfVisibilityAndPackageTestClass(visibilityTestClassA);
    }

    @Test
    public void test_read_access_to_field() {
        //given
        Object innerInstance = boundBoxOfA.boundBox_new_Inner();
        Object cInstance = BoundBoxOfVisibilityAndPackageTestClass.boundBox_new_C();
        
        //when
        BoundBoxOfVisibilityAndPackageTestClass.BoundBoxOfInner boundBoxOfInner = boundBoxOfA.new BoundBoxOfInner(innerInstance);
        VisibilityAndPackageTestClass.C cInstanceViaInner = boundBoxOfInner.boundBox_getFoo();
                
        //then
        assertNotEquals(innerInstance, cInstance);
        assertNotEquals(innerInstance.getClass(), cInstance.getClass());
        assertEquals(cInstanceViaInner.getClass(), cInstance.getClass());
    }
    
    @Test
    public void test_read_access_to_field_in_other_package() {
        //given
        Object innerInstance = boundBoxOfAInFoo.boundBox_new_Inner();
        Object cInstance = foo.BoundBoxOfVisibilityAndPackageTestClass.boundBox_new_C();
        
        //when
        foo.BoundBoxOfVisibilityAndPackageTestClass.BoundBoxOfInner boundBoxOfInner = boundBoxOfAInFoo.new BoundBoxOfInner(innerInstance);
        Object cInstanceViaInner = boundBoxOfInner.boundBox_getFoo();
                
        //then
        assertNotEquals(innerInstance, cInstance);
        assertNotEquals(innerInstance.getClass(), cInstance.getClass());
        assertEquals(cInstanceViaInner.getClass(), cInstance.getClass());
    }
}
