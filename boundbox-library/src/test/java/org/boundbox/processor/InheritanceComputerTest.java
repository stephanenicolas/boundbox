package org.boundbox.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.boundbox.model.FieldInfo;
import org.boundbox.model.InnerClassInfo;
import org.junit.Before;
import org.junit.Test;

public class InheritanceComputerTest {

    private InheritanceComputer simplifier;

    @Before
    public void setup() {
        simplifier = new InheritanceComputer();
    }

    @Test
    public void testComputeInheritanceAndHidingFields() {
        // given
        List<FieldInfo> listFieldInfos = new ArrayList<FieldInfo>();
        FieldInfo fieldInfo = new FieldInfo("foo", "bar");
        fieldInfo.setInheritanceLevel(1);
        FieldInfo fieldInfo2 = new FieldInfo("foo", "bar");
        fieldInfo2.setInheritanceLevel(2);
        listFieldInfos.add(fieldInfo);
        listFieldInfos.add(fieldInfo2);

        // when
        simplifier.computeInheritanceAndHidingFields(listFieldInfos);

        // then
        assertFalse(listFieldInfos.isEmpty());
        assertEquals(0, listFieldInfos.get(0).getEffectiveInheritanceLevel());
        assertEquals(2, listFieldInfos.get(1).getEffectiveInheritanceLevel());
    }
    
    @Test
    public void testComputeInheritanceAndHidingInnerClasses() {
        // given
        List<InnerClassInfo> listInnerClassInfos = new ArrayList<InnerClassInfo>();
        InnerClassInfo innerClassInfo = new InnerClassInfo("foo");
        innerClassInfo.setInheritanceLevel(1);
        InnerClassInfo innerClassInfo2 = new InnerClassInfo("foo");
        innerClassInfo2.setInheritanceLevel(2);
        listInnerClassInfos.add(innerClassInfo);
        listInnerClassInfos.add(innerClassInfo2);

        // when
        simplifier.computeInheritanceAndHidingInnerClasses(listInnerClassInfos);

        // then
        assertFalse(listInnerClassInfos.isEmpty());
        assertEquals(0, listInnerClassInfos.get(0).getEffectiveInheritanceLevel());
        assertEquals(2, listInnerClassInfos.get(1).getEffectiveInheritanceLevel());
    }

}
