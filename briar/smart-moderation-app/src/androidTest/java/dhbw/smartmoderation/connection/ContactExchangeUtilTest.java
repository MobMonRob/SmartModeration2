package dhbw.smartmoderation.connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ContactExchangeUtilTest {

	public static final byte[] TEST_STRING_TO_BYTES = {(byte) 3, (byte) 56, (byte) -107, (byte) 122, (byte) -3, (byte) 0, (byte) 99};
	public static final String TEST_BYTES_TO_STRING = "3,56,-107,122,-3,0,99,";

	@Test
	public void byteArrayToString() {
		assertEquals(TEST_BYTES_TO_STRING, ContactExchangeUtil.byteArrayToString(TEST_STRING_TO_BYTES));
	}

	@Test
	public void stringToByteArray() {
		assertArrayEquals(TEST_STRING_TO_BYTES, ContactExchangeUtil.stringToByteArray(TEST_BYTES_TO_STRING));
	}

}