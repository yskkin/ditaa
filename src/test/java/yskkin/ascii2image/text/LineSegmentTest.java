package yskkin.ascii2image.text;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import yskkin.ascii2image.text.LineSegment.LineDelimiter;
import yskkin.ascii2image.text.LineSegment.LineEnd;

public class LineSegmentTest {

	LineEnd a1 = new LineEnd(2, 8, LineDelimiter.ANTI_DAIGONAL_CORNER);
	LineEnd a2 = new LineEnd(2, 8, LineDelimiter.ANTI_DAIGONAL_CORNER);
	LineEnd b = new LineEnd(2, 8, LineDelimiter.CROSS_CORNER);
	LineEnd c = new LineEnd(8, 2, LineDelimiter.ANTI_DAIGONAL_CORNER);

	@Test
	public void testLineDelimiter_isIn() {
		// Given
		boolean shouldFalse = LineDelimiter.CROSS_CORNER.isIn(
				LineDelimiter.ANTI_DAIGONAL_CORNER,
				LineDelimiter.EAST_ARROW,
				LineDelimiter.WEST_ARROW);
		boolean shouldTrue = LineDelimiter.ANTI_DAIGONAL_CORNER.isIn(
				LineDelimiter.CROSS_CORNER,
				LineDelimiter.DIAGONAL_CORNER,
				LineDelimiter.WHITESPACE,
				LineDelimiter.ANTI_DAIGONAL_CORNER);
		// Then
		assertFalse(shouldFalse);
		assertTrue(shouldTrue);
	}

	@Test
	public void testLineEnd_identity() {
		assertThat(a1, is(a2));
		assertThat(a1.hashCode(), is(a2.hashCode()));
		assertThat(a1, is(not(b)));
		assertThat(a1, is(not(c)));
		assertThat(a1, is(not(new Object())));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetOrientation_diagonalError() {
		// Given
		LineSegment line = new LineSegment(a1, c);
		// When
		line.getOrientation();
	}

	@Test
	public void testIsAdjacent() {
		// Given
		LineSegment horizontal = new LineSegment(
				new LineEnd(10, 3, LineDelimiter.CROSS_CORNER),
				new LineEnd(1, 3, LineDelimiter.WHITESPACE));
		LineSegment vertical1 = new LineSegment(
				new LineEnd(1, 3, LineDelimiter.WHITESPACE),
				new LineEnd(1,10, LineDelimiter.WEST_ARROW));
		LineSegment vertical2 = new LineSegment(
				new LineEnd(10, 3, LineDelimiter.CROSS_CORNER),
				new LineEnd(10, 10, LineDelimiter.ANTI_DAIGONAL_CORNER));
		// Then
		assertFalse(horizontal.isAdjacent(vertical1));
		assertFalse(vertical1.isAdjacent(horizontal));
		assertTrue(horizontal.isAdjacent(vertical2));
		assertTrue(vertical2.isAdjacent(horizontal));
	}

}
