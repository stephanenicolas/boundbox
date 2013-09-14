package org.boundbox.processor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.boundbox.FakeFieldInfo;
import org.boundbox.model.FieldInfo;
import org.boundbox.processor.InheritanceComputer;
import org.junit.Before;
import org.junit.Test;

public class InheritanceComputerTest {

    private InheritanceComputer simplifier;

    @Before
    public void setup() {
        simplifier = new InheritanceComputer();
    }

    @Test
    public void testSimplifyInheritance() {
        // given
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        FieldInfo fieldInfo = new FakeFieldInfo("foo", "bar");
        fieldInfo.setInheritanceLevel(1);
        FieldInfo fieldInfo2 = new FakeFieldInfo("foo", "bar");
        fieldInfo2.setInheritanceLevel(2);
        listFieldInfos.add(fieldInfo);
        listFieldInfos.add(fieldInfo2);

        // when
        simplifier.computeInheritanceAndHiding(listFieldInfos);

        // then
        assertFalse(listFieldInfos.isEmpty());
        assertEquals(0, listFieldInfos.get(0).getEffectiveInheritanceLevel());
        assertEquals(2, listFieldInfos.get(1).getEffectiveInheritanceLevel());
    }

}
