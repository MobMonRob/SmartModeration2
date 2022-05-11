package dhbw.smartmoderation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.EditText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UtilTest {

	public static final String TEST_TEXT = "This is a test";

	private static Context appContext;

	@BeforeClass
	public static void setUpClass() {
		appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
	}

	@Test
	public void isEmpty() {
		EditText editText = new EditText(appContext);
		editText.setText(TEST_TEXT);

		assertFalse(Util.isEmpty(editText));

		EditText editText2 = new EditText(appContext);
		editText.setText("");

		assertTrue(Util.isEmpty(editText2));
	}

	@Test
	public void getText() {
		EditText editText = new EditText(appContext);
		editText.setText(" " + TEST_TEXT + "   ");

		assertEquals(TEST_TEXT, Util.getText(editText));
	}

}