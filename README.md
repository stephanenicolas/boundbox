<img src="https://raw.github.com/stephanenicolas/boundbox/master/assets/boundbox-logo.png" width="100px" align="left" />

BoundBox
========

BoundBox provides an easy way to test an object by accessing **all** its fields, constructors and methods, public or not. 
BoundBox breaks encapsulation.

BoundBox has been designed with *Android* in mind. But it will work for *pure Java* projects as well (J2SE and JEE), for instance to test legacy code.

To get started, have a look at [BoundBox's Wiki](https://github.com/stephanenicolas/boundbox/wiki) or the samples below.

Android Sample
--------------

On Android, the class below would be very hard to test. 
All logic is completely imbricated into an activity life cycle method and all fields are private.

```java
public class MainActivity extends Activity {

    // -------------------------------
    // ATTRIBUTES
    // -------------------------------

    private Button buttonMain;
    private TextView textViewMain;

    // -------------------------------
    // LIFECYCLE
    // -------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonMain = (Button) findViewById(R.id.button_main);
        textViewMain = (TextView) findViewById(R.id.textview_main);
        buttonMain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int result = 42;
                textViewMain.setText(String.valueOf(result));
            }
        });
    }
}
```

***

With BoundBox, without changing anything to your activity's code, you can access its private fields, or methods.

In the test below, as soon as you write the statement : `@BoundBox(boundClass=MainActivity.class)`, the BoundBox annotation 
processor will generate the class `BoundBoxOfMainActivity` that you can use to access all inner fields, constructors and methods of `MainActivity`.

An Android test becomes as easy as : 

```java
@BoundBox(boundClass = MainActivity.class, maxSuperClass = Activity.class)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    BoundBoxOfMainActivity boundBoxOfMainActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @UiThreadTest
    public void testCompute() {
        // given
        boundBoxOfMainActivity = new BoundBoxOfMainActivity(getActivity());

        // when
        boundBoxOfMainActivity.boundBox_getButtonMain().performClick();

        // then
        assertEquals("42", boundBoxOfMainActivity.boundBox_getTextViewMain().getText());
    }

}
```

Note that, using [FEST-Android](http://square.github.io/fest-android/) can make things even simpler.

Pure Java Sample
----------------

Let's say we have a class `A` with private fields, constructors and methods like :

```java
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
```

With BoundBox, you can write a test that accesses all fields and methods of `A`. 

Below, as soon as you write the statement : `@BoundBox(boundClass=A.class)`, the BoundBox annotation 
processor will generate the class `BoundBoxOfA` that you can use to access all inner fields, constructors and methods of `A`.


```java
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
```

Summary
-------

BoundBox's API is quite simple. Indeed in has no API at all, just a set of conventions to access the inner structure of an `Object`.

BoundBox offers the following advantages over alternative technologies : 
* it doesn't pollute your API under tests. Just code clean, don't change anything for testing even not a visibility modifier.
* objects under tests will be accessed using reflection, and this access will be checked at compile time (unlike using pure reflection or WhiteBox from PowerMock).
* all fields, constructors and methods, even those defined in super classes are accessible. For instance, it allows to access `foo.super.super.a` (that is not syntactically possible in Java).

Quality of code 
---------------

BoundBox is [heavily tested](https://github.com/stephanenicolas/boundbox/wiki/Contributors'-corner-:-testing) to ensure its quality. It uses both unit and integration tests and it is placed under continuous integration.
It also integrates checkstyle, findbugs, PMD to increase its robustness. Lombok is used to decrease the amount of code to write and maintain.


BoundBox is under CI on Travis <br/><a href="https://travis-ci.org/stephanenicolas/boundbox/builds"><img src="https://travis-ci.org/stephanenicolas/boundbox.png?branch=master"/></a>
<br/>
BoundBox uses coveralls.io for code coverage of unit-tests <br/>
[![Coverage Status](https://coveralls.io/repos/stephanenicolas/boundbox/badge.png?branch=master)](https://coveralls.io/r/stephanenicolas/boundbox?branch=master)

License
-------

 Copyright (C) 2013 Stéphane Nicolas
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	     http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
