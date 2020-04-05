package kr.ac.konkuk.ccslab.cm.event.handler;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMServiceManager;

/**
 * The CMEventHandler class represents a CM event handler that processes incoming events.
 * <p>CM internally has multiple event handlers according to event types.
 * 
 * @author CCSLab, Konkuk University
 *
 */
public abstract class CMEventHandler extends CMServiceManager {
	
	public CMEventHandler(CMInfo cmInfo)
	{
		super(cmInfo);
	}
	
	/**
	 * Handles an incoming CM event.
	 * 
	 * <p> A sub-class implements this method and it is called by CM whenever 
	 * the event with a specific event type is received.
	 * 
	 * @param event - the incoming CM event.
	 * @return true if the event is successfully processed, or false.
	 */
	public abstract boolean processEvent(CMEvent event);
}
