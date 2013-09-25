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
        assertNotNull( new BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBox_inner_InnerClass(instanceofInnerClass) );
        
        new BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBox_inner_InnerClass(instanceofInnerClass).foo();
        new BoundBoxOfStaticInnerClassWithManyFieldsAndMethodsTestClass.BoundBox_inner_InnerClass(instanceofInnerClass).bar(2);
    }
}
