package org.boundbox.sample.android;

import org.boundbox.BoundBox;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

@BoundBox(boundClass = MainActivity.class, maxSuperClass = Activity.class)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    BoundBoxOfMainActivity boundBoxOfMainActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @UiThreadTest
    public void testCompute() {
        //given
        boundBoxOfMainActivity = new BoundBoxOfMainActivity( getActivity() );
        
        //when
        boundBoxOfMainActivity.boundBox_getButtonMain().performClick();
        
        //then
        assertEquals( "42", boundBoxOfMainActivity.boundBox_getTextViewMain().getText() );
    }

}
