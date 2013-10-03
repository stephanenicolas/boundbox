package org.boundbox.sample;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value={"URF_UNREAD_FIELD"}, 
        justification="Only used for tests")
public class StaticInitializerTestClassA {
    protected static String field1 = "b";
    
    static {
        System.out.println(System.currentTimeMillis());
        field1 = "c";
    }

}
