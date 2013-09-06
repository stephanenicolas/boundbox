package org.boundbox.sample;

import org.boundbox.BoundBox;
import org.boundbox.sample.Foo;

public class Bar {

    @BoundBox(boundClass=Foo.class)
    private Object a;
    private static BoundBoxOfFoo boundBoxFoo = new BoundBoxOfFoo( new Foo());

    public static void main(String[] args) {
        System.out.println( "Assert foo.bar is bar " + boundBoxFoo.getBar().equals("bar"));
    }
}
