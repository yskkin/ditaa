package yskkin.ascii2image.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PresetTag {

	private Set<String> presetTag = new HashSet<String>();

	public PresetTag() {
		presetTag.add("d");
		presetTag.add("s");
		presetTag.add("io");
		presetTag.add("c");
		presetTag.add("mo");
		presetTag.add("tr");
		presetTag.add("o");
	}

	public void addAllTag(Collection<? extends String> tag) {
		presetTag.addAll(presetTag);
	}

	public Pattern getTagPatern() {
		StringBuilder sb = new StringBuilder("\\{(");
		for (String tag : presetTag) {
			sb.append(tag).append("|");
		}
		sb.deleteCharAt(sb.lastIndexOf("|")).append(")\\}");
		return Pattern.compile(sb.toString());
	}

}
