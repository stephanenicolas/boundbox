package org.boundbox.writer;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;

import org.boundbox.model.FieldInfo;
import org.boundbox.model.MethodInfo;
import org.junit.Before;
import org.junit.Test;

public class NamingGeneratorTest {

    public NamingGenerator namingGenerator;

    @Before
    public void setUp() {
        namingGenerator = new NamingGenerator();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateMethodName_throws_exception_for_instance_initializer() {
        //given
        MethodInfo MethodInfo = new MethodInfo("", "void", new ArrayList<FieldInfo>(), null);

        //when
        namingGenerator.createMethodName(MethodInfo, Collections.<String>emptyList());

        //then
        fail();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateMethodName_throws_exception_for_static_initializer() {
        //given
        MethodInfo MethodInfo = new MethodInfo("<clinit>", "void", new ArrayList<FieldInfo>(), null);

        //when
        namingGenerator.createMethodName(MethodInfo, Collections.<String>emptyList());

        //then
        fail();
    }

}
