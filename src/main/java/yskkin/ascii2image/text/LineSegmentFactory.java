package yskkin.ascii2image.text;

import java.util.HashSet;
import java.util.Set;

import org.stathissideris.ascii2image.text.TextGrid;

import yskkin.ascii2image.text.LineSegment.LineDelimiter;
import yskkin.ascii2image.text.LineSegment.LineEnd;

public class LineSegmentFactory {
	
	/**
	 * Erase boundary from given {@link TextGrid} and return erased boundaries as {@link LineSegment}s.
	 * 
	 * @param grid
	 * @return Set of {@code LineSegment}.
	 */
	public static Set<LineSegment> createLineSegment(TextGrid grid) {
		final Set<LineSegment> result = new HashSet<LineSegment>();
		for (int y = 0; y < grid.getHeight(); y++) {
			for (int x = 0; x < grid.getWidth(); x++) {
				char start = grid.get(x, y);
				if (start == '|' || start == ':') {
					result.add(resolveVertically(grid, x, y));
				}
				if (start == '-' || start == '=') {
					result.add(resolveHorizontally(grid, x, y));
				}
			}
		}
		return result;
	}

	private static LineSegment resolveVertically(TextGrid grid, int x, int y) {
		final int originalY = y;
		char boundary = grid.get(x, y);
		while (y >= 0 && (boundary == '|' || boundary == ':')) {
			grid.set(x, y, ' ');
			y--;
			boundary = grid.get(x, y);
		}
		LineEnd northEnd = new LineEnd(x, y, LineDelimiter.getLineDelimiter(boundary));
		y = originalY + 1;
		boundary = grid.get(x, y);
		while (y < grid.getHeight() && (boundary == '|' || boundary == ':')) {
			grid.set(x, y, ' ');
			y++;
			boundary = grid.get(x, y);
		}
		LineEnd southEnd = new LineEnd(x, y, LineDelimiter.getLineDelimiter(boundary));
		return new LineSegment(northEnd, southEnd);
	}

	private static LineSegment resolveHorizontally(TextGrid grid, int x, int y) {
		final int originalX = x;
		char boundary = grid.get(x, y);
		while (x >= 0 && (boundary == '-' || boundary == '=')) {
			grid.set(x, y, ' ');
			x--;
			boundary = grid.get(x, y);
		}
		LineEnd westEnd = new LineEnd(x, y, LineDelimiter.getLineDelimiter(boundary));
		x = originalX + 1;
		boundary = grid.get(x, y);
		while (x < grid.getWidth() && (boundary == '-' || boundary == '=')) {
			grid.set(x, y, ' ');
			x++;
			boundary = grid.get(x, y);
		}
		LineEnd eastEnd = new LineEnd(x, y, LineDelimiter.getLineDelimiter(boundary));
		return new LineSegment(westEnd, eastEnd);
	}

}
