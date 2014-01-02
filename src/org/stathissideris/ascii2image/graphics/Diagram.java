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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.stathissideris.ascii2image.core.ConversionOptions;
import org.stathissideris.ascii2image.core.Pair;
import org.stathissideris.ascii2image.text.AbstractionGrid;
import org.stathissideris.ascii2image.text.CellSet;
import org.stathissideris.ascii2image.text.TextGrid;
import org.stathissideris.ascii2image.text.TextGrid.Cell;
import org.stathissideris.ascii2image.text.TextGrid.CellColorPair;
import org.stathissideris.ascii2image.text.TextGrid.CellStringPair;
import org.stathissideris.ascii2image.text.TextGrid.CellTagPair;

import yskkin.ascii2image.util.Loggers;

/**
 * 
 * @author Efstathios Sideris
 */
public class Diagram {

	private static final Logger LOG = Loggers.getLogger(Diagram.class);

	private ArrayList<DiagramShape> shapes = new ArrayList<DiagramShape>();
	private ArrayList<CompositeDiagramShape> compositeShapes = new ArrayList<CompositeDiagramShape>();
	private ArrayList<DiagramText> textObjects = new ArrayList<DiagramText>();
	
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
		
		TextGrid workGrid = new TextGrid(grid);
		workGrid.replaceTypeOnLine();
		workGrid.replacePointMarkersOnLine();
		workGrid.printDebug();
		
		int width = grid.getWidth();
		int height = grid.getHeight();

	
		ArrayList<CellSet> boundarySetsStep2 = findBoundariesFromGrid(workGrid, width, height);

		LOG.finer("******* Removed duplicates *******");

		boundarySetsStep2 = CellSet.removeDuplicateSets(boundarySetsStep2);

		for (CellSet set : boundarySetsStep2) {
			set.printAsGrid();
		}
		LOG.finer(
				"******* Removed duplicates: now there are "
				+ boundarySetsStep2.size()
				+ " shapes.");


		//split boundaries to open, closed and mixed
		
		LOG.finer("******* First evaluation of openess *******");
		
		ArrayList<CellSet> open = new ArrayList<CellSet>();
		ArrayList<CellSet> closed = new ArrayList<CellSet>();
		ArrayList<CellSet> mixed = new ArrayList<CellSet>();
		
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
					boundarySetsStep2.addAll(set.breakIntoDistinctBoundaries(workGrid));
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

		boolean removedAnyObsolete = removeObsoleteShapes(workGrid, closed);
		
		boolean allCornersRound = false;
		if(options.processingOptions.areAllCornersRound()) allCornersRound = true;
		
		//make shapes from the boundary sets
		//make closed shapes
		LOG.finer("***** MAKING SHAPES FROM BOUNDARY SETS *****");
		LOG.finer("***** CLOSED: *****");
		
		ArrayList<DiagramComponent> closedShapes = new ArrayList<DiagramComponent>();
		for (CellSet set : closed) {
			set.printAsGrid();
			
			DiagramComponent shape = DiagramComponent.createClosedFromBoundaryCells(workGrid, set, cellWidth, cellHeight, allCornersRound); 
			if(shape != null){
				if(shape instanceof DiagramShape){
					addToShapes((DiagramShape) shape);
					closedShapes.add(shape);
				} else if(shape instanceof CompositeDiagramShape)
					addToCompositeShapes((CompositeDiagramShape) shape);
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
						addToShapes(shape); 
						shape.connectEndsToAnchors(workGrid, this);
					}
				}
			} else { //normal shape
                LOG.finer(set.getCellsAsString());
				
				DiagramComponent shape =
					CompositeDiagramShape
						.createOpenFromBoundaryCells(
								workGrid, set, cellWidth, cellHeight, allCornersRound);

				if(shape != null){
					if(shape instanceof CompositeDiagramShape){
						addToCompositeShapes((CompositeDiagramShape) shape);
						((CompositeDiagramShape) shape).connectEndsToAnchors(workGrid, this);
					} else if(shape instanceof DiagramShape) {
						addToShapes((DiagramShape) shape);
						((DiagramShape) shape).connectEndsToAnchors(workGrid, this);
						((DiagramShape) shape).moveEndsToCellEdges(grid, this);
					}
				}
					
			}
		}

		//assign color codes to shapes
		//TODO: text on line should not change its color
		for (CellColorPair pair : grid.findColorCodes()) {
			ShapePoint point =
				new ShapePoint(getCellMidX(pair.cell), getCellMidY(pair.cell));
			DiagramShape containingShape = findSmallestShapeContaining(point);
			
			if(containingShape != null)
				containingShape.setFillColor(pair.color);
		}

		//assign markup to shapes
		for (CellTagPair pair : grid.findMarkupTags()) {
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
			if(arrowhead != null) addToShapes(arrowhead);
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

		removeDuplicateShapes();
		
		LOG.finer("Shape count: "+shapes.size());
		LOG.finer("Composite shape count: "+compositeShapes.size());
		
		//copy again
		workGrid = new TextGrid(grid);
		workGrid.removeNonText();
		
		
		// ****** handle text *******
		//break up text into groups
		TextGrid textGroupGrid = new TextGrid(workGrid);
		CellSet gaps = textGroupGrid.getAllBlanksBetweenCharacters();
		//kludge
		textGroupGrid.fillCellsWith(gaps, '|');
		CellSet nonBlank = textGroupGrid.getAllNonBlank();
		ArrayList<CellSet> textGroups = nonBlank.breakIntoDistinctBoundaries();
		LOG.info(textGroups.size()+" text groups found");
		
		Font font = FontMeasurer.instance().getFontFor(cellHeight);
		
		for (CellSet textGroupCellSet : textGroups) {
			TextGrid isolationGrid = new TextGrid(width, height);
			workGrid.copyCellsTo(textGroupCellSet, isolationGrid);
			 
			for (CellStringPair pair : isolationGrid.findStrings()) {
				TextGrid.Cell cell = pair.cell;
				String string = pair.string;
				LOG.fine("Found string "+string);
				TextGrid.Cell lastCell = isolationGrid.new Cell(cell.x + string.length() - 1, cell.y);
			
				int minX = getCellMinX(cell);
				int y = getCellMaxY(cell);
				int maxX = getCellMaxX(lastCell);
			
				DiagramText textObject;
				if(FontMeasurer.instance().getWidthFor(string, font) > maxX - minX){ //does not fit horizontally
					Font lessWideFont = FontMeasurer.instance().getFontFor(maxX - minX, string);
					textObject = new DiagramText(minX, y, string, lessWideFont);
				} else textObject = new DiagramText(minX, y, string, font);
			
				textObject.centerVerticallyBetween(getCellMinY(cell), getCellMaxY(cell));
			
				//TODO: if the strings start with bullets they should be aligned to the left
			
				//position text correctly
				int otherStart = isolationGrid.otherStringsStartInTheSameColumn(cell);
				int otherEnd = isolationGrid.otherStringsEndInTheSameColumn(lastCell);
				if(0 == otherStart && 0 == otherEnd) {
					textObject.centerHorizontallyBetween(minX, maxX);
				} else if(otherEnd > 0 && otherStart == 0) {
					textObject.alignRightEdgeTo(maxX);
				} else if(otherEnd > 0 && otherStart > 0){
					if(otherEnd > otherStart){
						textObject.alignRightEdgeTo(maxX);
					} else if(otherEnd == otherStart){
						textObject.centerHorizontallyBetween(minX, maxX);
					}
				}
			
				addToTextObjects(textObject);
			}
		}
		
		LOG.info("Positioned text");
		
		//correct the color of the text objects according
		//to the underlying color
		for(DiagramText textObject : getTextObjects()) {
			DiagramShape shape = findSmallestShapeIntersecting(textObject.getBounds());
			if(shape != null 
					&& shape.getFillColor() != null 
					&& BitmapRenderer.isColorDark(shape.getFillColor())) {
				textObject.setColor(Color.white);
			}
		}

		//set outline to true for test within custom shapes
		for (DiagramShape shape : getAllDiagramShapes()) {
			if(shape.getType() == DiagramShape.TYPE_CUSTOM){
				for (DiagramText textObject : getTextObjects()) {
					textObject.setHasOutline(true);
					textObject.setColor(DiagramText.DEFAULT_COLOR);
				}
			}
		}
		
		LOG.info("Corrected color of text according to underlying color");

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

	private ArrayList<CellSet> findBoundariesFromGrid(TextGrid workGrid,
			int width, int height) {
		//split distinct shapes using AbstractionGrid 
		AbstractionGrid temp = new AbstractionGrid(workGrid, workGrid.getAllBoundaries());
		ArrayList<CellSet> boundarySetsStep1 = temp.getDistinctShapes();
		
		LOG.finer("******* Distinct shapes found using AbstractionGrid *******");
		for (CellSet set : boundarySetsStep1) {
			set.printAsGrid();
		}
		LOG.finer("******* Same set of shapes after processing them by filling *******");
		
		
		//Find all the boundaries by using the special version of the filling method
		//(fills in a different buffer than the buffer it reads from)
		ArrayList<CellSet> boundarySetsStep2 = new ArrayList<CellSet>();
		for(CellSet set : boundarySetsStep1) {			
			//the fill buffer keeps track of which cells have been
			//filled already
			TextGrid fillBuffer = new TextGrid(width * 3, height * 3);
			
			for(int yi = 0; yi < height * 3; yi++){
				for(int xi = 0; xi < width * 3; xi++){
					if(fillBuffer.isBlank(xi, yi)){
						
						TextGrid copyGrid = new AbstractionGrid(workGrid, set).getCopyOfInternalBuffer();

						CellSet boundaries =
							copyGrid
							.findBoundariesExpandingFrom(copyGrid.new Cell(xi, yi));
						if(boundaries.size() == 0) continue; //i'm not sure why these occur
						boundarySetsStep2.add(boundaries.makeScaledOneThirdEquivalent());
					
						copyGrid = new AbstractionGrid(workGrid, set).getCopyOfInternalBuffer();
						CellSet filled =
							copyGrid
							.fillContinuousArea(copyGrid.new Cell(xi, yi), '*');
						fillBuffer.fillCellsWith(filled, '*');
						fillBuffer.fillCellsWith(boundaries, '-');
						
						//System.out.println("Fill buffer:");
						//fillBuffer.printDebug();
						boundaries.makeScaledOneThirdEquivalent().printAsGrid();
						LOG.finer("-----------------------------------");
						
					}
				}
			}
		}
		return boundarySetsStep2;
	}
	
	/**
	 * Returns a list of all DiagramShapes in the Diagram, including
	 * the ones within CompositeDiagramShapes
	 * 
	 * @return
	 */
	public ArrayList<DiagramShape> getAllDiagramShapes(){
		ArrayList<DiagramShape> shapes = new ArrayList<DiagramShape>();
		shapes.addAll(this.getShapes());
		
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
	private boolean removeObsoleteShapes(TextGrid grid, ArrayList<CellSet> sets){
		LOG.finer("******* Removing obsolete shapes *******");
		
		boolean removedAny = false;
		
		ArrayList<CellSet> filledSets = new ArrayList<CellSet>();

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
		
		ArrayList<Integer> toBeRemovedIndices = new ArrayList<Integer>();

		for (CellSet set : filledSets){
			LOG.finer("*** Deciding if the following should be removed:");
			set.printAsGrid();
			
			//find the other sets that have common cells with set
			ArrayList<CellSet> common = new ArrayList<CellSet>();
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
					&& !toBeRemovedIndices.contains(new Integer(index))) {
				toBeRemovedIndices.add(new Integer(index));

				LOG.finer("Decided to remove set:");
				largest.printAsGrid();

			} /*else if (DEBUG){
				System.out.println("This set WILL NOT be removed:");
				largest.printAsGrid();
			}*/
			//if(gridLargest.equals(gridOfSmalls)) toBeRemovedIndices.add(new Integer(index));
		}
		
		ArrayList<CellSet> setsToBeRemoved = new ArrayList<CellSet>();
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
	
	private void separateCommonEdges(ArrayList<? extends DiagramComponent> shapes){

		float offset = getMinimumOfCellDimension() / 5;

		ArrayList<ShapeEdge> edges = new ArrayList<ShapeEdge>();

		//get all adges
		for (DiagramComponent shape : shapes) {
			edges.addAll(((DiagramShape)shape).getEdges());
		}
		
		//group edges into pairs of touching edges
		ArrayList<Pair<ShapeEdge, ShapeEdge>> listOfPairs = new ArrayList<Pair<ShapeEdge, ShapeEdge>>();
		
		//all-against-all touching test for the edges
		int startIndex = 1; //skip some to avoid duplicate comparisons and self-to-self comparisons
		
		for (ShapeEdge edge1 : edges) {
			for(int k = startIndex; k < edges.size(); k++) {
				ShapeEdge edge2 =  edges.get(k);
				
				if(edge1.touchesWith(edge2)) {
					listOfPairs.add(new Pair<ShapeEdge, ShapeEdge>(edge1, edge2));
				}
			}
			startIndex++;
		}
		
		ArrayList<ShapeEdge> movedEdges = new ArrayList<ShapeEdge>();
		
		//move equivalent edges inwards
		for (Pair<ShapeEdge, ShapeEdge> pair : listOfPairs) {
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
	
	
	//TODO: removes more than it should
	private void removeDuplicateShapes() {
		ArrayList<DiagramShape> originalShapes = new ArrayList<DiagramShape>();

		for (DiagramShape shape : shapes) {
			boolean isOriginal = true;
			for (DiagramShape originalShape : originalShapes) {
				if(shape.equals(originalShape)){
					isOriginal = false;
				}
			}
			if(isOriginal) originalShapes.add(shape);
		}

		shapes.clear();
		shapes.addAll(originalShapes);
	}
	
	private DiagramShape findSmallestShapeContaining(ShapePoint point) {
		DiagramShape containingShape = null;
		for (DiagramShape shape : getShapes()) {
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
		for (DiagramShape shape : getShapes()) {
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

	private void addToCompositeShapes(CompositeDiagramShape shape){
		compositeShapes.add(shape);
	}

	
	private void addToShapes(DiagramShape shape){
		shapes.add(shape);
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
	public ArrayList<CompositeDiagramShape> getCompositeShapes() {
		return compositeShapes;
	}

	/**
	 * @return
	 */
	public ArrayList<DiagramShape> getShapes() {
		return shapes;
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
		TextGrid g = new TextGrid();
		return g.new Cell((int) point.x / cellWidth,
							(int) point.y / cellHeight);
	}


	/**
	 * @return
	 */
	public ArrayList<DiagramText> getTextObjects() {
		return textObjects;
	}

}
