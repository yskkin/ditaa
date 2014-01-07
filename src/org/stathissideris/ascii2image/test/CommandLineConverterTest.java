package org.stathissideris.ascii2image.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.stathissideris.ascii2image.core.CommandLineConverter;

public class CommandLineConverterTest {

	private static final String USAGE_HEAD = "usage: java -jar ditaa.jar";
	private static final String EOL = System.getProperty("line.separator");

	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	@Rule
	public final StandardErrorStreamLog err = new StandardErrorStreamLog();
	@Rule
	public final StandardOutputStreamLog out = new StandardOutputStreamLog();

	@Test
	public void testNoOption() {
		exit.expectSystemExitWithStatus(0);
		exit.checkAssertionAfterwards(new Assertion() {
			@Override
			public void checkAssertion() throws Exception {
				assertThat(out.getLog(), containsString(USAGE_HEAD));
				assertThat(err.getLog(), is(""));
			}
		});
		execute();
	}

	@Test
	public void testHelpOption() {
		exit.expectSystemExitWithStatus(0);
		exit.checkAssertionAfterwards(new Assertion(){
			@Override
			public void checkAssertion() throws Exception {
				assertThat(out.getLog(), containsString(USAGE_HEAD));
				assertThat(err.getLog(), is(""));
			}
		});
		execute("--help");
	}

	@Test
	public void testWrongEncodingOption() {
		exit.expectSystemExitWithStatus(2);
		exit.checkAssertionAfterwards(new Assertion() {
			@Override
			public void checkAssertion() throws Exception {
				assertThat(out.getLog(), is(""));
				assertThat(err.getLog(), is("Error: invalid_encoding_name" + EOL));
			}
		});
		execute("--encoding", "invalid_encoding_name", "input.txt");
	}

	@Test
	public void testNonNumericTabs() {
		exit.expectSystemExitWithStatus(2);
		exit.checkAssertionAfterwards(new Assertion() {
			@Override
			public void checkAssertion() throws Exception {
				assertThat(out.getLog(), containsString(USAGE_HEAD));
				assertThat(err.getLog(), is("Error: For input string: \"not_a_number\"" + EOL));
			}
		});
		execute("-tabs", "not_a_number", "input.txt");
	}

	@Test
	public void testUnknownOption() {
		exit.expectSystemExitWithStatus(2);
		exit.checkAssertionAfterwards(new Assertion() {
			@Override
			public void checkAssertion() throws Exception {
				assertThat(out.getLog(), containsString(USAGE_HEAD));
				assertThat(err.getLog(), is("Unrecognized option: --unknown-option" + EOL));
			}
			
		});
		execute("--unknown-option", "input.txt");
	}

	@Test
	public void testNoInputFile() {
		exit.expectSystemExitWithStatus(2);
		exit.checkAssertionAfterwards(new Assertion() {
			public void checkAssertion() throws Exception {
				assertThat(out.getLog(), containsString(USAGE_HEAD));
				assertThat(err.getLog(), is("Error: Please provide the input file filename" + EOL));
			}
		});
		execute("-v");
	}

	private void execute(String... args) {
		CommandLineConverter.main(args);
	}
}
