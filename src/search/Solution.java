package search;
import java.util.Stack;

/**
 * Klass Solution beinhaltet die L�sung
 * 
 * @author michael
 */

public class Solution {

	boolean realSolution;
	State goalState;

	/**
	 * Der Zielstate wird hiermit �bergeben
	 * @param state
	 */
	public Solution(State state){
		this.realSolution = true;
		this.goalState = state;
	}

	/**
	 * Ein Prozessor terminiert mit einer Dummy-L�sung
	 */
	public Solution(){
		this.realSolution = false;
	}

	/**
	 * Druckt die L�sung
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
