package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

public class FieldTest {

    private FieldTestClassB fieldTestClassB;
    private FieldTestClassC fieldTestClassC;
    @BoundBox(boundClass=FieldTestClassB.class)
    private BoundBoxOfFieldTestClassB boundBoxOfB;
    @BoundBox(boundClass=FieldTestClassC.class)
    private BoundBoxOfFieldTestClassC boundBoxOfC;

    @Before
    public void setup() {
        fieldTestClassB = new FieldTestClassB();
        fieldTestClassC = new FieldTestClassC();
        boundBoxOfB = new BoundBoxOfFieldTestClassB( fieldTestClassB);
        boundBoxOfC = new BoundBoxOfFieldTestClassC( fieldTestClassC);
    }

    @Test
    public void test_read_access_to_field() {
        assertEquals( "fieldTestClassB", boundBoxOfB.boundBox_getField1());
    }

    @Test
    public void test_read_access_to_hidden_field() {
        assertEquals( "a", boundBoxOfB.boundBox_super_FieldTestClassA_getField1());
    }

    @Test
    public void test_read_access_to_inherited_field() {
        assertEquals( "fieldTestClassB", boundBoxOfB.boundBox_getField2());
    }

    @Test
    public void test_read_access_to_inherited_field2() {
        assertEquals( "fieldTestClassB", boundBoxOfC.boundBox_getField2());
    }


    @Test
    public void test_write_access_to_field() {
        //given
        //when
        boundBoxOfB.boundBox_setField1("a");
        
        //then       
        assertEquals( "a", boundBoxOfB.boundBox_getField1());
    }
    
    @Test
    public void test_write_access_to_hidden_field() {
        //given
        //when
        boundBoxOfB.boundBox_super_FieldTestClassA_setField1("t");
        
        //then       
        assertEquals( "fieldTestClassB", boundBoxOfB.boundBox_getField1());
        assertEquals( "t", boundBoxOfB.boundBox_super_FieldTestClassA_getField1());
    }


}
