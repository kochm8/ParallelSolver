package search;
import java.util.Stack;

/**
 * Klass Solution beinhaltet die Lösung
 * 
 * @author michael
 */

public class Solution {

	boolean realSolution;
	State goalState;

	/**
	 * Der Zielstate wird hiermit übergeben
	 * @param state
	 */
	public Solution(State state){
		this.realSolution = true;
		this.goalState = state;
	}

	/**
	 * Ein Prozessor terminiert mit einer Dummy-Lösung
	 */
	public Solution(){
		this.realSolution = false;
	}

	/**
	 * Druckt die Lösung
	 */
	public void printSolution(){

		if(realSolution){
			Stack<String> stack = new Stack<String>();
			int step = 0;
			
			while(goalState.parent != null){
				stack.push(goalState.blankMoveDirection);
				goalState = goalState.parent;
			}
			
			System.out.println("-------------Solution-----------------");
			while(!stack.isEmpty()){
				step++;
				System.out.println(step + ". " + stack.pop());
			}
			System.out.println("--------------------------------------");
		}
	}
}
