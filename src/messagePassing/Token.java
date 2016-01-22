package messagePassing;
import java.io.Serializable;

/**
 * Token f�r den TokenRing
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
	 * F�rbt das Token Schwarz
	 */
	public void setToBlack(){
		token = TokenColor.BLACK;
	}

	/**
	 * F�rbt das Token Weiss
	 */
	public void setToWithe(){
		token = TokenColor.WHITE;
	}
	
	/**
	 * F�rbt das Token gem�ss Farbe
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
