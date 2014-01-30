package yskkin.ascii2image.text;

import java.util.Arrays;
import java.util.List;

/**
 * A line delimited by white spaces, arrows (v, V, <, >, ^), and corners (/, \, +).
 * 
 * A line delimited by corners can be jointed like following example.
 * 
 * <pre>
 *   line segment 1 (horizontal) delimited by + and white space.
 * +------
 * |
 * | line segment 2 (vertical) delimited by + and \.
 * |
 * |
 * \-------
 *   line segment 3 (horizontal) delimited by \ and white space.
 * 
 * 
 *   \----
 *   |  These line segments are not adjacent.
 *   |  Implementation should keep track of delimiter for excluding this case.
 *   |
 * </pre>
 *
 */
public class LineSegment {

	public enum LineDelimiter {
		WHITESPACE(false, false),
		NORTH_ARROW(false, true),
		EAST_ARROW(false, true),
		SOUTH_ARROW(false, true),
		WEST_ARROW(false, true),
		DIAGONAL_CORNER(true, false),
		ANTI_DAIGONAL_CORNER(true, false),
		CROSS_CORNER(true, false),
		;
		private boolean corner;
		private boolean arrow;
		LineDelimiter(boolean corner, boolean arrow) {
			this.corner = corner;
			this.arrow = arrow;
		}
		public boolean isCorner() {
			return corner;
		}
		public boolean isArrow() {
			return arrow;
		}
		public boolean isIn(LineDelimiter... list) {
			boolean result = false;
			for (LineDelimiter element : list) {
				result |= this == element;
			}
			return result;
		}
		public static LineDelimiter getLineDelimiter(char ch) {
			LineDelimiter result;
			switch (ch) {
			case '^':
				result = NORTH_ARROW;
				break;
			case '>':
				result = EAST_ARROW;
				break;
			case 'v': /*FALL THROUGH*/
			case 'V':
				result = SOUTH_ARROW;
				break;
			case '<':
				result = WEST_ARROW;
				break;
			case '/':
				result = DIAGONAL_CORNER;
				break;
			case '\\':
				result = ANTI_DAIGONAL_CORNER;
				break;
			case '+':
				result = CROSS_CORNER;
				break;
			case ' ': /*FALL THROUGH*/
			default:
				result = WHITESPACE;
				break;
			}
			return result;
		}
	}

	public static class LineEnd {
		private int x;
		private int y;
		private LineDelimiter delimiter;

		public LineEnd(int x, int y, LineDelimiter delimiter) {
			this.x = x;
			this.y = y;
			this.delimiter = delimiter;
		}
		public LineEnd(int x, int y, char delimiterChar) {
			this.x = x;
			this.y = y;
			this.delimiter = LineDelimiter.getLineDelimiter(delimiterChar);
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		public LineDelimiter getDelimiter() {
			return delimiter;
		}
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof LineEnd)) {
				return false;
			}
			LineEnd other = (LineEnd) o;
			return x == other.x && y == other.y && delimiter == other.delimiter;
		}
		@Override
		public int hashCode() {
			return x * 31 * 31 + y * 31 + delimiter.hashCode();
		}
		@Override
		public String toString() {
			return String.format("(%d, %d)", x, y);
		}
	}

	public static final int HORIZONTAL = 0x01;
	public static final int VERTICAL = 0x02;

	/**
	 * A smaller end of a line.
	 */
	private LineEnd start;
	/**
	 * A greater end of a line.
	 */
	private LineEnd end;

	public LineSegment(LineEnd a, LineEnd b) {
		if (a == null || b == null) {
			throw new NullPointerException("line end is null.");
		}
		if (a.x < b.x || (a.x == b.x && a.y < b.y)) {
			this.start = a;
			this.end = b;
		} else {
			this.start = b;
			this.end = a;
		}
	}

	public int getOrientation() {
		int orientation = -1;
		if (start.x == end.x) {
			orientation = VERTICAL;
		} else if (start.y == end.y) {
			orientation = HORIZONTAL;
		} else {
			throw new IllegalStateException("Diagonal lines are not supported yet.");
		}
		return orientation;
	}

	public boolean isAdjacent(LineSegment other) {
		boolean result = false;
		result = (start.equals(other.start) && start.delimiter.isIn(LineDelimiter.DIAGONAL_CORNER, LineDelimiter.CROSS_CORNER))
				|| (start.equals(other.end) && start.delimiter.isIn(LineDelimiter.ANTI_DAIGONAL_CORNER, LineDelimiter.CROSS_CORNER))
				|| (end.equals(other.start) && end.delimiter.isIn(LineDelimiter.ANTI_DAIGONAL_CORNER, LineDelimiter.CROSS_CORNER))
				|| (end.equals(other.end) && end.delimiter.isIn(LineDelimiter.DIAGONAL_CORNER, LineDelimiter.CROSS_CORNER));
		result &= getOrientation() != other.getOrientation();
		return result;
	}

	/**
	 * Get both ends of a line.
	 * 
	 * @return List of ends whose size is always 2.
	 */
	public List<LineEnd> getEnds() {
		return Arrays.asList(start, end);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LineSegment)) {
			return false;
		}
		LineSegment that = (LineSegment) o;
		return this.start.equals(that.start) && this.end.equals(that.end);
	}
	@Override
	public int hashCode() {
		return start.hashCode() * 31 + end.hashCode();
	}

	@Override
	public String toString() {
		return String.format("[%s, %s]", start, end);
	}
}
