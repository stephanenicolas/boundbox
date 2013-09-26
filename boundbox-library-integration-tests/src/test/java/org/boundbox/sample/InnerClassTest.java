package org.boundbox.sample;

import static org.junit.Assert.*;

import org.boundbox.BoundBox;
import org.junit.Test;

public class InnerClassTest {

    @BoundBox(boundClass = NonStaticInnerClassTestClass.class)
    @Test
    public void test_access_inner_class() {
        NonStaticInnerClassTestClass instanceofOuterClass = BoundBoxOfNonStaticInnerClassTestClass.boundBox_new();
        Object instanceofInnerClass = new BoundBoxOfNonStaticInnerClassTestClass(instanceofOuterClass).boundBox_new_InnerClass();
        assertTrue(instanceofInnerClass instanceof NonStaticInnerClassTestClass.InnerClass);
        assertNotNull( new BoundBoxOfNonStaticInnerClassTestClass(instanceofOuterClass).new BoundBoxOfInnerClass(instanceofInnerClass) );
    }
    
    @BoundBox(boundClass = NonStaticPrivateInnerClassTestClass.class)
    @Test
    public void test_access_private_inner_class() {
        NonStaticPrivateInnerClassTestClass instanceofOuterClass = BoundBoxOfNonStaticPrivateInnerClassTestClass.boundBox_new();
        Object instanceofInnerClass = new BoundBoxOfNonStaticPrivateInnerClassTestClass(instanceofOuterClass).boundBox_new_InnerClass();
        assertEquals(NonStaticPrivateInnerClassTestClass.class.getDeclaredClasses()[0], instanceofInnerClass.getClass() );
        assertNotNull( new BoundBoxOfNonStaticPrivateInnerClassTestClass(instanceofOuterClass).new BoundBoxOfInnerClass(instanceofInnerClass) );
    }
    
    @BoundBox(boundClass = NonStaticInnerClassWithManyConstructorsTestClass.class)
    @Test
    public void test_access_static_inner_class_with_many_constructors() {
        NonStaticInnerClassWithManyConstructorsTestClass instanceofOuterClass = BoundBoxOfNonStaticInnerClassWithManyConstructorsTestClass.boundBox_new();
        Object instanceofInnerClass = new BoundBoxOfNonStaticInnerClassWithManyConstructorsTestClass(instanceofOuterClass).boundBox_new_InnerClass();
        assertNotNull( new BoundBoxOfNonStaticPrivateInnerClassTestClass(instanceofOuterClass).new BoundBoxOfInnerClass(instanceofInnerClass) );

        assertNotNull(instanceofInnerClass);
        
        instanceofInnerClass = new BoundBoxOfNonStaticInnerClassWithManyConstructorsTestClass(instanceofOuterClass).boundBox_new_InnerClass(2);
        assertNotNull(instanceofInnerClass);

        instanceofInnerClass = new BoundBoxOfNonStaticInnerClassWithManyConstructorsTestClass(instanceofOuterClass).boundBox_new_InnerClass("toto");
        assertNotNull(instanceofInnerClass);
    }
    
    @BoundBox(boundClass = NonStaticInnerClassWithManyFieldsAndMethodsTestClass.class)
    @Test
    public void test_access_static_inner_class_with_many_fields_and_methods() {
        NonStaticInnerClassWithManyFieldsAndMethodsTestClass instanceofOuterClass = BoundBoxOfNonStaticInnerClassWithManyFieldsAndMethodsTestClass.boundBox_new();
        Object instanceofInnerClass = new BoundBoxOfNonStaticInnerClassWithManyFieldsAndMethodsTestClass(instanceofOuterClass).boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        BoundBoxOfNonStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBoxOfInnerClass boundBoxOfInnerClass = new BoundBoxOfNonStaticInnerClassWithManyFieldsAndMethodsTestClass(instanceofOuterClass).new BoundBoxOfInnerClass(instanceofInnerClass);
        assertNotNull( boundBoxOfInnerClass );
        
        boundBoxOfInnerClass.foo();
        boundBoxOfInnerClass.bar(2);
    }
    
    @BoundBox(boundClass = NonStaticInnerClassInNonStaticInnerTestClass.class)
    @Test
    public void test_access_static_inner_class_in_static_inner_class() {
        NonStaticInnerClassInNonStaticInnerTestClass instanceofOuterClass = BoundBoxOfNonStaticInnerClassInNonStaticInnerTestClass.boundBox_new();
        Object instanceofInBetweenClass = new BoundBoxOfNonStaticInnerClassInNonStaticInnerTestClass(instanceofOuterClass).boundBox_new_InBetweenClass();
        assertNotNull(instanceofInBetweenClass);
        BoundBoxOfNonStaticInnerClassInNonStaticInnerTestClass.BoundBoxOfInBetweenClass boundBoxOfInBetweenClass = new BoundBoxOfNonStaticInnerClassInNonStaticInnerTestClass(instanceofOuterClass).new BoundBoxOfInBetweenClass(instanceofInBetweenClass);
        assertNotNull( boundBoxOfInBetweenClass );
        
        Object instanceofInnerClass = new BoundBoxOfNonStaticInnerClassInNonStaticInnerTestClass(instanceofOuterClass).new BoundBoxOfInBetweenClass(instanceofInBetweenClass).boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        BoundBoxOfNonStaticInnerClassInNonStaticInnerTestClass.BoundBoxOfInBetweenClass.BoundBoxOfInnerClass boundBoxOfInnerClass = new BoundBoxOfNonStaticInnerClassInNonStaticInnerTestClass(instanceofOuterClass).new BoundBoxOfInBetweenClass(instanceofInBetweenClass).new BoundBoxOfInnerClass(instanceofInnerClass);
        assertNotNull( boundBoxOfInnerClass );
    }
    
}
