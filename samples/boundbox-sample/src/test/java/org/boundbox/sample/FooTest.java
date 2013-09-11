package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

@BoundBox(boundClass=Foo.class)
public class FooTest {

    private static BoundBoxOfFoo boundBoxFoo;

    @Before
    public void setup() {
        boundBoxFoo = new BoundBoxOfFoo( new Foo());
    }
    
    @Test
    public void test_access_to_field() {
        assertEquals( "bar", boundBoxFoo.boundBox_getBar());
    }
    
    @Test
    public void test_access_to_method() {
        assertEquals( 42, boundBoxFoo.foo());
    }

    @Test
    public void test_access_to_inherited_field() {
        assertEquals( "bar", boundBoxFoo.boundBox_getMotherBar());
    }
    
    @Test
    public void test_access_to_inherited_method() throws Exception {
        for( Method method : Foo.class.getDeclaredMethods() ) {
            System.out.println( method );
        }
        assertEquals( 42, boundBoxFoo.motherFoo(1));
    }


}
