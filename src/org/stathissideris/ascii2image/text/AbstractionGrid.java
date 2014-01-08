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
package org.stathissideris.ascii2image.text;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import yskkin.ascii2image.util.Loggers;

/**
 * 
 * @author Efstathios Sideris
 */
public class AbstractionGrid {

	private static final Logger LOG = Loggers.getLogger(AbstractionGrid.class);
	
	private TextGrid grid;
	
	/**
	 * Makes an AbstractionGrid using <code>internalGrid</code> as
	 * its internal buffer
	 * 
	 * @param internalGrid
	 * @return
	 */
	public static AbstractionGrid makeUsingBuffer(TextGrid internalGrid){
		if(internalGrid.getWidth() % 3 != 0
			|| internalGrid.getHeight() % 3 != 0) throw new IllegalArgumentException("Passed TextGrid must have dimensions that are divisible by 3."); 
		AbstractionGrid result = new AbstractionGrid(internalGrid.getWidth() / 3, internalGrid.getHeight() / 3);
		result.setInternalBuffer(internalGrid);
		return result;
	}
	
	/**
	 * Makes an AbstractionGrid using the <code>cellSet</code>
	 * of <code>textGrid</code>.
	 * 
	 * @param textGrid
	 * @param cellSet
	 */
	public AbstractionGrid(TextGrid textGrid, CellSet cellSet){
		this(textGrid.getWidth(), textGrid.getHeight());
		/*this(cellSet.getWidth(), cellSet.getHeight());
		
		cellSet = new CellSet(cellSet);
		cellSet.translate( - cellSet.getMinX(), - cellSet.getMinY());*/
		
		
		LOG.finer("Making AbstractionGrid using buffer:");
		textGrid.printDebug();
		LOG.finer("...and the following CellSet:");
		cellSet.printAsGrid();
		
		
		for (TextGrid.Cell cell : cellSet){
			if(textGrid.isBlank(cell)) continue;
			if(textGrid.isCross(cell)){
				set(cell.x, cell.y, AbstractCell.makeCross());
			} else if(textGrid.isT(cell)){
				set(cell.x, cell.y, AbstractCell.makeT());
			} else if(textGrid.isK(cell)){
				set(cell.x, cell.y, AbstractCell.makeK());
			} else if(textGrid.isInverseT(cell)){
				set(cell.x, cell.y, AbstractCell.makeInverseT());
			} else if(textGrid.isInverseK(cell)){
				set(cell.x, cell.y, AbstractCell.makeInverseK());
			} else if(textGrid.isCorner1(cell)){
				set(cell.x, cell.y, AbstractCell.makeCorner1());
			} else if(textGrid.isCorner2(cell)){
				set(cell.x, cell.y, AbstractCell.makeCorner2());
			} else if(textGrid.isCorner3(cell)){
				set(cell.x, cell.y, AbstractCell.makeCorner3());
			} else if(textGrid.isCorner4(cell)){
				set(cell.x, cell.y, AbstractCell.makeCorner4());
			} else if(textGrid.isHorizontalLine(cell)){
				set(cell.x, cell.y, AbstractCell.makeHorizontalLine());
			} else if(textGrid.isVerticalLine(cell)){
				set(cell.x, cell.y, AbstractCell.makeVerticalLine());
			} else if(textGrid.isCrossOnLine(cell)){
				set(cell.x, cell.y, AbstractCell.makeCross());
			} else if(textGrid.isStarOnLine(cell)){
				set(cell.x, cell.y, AbstractCell.makeStar());
			}
		}

		LOG.finer("...the resulting AbstractionGrid is:");
		grid.printDebug();
	}
	
	private AbstractionGrid(int width, int height){
		grid = new TextGrid(width*3, height*3);
	}
	
	public TextGrid getCopyOfInternalBuffer(){
		return new TextGrid(grid);
	}

	private void setInternalBuffer(TextGrid grid){
		this.grid = grid;
	}

	
	public int getWidth(){
		return grid.getWidth() / 3;
	}

	public int getHeight(){
		return grid.getHeight() / 3;
	}

	public List<CellSet> getDistinctShapes(){
		List<CellSet> result = new ArrayList<CellSet>();
		
		CellSet nonBlank = grid.getAllNonBlank();
		List<CellSet> distinct = nonBlank.breakIntoDistinctBoundaries();
		
		for (CellSet set : distinct) {
			TextGrid tempGrid = new TextGrid(grid.getWidth(), grid.getHeight());
			for (TextGrid.Cell cell : set) {
				tempGrid.set(cell.x / 3, cell.y / 3, '*');
			}
			result.add(tempGrid.getAllNonBlank());
		}
		
		return result; 
	}
	
	protected void fillCells(CellSet cells){
		grid.fillCellsWith(cells, '*');
	}
	
	public void set(int xPos, int yPos, AbstractCell cell){
		xPos *= 3;
		yPos *= 3;
		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 3; x++){
				if(cell.rows[x][y] == 1){
					grid.set(xPos + x, yPos + y, '*');
				}
			}
		}
	}

}
