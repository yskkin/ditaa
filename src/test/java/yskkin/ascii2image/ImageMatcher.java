package yskkin.ascii2image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ImageMatcher extends TypeSafeMatcher<File> {

	File actualFile;
	File expectedFile;
	BufferedImage expected;
	private int x;
	private int y;
	private boolean sameSize = true;
	private boolean samePixel = true;

	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub
		if (!sameSize) {
			description.appendText("Images are not same size");
		} else if (!samePixel) {
			description.appendText("Images for ")
				.appendValue(expectedFile.getName())
				.appendText(" are not pixel identical, first different pixel at ")
				.appendValueList("(", ",", ")", x, y);
		}
		if (!sameSize || !samePixel) {
			try {
				Runtime.getRuntime()
					.exec(String.format("compare %s %s %s",
							actualFile.getAbsoluteFile(),
							expectedFile.getAbsolutePath(),
							getDiiffFileName(expectedFile)))
							.waitFor();
			} catch (IOException ignored) {
			} catch (InterruptedException ignored) {
			}
		}
	}

	private String getDiiffFileName(File expectedFile) {
		StringBuffer baseName = new StringBuffer(expectedFile.getName());
		baseName.insert(baseName.lastIndexOf("."), "_diff");
		return baseName.toString();
	}

	@Override
	protected boolean matchesSafely(File actualFile) {
		this.actualFile = actualFile;
		BufferedImage actual;
		try {
			actual = ImageIO.read(actualFile);
		} catch (Exception e) {
			return false;
		}
		if (actual.getWidth() != expected.getWidth()
				&& actual.getHeight() != expected.getHeight()) {
			sameSize = false;
			return sameSize;
		}

		OUTER:
		for (y = 0; y < expected.getHeight(); y++) {
			for (x = 0; x < expected.getWidth(); x++) {
				int expectedPixel = expected.getRGB(x, y);
				int actualPixel = actual.getRGB(x, y);
				if(actualPixel != expectedPixel) {
					samePixel = false;
					break OUTER;
				}
			}
		}
		return samePixel;
	}

	public static ImageMatcher hasSameImageAs(File expectedFile) throws Exception {
		ImageMatcher result = new ImageMatcher();
		result.expected = ImageIO.read(expectedFile);
		result.expectedFile = expectedFile;
		return result;
	}

}
