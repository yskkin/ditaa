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
package org.stathissideris.ascii2image.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.stathissideris.ascii2image.core.DebugUtils;
import org.stathissideris.ascii2image.text.*;

import yskkin.ascii2image.util.Loggers;

/**
 * 
 * @author Efstathios Sideris
 */
public class CompositeDiagramShape extends DiagramComponent {

	private static final Logger LOG = Loggers.getLogger(CompositeDiagramShape.class);

	private ArrayList<DiagramShape> shapes = new ArrayList<DiagramShape>();

	public static void main(String[] args) {
	}

	public static DiagramComponent createFromBoundaryCells(
			final TextGrid grid,
			final CellSet boundaryCells,
			final int cellWidth,
			final int cellHeight) {
				return createOpenFromBoundaryCells(
						grid,
						boundaryCells,
						cellWidth, cellHeight,
						false);
	}


	public static DiagramComponent createOpenFromBoundaryCells(
			final TextGrid grid,
			final CellSet boundaryCells,
			final int cellWidth,
			final int cellHeight,
			boolean allRound) {
		
		if(boundaryCells.getType(grid) != CellSet.TYPE_OPEN) throw new IllegalArgumentException("This shape is closed and cannot be handled by this method");
		if(boundaryCells.size() == 0) return null;

		
		CompositeDiagramShape compositeShape = new CompositeDiagramShape();
		TextGrid workGrid = new TextGrid(grid.getWidth(), grid.getHeight());
		grid.copyCellsTo(boundaryCells, workGrid);

		LOG.finer("Making composite shape from grid:");
		workGrid.printDebug();
		
		
		CellSet visitedCells = new CellSet();
		
		List<DiagramShape> shapes = new ArrayList<DiagramShape>(100);
		
		for(TextGrid.Cell cell : boundaryCells) {
			if(workGrid.isLinesEnd(cell)) {
				CellSet nextCells = workGrid.followCell(cell);
				shapes.addAll(growEdgesFromCell(workGrid, cellWidth, cellHeight, allRound, nextCells.getFirst(), cell, visitedCells));
				break;
			}
		}
		
		//dashed shapes should "infect" the rest of the shapes
		boolean dashedShapeExists = false;
		for(DiagramShape shape : shapes)
			if(shape.isStrokeDashed())
				dashedShapeExists = true;
		
		for(DiagramShape shape : shapes) {
			if(dashedShapeExists) shape.setStrokeDashed(true);
			compositeShape.addToShapes(shape);
		}
		
		return compositeShape;
	}
	
	
	private static List<DiagramShape> growEdgesFromCell(
			TextGrid workGrid,
			final int cellWidth,
			final int cellHeight,
			boolean allRound,
			TextGrid.Cell cell, 
			TextGrid.Cell previousCell, 
			CellSet visitedCells) {
		
		List<DiagramShape> result = new ArrayList<DiagramShape>(50); 
		
		visitedCells.add(previousCell);
		
		DiagramShape shape = new DiagramShape();
		
		shape.addToPoints(makePointForCell(previousCell, workGrid, cellWidth, cellHeight, allRound));
		LOG.fine("point at "+previousCell+" (call from line: "+DebugUtils.getLineNumber()+")");
		if(workGrid.cellContainsDashedLineChar(previousCell)) shape.setStrokeDashed(true);

		boolean finished = false;
		while(!finished) {
			visitedCells.add(cell);
			if(workGrid.isPointCell(cell)) {
				LOG.fine("point at "+cell+" (call from line: "+DebugUtils.getLineNumber()+")");
				shape.addToPoints(makePointForCell(cell, workGrid, cellWidth, cellHeight, allRound));
			}
			
			if(workGrid.cellContainsDashedLineChar(cell)) shape.setStrokeDashed(true);

			if(workGrid.isLinesEnd(cell)){
				finished = true;
				LOG.fine("finished shape");
			}
			
			CellSet nextCells = workGrid.followCell(cell, previousCell);
			if(nextCells.size() == 1) {
				previousCell = cell;
				cell = (TextGrid.Cell) nextCells.getFirst();
				LOG.fine("tracing at "+cell+" (call from line: "+DebugUtils.getLineNumber()+")");
			} else if(nextCells.size() > 1 || nextCells.size() == 0) {//3- or 4- way intersection
				finished = true;
				for(TextGrid.Cell nextCell : nextCells)
					result.addAll(growEdgesFromCell(workGrid, cellWidth, cellHeight, allRound, nextCell, cell, visitedCells));
			}
		}
		
		result.add(shape);
		return result;
	}

	/**
	 * Returns a new diagram component with the lines of
	 * this CompositeDiagramShape connected. It can a composite
	 * or simple shape
	 * 
	 * @return
	 */
	public DiagramComponent connectLines(){
		CompositeDiagramShape result = new CompositeDiagramShape();

		//find all lines
		ArrayList<DiagramShape> lines = new ArrayList<DiagramShape>();
		for (DiagramShape shape : lines) {
			if(shape.getPoints().size() == 2){
				lines.add(shape);
			}
		}
		
		for (DiagramShape line1 : lines) {
			for (DiagramShape line2 : lines) {
				ShapePoint commonPoint = null;
				ShapePoint line1UncommonPoint = null;
				ShapePoint line2UncommonPoint = null;
				if(line1.getPoint(0).equals(line2.getPoint(0))){
					commonPoint = line1.getPoint(0);
					line1UncommonPoint = line1.getPoint(1);
					line2UncommonPoint = line2.getPoint(1);
				}
				if(line1.getPoint(0).equals(line2.getPoint(1))){
					commonPoint = line1.getPoint(0);
					line1UncommonPoint = line1.getPoint(1);
					line2UncommonPoint = line2.getPoint(0);
				}
				if(line1.getPoint(1).equals(line2.getPoint(0))){
					commonPoint = line1.getPoint(1);
					line1UncommonPoint = line1.getPoint(0);
					line2UncommonPoint = line2.getPoint(1);
				}
				if(line1.getPoint(1).equals(line2.getPoint(1))){
					commonPoint = line1.getPoint(1);
					line1UncommonPoint = line1.getPoint(0);
					line2UncommonPoint = line2.getPoint(0);
				}
				if(commonPoint != null){
					
				}
			}
		}
		
		return result;
	}

	public void connectEndsToAnchors(TextGrid grid, Diagram diagram){
		for (DiagramShape shape : shapes) {
			if(!shape.isClosed()){
				shape.connectEndsToAnchors(grid, diagram);
			}
		}
	}

	private static DiagramShape makeLine(TextGrid grid, TextGrid.Cell start, TextGrid.Cell end, int cellWidth, int cellHeight){
		DiagramShape line = new DiagramShape();
		
		if(grid.isHorizontalLine(start)){
			if(start.isWestOf(end)){
				line.addToPoints(new ShapePoint(
							Diagram.getCellMinX(start, cellWidth),
							Diagram.getCellMidY(start, cellHeight)));
			} else {
				line.addToPoints(new ShapePoint(
							Diagram.getCellMaxX(start, cellWidth),
							Diagram.getCellMidY(start, cellHeight)));
			}
		} else if(grid.isVerticalLine(start)){
			if(start.isNorthOf(end)){
				line.addToPoints(new ShapePoint(
							Diagram.getCellMidX(start, cellWidth),
							Diagram.getCellMinY(start, cellHeight)));
			} else {
				line.addToPoints(new ShapePoint(
							Diagram.getCellMidX(start, cellWidth),
							Diagram.getCellMaxY(start, cellHeight)));
			}			
		} else { //corner
			LOG.info("Corner");
			int type = (grid.isRoundCorner(start))?ShapePoint.TYPE_ROUND:ShapePoint.TYPE_NORMAL;
			line.addToPoints(new ShapePoint(
						Diagram.getCellMidX(start, cellWidth),
						Diagram.getCellMidY(start, cellHeight),
						type));
			
		}

		if(grid.isHorizontalLine(end)){
			if(start.isWestOf(start)){
				line.addToPoints(new ShapePoint(
							Diagram.getCellMinX(end, cellWidth),
							Diagram.getCellMidY(end, cellHeight)));
			} else {
				line.addToPoints(new ShapePoint(
							Diagram.getCellMaxX(end, cellWidth),
							Diagram.getCellMidY(end, cellHeight)));
			}
		} else if(grid.isVerticalLine(end)){
			if(start.isNorthOf(start)){
				line.addToPoints(new ShapePoint(
							Diagram.getCellMidX(end, cellWidth),
							Diagram.getCellMinY(end, cellHeight)));
			} else {
				line.addToPoints(new ShapePoint(
							Diagram.getCellMidX(end, cellWidth),
							Diagram.getCellMaxY(end, cellHeight)));
			}			
		} else { //corner
			int type = (grid.isRoundCorner(end))?ShapePoint.TYPE_ROUND:ShapePoint.TYPE_NORMAL;
			LOG.info("Corner");
			line.addToPoints(new ShapePoint(
						Diagram.getCellMidX(end, cellWidth),
						Diagram.getCellMidY(end, cellHeight),
						type));
			
		}

		
		return line;
	}

	public void addToShapes(DiagramShape shape){
		shapes.add(shape);
	}
	
	public void scale(float factor){
		for (DiagramShape shape : shapes){
			shape.scale(factor);
		}
	}
	/**
	 * @return
	 */
	public ArrayList<DiagramShape> getShapes() {
		return shapes;
	}

}

