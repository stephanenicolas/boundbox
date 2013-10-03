package org.boundbox.writer;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;

import org.boundbox.FakeMethodInfo;
import org.boundbox.model.FieldInfo;
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
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("", "void", new ArrayList<FieldInfo>(), null);

        //when
        namingGenerator.createMethodName(fakeMethodInfo, Collections.<String>emptyList());

        //then
        fail();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateMethodName_throws_exception_for_static_initializer() {
        //given
        FakeMethodInfo fakeMethodInfo = new FakeMethodInfo("<clinit>", "void", new ArrayList<FieldInfo>(), null);

        //when
        namingGenerator.createMethodName(fakeMethodInfo, Collections.<String>emptyList());

        //then
        fail();
    }

}
