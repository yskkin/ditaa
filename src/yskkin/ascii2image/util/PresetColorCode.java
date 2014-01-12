package yskkin.ascii2image.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class PresetColorCode {

	private Map<String, Color> presetColor = new HashMap<String, Color>();

	public PresetColorCode() {
		presetColor.put("GRE", new Color(0x99, 0xDD, 0x99));
		presetColor.put("BLU", new Color(0x55, 0x55, 0xBB));
		presetColor.put("PNK", new Color(0xFF, 0xAA, 0xAA));
		presetColor.put("RED", new Color(0xEE, 0x33, 0x22));
		presetColor.put("YEL", new Color(0xFF, 0xFF, 0x33));
		presetColor.put("BLK", Color.BLACK);
	}

	public Color getColor(String colorCode) {
		Color result = presetColor.get(colorCode);
		if (result == null) {
			throw new IllegalArgumentException("Color code " + colorCode + " is unknown.");
		}
		return result;
	}
}
