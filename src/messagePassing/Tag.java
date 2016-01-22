package messagePassing;

/**
 * Verfügbare Tags des Netzwerks
 * 
 * @author michael koch
 *
 */

public class Tag {
	
	// Sende und Empfange Arbeit
	public final static int WORK = 0;
	
	// Lösung wurde gefunden
	public final static int SOLUTION = 1;
	
	// TokenRing
	public final static int TOKEN = 2;
	
	// Beende die aktuelle Verarbeitung
	public final static int TERMINATE = 3;
	
	// Prozessor hat einen Leeren Stack
	public final static int IDLE = 4;
	
}