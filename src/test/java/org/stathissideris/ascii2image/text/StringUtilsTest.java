package org.stathissideris.ascii2image.text;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.stathissideris.ascii2image.text.StringUtils;

@RunWith(Enclosed.class)
public class StringUtilsTest {

	@RunWith(Parameterized.class)
	public static class RepeatStringTest {
		String sourceString;
		int number;
		String expected;

		public RepeatStringTest(String sourceString, int number, String expected) {
			this.sourceString = sourceString;
			this.number = number;
			this.expected = expected;
		}

		@Test
		public void testRepeatString() {
			String actual = StringUtils.repeatString(sourceString, number);
			assertThat(actual, is(expected));
		}
		@Parameters
		public static Collection<Object[]> data() {
			String data = "souce data";
			return Arrays.asList(new Object[][]{
					{data, 0, ""},
					{data, 1, data},
					{data, 2, data + data},
					{data, -1, ""}});
		}
	}

	@RunWith(Parameterized.class)
	public static class IsBlankTest {
		String given;
		boolean expected;

		public IsBlankTest(String given, boolean expected) {
			this.given = given;
			this.expected = expected;
		}

		@Test
		public void testIsBlank() {
			assertThat(StringUtils.isBlank(given), is(expected));
		}

		@Parameters
		public static Collection<Object[]> data() {
			return Arrays.asList(new Object[][]{
					{"", true},
					{" ", true},
					{"\t", true},
					{" \t\n", true},
					{"a", false}
			});
		}
	}
}
