package hsa.awp.usergui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomColor {

	private static char[] val = new char[] { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static Random random = new Random();


	public static String buildCssAttributeWRandomColor(String cssAtt) {
		return cssAtt + ": #" + getRandomHexColor();
	}
	
	public static String getRandomHexColor(){
		String s = "";
		while (s.length() < 6) {
			s += val[random.nextInt(val.length)];
		}
		return s;
	}
	
	public static List<String> getRandomHexColors(int numberOfColors){
		//good background for black text color
		List<String> goodReadableColors = new ArrayList<String>(Arrays.asList("79e159",
				"f0ffcf", "d9bbd4", "5166d3", "edf4ac", "6fb7f0", "c799a4",
				"c3bfdb", "f3cd51", "d3df3d"));
		List<String> colors = new ArrayList<String>();
		for (int i = 0; i < numberOfColors; i++) {
			if (i < goodReadableColors.size()) {
				int index = random.nextInt(goodReadableColors.size());
				String color = goodReadableColors.get(index);
				colors.add(color);
				goodReadableColors.remove(index);
			}
			else {
				String color = getRandomHexColor();
				while(colors.contains(color)){
					color = getRandomHexColor();
				}
				colors.add(color);
			}
		}
		return colors;
	}
}
