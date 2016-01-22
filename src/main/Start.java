package main;
import mpi.MPI;
import solver.ParallelSolver;

/**
 * Main Klasse für den parallelen NxM Puzzle Solver. Implementiert mit MPJ.
 * 
 * SamplePuzzles: http://mypuzzle.org/sliding
 * 
 * @author michael koch
 */

public class Start {

	public static void main(String[] args) throws Exception{

		MPI.Init(args);

		int[] puzzle = {
				 
				
				/*
				//4x4 Puzzle mit 38 Steps
				10, 4, 3 , 7,
				1 , 5, 9 , 2,
				13, 6, 14, 12,
				11, 0, 8 , 15
				//*/
				
				
				//*
				//4x4 Puzzle mit 43 Steps
				2 , 12, 14, 7,
				5 , 1 , 6 , 3,
				10, 13, 4 , 9,
				15, 8 , 0 , 11
				//*/
				
		};
		

		//Angabe der Anzahl Zeilen und Reihen des zu lösenden Puzzels im Konstruktor der Klasse ParallelSolver
		ParallelSolver parallelSolver = new ParallelSolver(4,4);
		
		//Löse und gebe die Lösung aus
		parallelSolver.solve(puzzle).printSolution();

		MPI.Finalize();

	}
}