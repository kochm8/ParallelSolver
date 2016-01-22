package search;

/**
 * class Util
 * 
 * @author michael
 */

public class Util {

	/**
	 * Inhaltlicher vergleich zweier Integer Arrays
	 * 
	 * @param Array a
	 * @param Array b
	 * @return true, wenn die 2 Array den gleich Inhalt haben
	 */
	public static boolean isEqual(int[] a, int[] b){

		boolean isEqual = true;

		for(int i=0; i<a.length; i++){
			if(a[i] != b[i]){
				isEqual = false;
			}
		}
		return isEqual;
	}
	
}
