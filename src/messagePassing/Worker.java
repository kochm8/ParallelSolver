package messagePassing;
import mpi.MPI;
import mpi.Status;
import search.State;

/**
 * Die Klasse Worker Empfängt oder Verteilt Arbeit und betreibt den TokenRing.
 * 
 * Die Kommunikation ist stets asynchron um Dead-Locks zu verhindern. 
 * Zudem wird immer mit iProbe geprüft, ob überhaupt eine Nachrichtempfangen werden kann.
 * 
 * @author michael koch
 */
public class Worker {

	//aktueller Rank
	private int me;
	
	//Jeder Prozessor besitzt eine Farbe
	private int processorColor;
	
	//Der Prozessor hat einen WorkRequest versendet
	private boolean sendWorkRequest;

	/**
	 * @param rank des Prozessors
	 */
	public Worker(int rank){
		this.me = rank;
		this.sendWorkRequest = true;
		this.processorColor = TokenColor.WHITE;
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////
	// WORK SHARING
	///////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Der Prozessor ist IDLE und versendet einen WORK-Request an einen zufälligen anderen Prozessor.
	 */
	public void sendWorkRequest(){

		if(sendWorkRequest){
			int[] buffer = new int[1];
			int size = MPI.COMM_WORLD.Size();
			int receiver = (int)(Math.random()*size); 

			while(receiver == me){
				receiver = (int)(Math.random()*size); 
			}

			MPI.COMM_WORLD.Isend(buffer, 0, buffer.length, MPI.INT, receiver, Tag.IDLE);

			sendWorkRequest = false;
		}
	}


	/**
	 * Falls ein Prozessor genügend Arbeit besitzt, prüft er periodisch, ob er einen WORK-Request beantworten kann.
	 * Der Empfangene WORK-Request dient zur Ermittlung des IDLE-Prozessors.
	 * 
	 * @return gibt den IDLE-Prozessor zurück
	 */
	public int recvWorkRequest(){

		int[] buffer = new int[1];
		int receiver = -1;

		Status status = MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE, Tag.IDLE);

		if (status != null){
			MPI.COMM_WORLD.Recv(buffer, 0, buffer.length, MPI.INT, status.source, Tag.IDLE);
			receiver = status.source;
		}

		return receiver;
	}


	/**
	 * Der Prozessor splittet seinen Stack und versendet ihn an den receiver (IDLE-Prozessor). 
	 * Er färbt sich schwarz, wenn der Empfangende Prozessor einen kleineren Rank hat. Weiss bei einem grösseren Rank.
	 * @param aufgeteilter Stack wird an den receiver gesendet
	 */
	public void sendWork(State[] state, int receiver){

		MPI.COMM_WORLD.Isend(state, 0, state.length, MPI.OBJECT, receiver, Tag.WORK);
		if(me > receiver){
			processorColor = TokenColor.BLACK;
		}else{
			processorColor = TokenColor.WHITE;
		}
	}


	/**
	 * Ein IDLE-Prozessor prüft periodisch, ob er vom Prozessor (ermittelt aus der Methode sendWorkRequest()) arbeit empfangen kann.
	 * @return Array mit den abzuarbeitenden States
	 */
	public State[] recvWork(){

		Status status = MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE, Tag.WORK);
		
		if (status != null){
			State[] buffer = new State[status.count];
			MPI.COMM_WORLD.Recv(buffer, 0, status.count, MPI.OBJECT, status.source, Tag.WORK);
			
			State[] states = buffer.clone();
			sendWorkRequest = true;
			return states;
		}else{
			return null;
		}
	}




	///////////////////////////////////////////////////////////////////////////////////////////////////
	// TOKEN RING
	///////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	Weisses Token -> nächster Deep
	Schwarzes Token -> ein anderer Prozessor hat noch Arbeit
	
	Weisser Prozessor -> Prozessor und alle hinter ihm sind sauber. Sie können bei einem leeren Stack ein Weisses Token weitergeben.
	Schwarzer Prozessor -> Prozessor hat Arbeit an einen Prozessor versendet, welcher einen kleineren Rank hat.
	                       Gibt immer ein schwarzes Token weiter.
	*/

	/**
	 * Der Root-Prozessor prüft, die Farbe des empfangenden Tokens. 
	 * Ist es weiss sind alle Prozessoren fertig bzw. IDLE und die Tiefe des IDA* kann erhöht werden.
	 * Ist es schwarz hat ein anderer Prozessor noch Arbeit und der TokenRing wird neu gestartet.
	 * @param queueLength
	 * @return
	 */
	public boolean recvWhiteToken(int queueLength){

		Token[] buffer = new Token[1];
		int lastProcessor = MPI.COMM_WORLD.Size()-1;

		Status status = MPI.COMM_WORLD.Iprobe(lastProcessor, Tag.TOKEN);

		if (status != null){
			MPI.COMM_WORLD.Recv(buffer, 0, buffer.length, MPI.OBJECT, status.source, Tag.TOKEN);

			Token token = buffer[0];

			if(token.isWhite()){
				return true;
			}else{

				if(queueLength == 0){
					Worker.startToken(TokenColor.WHITE);
				}else{
					Worker.startToken(TokenColor.BLACK);
				}
			}
		}

		return false;
	}

	/**
	 * Das Token wird eingefärbt und an den nächsten Prozessor weitergegeben.
	 * Wenn der Prozessor Schwarz ist, wird das Token ebenfalls schwarz
	 * @param queueLength
	 */
	public void passToken(int queueLength){

		int size =  MPI.COMM_WORLD.Size();
		Token[] buffer = new Token[1];

		int previous = me-1;
		int next = me+1;

		if(next == size){
			next = 0;
		}

		Status status = MPI.COMM_WORLD.Iprobe(previous, Tag.TOKEN);
		if (status != null){
			MPI.COMM_WORLD.Recv(buffer, 0, buffer.length, MPI.OBJECT, status.source, Tag.TOKEN);
			Token token = buffer[0];

			if(queueLength != 0){
				token.setToBlack();
			}

			if(processorColor == TokenColor.BLACK){
				token.setToBlack();
				processorColor = TokenColor.WHITE;
			}

			MPI.COMM_WORLD.Isend(buffer, 0, buffer.length, MPI.OBJECT, next, Tag.TOKEN);
		}
	}

	/**
	 * Startet den TokenRing
	 * @param color
	 */
	public static void startToken(int color){
		Token[] buffer = new Token[1];
		buffer[0] = new Token();
		buffer[0].setToColor(color);
		MPI.COMM_WORLD.Isend(buffer, 0, buffer.length, MPI.OBJECT, 1, Tag.TOKEN);
	}

}
