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

    @BoundBox(boundClass = StaticInnerClassWithManyConstructorsTestClass.class)
    @Test
    public void test_access_static_inner_class_with_many_constructors() {
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyConstructorsTestClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass();
        assertNotNull( new BoundBoxOfStaticPrivateInnerClassTestClass.BoundBox_inner_InnerClass(instanceofInnerClass) );

        assertNotNull(instanceofInnerClass);
        
        instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyConstructorsTestClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass(2);
        assertNotNull(instanceofInnerClass);

        instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyConstructorsTestClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass("toto");
        assertNotNull(instanceofInnerClass);
    }
    
    @BoundBox(boundClass = StaticInnerClassWithManyFieldsAndMethodsTestClass.class)
    @Test
    public void test_access_static_inner_class_with_many_fields_and_methods() {
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBox_inner_InnerClass boundBoxOfInnerClass = new BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBox_inner_InnerClass(instanceofInnerClass);
        assertNotNull( boundBoxOfInnerClass );
        
        boundBoxOfInnerClass.foo();
        boundBoxOfInnerClass.bar(2);
    }
    
    @BoundBox(boundClass = StaticInnerClassInStaticInnerTestClass.class)
    @Test
    public void test_access_static_inner_class_in_static_inner_class() {
        Object instanceofInBetweenClass = BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBox_inner_InBetweenClass.boundBox_new_InBetweenClass();
        assertNotNull(instanceofInBetweenClass);
        BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBox_inner_InBetweenClass boundBoxOfInBetweenClass = new BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBox_inner_InBetweenClass(instanceofInBetweenClass);
        assertNotNull( boundBoxOfInBetweenClass );
        
        Object instanceofInnerClass = BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBox_inner_InBetweenClass.BoundBox_inner_InnerClass.boundBox_new_InnerClass();
        assertNotNull(instanceofInnerClass);
        BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBox_inner_InBetweenClass.BoundBox_inner_InnerClass boundBoxOfInnerClass = new BoundBoxOfStaticInnerClassInStaticInnerTestClass.BoundBox_inner_InBetweenClass.BoundBox_inner_InnerClass(instanceofInBetweenClass);
        assertNotNull( boundBoxOfInnerClass );
    }
}
