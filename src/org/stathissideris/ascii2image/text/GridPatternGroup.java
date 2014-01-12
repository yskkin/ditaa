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

/**
 * 
 * @author Efstathios Sideris
 */
@SuppressWarnings("serial")
public class GridPatternGroup extends ArrayList<GridPattern> {

	public boolean isAnyMatchedBy(TextGrid grid){
		for (GridPattern pattern : this) {
			if(pattern.isMatchedBy(grid)) return true;
		}
		return false;
	}

	public static final GridPatternGroup normalCorner1Criteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".[.",
					"~+(",
					".^."));
		}
	};
	public static final GridPatternGroup normalCorner2Criteria = new GridPatternGroup(){
		{
			add(new GridPattern(
					".[.",
					"(+~",
					".^."));
		}
	};
	public static final GridPatternGroup normalCorner3Criteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".^.",
					"(+~",
					".[."));
		}
	};
	public static final GridPatternGroup normalCorner4Criteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".^.",
					"~+(",
					".[."));
		}
	};

	public static final GridPatternGroup roundCorner1Criteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".[.",
					"~/4",
					".2."));
		}
	};
	public static final GridPatternGroup roundCorner2Criteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".[.",
					"4\\~",
					".2."));
		}
	};
	public static final GridPatternGroup roundCorner3Criteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".6.",
					"4/~",
					".[."));
		}
	};
	public static final GridPatternGroup roundCorner4Criteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".6.",
					"~\\8",
					".[."));
		}
	};

	//TODO: define criteria for on-line type?

	public static final GridPatternGroup normalCornerCriteria = new GridPatternGroup() {
		{
			addAll(normalCorner1Criteria);
			addAll(normalCorner2Criteria);
			addAll(normalCorner3Criteria);
			addAll(normalCorner4Criteria);
		}
	};
	public static final GridPatternGroup roundCornerCriteria = new GridPatternGroup() {
		{
			addAll(roundCorner1Criteria);
			addAll(roundCorner2Criteria);
			addAll(roundCorner3Criteria);
			addAll(roundCorner4Criteria);
		}
	};

	public static final GridPatternGroup cornerCriteria = new GridPatternGroup() {
		{
			addAll(normalCornerCriteria);
			addAll(roundCornerCriteria);
		}
	};

	public static final GridPatternGroup corner1Criteria = new GridPatternGroup() {
		{
			addAll(normalCorner1Criteria);
			addAll(roundCorner1Criteria);
		}
	};
	public static final GridPatternGroup corner2Criteria = new GridPatternGroup() {
		{
			addAll(normalCorner2Criteria);
			addAll(roundCorner2Criteria);
		}
	};
	public static final GridPatternGroup corner3Criteria = new GridPatternGroup() {
		{
			addAll(normalCorner3Criteria);
			addAll(roundCorner3Criteria);
		}
	};
	public static final GridPatternGroup corner4Criteria = new GridPatternGroup() {
		{
			addAll(normalCorner4Criteria);
			addAll(roundCorner4Criteria);
		}
	};

	public static final GridPatternGroup TCriteria = new GridPatternGroup(){
		{
			add(new GridPattern(
					".%6.",
					"4+8",
					".2."));
		}
	};
	public static final GridPatternGroup inverseTCriteria = new GridPatternGroup(){
		{
			add(new GridPattern(
					".6.",
					"4+8",
					".%2."
			));
		}
	};
	public static final GridPatternGroup KCriteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".6.",
					"%4+8",
					".2."));
		}
	};
	public static final GridPatternGroup inverseKCriteria = new GridPatternGroup(){
		{
			add(new GridPattern(
					".6.",
					"4+%8",
					".2."));
		}
	};

	public static final GridPatternGroup crossCriteria = new GridPatternGroup(){
		{
			add(new GridPattern(
					".6.",
					"4+8",
					".2."));
		}
	};

	public static final GridPatternGroup intersectionCriteria = new GridPatternGroup() {
		{
			addAll(crossCriteria);
			addAll(KCriteria);
			addAll(TCriteria);
			addAll(inverseKCriteria);
			addAll(inverseTCriteria);
		}
	};

	public static final GridPatternGroup stubCriteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					"!^!",
					"!+!",
					".!."));
			add(new GridPattern(
					"!^!",
					"!+!",
					".-."));
			add(new GridPattern(
					"!!.",
					"(+!",
					"!!."));
			add(new GridPattern(
					"!!.",
					"(+|",
					"!!."));
			add(new GridPattern(
					".!.",
					"!+!",
					"!^!"));
			add(new GridPattern(
					".-.",
					"!+!",
					"!^!"));
			add(new GridPattern(
					".!!",
					"!+(",
					".!!"));
			add(new GridPattern(
					".!!",
					"|+(",
					".!!"));
		}
	};

	public static final GridPatternGroup linesEndCriteria = new GridPatternGroup() {
		{
			addAll(stubCriteria);
			// horizontal
			add(new GridPattern(
					"...",
					"(-!",
					"..."));
			add(new GridPattern(
					"...",
					"(-|",
					"..."));
			add(new GridPattern(
					"...",
					"!-(",
					"..."));
			add(new GridPattern(
					"...",
					"|-(",
					"..."));
			// vertical
			add(new GridPattern(
					".^.",
					".|.",
					".!."));
			add(new GridPattern(
					".^.",
					".|.",
					".-."));
			add(new GridPattern(
					".!.",
					".|.",
					".^."));
			add(new GridPattern(
					".-.",
					".|.",
					".^."));
		}
	};

	public static final GridPatternGroup horizontalCrossOnLineCriteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					"...",
					"(+(",
					"..."));
		}
	};
	public static final GridPatternGroup verticalCrossOnLineCriteria = new GridPatternGroup() {
		{
			add(new GridPattern(
					".^.",
					".+.",
					".^."));
		}
	};

	public static final GridPatternGroup crossOnLineCriteria = new GridPatternGroup() {
		{
			addAll(horizontalCrossOnLineCriteria);
			addAll(verticalCrossOnLineCriteria);
		}
	};

	public static final GridPatternGroup starOnLineCriteria = new GridPatternGroup() {
		{
			// horizontal
			add(new GridPattern(
					"...",
					"(*(",
					"..."));
			add(new GridPattern(
					"...",
					"!*(",
					"..."));
			add(new GridPattern(
					"...",
					"(*!",
					"..."));
			// vertical
			add(new GridPattern(
					".^.",
					".*.",
					".^."));
			add(new GridPattern(
					".!.",
					".*.",
					".^."));
			add(new GridPattern(
					".^.",
					".*.",
					".!."));
		}
	};
}
