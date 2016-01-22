package messagePassing;

import mpi.MPI;
import mpi.Status;

/**
 * Klasse TerminationCOM stellt die KommunikatinosWerkzeuge zur Verfügung um die Verarbeitung zu beenden
 * 
 * @author michael koch
 *
 */
public class TerminationCOM {


	/**
	 * Der Prozessor sendet er an alle anderen Prozessoren ein TERMINATE. z.B. wenn er die Lösung gefunden hat
	 */
	public static void sendTermination(int me){
		int size = MPI.COMM_WORLD.Size();
		int[] buffer = new int[1];

		for(int rank=0; rank<size; rank++){
			if(rank != me){
				MPI.COMM_WORLD.Isend(buffer, 0, buffer.length, MPI.INT, rank, Tag.TERMINATE);
			}
		}
	}

	/**
	 * Der Prozessor empfängt ein TERMINATE, falls er eine Empfangen kann.
	 * Jeder Prozessor muss die recvTermination-Methode regelmässig ausführen.
	 * 
	 * @return Ein anderer Prozessor hat die Lösung gefunden
	 */
	public static boolean recvTermination(){
		int[] buffer = new int[1];

		Status status = MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE, Tag.TERMINATE);
		if (status != null){
			MPI.COMM_WORLD.Recv(buffer, 0, buffer.length, MPI.INT, status.source, Tag.TERMINATE);
			return true;
		}else{
			return false;
		}
	}

}
