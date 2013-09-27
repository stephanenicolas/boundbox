package org.boundbox.sample.android;
import java.util.ArrayList;

import org.boundbox.BoundBox;
import org.boundbox.BoundBoxField;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.BoundBoxOfTextView;
import android.widget.TextView;

@BoundBox(
		boundClass = TextView.class,
		extraFields = {
			@BoundBoxField(
					fieldName = "mListeners",
					fieldClass = ArrayList.class
			)
		},
		maxSuperClass = TextView.class
		
)
public class TextViewTest extends ActivityInstrumentationTestCase2<MainActivity> {

    BoundBoxOfTextView boundBoxOfTextView;
    
    public TextViewTest() {
        super(MainActivity.class);
    }

    @UiThreadTest
    public void testCompute() {
        // given
        TextView tv = new TextView(getActivity());
        boundBoxOfTextView = new BoundBoxOfTextView(tv);

        // when
        final TextWatcher textWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		};
		tv.addTextChangedListener(textWatcher);	

        // then
        assertEquals(1, boundBoxOfTextView.boundBox_getMListeners().size());
        assertEquals(textWatcher, boundBoxOfTextView.boundBox_getMListeners().get(0));
    }

}
