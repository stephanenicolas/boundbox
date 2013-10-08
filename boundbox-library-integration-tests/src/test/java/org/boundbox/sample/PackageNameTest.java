package org.boundbox.sample;

import static org.junit.Assert.assertEquals;

import org.boundbox.BoundBox;
import org.junit.Before;
import org.junit.Test;

import foo.BoundBoxOfFieldTestClassA;

public class PackageNameTest {

    private FieldTestClassA fieldTestClassA;
    @BoundBox(boundClass = FieldTestClassA.class, boundBoxPackage="foo")
    private BoundBoxOfFieldTestClassA boundBoxOfA;

    @Before
    public void setup() {
        fieldTestClassA = new FieldTestClassA();
        boundBoxOfA = new BoundBoxOfFieldTestClassA(fieldTestClassA);
    }

    @Test
    public void test_read_access_to_field() {
        assertEquals("a", boundBoxOfA.boundBox_getField1());
    }
}
