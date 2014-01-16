package yskkin.ascii2image.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.stathissideris.ascii2image.text.AbstractionGrid;
import org.stathissideris.ascii2image.text.CellSet;
import org.stathissideris.ascii2image.text.TextGrid;
import org.stathissideris.ascii2image.text.TextGrid.Cell;

public class BoundarySearcher {

	private static final Logger LOG = Loggers.getLogger(BoundarySearcher.class);

	public static final List<CellSet> findBoundariesFromGrid(TextGrid workGrid) {
		int width = workGrid.getWidth();
		int height = workGrid.getHeight();
		//split distinct shapes using AbstractionGrid 
		AbstractionGrid temp = new AbstractionGrid(workGrid, workGrid.getAllBoundaries());
		List<CellSet> boundarySetsStep1 = temp.getDistinctShapes();
		
		LOG.finer("******* Distinct shapes found using AbstractionGrid *******");
		for (CellSet set : boundarySetsStep1) {
			set.printAsGrid();
		}
		LOG.finer("******* Same set of shapes after processing them by filling *******");
		
		
		//Find all the boundaries by using the special version of the filling method
		//(fills in a different buffer than the buffer it reads from)
		List<CellSet> boundarySetsStep2 = new ArrayList<CellSet>();
		for(CellSet set : boundarySetsStep1) {			
			//the fill buffer keeps track of which cells have been
			//filled already
			TextGrid fillBuffer = new TextGrid(width * 3, height * 3);
			
			for(int yi = 0; yi < height * 3; yi++){
				for(int xi = 0; xi < width * 3; xi++){
					if(fillBuffer.isBlank(xi, yi)){
						
						TextGrid copyGrid = new AbstractionGrid(workGrid, set).getCopyOfInternalBuffer();
						
						CellSet[] op = copyGrid.findBoundariesAndFillInternal(new Cell(xi, yi));

						CellSet boundaries = op[0];
						if(boundaries.size() == 0) continue; //i'm not sure why these occur
						CellSet resultCandidate = boundaries.makeScaledOneThirdEquivalent();
						if (boundarySetsStep2.isEmpty() || !boundarySetsStep2.get(boundarySetsStep2.size() - 1).equals(resultCandidate)) {
							boundarySetsStep2.add(resultCandidate);
						}
					
						CellSet filled = op[1];
						fillBuffer.fillCellsWith(filled, '*');
						fillBuffer.fillCellsWith(boundaries, '-');
						
						LOG.finer("Fill buffer:");
						resultCandidate.printAsGrid();
						LOG.finer("-----------------------------------");
						
					}
				}
			}
		}
		return boundarySetsStep2;
	}

}
