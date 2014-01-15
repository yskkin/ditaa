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

/**
 * @author sideris
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class StringUtils {

	/**
	 * The indexOf idiom
	 * 
	 * @param big
	 * @param fragment
	 * @return
	 */
	public static boolean contains(String big, String fragment){
		return (big.indexOf(fragment) != -1);
	}
	
	public static String repeatString(String string, int repeats){
		if(repeats == 0) return "";
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i < repeats; i++){
			buffer.append(string);
		}
		return buffer.toString();
	}
	
	/*public static String repeatString(String string, int repeats){
		if(repeats == 0) return "";
		StringBuffer buffer = new StringBuffer("");
		for(int i=0; i < repeats; i++){
			buffer.append(string);
		}
		return buffer.toString();
	}*/
	
	public static boolean isBlank(String s){
		return (s.length() == 0 || s.matches("^\\s*$"));
	}
	
	/**
	 * 
	 * Converts the first character of <code>string</code> into a capital letter
	 * 
	 * @param string
	 * @return
	 */
	public static String firstToUpper(String string){
		return string.substring(0,1).toUpperCase()+string.substring(1);
	}
	
	public static boolean isOneOf(char c, Character[] group){
		for(int i = 0; i < group.length; i++)
			if(c == group[i]) return true;
		return false;
	}

	public static boolean isOneOf(char c, char[] group){
		for(int i = 0; i < group.length; i++)
			if(c == group[i]) return true;
		return false;
	}

	public static boolean isOneOf(String str, String[] group){
		for(int i = 0; i < group.length; i++)
			if(str.equals(group[i])) return true;
		return false;
	}

	public static String getPath(String fullPath){
		if(fullPath.lastIndexOf("\\") != -1)
			return fullPath.substring(0, fullPath.lastIndexOf("\\"));
		else return "";
	}

	public static String getBaseFilename(String fullPath){
		if(fullPath.lastIndexOf(".") != -1 && fullPath.lastIndexOf("\\") != -1)
			return fullPath.substring(fullPath.lastIndexOf("\\") + 1, fullPath.lastIndexOf("."));
		else return fullPath; 
	}

	public static String getExtension(String fullPath){
		if(fullPath.lastIndexOf(".") != -1)
			return fullPath.substring(fullPath.lastIndexOf(".") + 1);
		else return "";
	}

	
	public static void main(String[] args){
		System.out.println("1 "+StringUtils.firstToUpper("testing"));
		System.out.println("2 "+StringUtils.firstToUpper(" testing"));
		System.out.println("3 "+StringUtils.firstToUpper("_testing"));
		System.out.println("4 "+StringUtils.firstToUpper("Testing"));
		System.out.println("5 "+StringUtils.firstToUpper("ttesting"));
		String path = "C:\\Files\\test.txt";
		System.out.println(path);
		System.out.println(StringUtils.getPath(path));
		System.out.println(StringUtils.getBaseFilename(path));
		System.out.println(StringUtils.getExtension(path));
		
		path = "test.txt";
		System.out.println(path);
		System.out.println(StringUtils.getPath(path));
		System.out.println(StringUtils.getBaseFilename(path));
		System.out.println(StringUtils.getExtension(path));

		path = "test";
		System.out.println(path);
		System.out.println("path: "+StringUtils.getPath(path));
		System.out.println("base: "+StringUtils.getBaseFilename(path));
		System.out.println(" ext: "+StringUtils.getExtension(path));


	}
}