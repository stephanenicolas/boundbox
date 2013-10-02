package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

public class NamingTest {

    private FieldTestClassA fieldTestClassA;
    @BoundBox(boundClass = FieldTestClassA.class, prefixes={"BB","bb"})
    private BBFieldTestClassA boundBoxOfA;

    @Before
    public void setup() {
        fieldTestClassA = new FieldTestClassA();
        boundBoxOfA = new BBFieldTestClassA(fieldTestClassA);
    }
    
    @Test
    public void test_read_access_to_fields() {
        assertEquals("a", boundBoxOfA.bb_getField1());
        assertEquals("b", boundBoxOfA.bb_getField2());
    }
    
    @Test
    public void test_write_access_to_fields() {
        // given
        // when
        boundBoxOfA.bb_setField1("b");
        boundBoxOfA.bb_setField2("a");

        // then
        assertEquals("b", boundBoxOfA.bb_getField1());
        assertEquals("a", boundBoxOfA.bb_getField2());
    }


}
