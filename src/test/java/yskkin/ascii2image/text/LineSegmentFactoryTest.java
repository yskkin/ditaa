package yskkin.ascii2image.text;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.junit.Test;
import org.stathissideris.ascii2image.text.TextGrid;

import yskkin.ascii2image.text.LineSegment.LineDelimiter;
import yskkin.ascii2image.text.LineSegment.LineEnd;

public class LineSegmentFactoryTest {

	TextGrid seed = new TextGrid();

	@Test
	public void test() throws Exception {
		seed.loadFrom(getFilePath("/tests/text/ditaa_bug2.txt"));
		Set<LineSegment> testee = LineSegmentFactory.createLineSegment(seed);
		assertThat(testee, hasItems(
				line(8, 2, ' ', 10, 2, ' '),
				line(18, 1, ' ', 18, 6, '+'),
				line(18, 6, '+', 18, 12, '+'),
				line(29, 7, '^', 29, 9, 'v'),
				line(43, 11, '/', 43, 13, '\\')));
	}

	private String getFilePath(String path) throws Exception {
		return new File(getClass().getResource(path).toURI()).getAbsolutePath();
	}

	private LineSegment line(int x1, int y1, char delimiter1, int x2, int y2, char delimiter2) {
		LineEnd l1 = new LineEnd(x1, y1, LineDelimiter.getLineDelimiter(delimiter1));
		LineEnd l2 = new LineEnd(x2, y2, LineDelimiter.getLineDelimiter(delimiter2));
		return new LineSegment(l1, l2);
	}
}
