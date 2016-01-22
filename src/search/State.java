package search;
import java.io.Serializable;
import java.util.Stack;

/**
 * Class State
 * 
 * Ein State ist eine Puzzle-Konstellation. Jeder State besitzt eine Heuristik und einen ParentState.
 * Weiter wird die Dimensionen (Anzahl Zeilen, Reihen und die Länge) des Puzzels benötigt.
 * 
 * Manhattan Distanz bestimmt die Heuristik.
 * 
 * Das Puzzel ist wird in einem Buffer (int Array) gespeichert.
 * Die Leere Stelle (das Blank) des Puzzels besitzt die Ziffer 0.
 * 
 * @author michael koch
 */

public class State implements Serializable{

	private static final long serialVersionUID = 1L;

	//Puzzle
	public int[] puzzle;
	
	//Puzzle Dimension
	private int row;
	private int col;
	private int deep;

	//Parent
	public State parent;
	
	//Manhattan Distanz
	private int heuristic;

    //Blank Bewegung für Solution
	String blankMoveDirection;

	/**
	 * Nur der Root-Node wird hiermit initialisiert
	 * @param puzzle
	 * @param row
	 * @param col
	 */
	public State(int[] puzzle, int row, int col){
		this.puzzle = new int[row*col];
		this.col = col;
		this.row = row;
		this.puzzle = puzzle;
		this.heuristic = calcTotalManhattanDistance(this);
	}


	/**
	 * Der State wird mit seinem Parent initialisiert
	 * @param parentState
	 */
	private State(State parentState){
		this.puzzle = parentState.puzzle.clone();
		this.col = parentState.col;
		this.row = parentState.row;
		this.deep = parentState.deep + 1;
	}


	/**
	 * Vergleicht den State mit der Zielkonfiguration
	 * @return true, wenn die Lösung gefunden wurde
	 */
	public boolean checkSolution(){

		boolean success = true;

		for(int i=0; i<puzzle.length-1; i++){
			if(puzzle[i] != i+1){
				success = false;
				break;
			}
		}
		return success;
	}


	/**
	 * Expandiert den aktuellen State und fügt die Childs dem Stack hinzu
	 * @param Stack
	 * @return Stack mit den Childs
	 */
	public Stack<State> expandNode(Stack<State> stack){

		int x = getX( getBlankPos() );
		int y = getY( getBlankPos() );


		//blank is left top
		if((x == 0) && (y == 0)){
			stack.add(moveRight(x, y));
			stack.add(moveDown(x, y));


			//blank is right top
		} else if((x == row-1) && (y == 0)){
			stack.add(moveLeft(x, y));
			stack.add(moveDown(x, y));


			//blank is top
		} else if(y == 0){
			stack.add(moveLeft(x, y));
			stack.add(moveRight(x, y));
			stack.add(moveDown(x, y));


			//blank is left bottom
		} else if((x == 0) && (y == col-1)){
			stack.add(moveRight(x, y));
			stack.add(moveUp(x, y));


			//blank is right bottom
		} else if((x == row-1) && (y == col-1)){
			stack.add(moveLeft(x, y));
			stack.add(moveUp(x, y));


			//blank is bottom
		} else if(y == col-1){
			stack.add(moveLeft(x, y));
			stack.add(moveRight(x, y));
			stack.add(moveUp(x, y));


			//blank is left
		} else if(x == 0){
			stack.add(moveDown(x, y));
			stack.add(moveUp(x, y));
			stack.add(moveRight(x, y));


			//blank is right
		} else if(x == row-1){
			stack.add(moveDown(x, y));
			stack.add(moveUp(x, y));
			stack.add(moveLeft(x, y));


			// blank is in the middle
		} else{
			stack.add(moveDown(x, y));
			stack.add(moveUp(x, y));
			stack.add(moveRight(x, y));
			stack.add(moveLeft(x, y));
		}
		return stack;
	}


	/**
	 * gibt die Manhattan-Distanz zurück
	 * @return liefert die Heuristik
	 */
	public int getHeuristic() {
		return heuristic;
	}


	/**
	 * setzt die Manhattan-Distanz
	 * @param setzt die Heuristik
	 */
	public void setHeuristic(int heuristic) {
		this.heuristic = heuristic;
	}


	/**
	 * Verhindert Loops beim Expandieren der Nodes
	 * Das vorherige Verschieben des Blanks, darf nicht in die entgegengesetzte Richtung verschoben werden
	 * @return true, falls ein loop gefunden wurde
	 */
	public boolean hasLoop(){
		boolean loop = false;

		if(this.deep > 2){
			if(!Util.isEqual(this.parent.parent.puzzle,this.puzzle)){
				loop = true;
			}
		}else{
			loop = true;
		}
		return loop;
	}


	/**
	 * Verschiebt das Blank nach rechts
	 */
	private State moveRight(int x, int y){
		return swapAndCreateChildState(x, y, x+1, y, "right");
	}

	/**
	 * Verschiebt das Blank nach links
	 */
	private State moveLeft(int x, int y){
		return swapAndCreateChildState(x, y, x-1, y, "left");
	}

	/**
	 * Verschiebt das Blank nach unten
	 */
	private State moveDown(int x, int y){
		return swapAndCreateChildState(x, y, x, y+1, "down");
	}

	/**
	 * Verschiebt das Blank nach oben
	 */
	private State moveUp(int x, int y){
		return swapAndCreateChildState(x, y, x, y-1, "up");
	}

	/**
	 * Konvertiert die BufferPosition in die X-Position
	 * @param bufferPos
	 * @return X-Position im Puzzle
	 */
	private int getX(int bufferPos){
		return bufferPos % row;
	}

	/**
	 * Konvertiert die BufferPosition in die Y-Position
	 * @param bufferPos
	 * @return X-Position im Puzzle
	 */
	private int getY(int bufferPos){
		return bufferPos / col;
	}

	/**
	 * Konvertiert die X und Y Koordinate in die BufferPosition
	 * @param x
	 * @param y
	 * @return BufferPosition
	 */
	private int getPos(int x, int y){
		return y*row + x;
	}

	/**
	 * Position des Blanks
	 * @return gibt die Position des Blanks zurück
	 */
	private int getBlankPos(){
		int pos = 0;
		for(int i=0; i<puzzle.length; i++){
			if(puzzle[i] == 0){
				pos = i;
				break;
			}
		}
		return pos;
	}

	/**
	 * Vertauscht je nach Bewegung des Blanks die beiden Positionen im Buffer.
	 * Generiert daraus einen ChildState.
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @param blankMoveDirection
	 * @return wendet die Verschiebung des Blanks an und erzeugt einen ChildState daraus
	 */
	private State swapAndCreateChildState(int fromX, int fromY, int toX, int toY, String blankMoveDirection){

		State child = new State(this);

		child.parent = this;
		child.blankMoveDirection = blankMoveDirection;

		int toN = child.puzzle[getPos(toX, toY)];
		int fromN = child.puzzle[getPos(fromX, fromY)];

		child.puzzle[getPos(toX, toY)] = fromN;
		child.puzzle[getPos(fromX, fromY)] = toN;

		child.setHeuristic( calcTotalManhattanDistance(child) );

		return child;
	}

	/**
	 * Bestimmt die Position einer Zahl mi Buffer
	 * @param digit
	 * @param buffer
	 * @return gibt die BufferPosition einer Puzzle-Zahl zurück
	 */
	private int getNodePos(int digit, int[] buffer){
		int pos = -1;
		for(int i=0; i<buffer.length; i++){
			if(digit == buffer[i]){
				pos = i;
				break;
			}
		}
		return pos;
	}

	/**
	 * Errechnet die Manhattan Distanz einer Bestimmten Zahl n des Puzzles
	 * @param n
	 * @param buffer
	 * @return gibt die Manhattan Distance einer Zahl n zurück
	 */
	private int calcManhanttanDistance(int n, int[] buffer){

		int heuristic = 0;

		if(n != 0){
			int bufferPos = getNodePos(n, buffer);

			int xIst = getX(bufferPos);
			int yIst = getY(bufferPos);

			int xSoll = getX(n-1);
			int ySoll = getY(n-1);

			heuristic = Math.abs(xIst - xSoll) + Math.abs(yIst - ySoll);
		}
		return heuristic;
	}

	/**
	 * Berechnet die totale Manhattan Distanz eines Puzzels
	 * @param state
	 * @return gibt die Manhattan Distanz des Puzzles zurück
	 */
	private int calcTotalManhattanDistance(State state){

		int childHeuristic = 0;

		for(int i=0; i<state.puzzle.length; i++){
			childHeuristic = childHeuristic + calcManhanttanDistance(state.puzzle[i], state.puzzle);
		}

		return childHeuristic + state.deep;
	}
}
