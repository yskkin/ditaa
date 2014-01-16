package yskkin.ascii2image.util;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.stathissideris.ascii2image.text.CellSet;
import org.stathissideris.ascii2image.text.TextGrid;
import org.stathissideris.ascii2image.text.TextGrid.Cell;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class BoundarySearcherTest {

	TextGrid seed = new TextGrid();

	@Test
	public void test() throws Exception {
		seed.loadFrom(getFilePath("/tests/text/ditaa_bug2.txt"));
		List<CellSet> testee = BoundarySearcher.findBoundariesFromGrid(seed);
		CellSet boundary1 = getCellSet(new int[]{
				2,4, 3,4, 4,4, 5,4, 6,4, 7,4, 8,4, 9,4, 10,4, 11,4, 12,4, 13,4,
				2,5,                                                      13,5,
				2,6,                                                      13,6,
				2,7, 3,7, 4,7, 5,7, 6,7, 7,7, 8,7, 9,7, 10,7, 11,7, 12,7, 13,7});
		CellSet boundary2 = getCellSet(new int[]{
				23,4, 24,4, 25,4,       27,4, 28,4, 29,4, 30,4, 31,4, 32,4, 33,4,
				23,5,                                                       33,5,
				23,6, 24,6, 25,6, 26,6, 27,6, 28,6, 29,6, 30,6, 31,6, 32,6, 33,6});
		// No arrow.
		CellSet boundary3 = getCellSet(new int[]{
				18,2, 18,3, 18,4, 18,5, 18,6, 18,7, 18,8, 18,9, 18,9, 18,10, 18,11, 18,12, 18,13, 18,14, 18,15,
				15,6, 16,6, 17,6, 18,6, 19,6, 20,6, 21,6,
				15,12, 16,12, 17,12, 18,12, 19,12, 20,12, 21,12
		});
		assertThat(testee, hasItem(boundary1));
		assertThat(testee, hasItem(boundary2));
		assertThat(testee, hasItem(boundary3));
	}
	
	private String getFilePath(String path) throws Exception {
		return new File(getClass().getResource(path).toURI()).getAbsolutePath();
	}

	private CellSet getCellSet(int[] coordinate) {
		CellSet result = new CellSet();
		for (int i = 0; i < coordinate.length; i += 2) {
			result.add(new Cell(coordinate[i], coordinate[i + 1]));
		}
		return result;
	}
}
