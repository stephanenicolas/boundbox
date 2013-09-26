package org.boundbox.sample;

import static org.junit.Assert.*;

import org.boundbox.BoundBox;
import org.junit.Test;

public class StaticInnerClassTest {

    @BoundBox(boundClass = StaticInnerClassTestClass.class)
    @Test
    public void test_access_static_inner_class() {
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassTestClass.boundBox_new_InnerClass();
        assertTrue(instanceofInnerClass instanceof StaticInnerClassTestClass.InnerClass);
        assertNotNull( new BoundBoxOfStaticInnerClassTestClass.BoundBoxOfInnerClass(instanceofInnerClass) );
    }
    
    @BoundBox(boundClass = StaticPrivateInnerClassTestClass.class)
    @Test
    public void test_access_static_private_inner_class() {
        Object instanceofInnerClass = BoundBoxOfStaticPrivateInnerClassTestClass.boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        assertEquals(StaticPrivateInnerClassTestClass.class.getDeclaredClasses()[0], instanceofInnerClass.getClass() );
        assertNotNull( new BoundBoxOfStaticPrivateInnerClassTestClass.BoundBoxOfInnerClass(instanceofInnerClass) );
    }

    @BoundBox(boundClass = StaticInnerClassWithManyConstructorsTestClass.class)
    @Test
    public void test_access_static_inner_class_with_many_constructors() {
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyConstructorsTestClass.boundBox_new_InnerClass();
        assertNotNull( new BoundBoxOfStaticPrivateInnerClassTestClass.BoundBoxOfInnerClass(instanceofInnerClass) );

        assertNotNull(instanceofInnerClass);
        
        instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyConstructorsTestClass.boundBox_new_InnerClass(2);
        assertNotNull(instanceofInnerClass);

        instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyConstructorsTestClass.boundBox_new_InnerClass("toto");
        assertNotNull(instanceofInnerClass);
    }
    
    @BoundBox(boundClass = StaticInnerClassWithManyFieldsAndMethodsTestClass.class)
    @Test
    public void test_access_static_inner_class_with_many_fields_and_methods() {
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBoxOfInnerClass boundBoxOfInnerClass = new BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBoxOfInnerClass(instanceofInnerClass);
        assertNotNull( boundBoxOfInnerClass );
        
        boundBoxOfInnerClass.foo();
        boundBoxOfInnerClass.bar(2);
    }
    
    @BoundBox(boundClass = StaticInnerClassInStaticInnerTestClass.class)
    @Test
    public void test_access_static_inner_class_in_static_inner_class() {
        Object instanceofInBetweenClass = BoundBoxOfStaticInnerClassInStaticInnerTestClass.boundBox_new_InBetweenClass();
        assertNotNull(instanceofInBetweenClass);
        BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBoxOfInBetweenClass boundBoxOfInBetweenClass = new BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBoxOfInBetweenClass(instanceofInBetweenClass);
        assertNotNull( boundBoxOfInBetweenClass );
        
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBoxOfInBetweenClass.boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBoxOfInBetweenClass.BoundBoxOfInnerClass boundBoxOfInnerClass = new BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBoxOfInBetweenClass.BoundBoxOfInnerClass(instanceofInBetweenClass);
        assertNotNull( boundBoxOfInnerClass );
    }
}
