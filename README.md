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
 
 public String getFoo() {
   return "The value of foo is " + foo;
 }
}
````
