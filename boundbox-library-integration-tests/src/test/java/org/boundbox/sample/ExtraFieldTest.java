package org.boundbox.sample;

import static org.junit.Assert.assertTrue;

import org.boundbox.BoundBox;
import org.boundbox.BoundBoxException;
import org.boundbox.BoundBoxField;
import org.junit.Before;
import org.junit.Test;

public class ExtraFieldTest {

    private FieldTestClassB fieldTestClassB;
    @BoundBox(
    		boundClass = FieldTestClassB.class,
    		extraFields = {
    			@BoundBoxField(
    					fieldName =  "fakeField1",
    					fieldClass = String.class
    			)
    		}
    )
    private BoundBoxOfFieldTestClassB boundBoxOfB;

    @Before
    public void setup() {
        fieldTestClassB = new FieldTestClassB();
        boundBoxOfB = new BoundBoxOfFieldTestClassB(fieldTestClassB);
    }

    @Test
    public void test_read_access_to_field() {
    	boolean thrownNoSuchField = false;
    	try {
			boundBoxOfB.boundBox_getFakeField1();
		} catch (BoundBoxException e) {
			thrownNoSuchField = e.getCause() instanceof NoSuchFieldException;
		}
    	assertTrue(thrownNoSuchField);
    }
}
