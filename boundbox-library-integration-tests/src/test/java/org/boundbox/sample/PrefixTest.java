package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

public class PrefixTest {

    private FieldTestClassB fieldTestClassB;
    @BoundBox(boundClass = FieldTestClassB.class, prefixes={"BB"})
    private BBFieldTestClassB boundBoxOfB;
    @BoundBox(boundClass = FieldTestClassB.class, prefixes={"CC","dd"})
    private CCFieldTestClassB boundBoxOfB2;

    @Before
    public void setup() {
        fieldTestClassB = new FieldTestClassB();
        boundBoxOfB = new BBFieldTestClassB(fieldTestClassB);
        boundBoxOfB2 = new CCFieldTestClassB(fieldTestClassB);
    }

    @Test
    public void test_read_access_to_field() {
        assertEquals("b", boundBoxOfB.bb_getField1());
        assertEquals("b", boundBoxOfB2.dd_getField1());
    }

    @Test
    public void test_read_access_to_hidden_field() {
        assertEquals("a", boundBoxOfB.bb_super_FieldTestClassA_getField1());
        assertEquals("a", boundBoxOfB2.dd_super_FieldTestClassA_getField1());
    }

    @Test
    public void test_read_access_to_inherited_field() {
        assertEquals("b", boundBoxOfB.bb_getField2());
        assertEquals("b", boundBoxOfB2.dd_getField2());
    }

    @Test
    public void test_write_access_to_field() {
        // given
        // when
        boundBoxOfB.bb_setField1("a");
        boundBoxOfB2.dd_setField1("a");

        // then
        assertEquals("a", boundBoxOfB.bb_getField1());
        assertEquals("a", boundBoxOfB2.dd_getField1());
    }

    @Test
    public void test_write_access_to_hidden_field() {
        // given
        // when
        boundBoxOfB.bb_super_FieldTestClassA_setField1("t");
        boundBoxOfB2.dd_super_FieldTestClassA_setField1("t");

        // then
        assertEquals("b", boundBoxOfB.bb_getField1());
        assertEquals("t", boundBoxOfB.bb_super_FieldTestClassA_getField1());
        assertEquals("b", boundBoxOfB2.dd_getField1());
        assertEquals("t", boundBoxOfB2.dd_super_FieldTestClassA_getField1());
    }

}
