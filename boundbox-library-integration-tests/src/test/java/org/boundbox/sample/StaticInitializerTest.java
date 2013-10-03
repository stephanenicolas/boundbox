package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.boundbox.BoundBox;
import org.boundbox.BoundBoxException;
import org.junit.Test;

public class StaticInitializerTest {

    @BoundBox(boundClass = StaticInitializerTestClassA.class)
    @Test
    public void test_access_static_initializer() {
        //given
        BoundBoxOfStaticInitializerTestClassA.boundBox_setField1("a");
        assertEquals("a", BoundBoxOfStaticInitializerTestClassA.boundBox_getField1());
        
        //when
        BoundBoxOfStaticInitializerTestClassA.boundBox_static_init();
        
        //then
        assertEquals("c", BoundBoxOfStaticInitializerTestClassA.boundBox_getField1());
    }

    @BoundBox(boundClass = StaticInitializerTestClassB.class)
    @Test
    public void test_access_inherited_static_initializer() {
        //given
        BoundBoxOfStaticInitializerTestClassB.boundBox_setField1("a");
        assertEquals("a", BoundBoxOfStaticInitializerTestClassB.boundBox_getField1());
        
        //when
        //BoundBoxOfStaticInitializerTestClassB.boundBox_super_StaticInitializerTestClassB_static_init();
        try {
            Method methodToInvoke = org.boundbox.sample.StaticInitializerTestClassA.class.getDeclaredMethods()[0];
            methodToInvoke.setAccessible(true);
             methodToInvoke.invoke(null);
          }
          catch( IllegalAccessException e ) {
            throw new BoundBoxException(e);
          }
          catch( IllegalArgumentException e ) {
            throw new BoundBoxException(e);
          }
          catch( InvocationTargetException e ) {
            throw new BoundBoxException(e);
          }
        
        //then
        assertEquals("c", BoundBoxOfStaticInitializerTestClassB.boundBox_getField1());
    }
}
