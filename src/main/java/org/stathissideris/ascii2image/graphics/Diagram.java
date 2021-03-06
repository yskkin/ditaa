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

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.stathissideris.ascii2image.core.ConversionOptions;
import org.stathissideris.ascii2image.core.Pair;
import org.stathissideris.ascii2image.text.CellSet;
import org.stathissideris.ascii2image.text.TextGrid;
import org.stathissideris.ascii2image.text.TextGrid.Cell;
import org.stathissideris.ascii2image.text.TextGrid.CellColorPair;
import org.stathissideris.ascii2image.text.TextGrid.CellStringPair;
import org.stathissideris.ascii2image.text.TextGrid.CellTagPair;

import yskkin.ascii2image.util.BoundarySearcher;
import yskkin.ascii2image.util.Loggers;

/**
 * 
 * @author Efstathios Sideris
 */
public class Diagram {

	private static final Logger LOG = Loggers.getLogger(Diagram.class);

	private Set<DiagramShape> shapes = new HashSet<DiagramShape>();
	private List<CompositeDiagramShape> compositeShapes = new ArrayList<CompositeDiagramShape>();
	private List<DiagramText> textObjects = new ArrayList<DiagramText>();
	
	private int width, height;
	private int cellWidth, cellHeight;
	
	
	/**
	 * 
	 * <p>An outline of the inner workings of this very important (and monstrous)
	 * constructor is presented here. Boundary processing is the first step
	 * of the process:</p>
	 * 
	 * <ol>
	 *   <li>Copy the grid into a work grid and remove all type-on-line
	 *       and point markers from the work grid</li>
	 *   <li>Split grid into distinct shapes by plotting the grid
	 * 	     onto an AbstractionGrid and its getDistinctShapes() method.</li>
	 *   <li>Find all the possible boundary sets of each of the
	 *       distinct shapes. This can produce duplicate shapes (if the boundaries
	 *       are the same when filling from the inside and the outside).</li>
	 *   <li>Remove duplicate boundaries.</li>
	 *   <li>Remove obsolete boundaries. Obsolete boundaries are the ones that are
	 *       the sum of their parts when plotted as filled shapes. (see method
	 *       removeObsoleteShapes())</li>
	 *   <li>Seperate the found boundary sets to open, closed or mixed
	 *       (See CellSet class on how its done).</li>
	 *   <li>Are there any closed boundaries?
	 *        <ul>
	 *           <li>YES. Subtract all the closed boundaries from each of the
	 *           open ones. That should convert the mixed shapes into open.</li>
	 *           <li>NO. In this (harder) case, we use the method
	 *           breakTrulyMixedBoundaries() of CellSet to break boundaries
	 *           into open and closed shapes (would work in any case, but it's
	 *           probably slower than the other method). This method is based
	 *           on tracing from the lines' ends and splitting when we get to
	 *           an intersection.</li>
	 *        </ul>
	 *   </li>
	 *   <li>If we had to eliminate any mixed shapes, we seperate the found
	 *   boundary sets again to open, closed or mixed.</li>
	 * </ol>
	 * 
	 * <p>At this stage, the boundary processing is all complete and we
	 * proceed with using those boundaries to create the shapes:</p>
	 * 
	 * <ol>
	 *   <li>Create closed shapes.</li>
	 *   <li>Create open shapes. That's when the line end corrections are
	 *   also applied, concerning the positioning of the ends of lines
	 *   see methods connectEndsToAnchors() and moveEndsToCellEdges() of
	 *   DiagramShape.</li>
	 *   <li>Assign color codes to closed shapes.</li>
	 *   <li>Assing extended markup tags to closed shapes.</p>
	 *   <li>Create arrowheads.</p>
	 *   <li>Create point markers.</p>
	 * </ol>
	 * 
	 * <p>Finally, the text processing occurs: [pending]</p>
	 * 
	 * @param grid
	 * @param cellWidth
	 * @param cellHeight
	 */
	public Diagram(TextGrid grid, ConversionOptions options) {
		
		this.cellWidth = options.renderingOptions.getCellWidth();
		this.cellHeight = options.renderingOptions.getCellHeight();
		
		width = grid.getWidth() * cellWidth;
		height = grid.getHeight() * cellHeight;
		
		List<CellColorPair> colorPair = grid.resolveColorCode();
		List<CellTagPair> tagPair = grid.resolveTag();
		TextGrid workGrid = new TextGrid(grid);
		workGrid.replaceTypeOnLine();
		workGrid.replacePointMarkersOnLine();
		workGrid.printDebug();

		List<CellSet> boundarySetsStep2 = BoundarySearcher.findBoundariesFromGrid(workGrid);

		//split boundaries to open, closed and mixed
		
		LOG.finer("******* First evaluation of openess *******");
		
		List<CellSet> open = new ArrayList<CellSet>();
		List<CellSet> closed = new ArrayList<CellSet>();
		List<CellSet> mixed = new ArrayList<CellSet>();
		
		for (CellSet set : boundarySetsStep2) {
			int type = set.getType(workGrid);
			if(type == CellSet.TYPE_CLOSED) closed.add(set);
			else if(type == CellSet.TYPE_OPEN) open.add(set);
			else if(type == CellSet.TYPE_MIXED) mixed.add(set);

			if(type == CellSet.TYPE_CLOSED) LOG.finer("Closed boundaries:");
			else if(type == CellSet.TYPE_OPEN) LOG.finer("Open boundaries:");
			else if(type == CellSet.TYPE_MIXED) LOG.finer("Mixed boundaries:");
			set.printAsGrid();
			
		}
		
		boolean hadToEliminateMixed = false;
		
		if(mixed.size() > 0 && closed.size() > 0) {
							// mixed shapes can be eliminated by
							// subtracting all the closed shapes from them 
			LOG.finer("******* Eliminating mixed shapes (basic algorithm) *******");
		
			hadToEliminateMixed = true;
			
			//subtract from each of the mixed sets all the closed sets
			for (CellSet set : mixed) {
				for (CellSet closedSet : closed) {
					set.subtractSet(closedSet);
				}
				// this is necessary because some mixed sets produce
				// several distinct open sets after you subtract the
				// closed sets from them
				if(set.getType(workGrid) == CellSet.TYPE_OPEN) {
					boundarySetsStep2.remove(set);
					boundarySetsStep2.addAll(set.breakIntoDistinctBoundaries());
				}
			}

		} else if(mixed.size() > 0 && closed.size() == 0) {
							// no closed shape exists, will have to
							// handle mixed shape on its own 
			// an example of this case is the following:
			// +-----+
			// |  A  |C                 B
			// +  ---+-------------------
			// |     |
			// +-----+

			hadToEliminateMixed = true;

			LOG.finer("******* Eliminating mixed shapes (advanced algorithm for truly mixed shapes) *******");
				
			for (CellSet set : mixed) {
				boundarySetsStep2.remove(set);
				boundarySetsStep2.addAll(set.breakTrulyMixedBoundaries(workGrid));
			}

		} else {
			LOG.finer("No mixed shapes found. Skipped mixed shape elimination step");
		}
		
		
		if(hadToEliminateMixed){
			LOG.finer("******* Second evaluation of openess *******");
		
			//split boundaries again to open, closed and mixed
			open = new ArrayList<CellSet>();
			closed = new ArrayList<CellSet>();
			mixed = new ArrayList<CellSet>();
		
			for (CellSet set : boundarySetsStep2){
				int type = set.getType(workGrid);
				if(type == CellSet.TYPE_CLOSED) closed.add(set);
				else if(type == CellSet.TYPE_OPEN) open.add(set);
				else if(type == CellSet.TYPE_MIXED) mixed.add(set);

				if(type == CellSet.TYPE_CLOSED) LOG.finer("Closed boundaries:");
				else if(type == CellSet.TYPE_OPEN) LOG.finer("Open boundaries:");
				else if(type == CellSet.TYPE_MIXED) LOG.finer("Mixed boundaries:");
				set.printAsGrid();
				
			}
		}

		removeObsoleteShapes(workGrid, closed);
		
		boolean allCornersRound = false;
		if(options.processingOptions.areAllCornersRound()) allCornersRound = true;
		
		//make shapes from the boundary sets
		//make closed shapes
		LOG.finer("***** MAKING SHAPES FROM BOUNDARY SETS *****");
		LOG.finer("***** CLOSED: *****");
		
		List<DiagramComponent> closedShapes = new ArrayList<DiagramComponent>();
		for (CellSet set : closed) {
			set.printAsGrid();
			
			DiagramShape shape = DiagramComponent.createClosedFromBoundaryCells(workGrid, set, cellWidth, cellHeight, allCornersRound); 
			if (shape != null) {
				shapes.add(shape);
				closedShapes.add(shape);
			}
		}

		if(options.processingOptions.performSeparationOfCommonEdges())
			separateCommonEdges(closedShapes);

		//make open shapes
		for (CellSet set : open){
			if(set.size() == 1){ //single cell "shape"
				TextGrid.Cell cell = (TextGrid.Cell) set.getFirst();
				if(!grid.cellContainsDashedLineChar(cell)) { 
					DiagramShape shape = DiagramShape.createSmallLine(workGrid, cell, cellWidth, cellHeight); 
					if(shape != null) {
						shapes.add(shape); 
						shape.connectEndsToAnchors(workGrid, this);
					}
				}
			} else { //normal shape
                LOG.finer(set.getCellsAsString());
				
				CompositeDiagramShape shape =
					CompositeDiagramShape
						.createOpenFromBoundaryCells(
								workGrid, set, cellWidth, cellHeight, allCornersRound);

				if(shape != null){
					compositeShapes.add(shape);
					shape.connectEndsToAnchors(workGrid, this);
				}
					
			}
		}

		//assign color codes to shapes
		//TODO: text on line should not change its color
		for (CellColorPair pair : colorPair) {
			ShapePoint point =
				new ShapePoint(getCellMidX(pair.cell), getCellMidY(pair.cell));
			DiagramShape containingShape = findSmallestShapeContaining(point);
			
			if(containingShape != null)
				containingShape.setFillColor(pair.color);
		}

		//assign markup to shapes
		for (CellTagPair pair : tagPair) {
			ShapePoint point =
				new ShapePoint(getCellMidX(pair.cell), getCellMidY(pair.cell));
			
			DiagramShape containingShape = findSmallestShapeContaining(point);
			
			//this tag is not within a shape, skip
			if(containingShape == null) continue;
			
			handleCellTagPair(options, pair, containingShape);
		}
		
		//make arrowheads
		for (Cell cell : workGrid.findArrowheads()) {
			DiagramShape arrowhead = DiagramShape.createArrowhead(workGrid, cell, cellWidth, cellHeight);
			if(arrowhead != null) shapes.add(arrowhead);
			else LOG.warning("Could not create arrowhead shape. Unexpected error.");
		}
		
		//make point markers
		for (Cell cell : grid.getPointMarkersOnLine()) {
			DiagramShape mark = new DiagramShape();
			mark.addToPoints(new ShapePoint(
					getCellMidX(cell),
					getCellMidY(cell)
				));
			mark.setType(DiagramShape.TYPE_POINT_MARKER);
			mark.setFillColor(Color.white);
			shapes.add(mark);
		}

		LOG.finer("Shape count: "+shapes.size());
		LOG.finer("Composite shape count: "+compositeShapes.size());
		
		//copy again
		workGrid = new TextGrid(grid);
		workGrid.removeNonText();
		
		
		// ****** handle text *******
		//break up text into groups
		TextGrid textGroupGrid = new TextGrid(workGrid);

		
		Font font = FontMeasurer.instance().getFontFor(cellHeight);

		for (CellStringPair pair : textGroupGrid.findStrings()) {
			TextGrid.Cell cell = pair.cell;
			String string = pair.string;
			LOG.fine("Found string " + string);
			TextGrid.Cell lastCell = new Cell(cell.x + string.length() - 1, cell.y);

			int minX = getCellMinX(cell);
			int y = getCellMaxY(cell);
			int maxX = getCellMaxX(lastCell);

			DiagramText textObject;
			if (FontMeasurer.instance().getWidthFor(string, font) > maxX - minX) {
				// does not fit horizontally
				Font lessWideFont = FontMeasurer.instance().getFontFor(maxX - minX, string);
				textObject = new DiagramText(minX, y, string, lessWideFont);
			} else {
				textObject = new DiagramText(minX, y, string, font);
			}

			textObject.centerVerticallyBetween(getCellMinY(cell), getCellMaxY(cell));
			// TODO: if the strings start with bullets they should be aligned to the left

			//position text correctly
			int otherStart = textGroupGrid.otherStringsStartInTheSameColumn(cell);
			int otherEnd = textGroupGrid.otherStringsEndInTheSameColumn(lastCell);
			if (otherEnd > 0 && otherStart < otherEnd) {
				textObject.alignRightEdgeTo(maxX);
			}

			DiagramShape shape = findSmallestShapeIntersecting(textObject.getBounds());
			if (shape != null
					&& shape.getFillColor() != null
					&& BitmapRenderer.isColorDark(shape.getFillColor())) {
				LOG.info("Corrected color of text according to underlying color");
				textObject.setColor(Color.WHITE);
			}
			addToTextObjects(textObject);
		}
		
		LOG.info("Positioned text");
	}

	private void handleCellTagPair(ConversionOptions options, CellTagPair pair,
			DiagramShape containingShape) {
		@SuppressWarnings("serial")
		final Map<String, Integer> tagType = new HashMap<String, Integer>() {{
			put("d", DiagramShape.TYPE_DOCUMENT);
			put("s", DiagramShape.TYPE_STORAGE);
			put("io", DiagramShape.TYPE_IO);
			put("c", DiagramShape.TYPE_DECISION);
			put("mo", DiagramShape.TYPE_MANUAL_OPERATION);
			put("tr", DiagramShape.TYPE_TRAPEZOID);
			put("o", DiagramShape.TYPE_ELLIPSE);
		}};
		Integer type = tagType.get(pair.tag);
		if (type != null) {
			CustomShapeDefinition def = options.processingOptions.getFromCustomShapes(pair.tag);
			if (def == null) {
				containingShape.setType(type);
			} else {
				containingShape.setType(DiagramShape.TYPE_CUSTOM);
				containingShape.setDefinition(def);
			}
		} else {
			CustomShapeDefinition def =
				options.processingOptions.getFromCustomShapes(pair.tag);
			containingShape.setType(DiagramShape.TYPE_CUSTOM);
			containingShape.setDefinition(def);						
		}
	}


	
	/**
	 * Returns a list of all DiagramShapes in the Diagram, including
	 * the ones within CompositeDiagramShapes
	 * 
	 * @return
	 */
	public List<DiagramShape> getAllDiagramShapes(){
		List<DiagramShape> shapes = new ArrayList<DiagramShape>();
		shapes.addAll(this.shapes);
		
		for(CompositeDiagramShape compShape : getCompositeShapes()) {
			shapes.addAll(compShape.getShapes());
		}
		return shapes;		
	}
	
	/**
	 * Removes the sets from <code>sets</code>that are the sum of their parts
	 * when plotted as filled shapes.
	 * 
	 * @return true if it removed any obsolete.
	 * 
	 */
	private boolean removeObsoleteShapes(TextGrid grid, List<CellSet> sets){
		LOG.finer("******* Removing obsolete shapes *******");
		
		boolean removedAny = false;
		
		List<CellSet> filledSets = new ArrayList<CellSet>();

		LOG.finer("******* Sets before *******");
		for (CellSet set : sets) {
			set.printAsGrid();
		}

		//make filled versions of all the boundary sets
		for (CellSet set : sets) {
			set = set.getFilledEquivalent(grid);
			if(set == null){
				return false;
			} else filledSets.add(set);
		}
		
		List<Integer> toBeRemovedIndices = new ArrayList<Integer>();

		for (CellSet set : filledSets){
			LOG.finer("*** Deciding if the following should be removed:");
			set.printAsGrid();
			
			//find the other sets that have common cells with set
			List<CellSet> common = new ArrayList<CellSet>();
			common.add(set);
			for (CellSet set2 : filledSets) {
				if(set != set2 && set.hasCommonCells(set2)){
					common.add(set2);
				}
			}
			//it only makes sense for more than 2 sets
			if(common.size() == 2) continue;
			
			//find largest set
			CellSet largest = set;
			for (CellSet set2 : common) {
				if(set2.size() > largest.size()){
					largest = set2;
				}
			}
			
			LOG.finer("Largest:");
			largest.printAsGrid();

			//see if largest is sum of others
			common.remove(largest);

			//make the sum set of the small sets on a grid
			TextGrid gridOfSmalls = new TextGrid(largest.getMaxX() + 2, largest.getMaxY() + 2);

			for (CellSet set2 : common){
				LOG.finer("One of smalls:");
				set2.printAsGrid();

				gridOfSmalls.fillCellsWith(set2, '*');
			}
			
			LOG.finer("Sum of smalls:");
			gridOfSmalls.printDebug();

			TextGrid gridLargest = new TextGrid(largest.getMaxX() + 2, largest.getMaxY() + 2);
			gridLargest.fillCellsWith(largest, '*');

			int index = filledSets.indexOf(largest);
			if(gridLargest.equals(gridOfSmalls)
					&& !toBeRemovedIndices.contains(index)) {
				toBeRemovedIndices.add(index);

				LOG.finer("Decided to remove set:");
				largest.printAsGrid();

			} /*else if (DEBUG){
				System.out.println("This set WILL NOT be removed:");
				largest.printAsGrid();
			}*/
			//if(gridLargest.equals(gridOfSmalls)) toBeRemovedIndices.add(new Integer(index));
		}
		
		List<CellSet> setsToBeRemoved = new ArrayList<CellSet>();
		for (int i : toBeRemovedIndices){
			setsToBeRemoved.add(sets.get(i));
		}
	
		for (CellSet set : setsToBeRemoved) {
			removedAny = true;
			sets.remove(set);
		}
	
		LOG.finer("******* Sets after *******");
		for (CellSet set : sets) {
			set.printAsGrid();
		}
		
		return removedAny;
	}
	
	public float getMinimumOfCellDimension(){
		return Math.min(getCellWidth(), getCellHeight());
	}
	
	private void separateCommonEdges(List<? extends DiagramComponent> shapes){

		float offset = getMinimumOfCellDimension() / 5;

		List<ShapeEdge> edges = new ArrayList<ShapeEdge>();

		//get all adges
		for (DiagramComponent shape : shapes) {
			edges.addAll(((DiagramShape)shape).getEdges());
		}
		
		//group edges into pairs of touching edges
		List<Pair<ShapeEdge>> listOfPairs = new ArrayList<Pair<ShapeEdge>>();
		
		//all-against-all touching test for the edges
		int startIndex = 1; //skip some to avoid duplicate comparisons and self-to-self comparisons
		
		for (ShapeEdge edge1 : edges) {
			for(int k = startIndex; k < edges.size(); k++) {
				ShapeEdge edge2 =  edges.get(k);
				
				if(edge1.touchesWith(edge2)) {
					listOfPairs.add(new Pair<ShapeEdge>(edge1, edge2));
				}
			}
			startIndex++;
		}
		
		List<ShapeEdge> movedEdges = new ArrayList<ShapeEdge>();
		
		//move equivalent edges inwards
		for (Pair<ShapeEdge> pair : listOfPairs) {
			if(!movedEdges.contains(pair.first)) {
				pair.first.moveInwardsBy(offset);
				movedEdges.add(pair.first);
			}
			if(!movedEdges.contains(pair.second)) {
				pair.second.moveInwardsBy(offset);
				movedEdges.add(pair.second);
			}
		}

	}

	private DiagramShape findSmallestShapeContaining(ShapePoint point) {
		DiagramShape containingShape = null;
		for (DiagramShape shape : shapes) {
			if(shape.contains(point)){
				if(containingShape == null){
					containingShape = shape;
				} else {
					if(shape.isSmallerThan(containingShape)){
						containingShape = shape;
					}
				}
			}
		}
		return containingShape;
	}
	
	private DiagramShape findSmallestShapeIntersecting(Rectangle2D rect) {
		DiagramShape intersectingShape = null;
		for (DiagramShape shape : shapes) {
			if(shape.intersects(rect)){
				if(intersectingShape == null){
					intersectingShape = shape;
				} else {
					if(shape.isSmallerThan(intersectingShape)){
						intersectingShape = shape;
					}
				}
			}
		}
		return intersectingShape;
	}
	
	private void addToTextObjects(DiagramText shape){
		textObjects.add(shape);
	}
	
	/**
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return
	 */
	public int getCellWidth() {
		return cellWidth;
	}

	/**
	 * @return
	 */
	public int getCellHeight() {
		return cellHeight;
	}

	/**
	 * @return
	 */
	public List<CompositeDiagramShape> getCompositeShapes() {
		return compositeShapes;
	}
	
	public int getCellMinX(TextGrid.Cell cell){
		return getCellMinX(cell, cellWidth);
	}
	public static int getCellMinX(TextGrid.Cell cell, int cellXSize){
		return cell.x * cellXSize;
	}

	public int getCellMidX(TextGrid.Cell cell){
		return getCellMidX(cell, cellWidth);
	}
	public static int getCellMidX(TextGrid.Cell cell, int cellXSize){
		return cell.x * cellXSize + cellXSize / 2;
	}

	public int getCellMaxX(TextGrid.Cell cell){
		return getCellMaxX(cell, cellWidth);
	}
	public static int getCellMaxX(TextGrid.Cell cell, int cellXSize){
		return cell.x * cellXSize + cellXSize;
	}

	public int getCellMinY(TextGrid.Cell cell){
		return getCellMinY(cell, cellHeight);
	}
	public static int getCellMinY(TextGrid.Cell cell, int cellYSize){
		return cell.y * cellYSize;
	}

	public int getCellMidY(TextGrid.Cell cell){
		return getCellMidY(cell, cellHeight);
	}
	public static int getCellMidY(TextGrid.Cell cell, int cellYSize){
		return cell.y * cellYSize + cellYSize / 2;
	}

	public int getCellMaxY(TextGrid.Cell cell){
		return getCellMaxY(cell, cellHeight);
	}
	public static int getCellMaxY(TextGrid.Cell cell, int cellYSize){
		return cell.y * cellYSize + cellYSize;
	}

	public TextGrid.Cell getCellFor(ShapePoint point){
		if(point == null) throw new IllegalArgumentException("ShapePoint cannot be null");
		//TODO: the fake grid is a problem
		return new Cell((int) point.x / cellWidth,
							(int) point.y / cellHeight);
	}


	/**
	 * @return
	 */
	public List<DiagramText> getTextObjects() {
		return textObjects;
	}

}
