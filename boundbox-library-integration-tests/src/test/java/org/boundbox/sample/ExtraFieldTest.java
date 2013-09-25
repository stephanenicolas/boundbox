package org.boundbox.sample;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.boundbox.BoundBox;
import org.boundbox.BoundBoxException;
import org.boundbox.BoundBoxField;
import org.junit.Before;
import org.junit.Test;

public class ExtraFieldTest {

    @BoundBox(
    		boundClass = ExtraFieldTestClassB.class,
    		extraFields = {
    			@BoundBoxField(
    					fieldName =  "fakeField1",
    					fieldClass = String.class
    			)
    		}
    )
    private BoundBoxOfExtraFieldTestClassB boundBoxOfB;

    @Before
    public void setup() {
        boundBoxOfB = new BoundBoxOfExtraFieldTestClassB(new ExtraFieldTestClassB());
    }

    @Test
    public void test_read_access_to_field() {
    	try {
			boundBoxOfB.boundBox_getFakeField1();
			fail();
		} catch (BoundBoxException e) {
		    assertTrue(e.getCause() instanceof NoSuchFieldException);
		}
    }
}
