package kr.ac.konkuk.ccslab.cm.event.handler;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;

/**
 * The CMAppEventHandler interface represents an event handler of a CM application.
 * <p>The application should implements this interface so that it can receive incoming CM events.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public interface CMAppEventHandler {
	
	/**
	 * Processes the CM event.
	 * <p>This method is a callback method that is called by CM whenever it receives 
	 * a CM event from a remote CM node.
	 * <br>A CM application can catch the CM event by implementing this method.
	 * 
	 * @param cme - the received CM event
	 */
	void processEvent(CMEvent cme);
}
