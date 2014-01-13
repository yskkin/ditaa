package yskkin.ascii2image.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.stathissideris.ascii2image.core.ConversionOptions;

public class DitaaCommandLineParser {

	private static final String NOTICE = "ditaa version 0.9, Copyright (C) 2004--2009  Efstathios (Stathis) Sideris";
	private static final CommandLineParser parser = new PosixParser();
	@SuppressWarnings({ "static-access", "serial" })
	public static final Options DITAA_CLI_SPEC = new Options() {
		{
			addOption(
					OptionBuilder
					.withLongOpt("help")
					.withDescription("Prints usage help.")
					.create());
			addOption("v", "verbose", false, "Makes ditaa more verbose.");
			addOption(
					"o",
					"overwrite",
					false,
					"If the filename of the destination image already exists, an alternative name is chosen. If the overwrite option is selected, the image file is instead overwriten.");
			addOption("S", "no-shadows", false,
					"Turns off the drop-shadow effect.");
			addOption("A", "no-antialias", false, "Turns anti-aliasing off.");
			addOption(
					"W",
					"fixed-slope",
					false,
					"Makes sides of parallelograms and trapezoids fixed slope instead of fixed width.");
			addOption("d", "debug", false,
					"Renders the debug grid over the resulting image.");
			addOption("r", "round-corners", false,
					"Causes all corners to be rendered as round corners.");
			addOption("E", "no-separation", false,
					"Prevents the separation of common edges of shapes.");
			addOption(
					"h",
					"html",
					false,
					"In this case the input is an HTML file. The contents of the <pre class=\"textdiagram\"> tags are rendered as diagrams and saved in the images directory and a new HTML file is produced with the appropriate <img> tags.");
			addOption(
					"T",
					"transparent",
					false,
					"Causes the diagram to be rendered on a transparent background. Overrides --background.");

			addOption(
					OptionBuilder
					.withLongOpt("encoding")
					.withDescription("The encoding of the input file.")
					.hasArg()
					.withArgName("ENCODING")
					.create('e'));

			addOption(
					OptionBuilder
					.withLongOpt("scale")
					.withDescription(
							"A natural number that determines the size of the rendered image. The units are fractions of the default size (2.5 renders 1.5 times bigger than the default).")
					.hasArg()
					.withArgName("SCALE")
					.create('s'));

			addOption(
					OptionBuilder
					.withLongOpt("tabs")
					.withDescription(
							"Tabs are normally interpreted as 8 spaces but it is possible to change that using this option. It is not advisable to use tabs in your diagrams.")
					.hasArg()
					.withArgName("TABS")
					.create('t'));

			addOption(
					OptionBuilder
					.withLongOpt("background")
					.withDescription(
							"The background colour of the image. The format should be a six-digit hexadecimal number (as in HTML, FF0000 for red). Pass an eight-digit hex to define transparency. This is overridden by --transparent.")
					.hasArg()
					.withArgName("BACKGROUND")
					.create('b'));

			addOption(
					OptionBuilder
					.withLongOpt("logfile")
					.withDescription("Use given FILE for logging.")
					.hasArg()
					.withArgName("FILE")
					.create());

			// TODO: uncomment this for next version:
			// cmdLnOptions.addOption(
			// OptionBuilder.withLongOpt("config")
			// .withDescription( "The shape configuration file." )
			// .hasArg()
			// .withArgName("CONFIG_FILE")
			// .create('c') );
		}
	};

	private boolean exitImmediately = false;
	private int exitStatus = 0;
	private ConversionOptions conversionOptions;

	public CommandLine parse(String[] arguments) {
		CommandLine result;
		try {
			result = parser.parse(DITAA_CLI_SPEC, arguments, false);
		} catch (ParseException e) {
			exit(2);
			System.err.println(e.getMessage());
			printDitaaHelp();
			return null;
		}

		String logFileName = result.getOptionValue("logfile");
		Loggers.addFileOutputToAllLoggers(logFileName);
		if (result.hasOption("help") || arguments.length == 0) {
			exit(0);
			printDitaaHelp();
			return null;
		}

		try {
			conversionOptions = new ConversionOptions(result);
		} catch (UnsupportedEncodingException e2) {
			System.err.println("Error: " + e2.getMessage());
			exit(2);
			return null;
		} catch (IllegalArgumentException e2) {
			System.err.println("Error: " + e2.getMessage());
			printDitaaHelp();
			exit(2);
			return null;
		}

		String[] args = result.getArgs();
		if (args.length == 0) {
			System.err.println("Error: Please provide the input file filename");
			printDitaaHelp();
			exit(2);
			return null;
		}
		printRunInfo(result);
		return result;
	}

	private void printRunInfo(CommandLine cmdLine) {
		String[] args = cmdLine.getArgs();
		if (cmdLine.hasOption("html")
				|| (args.length == 1 && !args[0].equals("-")
				|| (args.length < 1 && !args[1].equals("-")))) {
			System.out.println("\n" + NOTICE + "\n");

			System.out.println("Running with options:");
			Option[] opts = cmdLine.getOptions();
			for (Option option : opts) {
				if (option.hasArgs()) {
					for (String value : option.getValues()) {
						System.out.println(option.getLongOpt() + " = " + value);
					}
				} else if (option.hasArg()) {
					System.out.println(option.getLongOpt() + " = "
							+ option.getValue());
				} else {
					System.out.println(option.getLongOpt());
				}
			}
		}
	}

	public boolean shouldExitImmediately() {
		return exitImmediately;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public ConversionOptions getConversionOptions() {
		return conversionOptions;
	}

	private void exit(int exitStatus) {
		exitImmediately = true;
		this.exitStatus = exitStatus;
	}

	private static void printDitaaHelp() {
		new HelpFormatter().printHelp("java -jar ditaa.jar <inpfile> [outfile]", DITAA_CLI_SPEC, true);
	}
}
