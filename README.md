boundbox
========

BoundBox provides an easy way to test an object by accessing **all** its fields, constructor and methods, public or not. 
BoundBox breaks encapsulation.

Sample
======

Let's say we have a class A like :

````java
public class A {
 private String foo;
 
 protected A(String foo) {
   this.foo = foo;
 }
 
 private void bar(String foo) {
  this.foo += foo; 
 }
 
 protected String getFoo() {
   return "The value of foo is " + foo;
 }
}
````

With BoundBox, you can write a test that accesses all fields and methods of A :

````java
public class ATest {
 @BoundBox( boundClass = A.class )
 private BoundBoxOfA boundBoxOfA;
 
 @Before
 public void setUp() {
   boundBoxOfA = new BoundBoxOfA( new A("bb") );
 }
 
 @Test
 public void testConstructor() {
   //GIVEN
   //WHEN
   //THEN
   assertEquals( "bb", boundBoxOfA.boundBox_getFoo());
 }
 
 @Test
 public void testBar() {
   //GIVEN
   //WHEN
   boundBoxOfA.bar("cc");
   
   //THEN
   assertEquals( "bbcc", boundBoxOfA.boundBox_getFoo();
 }
 
 @Test
 public void testGetFoo() {
   //GIVEN
   //WHEN
   //THEN
   assertEquals( "The value of foo is bb", boundBoxOfA.getFoo();
 }
}
````
