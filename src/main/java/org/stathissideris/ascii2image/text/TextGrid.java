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

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stathissideris.ascii2image.core.FileUtils;
import org.stathissideris.ascii2image.core.ProcessingOptions;

import yskkin.ascii2image.util.Loggers;
import yskkin.ascii2image.util.PresetColorCode;
import yskkin.ascii2image.util.PresetTag;


/**
 * 
 * @author Efstathios Sideris
 */
public class TextGrid {

	private static final Logger LOG = Loggers.getLogger(TextGrid.class);

	private List<StringBuilder> rows;

	private static char[] boundaries = {'/', '\\', '|', '-', '*', '=', ':'};
	private static char[] undisputableBoundaries = {'|', '-', '*', '=', ':'};
	private static char[] horizontalLines = {'-', '='};
	private static char[] verticalLines = {'|', ':'};
	private static char[] arrowHeads = {'<', '>', '^', 'v', 'V'};
	private static char[] cornerChars = {'\\', '/', '+'};
	private static char[] pointMarkers = {'*'};
	private static char[] dashedLines = {':', '~', '='};
	
	@SuppressWarnings("serial")
	private static final Map<Integer, Character[]> entryPoints = new HashMap<Integer, Character[]>() {{
		put(1, new Character[]{'\\'});
		put(2, new Character[]{'|', ':', '+', '\\', '/'});
		put(3, new Character[]{'/'});
		put(4, new Character[]{'-', '=', '+', '\\', '/'});
		put(5, new Character[]{'\\'});
		put(6, new Character[]{'|', ':', '+', '\\', '/'});
		put(7, new Character[]{'/'});
		put(8, new Character[]{'-', '=', '+', '\\', '/'});
	}};

	private static PresetTag tag = new PresetTag();

	public void addToMarkupTags(Collection<String> tags){
		tag.addAllTag(tags);
	}

	public TextGrid(){
		rows = new ArrayList<StringBuilder>();
	}
	
	public TextGrid(int width, int height){
		String space = StringUtils.repeatString(" ", width);
		rows = new ArrayList<StringBuilder>();
		for(int i = 0; i < height; i++)
			rows.add(new StringBuilder(space));
	}

	public static TextGrid makeSameSizeAs(TextGrid grid){
		return new TextGrid(grid.getWidth(), grid.getHeight());
	}


	public TextGrid(TextGrid otherGrid){
		rows = new ArrayList<StringBuilder>();
		for(StringBuilder row : otherGrid.getRows()) {
			rows.add(new StringBuilder(row));
		}		
	}

//	duplicated code due to lots of hits to this function
	private char get(int x, int y){
		if(x > getWidth() - 1
			|| y > getHeight() - 1
			|| x < 0
			|| y < 0) return 0;
		return rows.get(y).charAt(x);
	}

	//duplicated code due to lots of hits to this function
	private char get(Cell cell){
		if(cell.x > getWidth() - 1
			|| cell.y > getHeight() - 1
			|| cell.x < 0
			|| cell.y < 0) return 0;
		return rows.get(cell.y).charAt(cell.x);
	}
	
	public StringBuilder getRow(int y){
		return rows.get(y);
	}

	public TextGrid getSubGrid(int x, int y, int width, int height){
		TextGrid grid = new TextGrid(width, height);
		for(int i = 0; i < height; i++){
			grid.setRow(i, new StringBuilder(getRow(y + i).subSequence(x, x + width)));
		}
		return grid;
	}

	private TextGrid getTestingSubGrid(Cell cell){
		return getSubGrid(cell.x - 1, cell.y - 1, 3, 3);
	}

	public void writeStringTo(int x, int y, String str){
		writeStringTo(new Cell(x, y), str);
	}

	private void writeStringTo(Cell cell, String str){
		if(isOutOfBounds(cell)) return;
		rows.get(cell.y).replace(cell.x, cell.x + str.length(), str);
	}

	private void set(Cell cell, char c){
		set(cell.x, cell.y, c);
	}

	public void set(int x, int y, char c){
		if(x > getWidth() - 1 || y > getHeight() - 1) return;
		StringBuilder row = rows.get(y);
		row.setCharAt(x, c);
	}
	
	public void setRow(int y, String row){
		if(y > getHeight() || row.length() != getWidth())
			throw new IllegalArgumentException("setRow out of bounds or string wrong size");
		rows.set(y, new StringBuilder(row));
	}

	private void setRow(int y, StringBuilder row){
		if(y > getHeight() || row.length() != getWidth())
			throw new IllegalArgumentException("setRow out of bounds or string wrong size");
		rows.set(y, row);
	}
	
	public int getWidth(){
		if(rows.size() == 0) return 0; //empty buffer
		return rows.get(0).length();
	}

	public int getHeight(){
		return rows.size();
	}

	public void printDebug(){
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("TextGrid:\n" + this);
		}
	}

	public String toString(){
		StringBuilder buffer = new StringBuilder();
		int i = 0;
		buffer.append(
			"    "
			+StringUtils.repeatString("0123456789", (int) Math.floor(getWidth()/10)+1)+"\n");
		for (StringBuilder sb : rows) {
			String row = sb.toString();
			String index = Integer.toString(i);
			if(i < 10) index = " "+index;
			row = row.replaceAll("\n", "\\\\n");
			row = row.replaceAll("\r", "\\\\r");
			buffer.append(index+" ("+row+")\n");
			i++; 
		}
		return buffer.toString();
	}

	/**
	 * Replaces letters or numbers that are on horizontal or vertical
	 * lines, with the appropriate character that will make the line
	 * continuous (| for vertical and - for horizontal lines)
	 *
	 */
	public void replaceTypeOnLine(){
		int width = getWidth();
		int height = getHeight();
		for(int yi = 0; yi < height; yi++){
			for(int xi = 0; xi < width; xi++){
				char c = get(xi, yi);
				if(Character.isLetterOrDigit(c)) {
					boolean isOnHorizontalLine = isOnHorizontalLine(xi, yi);
					boolean isOnVerticalLine = isOnVerticalLine(xi, yi); 
					if(isOnHorizontalLine && isOnVerticalLine){
						set(xi, yi, '+');
						LOG.info("replaced type on line '"+c+"' with +");
					} else if(isOnHorizontalLine){
						set(xi, yi, '-');
						LOG.info("replaced type on line '"+c+"' with -");
					} else if(isOnVerticalLine){
						set(xi, yi, '|');
						LOG.info("replaced type on line '"+c+"' with |");
					}
				}
			}
		}		
	}

	public void replacePointMarkersOnLine(){
		int width = getWidth();
		int height = getHeight();
		for(int yi = 0; yi < height; yi++){
			for(int xi = 0; xi < width; xi++){
				char c = get(xi, yi);
				Cell cell = new Cell(xi, yi);
				if(StringUtils.isOneOf(c, pointMarkers)
						&& isStarOnLine(cell)){
					
					boolean isOnHorizontalLine = false;
					if(StringUtils.isOneOf(get(cell.getEast()), horizontalLines))
						isOnHorizontalLine = true;
					if(StringUtils.isOneOf(get(cell.getWest()), horizontalLines))
						isOnHorizontalLine = true;

					boolean isOnVerticalLine = false;
					if(StringUtils.isOneOf(get(cell.getNorth()), verticalLines))
						isOnVerticalLine = true;
					if(StringUtils.isOneOf(get(cell.getSouth()), verticalLines))
						isOnVerticalLine = true;
 
					if(isOnHorizontalLine && isOnVerticalLine){
						set(xi, yi, '+');
						LOG.info("replaced marker on line '"+c+"' with +");
					} else if(isOnHorizontalLine){
						set(xi, yi, '-');
						LOG.info("replaced marker on line '"+c+"' with -");
					} else if(isOnVerticalLine){
						set(xi, yi, '|');
						LOG.info("replaced marker on line '"+c+"' with |");
					}
				}
			}
		}		
	}

	public CellSet getPointMarkersOnLine(){
		CellSet result = new CellSet();
		int width = getWidth();
		int height = getHeight();
		for(int yi = 0; yi < height; yi++){
			for(int xi = 0; xi < width; xi++){
				char c = get(xi, yi);
				if(StringUtils.isOneOf(c, pointMarkers)
						&& isStarOnLine(new Cell(xi, yi))){
					result.add(new Cell(xi, yi));
				}
			}
		}
		return result;
	}

	public boolean hasBlankCells(){
		Pattern blank = Pattern.compile("\\s");
		for (int y = 0; y < getHeight(); y++) {
			Matcher matcher = blank.matcher(rows.get(y));
			if (matcher.find()) {
				return true;
			}
		}
		return false;
	}

	public CellSet getAllNonBlank(){
		Pattern nonBlank = Pattern.compile("\\S");
		CellSet set = new CellSet();
		for (int y = 0; y < getHeight(); y++){
			Matcher matcher = nonBlank.matcher(rows.get(y));
			while (matcher.find()) {
				set.add(new Cell(matcher.start(), y));
			}
		}
		return set;
	}

	public CellSet getAllBoundaries(){
		CellSet set = new CellSet();
		int width = getWidth();
		int height = getHeight();
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				Cell cell = new Cell(x, y);
				if(isBoundary(cell)) set.add(cell);
			}
		}
		return set;
	}

	/**
	 * Returns an List of CellStringPairs that
	 * represents all the continuous (non-blank) Strings
	 * in the grid. Used on buffers that contain only
	 * type, in order to find the positions and the
	 * contents of the strings.
	 * 
	 * @return
	 */
	public List<CellStringPair> findStrings(){
		List<CellStringPair> result = new ArrayList<CellStringPair>();
		// This is weird, but compatible with original ditaa.
		Pattern stringPattern = Pattern.compile("(\\S..*?)(\\s\\s|\\s$|$)");
		for (int y = 0; y < getHeight(); y++) {
			Matcher matcher = stringPattern.matcher(rows.get(y));
			while (matcher.find()) {
				result.add(new CellStringPair(new Cell(matcher.start(1), y), matcher.group(1)));
			}
		}
		return result;
	}

	/**
	 * This is done in a bit of a messy way, should be impossible
	 * to go out of sync with corresponding GridPatternGroup.
	 * 
	 * @param cell
	 * @param entryPointId
	 * @return
	 */
	private boolean hasEntryPoint(Cell cell, int entryPointId){
		if (0 < entryPointId && entryPointId < 9) {
			char c = get(cell);
			return StringUtils.isOneOf(c, entryPoints.get(entryPointId));
		}
		return false;
	}

	/**
	 * Makes blank all the cells that contain non-text
	 * elements.
	 */
	public void removeNonText(){
		//the following order is significant
		//since the south-pointing arrowheads
		//are determined based on the surrounding boundaries
		removeArrowheads();
		// TODO This removes textual colon (e.g. URL's 'http://').
		removeBoundaries();
	}

	private void removeArrowheads(){
		int width = getWidth();
		int height = getHeight();
		for(int yi = 0; yi < height; yi++){
			for(int xi = 0; xi < width; xi++){
				Cell cell = new Cell(xi, yi);
				if(isArrowhead(cell)) set(cell, ' ');
			}
		}		
	}

	private void removeBoundaries(){
		List<Cell> toBeRemoved = new ArrayList<Cell>();

		int width = getWidth();
		int height = getHeight();
		for(int yi = 0; yi < height; yi++){
			for(int xi = 0; xi < width; xi++){
				Cell cell = new Cell(xi, yi);
				if(isBoundary(cell)) toBeRemoved.add(cell);
			}
		}
		
		//remove in two stages, because decision of
		//isBoundary depends on contants of surrounding
		//cells 
		for (Cell cell : toBeRemoved) {
			set(cell, ' ');
		}
	}

	public List<Cell> findArrowheads(){
		List<Cell> result = new ArrayList<Cell>();
		int width = getWidth();
		int height = getHeight();
		for(int yi = 0; yi < height; yi++){
			for(int xi = 0; xi < width; xi++){
				Cell cell = new Cell(xi, yi);
				if(isArrowhead(cell)) result.add(cell);
			}
		}
		LOG.fine(result.size()+" arrowheads found");
		return result;
	}

	/**
	 * Read color code on TextGrid and remove it from underlying TextGrid.
	 * 
	 * @return color code appeared on TextGrid.
	 */
	public List<CellColorPair> resolveColorCode() {
		PresetColorCode pcc = new PresetColorCode();
		Pattern colorCodePattern = pcc.getColorCodePattern();
		List<CellColorPair> result = new ArrayList<CellColorPair>();

		for (int y = 0; y < getHeight(); y++) {
			StringBuilder row = rows.get(y);
			Matcher matcher = colorCodePattern.matcher(row);
			while (matcher.find()) {
				Color color = pcc.getColor(matcher.group(1));
				result.add(new CellColorPair(new Cell(matcher.start(), y), color));
				row.replace(matcher.start(), matcher.end(), "    ");
			}
		}
		LOG.info(result.size() + " color codes found");
		return result;
	}

	public List<CellTagPair> resolveTag() {
		Pattern tagPattern = tag.getTagPatern();
		List<CellTagPair> result = new ArrayList<CellTagPair>();

		for (int y = 0; y < getHeight(); y++) {
			StringBuilder row = rows.get(y);
			Matcher matcher = tagPattern.matcher(row);
			while (matcher.find()) {
				String tagName = matcher.group(1);
				int x = matcher.start();
				int end = matcher.end();
				LOG.fine("found tag " + tagName + " at " + x + ", " + y);
				result.add(new CellTagPair(new Cell(x, y), tagName));
				String padding = StringUtils.repeatString(" ", end - x);
				row.replace(x, end, padding);
			}
		}
		return result;
	}


	private boolean matchesAny(GridPatternGroup criteria){
		return criteria.isAnyMatchedBy(this);
	}

	private boolean isOnHorizontalLine(int x, int y){
		char c1 = get(x - 1, y);
		char c2 = get(x + 1, y);
		if(isHorizontalLine(c1) && isHorizontalLine(c2)) return true;
		return false;
	}

	private boolean isOnVerticalLine(int x, int y){
		char c1 = get(x, y - 1);
		char c2 = get(x, y + 1);
		if(isVerticalLine(c1) && isVerticalLine(c2)) return true;
		return false;
	}

	private boolean isBoundary(Cell cell){
		char c = get(cell.x, cell.y);
		if(0 == c) return false;
		if('+' == c || '\\' == c || '/' == c){
			if(
			       isIntersection(cell) 
				|| isCorner(cell)
				|| isStub(cell) 
				|| isCrossOnLine(cell)){
				return true;
			} else return false;
		}
		if(StringUtils.isOneOf(c, boundaries)){
			return true;
		}
		return false;
	}

	public boolean isLine(Cell cell){
		return isHorizontalLine(cell) || isVerticalLine(cell);
	}

	private static boolean isHorizontalLine(char c){
		return StringUtils.isOneOf(c, horizontalLines);
	}
	public boolean isHorizontalLine(Cell cell){ return isHorizontalLine(cell.x, cell.y); }
	private boolean isHorizontalLine(int x, int y){
		char c = get(x, y);
		if(0 == c) return false;
		return StringUtils.isOneOf(c, horizontalLines);
	}

	private static boolean isVerticalLine(char c){
		return StringUtils.isOneOf(c, verticalLines);
	}
	public boolean isVerticalLine(Cell cell){ return isVerticalLine(cell.x, cell.y); }
	private boolean isVerticalLine(int x, int y){
		char c = get(x, y);
		if(0 == c) return false;
		return StringUtils.isOneOf(c, verticalLines);
	}

	/**
	 * Stubs are also considered end of lines
	 * 
	 * @param cell
	 * @return
	 */
	public boolean isLinesEnd(Cell cell){
		return matchesAny(cell, GridPatternGroup.linesEndCriteria);
	}

	public boolean isPointCell(Cell cell){
		return (
			isCorner(cell)
			|| isIntersection(cell)
			|| isStub(cell)
			|| isLinesEnd(cell));
	}


	public boolean containsAtLeastOneDashedLine(CellSet set){
		for (Cell cell : set) {
			if(StringUtils.isOneOf(get(cell), dashedLines)) return true;
		}
		return false;
	}

	/**
	 * 
	 * A stub looks like that:
	 * 
	 * <pre>
	 * 
	 * +- or -+ or + or + or /- or -/ or / (you get the point)
	 *             |    |                |
	 * 
	 * </pre>
	 * 
	 * @param cell
	 * @return
	 */

	private boolean isStub(Cell cell){
		return matchesAny(cell, GridPatternGroup.stubCriteria);
	}

	public boolean isCrossOnLine(Cell cell){
		return matchesAny(cell, GridPatternGroup.crossOnLineCriteria);
	}

	private boolean isHorizontalCrossOnLine(Cell cell){
		return matchesAny(cell, GridPatternGroup.horizontalCrossOnLineCriteria);
	}

	private boolean isVerticalCrossOnLine(Cell cell){
		return matchesAny(cell, GridPatternGroup.verticalCrossOnLineCriteria);
	}

	public boolean isStarOnLine(Cell cell){
		return matchesAny(cell, GridPatternGroup.starOnLineCriteria);
	}

	public boolean isArrowhead(Cell cell){
		return (isNorthArrowhead(cell)
				|| isSouthArrowhead(cell)
				|| isWestArrowhead(cell)
				|| isEastArrowhead(cell));
	}
	
	public boolean isNorthArrowhead(Cell cell){
		return get(cell) == '^';
	}

	public boolean isEastArrowhead(Cell cell){
		return get(cell) == '>';
	}

	public boolean isWestArrowhead(Cell cell){
		return get(cell) == '<';
	}

	public boolean isSouthArrowhead(Cell cell){
		return (get(cell) == 'v' || get(cell) == 'V')
				&& isVerticalLine(cell.getNorth());
	}
	
	
//	unicode for bullets
//
//	2022 bullet
//	25CF black circle
//	25AA black circle (small)
//	25A0 black square
//	25A1 white square
//	25CB white circle
//	25BA black right-pointing pointer

	private void replaceBullets(){
		Pattern bulletPattern = Pattern.compile("( |^)(o|\\*) [^\\p{Punct}\\s]");
		for (int yi = 0; yi < getHeight(); yi++) {
			StringBuilder row = rows.get(yi);
			Matcher matcher = bulletPattern.matcher(row);
			while (matcher.find()) {
				int bulletPosition = matcher.start(2);
				row.replace(bulletPosition, bulletPosition + 2, " \u2022");
			}
		}
	}
	
	/**
	 * true if the cell is not blank
	 * but the previous (west) is
	 * 
	 * @param cell
	 * @return
	 */
	private boolean isStringsStart(Cell cell){
		return (!isBlank(cell) && isBlank(cell.getWest()));
	}

	/**
	 * true if the cell is not blank
	 * but the next (east) is
	 * 
	 * @param cell
	 * @return
	 */
	private boolean isStringsEnd(Cell cell){
		return (!isBlank(cell)
			//&& (isBlank(cell.getEast()) || get(cell.getEast()) == 0));
			&& isBlank(cell.getEast()));
	}

	public int otherStringsStartInTheSameColumn(Cell cell){
		if(!isStringsStart(cell)) return 0;
		int result = 0;
		int height = getHeight();
		for(int y = 0; y < height; y++){
			Cell cCell = new Cell(cell.x, y);
			if(!cCell.equals(cell) && isStringsStart(cCell)){
				result++;
			}
		}
		return result;
	}

	public int otherStringsEndInTheSameColumn(Cell cell){
		if(!isStringsEnd(cell)) return 0;
		int result = 0;
		int height = getHeight();
		for(int y = 0; y < height; y++){
			Cell cCell = new Cell(cell.x, y);
			if(!cCell.equals(cell) && isStringsEnd(cCell)){
				result++;
			}
		}
		return result;
	}

	private CellSet followIntersection(Cell cell, Cell blocked){
		if(!isIntersection(cell)) return null;
		CellSet result = new CellSet();
		Cell cN = cell.getNorth();
		Cell cS = cell.getSouth();
		Cell cE = cell.getEast();
		Cell cW = cell.getWest();
		if(hasEntryPoint(cN, 6)) result.add(cN);
		if(hasEntryPoint(cS, 2)) result.add(cS);
		if(hasEntryPoint(cE, 8)) result.add(cE);
		if(hasEntryPoint(cW, 4)) result.add(cW);
		if(result.contains(blocked)) result.remove(blocked);
		return result;
	}

	/**
	 * Returns the neighbours of a line-cell that are boundaries
	 *  (0 to 2 cells are returned)
	 * 
	 * @param cell
	 * @return null if the cell is not a line
	 */
	private CellSet followLine(Cell cell){
		if(isHorizontalLine(cell)){
			CellSet result = new CellSet();
			if(isBoundary(cell.getEast())) result.add(cell.getEast());
			if(isBoundary(cell.getWest())) result.add(cell.getWest());
			return result;
		} else if (isVerticalLine(cell)){
			CellSet result = new CellSet();
			if(isBoundary(cell.getNorth())) result.add(cell.getNorth());
			if(isBoundary(cell.getSouth())) result.add(cell.getSouth());
			return result;			
		}
		return null;
	}

	private CellSet followLine(Cell cell, Cell blocked){
		CellSet nextCells = followLine(cell);
		if(nextCells.contains(blocked)) nextCells.remove(blocked);
		return nextCells;
	}

	private CellSet followCorner(Cell cell, Cell blocked){
		if(!isCorner(cell)) return null;
		if(isCorner1(cell)) return followCorner1(cell, blocked);
		if(isCorner2(cell)) return followCorner2(cell, blocked);
		if(isCorner3(cell)) return followCorner3(cell, blocked);
		if(isCorner4(cell)) return followCorner4(cell, blocked);
		return null;
	}

	private CellSet followCorner1(Cell cell, Cell blocked){
		if(!isCorner1(cell)) return null;
		CellSet result = new CellSet();
		if(!cell.getSouth().equals(blocked)) result.add(cell.getSouth());
		if(!cell.getEast().equals(blocked)) result.add(cell.getEast());
		return result;
	}

	private CellSet followCorner2(Cell cell, Cell blocked){
		if(!isCorner2(cell)) return null;
		CellSet result = new CellSet();
		if(!cell.getSouth().equals(blocked)) result.add(cell.getSouth());
		if(!cell.getWest().equals(blocked)) result.add(cell.getWest());
		return result;
	}

	private CellSet followCorner3(Cell cell, Cell blocked){
		if(!isCorner3(cell)) return null;
		CellSet result = new CellSet();
		if(!cell.getNorth().equals(blocked)) result.add(cell.getNorth());
		if(!cell.getWest().equals(blocked)) result.add(cell.getWest());
		return result;
	}

	private CellSet followCorner4(Cell cell, Cell blocked){
		if(!isCorner4(cell)) return null;
		CellSet result = new CellSet();
		if(!cell.getNorth().equals(blocked)) result.add(cell.getNorth());
		if(!cell.getEast().equals(blocked)) result.add(cell.getEast());
		return result;
	}

	private CellSet followStub(Cell cell, Cell blocked){
		if(!isStub(cell)) return null;
		CellSet result = new CellSet();
		if(isBoundary(cell.getEast())) result.add(cell.getEast());
		else if(isBoundary(cell.getWest())) result.add(cell.getWest());
		else if(isBoundary(cell.getNorth())) result.add(cell.getNorth());
		else if(isBoundary(cell.getSouth())) result.add(cell.getSouth());
		if(result.contains(blocked)) result.remove(blocked);
		return result;
	}
	
	public CellSet followCell(Cell cell){
		return followCell(cell, null);
	}
	
	public CellSet followCell(Cell cell, Cell blocked){
		if(isIntersection(cell)) return followIntersection(cell, blocked);
		if(isCorner(cell)) return followCorner(cell, blocked);
		if(isLine(cell)) return followLine(cell, blocked);
		if(isStub(cell)) return followStub(cell, blocked);
		if(isCrossOnLine(cell)) return followCrossOnLine(cell, blocked);
		LOG.severe("Umbiguous input at position "+cell+":");
		TextGrid subGrid = getTestingSubGrid(cell);
		subGrid.printDebug();
		throw new RuntimeException("Cannot follow cell "+cell+": cannot determine cell type");
	}

	public String getCellTypeAsString(Cell cell){
		if(isK(cell)) return "K";
		if(isT(cell)) return "T";
		if(isInverseK(cell)) return "inverse K";
		if(isInverseT(cell)) return "inverse T";
		if(isCorner1(cell)) return "corner 1";
		if(isCorner2(cell)) return "corner 2";
		if(isCorner3(cell)) return "corner 3";
		if(isCorner4(cell)) return "corner 4";
		if(isLine(cell)) return "line";
		if(isStub(cell)) return "stub";
		if(isCrossOnLine(cell)) return "crossOnLine";
		return "unrecognisable type";
	}

	
	private CellSet followCrossOnLine(Cell cell, Cell blocked){
		CellSet result = new CellSet();
		if(isHorizontalCrossOnLine(cell)){
			result.add(cell.getEast());
			result.add(cell.getWest());
		} else if(isVerticalCrossOnLine(cell)){
			result.add(cell.getNorth());
			result.add(cell.getSouth());
		}
		if(result.contains(blocked)) result.remove(blocked);
		return result;
	}

	public boolean isOutOfBounds(Cell cell){
		if(cell.x > getWidth() - 1
			|| cell.y > getHeight() - 1
			|| cell.x < 0
			|| cell.y < 0) return true;
		return false;
	}

	public boolean isBlank(Cell cell){
		char c = get(cell);
		if(0 == c) return false;
		return c == ' ';
	}

	public boolean isBlank(int x, int y){
		char c = get(x, y);
		if(0 == c) return true;
		return c == ' ';
	}

	public boolean isCorner(Cell cell){
		return isCorner(cell.x, cell.y);
	}
	private boolean isCorner(int x, int y){
		return (isNormalCorner(x,y) || isRoundCorner(x,y));
	}


	private boolean matchesAny(Cell cell, GridPatternGroup criteria){
		TextGrid subGrid = getTestingSubGrid(cell);
		return subGrid.matchesAny(criteria);
	}
	
	public boolean isCorner1(Cell cell){
		return matchesAny(cell, GridPatternGroup.corner1Criteria);
	}

	public boolean isCorner2(Cell cell){
		return matchesAny(cell, GridPatternGroup.corner2Criteria);
	}

	public boolean isCorner3(Cell cell){
		return matchesAny(cell, GridPatternGroup.corner3Criteria);
	}

	public boolean isCorner4(Cell cell){
		return matchesAny(cell, GridPatternGroup.corner4Criteria);
	}

	public boolean isCross(Cell cell){
		return matchesAny(cell, GridPatternGroup.crossCriteria);
	}

	public boolean isK(Cell cell){
		return matchesAny(cell, GridPatternGroup.KCriteria);
	}

	public boolean isInverseK(Cell cell){
		return matchesAny(cell, GridPatternGroup.inverseKCriteria);
	}

	public boolean isT(Cell cell){
		return matchesAny(cell, GridPatternGroup.TCriteria);
	}

	public boolean isInverseT(Cell cell){
		return matchesAny(cell, GridPatternGroup.inverseTCriteria);
	}

	public boolean isNormalCorner(Cell cell){
		return matchesAny(cell, GridPatternGroup.normalCornerCriteria);
	}
	private boolean isNormalCorner(int x, int y){
		return isNormalCorner(new Cell(x, y));
	}

	public boolean isRoundCorner(Cell cell){
		return matchesAny(cell, GridPatternGroup.roundCornerCriteria);
	}

	private boolean isRoundCorner(int x, int y){
		return isRoundCorner(new Cell(x, y));
	}

	public boolean isIntersection(Cell cell){
		return matchesAny(cell, GridPatternGroup.intersectionCriteria);
	}

	public void copyCellsTo(CellSet cells, TextGrid grid){
		for (Cell cell : cells) {
			grid.set(cell, this.get(cell));
		}
	}
	
	public boolean equals(Object o){
		if (o == null) {
			return false;
		}
		if (this.getClass() != o.getClass()) {
			return false;
		}
		TextGrid grid = (TextGrid) o;
		if (grid.getHeight() != this.getHeight()
		|| grid.getWidth() != this.getWidth()) {
			return false;
		}
		int height = grid.getHeight();
		for (int i = 0; i < height; i++) {
			String row1 = this.getRow(i).toString();
			String row2 = grid.getRow(i).toString();
			if (!row1.equals(row2)) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		for (StringBuilder sb : rows) {
			result += sb.toString().hashCode();
		}
		return result;
	}
	
	/**
	 * Fills all the cells in <code>cells</code> with <code>c</code>
	 * 
	 * @param cells
	 * @param c
	 */
	public void fillCellsWith(Iterable<Cell> cells, char c){
		for (Cell cell : cells) {
			set(cell.x, cell.y, c);
		}
	}

	/**
	 * 
	 * Fills the continuous area with if c1 characters with c2,
	 * flooding from cell x, y
	 * 
	 * @param x
	 * @param y
	 * @param c1 the character to replace
	 * @param c2 the character to replace c1 with
	 * @return the list of cells filled
	 */
//	public CellSet fillContinuousArea(int x, int y, char c1, char c2){
//		CellSet cells = new CellSet();
//		//fillContinuousArea_internal(x, y, c1, c2, cells);
//		seedFill(new Cell(x, y), c1, c2);
//		return cells;
//	}

	public CellSet fillContinuousArea(int x, int y, char c){
		return fillContinuousArea(new Cell(x, y), c);
	}

	public CellSet fillContinuousArea(Cell cell, char c){
		if(isOutOfBounds(cell)) throw new IllegalArgumentException("Attempted to fill area out of bounds: "+cell);
		return seedFillOld(cell, c);
	}

	private CellSet seedFillOld(Cell seed, char newChar){
		CellSet cellsFilled = new CellSet();
		char oldChar = get(seed);
		
		if(oldChar == newChar) return cellsFilled;
		if(isOutOfBounds(seed)) return cellsFilled;

		Stack<Cell> stack = new Stack<Cell>();

		stack.push(seed);
		
		while(!stack.isEmpty()){
			Cell cell = (Cell) stack.pop();
			
			set(cell, newChar);
			cellsFilled.add(cell);

			Cell nCell = cell.getNorth();
			Cell sCell = cell.getSouth();
			Cell eCell = cell.getEast();
			Cell wCell = cell.getWest();
			
			if(get(nCell) == oldChar) stack.push(nCell);
			if(get(sCell) == oldChar) stack.push(sCell);
			if(get(eCell) == oldChar) stack.push(eCell);
			if(get(wCell) == oldChar) stack.push(wCell);
		}
		
		return cellsFilled;
	}


	/**
	 * 
	 * Locates and returns the '*' boundaries that we would
	 * encounter if we did a flood-fill at <code>seed</code>. 
	 * 
	 * @param seed
	 * @return Array of 2 {@code CellSet}s. 0th element is a boundary surrounding
	 * given cell. 1st element is a region surrounded by 0th element.
	 */
	public CellSet[] findBoundariesAndFillInternal(Cell seed){
		CellSet boundaries = new CellSet();
		CellSet fill = new CellSet();
		char oldChar = get(seed);

		if(isOutOfBounds(seed)) return new CellSet[] {boundaries, fill};

		char newChar = 1; //TODO: kludge

		Stack<Cell> stack = new Stack<Cell>();

		stack.push(seed);
		
		while(!stack.isEmpty()){
			Cell cell = stack.pop();
			
			set(cell, newChar);

			Cell nCell = cell.getNorth();
			Cell sCell = cell.getSouth();
			Cell eCell = cell.getEast();
			Cell wCell = cell.getWest();
                        
			char nChar = get(nCell);
			char sChar = get(sCell);
			char eChar = get(eCell);
			char wChar = get(wCell);
			
			if(nChar == oldChar) {
				stack.push(nCell);
				fill.add(nCell);
			}
			else if(nChar == '*') boundaries.add(nCell);
			
			if(sChar == oldChar) {
				stack.push(sCell);
				fill.add(sCell);
			}
			else if(sChar == '*') boundaries.add(sCell);
			
			if(eChar == oldChar) {
				stack.push(eCell);
				fill.add(eCell);
			}
			else if(eChar == '*') boundaries.add(eCell);
			
			if(wChar == oldChar) {
				stack.push(wCell);
				fill.add(wCell);
			}
			else if(wChar == '*') boundaries.add(wCell);
		}
		return new CellSet[] {boundaries, fill};
	}
	
	public boolean cellContainsDashedLineChar(Cell cell){
		char c = get(cell);
		return StringUtils.isOneOf(c, dashedLines);
	}

	public boolean loadFrom(String filename)
			throws FileNotFoundException, IOException
			{
		return loadFrom(filename, null);
	}

	public boolean loadFrom(String filename, ProcessingOptions options)
		throws IOException
	{
				
		String encoding = (options == null) ? null : options.getCharacterEncoding();
		List<StringBuilder> lines = new ArrayList<StringBuilder>();
		InputStream is;
		if ("-".equals(filename))
		    is = System.in;
		else
		    is = new FileInputStream(filename);
		String[] linesArray = FileUtils.readFile(is, filename, encoding).split("(\r)?\n");
		for(int i = 0; i  < linesArray.length; i++)
			lines.add(new StringBuilder(linesArray[i]));
		
		return initialiseWithLines(lines, options);
	}

	public boolean initialiseWithText(String text, ProcessingOptions options) throws UnsupportedEncodingException {

		List<StringBuilder> lines = new ArrayList<StringBuilder>();
		String[] linesArray = text.split("(\r)?\n");
		for(int i = 0; i  < linesArray.length; i++)
			lines.add(new StringBuilder(linesArray[i]));

		return initialiseWithLines(lines, options);
	}

	private boolean initialiseWithLines(List<StringBuilder> lines, ProcessingOptions options) throws UnsupportedEncodingException {

		//remove blank rows at the bottom
		boolean done = false;
		int i;
		for(i = lines.size() - 1; i >= 0 && !done; i--){
			StringBuilder row = lines.get(i);
			if(!StringUtils.isBlank(row.toString())) done = true;
		}
		rows = lines.subList(0, i + 2);

		if(options != null) fixTabs(options.getTabSize());
		else fixTabs(ProcessingOptions.DEFAULT_TAB_SIZE);


		// make all lines of equal length
		// add blank outline around the buffer to prevent fill glitch
		// convert tabs to spaces (or remove them if setting is 0)
		
		int blankBorderSize = 2;
		
		int maxLength = 0;
		for (StringBuilder sb : rows) {
			maxLength = Math.max(maxLength, sb.length());
		}

		List<StringBuilder> newRows = new ArrayList<StringBuilder>();
		//TODO: make the following depend on blankBorderSize
		
		String topBottomRow = StringUtils.repeatString(" ", maxLength + blankBorderSize * 2);
		
		newRows.add(new StringBuilder(topBottomRow));
		newRows.add(new StringBuilder(topBottomRow));
		for (StringBuilder row : rows) {
			if(row.length() < maxLength) {
				String borderString = StringUtils.repeatString(" ", blankBorderSize);
				StringBuilder newRow = new StringBuilder();
				
				newRow.append(borderString);
				newRow.append(row);
				newRow.append(StringUtils.repeatString(" ", maxLength - row.length()));
				newRow.append(borderString);
				
				newRows.add(newRow);
			} else { //TODO: why is the following line like that?
				newRows.add(new StringBuilder("  ").append(row).append("  "));
			}
		}
		//TODO: make the following depend on blankBorderSize
		newRows.add(new StringBuilder(topBottomRow));
		newRows.add(new StringBuilder(topBottomRow));
		rows = newRows;
		
		replaceBullets();
		
		return true;
	}
	
	private void fixTabs(int tabSize){

		int rowIndex = 0;

		for (StringBuilder sb : rows) {
			String row = sb.toString();
			StringBuilder newRow = new StringBuilder();
			
			char[] chars = row.toCharArray();
			for(int i = 0; i < chars.length; i++){
				if(chars[i] == '\t'){
					int spacesLeft = tabSize - newRow.length() % tabSize;
					LOG.info("Found tab. Spaces left: "+spacesLeft);
					String spaces = StringUtils.repeatString(" ", spacesLeft);
					newRow.append(spaces);
				} else {
					String character = Character.toString(chars[i]);
					newRow.append(character);
				}
			}
			rows.set(rowIndex, newRow);
			rowIndex++;
		}
	}
	
	/**
	 * @return
	 */
	protected List<StringBuilder> getRows() {
		return rows;
	}
	
	public static class CellColorPair{
		public CellColorPair(Cell cell, Color color){
			this.cell = cell;
			this.color = color;
		}
		public Color color;
		public Cell cell;
	}

	public static class CellStringPair{
		public CellStringPair(Cell cell, String string){
			this.cell = cell;
			this.string = string;
		}
		public Cell cell;
		public String string;

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			if (this == o) {
				return true;
			}
			CellStringPair other = (CellStringPair) o;
			return cell.equals(other.cell) && string.equals(other.string);
		}

		@Override
		public int hashCode() {
			return cell.hashCode() * 31 + string.hashCode();
		}

		@Override
		public String toString() {
			return String.format("[x = %d, y = %d: %s]", cell.x, cell.y, string);
		}
	}

	public static class CellTagPair{
		public CellTagPair(Cell cell, String tag){
			this.cell = cell;
			this.tag = tag;
		}
		public Cell cell;
		public String tag;
	}

	public static class Cell {

		public int x, y;
		
		public Cell(Cell cell){
			this(cell.x, cell.y);
		}
		
		public Cell(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		public Cell getNorth(){ return new Cell(x, y - 1); }
		public Cell getSouth(){ return new Cell(x, y + 1); }
		public Cell getEast(){ return new Cell(x + 1, y); }
		public Cell getWest(){ return new Cell(x - 1, y); }

		public Cell getNW(){ return new Cell(x - 1, y - 1); }
		public Cell getNE(){ return new Cell(x + 1, y - 1); }
		public Cell getSW(){ return new Cell(x - 1, y + 1); }
		public Cell getSE(){ return new Cell(x + 1, y + 1); }

		public boolean equals(Object o){
			if (o == null) {
				return false;
			}
			if (o.getClass() != this.getClass()) {
				return false;
			}
			Cell cell = (Cell) o;
			if(x == cell.x && y == cell.y) return true;
			else return false;
		}
		
		public int hashCode() {
			return (x << 16) | y;
		}
		
		private boolean isNextTo(int x2, int y2){
			if(Math.abs(x2 - x) == 1 && Math.abs(y2 - y) == 1) return false;
			if(Math.abs(x2 - x) == 1 && y2 == y) return true;
			if(Math.abs(y2 - y) == 1 && x2 == x) return true;
			return false;
		}
		
		public boolean isNextTo(Cell cell){
			if(cell == null) throw new IllegalArgumentException("cell cannot be null");
			return this.isNextTo(cell.x, cell.y);
		}
		
		public String toString(){
			return "("+x+", "+y+")";
		}
		
		public void scale(int s){
			x = x * s;
			y = y * s;
		}
		
	}
}

