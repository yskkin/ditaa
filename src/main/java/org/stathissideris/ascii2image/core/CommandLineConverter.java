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
package org.stathissideris.ascii2image.core;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.stathissideris.ascii2image.graphics.BitmapRenderer;
import org.stathissideris.ascii2image.graphics.Diagram;
import org.stathissideris.ascii2image.text.TextGrid;

import yskkin.ascii2image.util.DitaaCommandLineParser;

/**
 * 
 * @author Efstathios Sideris
 */
public class CommandLineConverter {
	
	public static void main(String[] args){
		
		long startTime = System.currentTimeMillis();
		
		CommandLine cmdLine = null;
		
		///// parse command line options
		// parse the command line arguments
		DitaaCommandLineParser parser = new DitaaCommandLineParser();
		cmdLine = parser.parse(args);

		if (parser.shouldExitImmediately()) {
			System.exit(parser.getExitStatus());
		}
		
		ConversionOptions options = parser.getConversionOptions();
		
		args = cmdLine.getArgs();
		
		if(cmdLine.hasOption("html")){
			String filename = args[0];
			
			boolean overwrite = false;
			if(options.processingOptions.overwriteFiles()) overwrite = true;
			
			String toFilename;
			if(args.length == 1){
				toFilename = FileUtils.makeTargetPathname(filename, "html", "_processed", true);
			} else {
				toFilename = args[1];
			}
			File target = new File(toFilename);
			if(!overwrite && target.exists()) {
				System.out.println("Error: File "+toFilename+" exists. If you would like to overwrite it, please use the --overwrite option.");
				System.exit(0);
			}
			
			new HTMLConverter().convertHTMLFile(filename, toFilename, "ditaa_diagram", "images", options);
			System.exit(0);
			
		} else { //simple mode
			
			TextGrid grid = new TextGrid();
			if(options.processingOptions.getCustomShapes() != null){
				grid.addToMarkupTags(options.processingOptions.getCustomShapes().keySet());
			}

			// "-" means stdin / stdout
			String fromFilename = args[0];
			boolean stdIn = "-".equals(fromFilename);

			String toFilename;
			boolean stdOut;

			boolean overwrite = false;
			if(options.processingOptions.overwriteFiles()) overwrite = true;
			
			if(args.length == 1){
				if (stdIn) { // if using stdin and no output specified, use stdout
					stdOut = true;
					toFilename = "-";
				} else {
					toFilename = FileUtils.makeTargetPathname(fromFilename, "png", overwrite);
					stdOut = false;
				}
			} else {
				toFilename = args[1];
				stdOut = "-".equals(toFilename);
			}

			if (!stdOut) {
				System.out.println("Reading "+ (stdIn ? "standard input" : "file: " + fromFilename));
			}

			try {
				if(!grid.loadFrom(fromFilename, options.processingOptions)){
					System.err.println("Cannot open file "+fromFilename+" for reading");
				}
			} catch (UnsupportedEncodingException e1){
				System.err.println("Error: "+e1.getMessage());
				System.exit(1);
			} catch (FileNotFoundException e1) {
				System.err.println("Error: File "+fromFilename+" does not exist");
				System.exit(1);
			} catch (IOException e1) {
				System.err.println("Error: Cannot open file "+fromFilename+" for reading");
				System.exit(1);
			}
			
			if(options.processingOptions.printDebugOutput()){
				if (!stdOut) System.out.println("Using grid:");
				grid.printDebug();
			}
			
			Diagram diagram = new Diagram(grid, options);
			if (!stdOut) System.out.println("Rendering to file: "+toFilename);
			
			
			RenderedImage image = new BitmapRenderer().renderToImage(diagram, options.renderingOptions);
			
			try {
				OutputStream os = stdOut ? System.out : new FileOutputStream(toFilename);
				ImageIO.write(image, "png", os);
			} catch (IOException e) {
				//e.printStackTrace();
				System.err.println("Error: Cannot write to file "+toFilename);
				System.exit(1);
			}
			
			//BitmapRenderer.renderToPNG(diagram, toFilename, options.renderingOptions);
			
			long endTime = System.currentTimeMillis();
			long totalTime  = (endTime - startTime) / 1000;
			if (!stdOut) System.out.println("Done in "+totalTime+"sec");
		}
	}
}
