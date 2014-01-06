/**
 * ditaa - Diagrams Through Ascii Art
 * 
 * Copyright (C) 2004-2011 Efstathios Sideris
 *
 * ditaa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * ditaa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with ditaa.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package org.stathissideris.ascii2image.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.stathissideris.ascii2image.text.CellSet;
import org.stathissideris.ascii2image.text.TextGrid;
import org.stathissideris.ascii2image.text.TextGrid.Cell;

public class CellSetTest {
	
	CellSet set = new CellSet();
	
	@Before
	public void setUp() {
		set.add(new Cell(10, 20));
		set.add(new Cell(10, 60));
		set.add(new Cell(10, 30));
		set.add(new Cell(60, 20));
	}

	@Test
	public void equality() {
		// Given
		CellSet other = new CellSet(); 
		other.add(new Cell(10, 20));
		other.add(new Cell(10, 60));
		other.add(new Cell(10, 30));
		other.add(new Cell(60, 20));

		// Then
		assertThat(other, is(set));
		assertThat(other.hashCode(), is(set.hashCode()));
	}
	
	@Test
	public void testContains() {
		TextGrid.Cell cell1 = new Cell(10, 20);
		TextGrid.Cell cell2 = new Cell(10, 20);

		assertTrue(cell1.equals(cell2));
		assertTrue(set.contains(cell1));
	}

	@Test
	public void testBreakIntoDistinctBoundaries() {
		// Given
		set = new CellSet();
		set.add(new Cell(1, 2));
		set.add(new Cell(1, 3));
		set.add(new Cell(1, 4));
		set.add(new Cell(2, 3));
		set.add(new Cell(10,10));
		set.add(new Cell(10, 11));
		set.add(new Cell(9, 9));
		CellSet[] expected = new CellSet[3];
		expected[0] = new CellSet();
		expected[0].add(new Cell(1, 2));
		expected[0].add(new Cell(1, 3));
		expected[0].add(new Cell(1, 4));
		expected[0].add(new Cell(2, 3));
		expected[1] = new CellSet();
		expected[1].add(new Cell(10,10));
		expected[1].add(new Cell(10, 11));
		expected[2] = new CellSet();
		expected[2].add(new Cell(9, 9));
		// When
		List<CellSet> result = set.breakIntoDistinctBoundaries();
		// Then
		assertThat(result, hasItems(expected));
	}
}
