package messagePassing;
import java.io.Serializable;

/**
 * Token für den TokenRing
 * 
 * @author michael koch
 *
 */

public class Token implements Serializable{

	private static final long serialVersionUID = 1151621074833486741L;
	private int token;

	/**
	 * Token wird mit schwarz initialisiert
	 */
	public Token(){
		setToBlack();
	}

	/**
	 * Färbt das Token Schwarz
	 */
	public void setToBlack(){
		token = TokenColor.BLACK;
	}

	/**
	 * Färbt das Token Weiss
	 */
	public void setToWithe(){
		token = TokenColor.WHITE;
	}
	
	/**
	 * Färbt das Token gemäss Farbe
	 * @param color
	 */
	public void setToColor(int color){
		token = color;
	}

	/**
	 * Weisses Token?
	 * @return
	 */
	public boolean isWhite(){
		if(token == TokenColor.WHITE){
			return true;
		}else{
			return false;
		}
	}
}
