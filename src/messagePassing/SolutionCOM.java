package messagePassing;

import mpi.MPI;
import mpi.Status;

/**
 * Klasse SolutionCOM stellt die Kommunikatinoswerkzeuge zur Verfügung für das Solution-Handling.
 *  
 * @author michael koch
 */
public class SolutionCOM{
	
	/**
	 * Der Prozessor hat eine Lösung gefunden.
	 */
	public static void foundSolution(int me){
		int size = MPI.COMM_WORLD.Size();
		int[] buffer = new int[1];

		for(int rank=0; rank<size; rank++){
			if(rank != me){
				MPI.COMM_WORLD.Isend(buffer, 0, buffer.length, MPI.INT, rank, Tag.SOLUTION);
			}
		}
	}

	/**
	 * Prüfung, ob ein anderer Prozessor die Lösung gefunden hat.
	 * @return true, wenn ein anderer Prozessor die Lösung gefunden hat.
	 */
	public static boolean checkSolution(int me){
		int[] buffer = new int[1];

		Status status = MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE, Tag.SOLUTION);
		if (status != null){
			MPI.COMM_WORLD.Recv(buffer, 0, buffer.length, MPI.INT, status.source, Tag.SOLUTION);
			return true;
		}else{
			return false;
		}
	}

}
