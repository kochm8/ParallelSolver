package solver;

import java.util.Stack;
import messagePassing.Worker;
import messagePassing.SolutionCOM;
import messagePassing.TerminationCOM;
import messagePassing.TokenColor;
import mpi.MPI;
import search.Solution;
import search.State;

/**
 * ParallelSolver lösst mit IDA* und der Manhattan-Distanz ein NxM Puzzle
 * 
 * @author michael koch
 */

public class ParallelSolver {

	private Stack<State> stack = new Stack<State>();
	private int deep;
	private int rank;
	private int row;
	private int col;
	
	private Solution solution;
	private long startTime;

	/**
	 * ParallelSolver wird mit der Puzzle-Dimension (Row, Col) initialisiert
	 * 
	 * @param row Anzahl Zeilen des Puzzles
	 * @param col Anzahl Reihen des Puzzles
	 */
	public ParallelSolver(int row, int col) {
		this.startTime = System.currentTimeMillis();
		this.rank = MPI.COMM_WORLD.Rank();
		this.row = row;
		this.col = col;
	}


	/**
	 * Löst ein NxM Puzzle
	 * 
	 * @param Puzzle
	 * @return Gibt die Lösung zurück
	 */
	public Solution solve(int[] puzzle){

		//Prüft die Puzzle-Dimension: length, row, col
		if(!checkPuzzleLength(puzzle.length)){
			System.out.println("puzzleLength/row/col does not match");
			MPI.Finalize();
			System.exit(0);
		}


		//Prüft, ob das Puzzle lösbar ist.
		if (!isSolvable(puzzle)){
			System.out.println("puzzle is not solvable");
			MPI.Finalize();
			System.exit(0);
		}


		//Rootstate wird bestimmt
		State root = new State(puzzle, row, col);
		deep = root.getHeuristic();


		//Beginne die Suche
		while(solution == null){

			if(rank == 0){
				stack.push(root);
				Worker.startToken(TokenColor.BLACK);
			} 

			solution = idaStar(deep);

			//Erhöhung der Suchtiefe bei einer Manhattan Distanz
			deep+=2;
		}


		//Barrier für die Reihenfolge der PrintLine-Ausgaben
		MPI.COMM_WORLD.Barrier(); //Nur wegen println
		if(rank==0){
			System.out.println("elapsed time: " + String.valueOf(System.currentTimeMillis()-startTime)+"ms");
		}
		MPI.COMM_WORLD.Barrier(); //Nur wegen println

		return solution;
	}



	/**
	 * Iterative deepening A*
	 * 
	 * @param deeplimit
	 * @return gibt die Lösung zurück
	 */
	public Solution idaStar(int deeplimit){

		Worker worker = new Worker(rank);

		while(true){

			//Prozessor ist nicht IDLE
			if(!stack.isEmpty()){

				State state = stack.pop();

				//prüfe, ob der State die Lösung beinhaltet
				if(state.checkSolution()){
					Solution solution = new Solution(state);
					SolutionCOM.foundSolution(rank);
					return solution;
				}

				//Expandiere nur State, welche kleiner als die Suchtiefe sind.
				if(deeplimit >= state.getHeuristic()){
					//verhindere Loops des Blank
					if(state.hasLoop()){
						stack = state.expandNode(stack);
					}
				}
			}

			//verteile Arbeit an einen IDLE-Prozessor
			shareWork(worker);


			//prüfe, ob ein anderer Prozessor die Lösung gefunden hat
			if(SolutionCOM.checkSolution(rank)){
				return new Solution();
			}

			//Prozessor ist IDLE
			if(stack.isEmpty()){
				isIdle(worker);
			}


			//Root-Prozessor prüft, ob die aktuelle Suchtiefe beendet ist
			if(rank == 0){
				//Welche Farbe hat das Token?
				if(worker.recvWhiteToken(stack.size())) {
					TerminationCOM.sendTermination(rank);
					System.out.println("deep " + deep + " finished");
					return null;
				}

			//alle Nicht-Root-Prozessoren geben das Token im TokenRing weiter
			}else{
				worker.passToken(stack.size());
			}

			//Empfange TERMINATION -> aktuelle Tiefe ist erledigt
			if(TerminationCOM.recvTermination()){
				return null;
			}

		}
	}
	
	
	/**
	 * Der Prozessor ist IDLE und empfängt Arbeit
	 * @param worker
	 */
	private void isIdle(Worker worker){
		State[] sharedStates = worker.recvWork();
		if(sharedStates != null){

			for(int i=0; i<sharedStates.length; i++){
				stack.push(sharedStates[i]);
			}
		}else{
			worker.sendWorkRequest();
		}
	}
	
	
	/**
	 * Arbeit wird Verteilt
	 * @param worker
	 */
	private void shareWork(Worker worker){
		
		if(stack.size() > 10){
			int receiver = worker.recvWorkRequest();
			if(receiver != -1){
				State[] sharedStack = split(stack);
				worker.sendWork(sharedStack, receiver);
			}
		}
	}
	
	
	/**
	 * Splittet den Stack
	 * 
	 * @param stackToSplit
	 * @return Array mit den SharedStates
	 */
	private State[] split(Stack<State> stackToSplit){

		int index = 0;
		State[] sharedStates = new State[stackToSplit.size()/2];
		Stack<State> tmpStack = new Stack<State>();

		while(stackToSplit.size() > 0){
			tmpStack.push(stackToSplit.pop());
			if(stackToSplit.size() > 0){
				sharedStates[index] = stackToSplit.pop();
				index++;
			}
		}

		stack = tmpStack;
		return sharedStates;
	}

	
	/**
	 * Prüft, ob die Puzzlelänge zu der angegebenen Anzahl Zeilen (row) und Spalten (col) passt
	 * 
	 * @param puzzleLength
	 * @return true, wenn die Länge korrekt ist
	 */
	private boolean checkPuzzleLength(int puzzleLength){
		boolean check = true;
		if(row*col != puzzleLength){
			check = false;
		}
		return check;
	}
	

	/**
	 * Prüft, ob das Puzzle lösbar ist
	 * 
	 * @param puzzle
	 * @return true, wenn das Puzzle lösbar ist
	 */
	private boolean isSolvable(int[] puzzle){

		int parity = 0;
		int gridWidth = col;
		int row = 0; 
		int blankRow = 0; 

		for (int i = 0; i < puzzle.length; i++){

			if (i % gridWidth == 0) { 
				row++;
			}

			if (puzzle[i] == 0) { 
				blankRow = row; 
				continue;
			}

			for (int j = i + 1; j < puzzle.length; j++){
				if (puzzle[i] > puzzle[j] && puzzle[j] != 0){
					parity++;
				}
			}
		}

		if (gridWidth % 2 == 0) { 

			if (blankRow % 2 == 0) { 
				return parity % 2 == 0;
			} else { 
				return parity % 2 != 0;
			}

		} else { 
			return parity % 2 == 0;
		}
	}

}
