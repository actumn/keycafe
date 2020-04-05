package kr.ac.konkuk.ccslab.cm.event.handler;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttSession;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttTopicQoS;
import kr.ac.konkuk.ccslab.cm.entity.CMMqttWill;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventDISCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPINGREQ;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPINGRESP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMMqttInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;

/**
 * The CMMqttEventHandler class represents a CM event handler that processes 
 * incoming CM MQTT events.
 * @author CCSLab, Konkuk University
 *
 */
public class CMMqttEventHandler extends CMEventHandler {

	public CMMqttEventHandler(CMInfo cmInfo)
	{
		super(cmInfo);
		m_nType = CMInfo.CM_MQTT_EVENT_HANDLER;
	}
	
	@Override
	public boolean processEvent(CMEvent event) {
		
		boolean bRet = false;
		CMMqttEvent mqttEvent = (CMMqttEvent)event;
		
		switch(event.getID())
		{
		case CMMqttEvent.CONNECT:
			bRet = processCONNECT(mqttEvent);
			break;
		case CMMqttEvent.CONNACK:
			bRet = processCONNACK(mqttEvent);
			break;
		case CMMqttEvent.PUBLISH:
			bRet = processPUBLISH(mqttEvent);
			break;
		case CMMqttEvent.PUBACK:
			bRet = processPUBACK(mqttEvent);
			break;
		case CMMqttEvent.PUBREC:
			bRet = processPUBREC(mqttEvent);
			break;
		case CMMqttEvent.PUBREL:
			bRet = processPUBREL(mqttEvent);
			break;
		case CMMqttEvent.PUBCOMP:
			bRet = processPUBCOMP(mqttEvent);
			break;
		case CMMqttEvent.SUBSCRIBE:
			bRet = processSUBSCRIBE(mqttEvent);
			break;
		case CMMqttEvent.SUBACK:
			bRet = processSUBACK(mqttEvent);
			break;
		case CMMqttEvent.UNSUBSCRIBE:
			bRet = processUNSUBSCRIBE(mqttEvent);
			break;
		case CMMqttEvent.UNSUBACK:
			bRet = processUNSUBACK(mqttEvent);
			break;
		case CMMqttEvent.PINGREQ:
			bRet = processPINGREQ(mqttEvent);
			break;
		case CMMqttEvent.PINGRESP:
			bRet = processPINGRESP(mqttEvent);
			break;
		case CMMqttEvent.DISCONNECT:
			bRet = processDISCONNECT(mqttEvent);
			break;
		default:
			System.err.println("CMMqttEventHandler.processEvent(), invalid event id: ("
					+event.getID()+")!");
			return false;
		}
		return bRet;
	}

	private boolean processCONNECT(CMMqttEvent event)
	{
		// initialization
		CMMqttEventCONNECT conEvent = (CMMqttEventCONNECT)event;
		String strClient = conEvent.getSender();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		CMMqttEventCONNACK ackEvent = new CMMqttEventCONNACK();
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		boolean bConnAckFlag = false;
		byte returnCode = 0;	// connection success
		boolean bRet = false;
		
		// print the received CONNECT event
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processCONNECT(): received "
					+conEvent.toString());
		}
		
		// validate CONNECT packet format and set return code
		// If the format is invalid, the server responds with the failure return code.
		// In MQTT v3.1.1, if the format is invalid, the server disconnects with the client.
		returnCode = validateCONNECT(conEvent);
		if( returnCode != 0 ) // if the validation failed,
		{
			ackEvent.setSender(myself.getName());
			ackEvent.setReturnCode((byte)6);
			bRet = CMEventManager.unicastEvent(ackEvent, strClient, m_cmInfo);
			return bRet;
		}
		
		// to determine ack flag
		CMMqttSession mqttSession = mqttInfo.getMqttSessionHashtable().get(strClient);
		if(conEvent.isCleanSessionFlag())
			bConnAckFlag = false;
		else if( mqttSession != null )
			bConnAckFlag = true;
		else
			bConnAckFlag = false;
		
		// to process clean-session flag
		if(mqttSession != null && conEvent.isCleanSessionFlag())
		{
			mqttInfo.getMqttSessionHashtable().remove(strClient);
			mqttSession = null;
		}
		if(mqttSession == null)
		{
			mqttSession = new CMMqttSession();
			mqttInfo.getMqttSessionHashtable().put(strClient, mqttSession);
		}
		
		// to process will flag
		if(conEvent.isWillFlag())
		{
			CMMqttWill will = new CMMqttWill();
			will.setWillMessage(conEvent.getWillMessage());
			will.setWillTopic(conEvent.getWillTopic());
			will.setWillQoS(conEvent.getWillQoS());
			will.setWillRetain(conEvent.isWillRetainFlag());
			mqttSession.setMqttWill(will);
		}
		
		// to process keep-alive value (not yet)
		if(conEvent.getKeepAlive() > 0)
		{
			// will be incorporated with the CM keep-alive strategy
		}
		
		// to send CONNACK event
		ackEvent.setSender(myself.getName());
		ackEvent.setConnAckFlag(bConnAckFlag);
		ackEvent.setReturnCode(returnCode);
		bRet = CMEventManager.unicastEvent(ackEvent, strClient, m_cmInfo);
		
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processCONNECT(), sent "
					+ackEvent.toString());
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processCONNECT(), error to send "
					+ackEvent.toString());
			return false;
		}

		// to get CMMqttManager
		CMMqttManager mqttManager = (CMMqttManager)m_cmInfo.getServiceManagerHashtable()
										.get(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.err.println("CMMqttEventHandler.processCONNECT(), CMMqttManager is null!");
			return false;
		}

		// to resend all sent-unack-pubrel events to this client (QoS 2)
		bRet = mqttManager.resendSentUnAckPubrel(strClient, mqttSession);
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processCONNECT(), error to resend all "
					+"sent-unack-pubrel events to client ("+strClient+")!");
		}
		// to resend all sent-unack-publish events to this client (QoS 1 or 2)
		bRet = mqttManager.resendSentUnAckPublish(strClient, mqttSession);
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processCONNECT(), error to resend all "
					+"sent-unack-publish events to client ("+strClient+")!");
		}
		// to resend all transmission-pending-publish events to this client (QoS 1 or 2)
		bRet = mqttManager.sendAndClearPendingTransPublish(strClient, mqttSession);
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processCONNECT(), error to send and clear "
					+"transmission pending publish events to client ("+strClient+")!");
		}

		return bRet;
	}
	
	// return value 0 : success
	// return value 1 : wrong packet level
	// return value 2 : client ID not allowed
	// return value 3 : MQTT service unavailable
	// return value 4 : user name and password malformed
	// return value 5 : client not authorized to connect
	// return value 6 : other failure (not defined in MQTT v3.1.1)
	private byte validateCONNECT(CMMqttEventCONNECT conEvent)
	{
		////////////////// validate fixed header

		// validate packet type
		if( conEvent.getPacketType() != 1 )
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), packet type is not 1! : "
					+conEvent.getPacketType());
			return 6;
		}
		// validate flag
		if( conEvent.getFlag() != 0 )
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), fixed header flag is not 0 : "
					+conEvent.getFlag());
			return 6;
		}
		
		////////////////// validate variable header

		// validate protocol name
		if(!conEvent.getProtocolName().contentEquals("MQTT"))
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), protocol name is not MQTT! : "
					+conEvent.getProtocolName());
			return 6;
		}
		// validate protocol level
		if( conEvent.getProtocolLevel() != 4 )
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), protocol level is not 4 : "
					+conEvent.getProtocolLevel());
			return 1;
		}
		// validate will flag and will qos
		if( conEvent.isWillFlag() && (conEvent.getWillQoS() > 2 || conEvent.getWillQoS() < 0 ))
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), wrong will QoS : "
					+conEvent.getWillQoS());
			return 6;
		}
		if( !conEvent.isWillFlag() && (conEvent.getWillQoS() != 0) )
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), will flag is not set, "
					+"but will QoS is not 0!");
			return 6;
		}
		// validate will retain
		if( !conEvent.isWillFlag() && conEvent.isWillRetainFlag() )
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), will flag is not set, "
					+"but will retain is set!");
			return 6;
		}
		
		/////////////////////// validate payload
		
		// validate client ID. In CM, client ID is the same as user name
		if( !conEvent.getClientID().equals(conEvent.getUserName()))
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), client ID("+conEvent.getClientID()
				+") and user name("+conEvent.getUserName()+") are different!");
			return 2;
		}
		// validate user name and flag
		String strUserName = conEvent.getUserName();
		if( conEvent.isUserNameFlag() && (strUserName == null || strUserName.isEmpty()))
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), user name flag is set, "
					+"but there is no user name!");
			return 4;
		}
		if( !conEvent.isUserNameFlag() && strUserName != null && !strUserName.isEmpty())
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), user name flag is not set, "
					+"but user name is not null and not empty ("+conEvent.getUserName()+")!");
			return 4;
		}
		// validate password and flag
		String strPassword = conEvent.getPassword();
		if( !conEvent.isPasswordFlag() && strPassword != null && !strPassword.isEmpty())
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), password flag is not set, "
					+"but password is not null and not empty ("+conEvent.getPassword()+")!");
			return 4;
		}
		if( conEvent.isPasswordFlag() && (strPassword == null || strPassword.isEmpty()))
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), password flag is set, "
					+"but there is no password");
			return 4;
		}
		if( !conEvent.isUserNameFlag() && conEvent.isPasswordFlag() )
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), user name flag is not set, "
					+"but password flag is set ("+conEvent.getPassword()+")!");
			return 4;
		}
		// authenticate user name and password
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		if( confInfo.isLoginScheme() && !CMDBManager.authenticateUser(strUserName, strPassword, m_cmInfo) )
		{
			System.err.println("CMMqttEventHandler.validateCONNECT(), user authentication failed! "
					+"user("+strUserName+"), password("+strPassword+")");
			return 5;
		}

		return 0;	// success
	}
	
	private boolean processCONNACK(CMMqttEvent event)
	{
		CMMqttEventCONNACK connackEvent = (CMMqttEventCONNACK)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processCONNACK(): received "
					+connackEvent.toString());
		}
		
		// if the MQTT connect request is successful,
		if(connackEvent.getReturnCode() == (byte)0)
		{
			CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
			CMMqttSession session = mqttInfo.getMqttSession();
			if(session == null)
			{
				System.err.println("CMMqttEventHandler.processCONNACK(), session is null!");
				return false;
			}

			CMMqttManager mqttManager = (CMMqttManager)m_cmInfo.getServiceManagerHashtable()
					.get(CMInfo.CM_MQTT_MANAGER);
			
			// resent all sent-unack-pubrel events to the server
			boolean bRet = mqttManager.resendSentUnAckPubrel(connackEvent.getSender(), 
					session);
			if(!bRet)
			{
				System.err.println("CMMqttEventHandler.processCONNACK(), error to resend all "
						+"sent-unack-pubrel events to the server!");
			}
			
			// resent all sent-unack-publish events to the server
			bRet = mqttManager.resendSentUnAckPublish(connackEvent.getSender(), session);
			if(!bRet)
			{
				System.err.println("CMMqttEventHandler.processCONNACK(), error to resend all "
						+"sent-unack-publish events to the server!");
			}
		}
		
		return true;
	}
	
	private boolean processPUBLISH(CMMqttEvent event)
	{
		CMMqttEventPUBLISH pubEvent = (CMMqttEventPUBLISH)event;
		boolean bRet = true;
		boolean bDuplicate = false;
		// print received event
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBLISH(): received "
					+pubEvent.toString());
		}
		
		// response
		switch(pubEvent.getQoS())
		{
		case 0:
			// do nothing
			break;
		case 1:
			// send PUBACK
			bRet = sendPUBACK(pubEvent);
			if(!bRet)
				return false; 
			break;
		case 2:
			// check duplicate packet reception
			bDuplicate = isDuplicatePUBLISH(pubEvent);
			if(!bDuplicate)
			{
				// store received PUBLISH event
				bRet = storeRecvPUBLISH(pubEvent);
				if(!bRet)
					return false;
			}
			// send PUBREC
			bRet = sendPUBREC(pubEvent);
			if(!bRet)
				return false;
			break;
		default:
			System.err.println("CMMqttEventHandler.processPUBLISH(), wrong QoS: "
					+pubEvent.getQoS());
			return false;
		}
		
		// if CM is server, it forwards the event to the subscribers
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		if(confInfo.getSystemType().equals("SERVER"))
		{
			if(!bDuplicate)
			{
				CMMqttManager mqttManager = (CMMqttManager)m_cmInfo.getServiceManagerHashtable()
						.get(CMInfo.CM_MQTT_MANAGER);
				// DUP flag = false, RETAIN flag = false
				mqttManager.publish(pubEvent.getTopicName(), 
						pubEvent.getAppMessage(), pubEvent.getQoS(), false, false);				
			}
			
			// process the retain flag
			if(pubEvent.isRetainFlag())
			{
				processRetainEvent(pubEvent);
			}
		}
		
		return bRet;
	}
	
	private void processRetainEvent(CMMqttEventPUBLISH pubEvent)
	{
		// get retain-event hash table
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		Hashtable<String, CMMqttEventPUBLISH> retainHashtable = 
				mqttInfo.getMqttRetainHashtable();
		CMMqttEventPUBLISH oldEvent = null;

		// check whether the application message is zero byte or not
		if(pubEvent.getAppMessage().isEmpty())
		{
			// delete the retain event
			oldEvent = retainHashtable.remove(pubEvent.getTopicName());
			if(CMInfo._CM_DEBUG)
			{
				if(oldEvent != null)
				{
					System.out.println("CMMqttEventHandler.processRetainEvent(), "
							+ "deleted retain event "+oldEvent.toString());
				}
				else {
					System.err.println("CMMqttEventHandler.processRetainEvent(), "
							+"no retain event with topic \""+pubEvent.getTopicName()+"\"!");
				}
			}
		}
		else
		{
			// change the PUBLISH event before retain it
			// The server will send the retained event
			pubEvent.setSender(m_cmInfo.getInteractionInfo().getMyself().getName());
			// DUP flag is reset
			pubEvent.setDupFlag(false);
			// retain the PUBLISH event about the topic
			oldEvent = retainHashtable.put(pubEvent.getTopicName(), 
					pubEvent);
			if(CMInfo._CM_DEBUG)
			{
				if(oldEvent != null)
				{
					System.out.println("CMMqttEventHandler.processRetainEvent(), "
							+ "old retain event "+oldEvent.toString()+" is "
									+ "replaced by the new retain event.");
				}

				System.out.println("CMMqttEventHandler.processRetainEvent(), "
						+ "added new retained event "+pubEvent.toString());
			}
			
		}

		return;
	}
	
	private boolean isDuplicatePUBLISH(CMMqttEventPUBLISH pubEvent)
	{
		// to get session information
		CMMqttSession session = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		String strSysType = confInfo.getSystemType();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		if(strSysType.equals("CLIENT"))
		{
			session = mqttInfo.getMqttSession();
		}
		else if(strSysType.equals("SERVER"))
		{
			session = mqttInfo.getMqttSessionHashtable().get(pubEvent.getSender());
		}
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.isDuplicatePUBLISH(): session is null!");
			return false;
		}
		
		// to check whether the same packet ID is in the recv-unack-publish list
		int nPacketID = pubEvent.getPacketID();
		if(session.findRecvUnAckPublish(nPacketID) != null)
		{
			System.err.println("CMMqttEventHandler.isDuplicatePUBLISH(), event with packet ID ("
					+nPacketID+") is received and still in use in the recv-unack-publish list!");
			return true;
		}
		
		return false;
	}
	
	private boolean storeRecvPUBLISH(CMMqttEventPUBLISH pubEvent)
	{
		// to get session information
		CMMqttSession session = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		String strSysType = confInfo.getSystemType();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		if(strSysType.equals("CLIENT"))
		{
			session = mqttInfo.getMqttSession();
		}
		else if(strSysType.equals("SERVER"))
		{
			session = mqttInfo.getMqttSessionHashtable().get(pubEvent.getSender());
		}
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.storeRecvPUBLISH(): session is null!");
			return false;
		}
		
		// to add event to recvUnackPublishList
		boolean bRet = session.addRecvUnAckPublish(pubEvent);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.storeRecvPUBLISH(): Ok "+pubEvent.toString());
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.storeRecvPUBLISH(): FAILED! "+pubEvent.toString());
			return false;
		}
		
		return bRet;
	}
	
	private boolean sendPUBACK(CMMqttEventPUBLISH pubEvent)
	{
		// initialize PUBACK event
		CMMqttEventPUBACK pubackEvent = new CMMqttEventPUBACK();
		// set sender (in CM event header)
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		pubackEvent.setSender(myself.getName());
		// set fixed header in the CMMqttEVentPUBACK constructor
		// set variable header
		pubackEvent.setPacketID(pubEvent.getPacketID());
		
		// send ack to the PUBLISH sender
		boolean bRet = false;
		String strPubSender = pubEvent.getSender();
		bRet = CMEventManager.unicastEvent(pubackEvent, strPubSender, m_cmInfo);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.sendPUBACK(): Ok "+pubackEvent.toString());
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.sendPUBACK(): FAILED! "+pubackEvent.toString());
			return false;
		}

		return bRet;
	}
	
	private boolean sendPUBREC(CMMqttEventPUBLISH pubEvent)
	{
		// initialize PUBREC event
		CMMqttEventPUBREC recEvent = new CMMqttEventPUBREC();
		// set sender (in CM event header)
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		recEvent.setSender(myself.getName());
		// set fixed header in the CMMqttEventPUBREC constructor
		// set variable header
		recEvent.setPacketID(pubEvent.getPacketID());
		
		// send to the PUBLISH sender
		boolean bRet = false;
		String strPubSender = pubEvent.getSender();
		bRet = CMEventManager.unicastEvent(recEvent, strPubSender, m_cmInfo);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.sendPUBREC(): Ok "+recEvent.toString());
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.sendPUBREC(): FAILED! "+recEvent.toString());
			return false;
		}
		
		return bRet;
	}
	
	private boolean processPUBACK(CMMqttEvent event)
	{
		// A receiver of PUBLISH event with QoS 1 sends the PUBACK event.
		CMMqttEventPUBACK pubackEvent = (CMMqttEventPUBACK)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBACK(), received "
					+pubackEvent.toString());
		}
		
		// to get session information
		CMMqttSession session = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		String strSysType = confInfo.getSystemType();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		
		if(strSysType.equals("CLIENT"))
		{
			session = mqttInfo.getMqttSession();
		}
		else if(strSysType.equals("SERVER"))
		{
			session = mqttInfo.getMqttSessionHashtable().get(pubackEvent.getSender());
		}
		else
		{
			System.err.println("CMMqttEventHandler.processPUBACK(), wrong system type! ("
					+strSysType+")");
			return false;
		}
		
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processPUBACK(), session is null!");
			return false;
		}
		
		// to remove the corresponding PUBLISH event (with the same packet ID) 
		// from the sent-unack-publish list
		int nPacketID = pubackEvent.getPacketID();
		boolean bRet = session.removeSentUnAckPublish(nPacketID);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBACK(), deleted PUBLISH event "
					+"with packet ID ("+nPacketID+").");
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPUBACK(), error to delete PUBLISH "
					+"event with packet ID ("+nPacketID+")!");
			return false;
		}
		
		return true;
	}
	
	private boolean processPUBREC(CMMqttEvent event)
	{
		// A receiver of PUBLISH event with QoS 2 sends the PUBACK event.
		CMMqttEventPUBREC recEvent = (CMMqttEventPUBREC)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBREC(), received "
					+recEvent.toString());
		}

		// to get session information
		CMMqttSession session = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		String strSysType = confInfo.getSystemType();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		
		if(strSysType.equals("CLIENT"))
		{
			session = mqttInfo.getMqttSession();
		}
		else if(strSysType.equals("SERVER"))
		{
			session = mqttInfo.getMqttSessionHashtable().get(recEvent.getSender());
		}
		else
		{
			System.err.println("CMMqttEventHandler.processPUBREC(), wrong system type! ("
					+strSysType+")");
			return false;
		}
		
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processPUBREC(), session is null!");
			return false;
		}

		// to remove PUBLISH event in the session (with the same packet ID)
		int nPacketID = recEvent.getPacketID();
		boolean bRet = session.removeSentUnAckPublish(nPacketID);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBREC(), deleted PUBLISH event "
					+"with packet ID ("+nPacketID+") from the sent-unack-publish list.");
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPUBREC(), error to delete "
					+"PUBLISH event with packet ID ("+nPacketID+") from the sent-unack-publish list !");
			return false;
		}
		
		// to add PUBREC event to the session
		bRet = session.addRecvUnAckPubrec(recEvent);

		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBREC(), added PUBREC event "
					+"with packet ID ("+nPacketID+") to the recv-unack-pubrec list.");				
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPUBREC(), error to add "
					+"PUBREC event with packet ID ("+nPacketID+") to the recv-unack-pubrec list!");
			return false;
		}
		
		// make and send PUBREL event
		CMMqttEventPUBREL relEvent =  new CMMqttEventPUBREL();
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		// set sender (CM event header)
		relEvent.setSender(myself.getName());
		// set fixed header in the CMMqttEventPUBREL constructor
		// set variable header
		relEvent.setPacketID(nPacketID);
		
		bRet = CMEventManager.unicastEvent(relEvent, recEvent.getSender(), m_cmInfo);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBREC(), sent "
					+relEvent.toString());
		}			
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPUBREC(), error to send "
					+relEvent.toString());
			return false;
		}

		return true;
	}
	
	private boolean processPUBREL(CMMqttEvent event)
	{
		// The PUBLISH sender with QoS 2 sends PUBREL in response to PUBREC.
		CMMqttEventPUBREL relEvent = (CMMqttEventPUBREL)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBREL(), received "
					+relEvent.toString());
		}
		
		// to get session information
		CMMqttSession session = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		String strSysType = confInfo.getSystemType();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		
		if(strSysType.equals("CLIENT"))
		{
			session = mqttInfo.getMqttSession();
		}
		else if(strSysType.equals("SERVER"))
		{
			session = mqttInfo.getMqttSessionHashtable().get(relEvent.getSender());
		}
		else
		{
			System.err.println("CMMqttEventHandler.processPUBREL(), wrong system type! ("
					+strSysType+")");
			return false;
		}
		
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processPUBREL(), session is null!");
			return false;
		}

		// to delete PUBLISH event from the session
		int nPacketID = relEvent.getPacketID();
		boolean bRet = session.removeRecvUnAckPublish(nPacketID);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBREL(), deleted PUBLISH event "
					+"with packet ID ("+nPacketID+") from the recv-unack-publish list.");
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPUBREL(), error to delete "
					+"PUBLISH event with packet ID ("+nPacketID+") from the "
							+ "recv-unack-publish list!");
			return false;
		}
		
		// to make and send PUBCOMP event
		CMMqttEventPUBCOMP compEvent = new CMMqttEventPUBCOMP();
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		// set sender (in CM event header)
		compEvent.setSender(myself.getName());
		// set fixed header in the CMMqttEventPUBCOMP constructor
		// set variable header
		compEvent.setPacketID(nPacketID);
		
		bRet = CMEventManager.unicastEvent(compEvent, relEvent.getSender(), m_cmInfo);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBREL(), sent "
					+compEvent.toString());
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPUBREL(), error to send "
					+compEvent.toString());
			return false;
		}
		
		return true;
	}
	
	private boolean processPUBCOMP(CMMqttEvent event)
	{
		// The PUBLISH receiver with QoS 2 sends PUBCOMP in response to PUBREL.
		CMMqttEventPUBCOMP compEvent = (CMMqttEventPUBCOMP)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBCOMP(), received "
					+compEvent.toString());
		}
		
		// to get session information
		CMMqttSession session = null;
		CMConfigurationInfo confInfo = m_cmInfo.getConfigurationInfo();
		String strSysType = confInfo.getSystemType();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		
		if(strSysType.equals("CLIENT"))
		{
			session = mqttInfo.getMqttSession();
		}
		else if(strSysType.equals("SERVER"))
		{
			session = mqttInfo.getMqttSessionHashtable().get(compEvent.getSender());
		}
		else
		{
			System.err.println("CMMqttEventHandler.processPUBCOMP(), wrong system type! ("
					+strSysType+")");
			return false;
		}
		
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processPUBCOMP(), session is null!");
			return false;
		}
		
		// to delete PUBREC from the recv-unack-pubrec list
		int nPacketID = compEvent.getPacketID();
		boolean bRet = session.removeRecvUnAckPubrec(nPacketID);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPUBCOMP(), deleted PUBREC event "
					+"with packet ID ("+nPacketID+") from the recv-unack-pubrec list.");
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPUBCOMP(), error to delete "
					+"PUBREC event with packet ID ("+nPacketID+") from the "
							+ "recv-unack-pubrec list!");
			return false;
		}
		
		return true;
	}
	
	private boolean processSUBSCRIBE(CMMqttEvent event)
	{
		CMMqttEventSUBSCRIBE subEvent = (CMMqttEventSUBSCRIBE)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processSUBSCRIBE(), received "
					+subEvent.toString());
		}
		
		// to check the topic/qos list
		if(subEvent.getTopicQoSList().isEmpty())
		{
			System.err.println("CMMqttEventHandler.processSUBSCRIBE(), there is "
					+"no (topic, qos) pair in the event!");
			return false;
		}
		
		// to get client session information
		String strClient = subEvent.getSender();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		Hashtable<String, CMMqttSession> sessionHashtable = mqttInfo.getMqttSessionHashtable();
		CMMqttSession session = sessionHashtable.get(strClient);
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processSUBSCRIBE(), session of client ("
					+strClient+") is null!");
			return false;
		}
		
		// make SUBACK event
		CMMqttEventSUBACK ackEvent = new CMMqttEventSUBACK();
		// set sender (in CM event header)
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		ackEvent.setSender(myself.getName());
		// set fixed header in the SUBACK constructor
		// set variable header
		ackEvent.setPacketID(subEvent.getPacketID());
		// set payload
		CMList<Byte> returnCodeList = ackEvent.getReturnCodeList();
		
		// add or update the requested (topic, qos) to the existing subscription
		CMList<CMMqttTopicQoS> subscriptionList = session.getSubscriptionList();
		CMList<CMMqttTopicQoS> reqSubList = subEvent.getTopicQoSList();
		Vector<CMMqttTopicQoS> reqSubVector = reqSubList.getList();
		byte qos = -1;
		for(CMMqttTopicQoS topicQoS : reqSubVector)
		{
			CMMqttTopicQoS tempTopicQoS = subscriptionList.findElement(topicQoS);
			if(tempTopicQoS != null)
				subscriptionList.removeElement(tempTopicQoS);
			subscriptionList.addElement(topicQoS);
			
			// determine a return code per topic
			qos = topicQoS.getQoS();
			if(qos >= 0 && qos <= 2)
				returnCodeList.addElement(qos);
			else
				returnCodeList.addElement((byte)0x80); // failure
		}
		
		// send SUBACK event
		boolean bRet = CMEventManager.unicastEvent(ackEvent, subEvent.getSender(), m_cmInfo);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processSUBSCRIBE(), sent "
					+ackEvent.toString());
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processSUBSCRIBE(), error to send "
					+ackEvent.toString());
			return false;
		}

		// send retained event that matches with the added topic
		sendRetainedEvents(subEvent.getSender(), reqSubList);
		
		return true;
	}
	
	private void sendRetainedEvents(String strClient, CMList<CMMqttTopicQoS> topicQoSList)
	{
		// get retain event list
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		Hashtable<String, CMMqttEventPUBLISH> retainHashtable = mqttInfo.getMqttRetainHashtable();
		// get client session
		CMMqttSession session = mqttInfo.getMqttSessionHashtable().get(strClient);
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.sendRetainEvents(), session of client ("
					+strClient+") is null!");
			return;
		}
		// get mqtt manager
		CMMqttManager mqttManager = (CMMqttManager)m_cmInfo.getServiceManagerHashtable()
				.get(CMInfo.CM_MQTT_MANAGER);
		for(String strTopic : retainHashtable.keySet())
		{
			CMMqttEventPUBLISH retainEvent = retainHashtable.get(strTopic);
			String strMsg = retainEvent.getAppMessage();
			byte qos = retainEvent.getQoS();
			boolean bDupFlag = retainEvent.isDupFlag();
			boolean bRetainFlag = retainEvent.isRetainFlag();
			mqttManager.publishFromServerToOneClient(strTopic, strMsg, qos, bDupFlag, 
					bRetainFlag, strClient, session);
		}

		return;
	}
	
	private boolean processSUBACK(CMMqttEvent event)
	{
		CMMqttEventSUBACK ackEvent = (CMMqttEventSUBACK)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processSUBACK(), received "
					+ackEvent.toString());
		}
		
		// add approved subscription to the subscription list
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		CMMqttSession session = mqttInfo.getMqttSession();
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processSUBACK(), session is null!");
			return false;
		}
		CMList<CMMqttTopicQoS> subscriptionList = session.getSubscriptionList();		
		Vector<CMMqttTopicQoS> reqSubscriptionVector = session.getReqSubscriptionList().getList();
		Vector<Byte> returnCodeVector = ackEvent.getReturnCodeList().getList(); 
		
		if(returnCodeVector.size() != reqSubscriptionVector.size())
		{
			System.err.println("CMMqttEventHandler.processSUBACK(), # return code list ("
					+returnCodeVector.size()+") and # req subscription list ("
					+reqSubscriptionVector.size()+") are different!");
			return false;
		}
		
		for(int i = 0; i < returnCodeVector.size(); i++)
		{
			byte returnCode = returnCodeVector.elementAt(i);
			CMMqttTopicQoS topicQoS = reqSubscriptionVector.elementAt(i);
			if(returnCode != (byte)0x80)
			{
				CMMqttTopicQoS subscription = subscriptionList.findElement(topicQoS);
				if(subscription != null)
					subscriptionList.removeElement(subscription);
				subscriptionList.addElement(topicQoS);
			}
		}
		
		session.setReqSubscriptionList(null);
		
		return true;
	}
	
	private boolean processUNSUBSCRIBE(CMMqttEvent event)
	{
		CMMqttEventUNSUBSCRIBE unsubEvent = (CMMqttEventUNSUBSCRIBE)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processUNSUBSCRIBE(), received "
					+unsubEvent.toString());
		}
		
		// to check the topic list
		if(unsubEvent.getTopicList().isEmpty())
		{
			System.err.println("CMMqttEventHandler.processUNSUBSCRIBE(), there is "
					+"no topic list in the event!");
			return false;
		}
		
		// to get client session information
		String strClient = unsubEvent.getSender();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		Hashtable<String, CMMqttSession> sessionHashtable = mqttInfo.getMqttSessionHashtable();
		CMMqttSession session = sessionHashtable.get(strClient);
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processUNSUBSCRIBE(), session of client ("
					+strClient+") is null!");
		}
		else
		{
			// delete any matched subscription with the requested topic filter
			CMList<CMMqttTopicQoS> subList = session.getSubscriptionList();
			Vector<String> topicVector = unsubEvent.getTopicList().getList(); 
			for(String strTopic : topicVector)
			{
				CMMqttTopicQoS topicQoS = new CMMqttTopicQoS();
				topicQoS.setTopic(strTopic);
				subList.removeElement(topicQoS);
			}
		}
		
		// make and send UNSUBACK event
		CMMqttEventUNSUBACK unsubAckEvent = new CMMqttEventUNSUBACK();
		// set sender (in CM event header)
		CMUser myself = m_cmInfo.getInteractionInfo().getMyself();
		unsubAckEvent.setSender(myself.getName());
		// set fixed header in the UNSUBACK constructor
		// set variable header
		unsubAckEvent.setPacketID(unsubEvent.getPacketID());
		
		boolean bRet = false;
		bRet = CMEventManager.unicastEvent(unsubAckEvent, strClient, m_cmInfo);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processUNSUBSCRIBE(), sent "
					+unsubAckEvent.toString());
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processUNSUBSCRIBE(), error to send "
					+unsubAckEvent.toString());
			return false;
		}

		return true;
	}
	
	private boolean processUNSUBACK(CMMqttEvent event)
	{
		CMMqttEventUNSUBACK unsubAckEvent = (CMMqttEventUNSUBACK)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processUNSUBACK(), received "
					+unsubAckEvent.toString());
		}
		return true;
	}
	
	private boolean processPINGREQ(CMMqttEvent event)
	{
		CMMqttEventPINGREQ reqPingEvent = (CMMqttEventPINGREQ)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPINGREQ(), received from ("
					+reqPingEvent.getSender()+")");
		}
		
		CMMqttEventPINGRESP resPingEvent = new CMMqttEventPINGRESP();
		String strMyName = m_cmInfo.getInteractionInfo().getMyself().getName();
		resPingEvent.setSender(strMyName);
		
		boolean bRet = CMEventManager.unicastEvent(resPingEvent, reqPingEvent.getSender(), m_cmInfo);
		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPINGREQ(), sent PINGRESP to ("
					+reqPingEvent.getSender()+")");
		}
		if(!bRet)
		{
			System.err.println("CMMqttEventHandler.processPINGREQ(), error to send "
					+"PINGRESP to ("+reqPingEvent.getSender()+")!");
		}
		
		return bRet;
	}
	
	private boolean processPINGRESP(CMMqttEvent event)
	{
		CMMqttEventPINGRESP resPingEvent = (CMMqttEventPINGRESP)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processPINGRESP(), received from ("
					+resPingEvent.getSender()+")");			
		}
		return true;
	}
	
	private boolean processDISCONNECT(CMMqttEvent event)
	{
		CMMqttEventDISCONNECT disconEvent = (CMMqttEventDISCONNECT)event;
		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMMqttEventHandler.processDISCONNECT(), received "
					+disconEvent.toString());
		}
		
		// to get client session information
		String strClient = disconEvent.getSender();
		CMMqttInfo mqttInfo = m_cmInfo.getMqttInfo();
		Hashtable<String, CMMqttSession> sessionHashtable = mqttInfo.getMqttSessionHashtable();
		CMMqttSession session = sessionHashtable.get(strClient);
		if(session == null)
		{
			System.err.println("CMMqttEventHandler.processDISCONNECT(), session of client ("
					+strClient+") is null!");
		}

		// to delete will information
		if(session.getMqttWill() != null)
		{
			session.setMqttWill(null);
		}
		
		return true;
	}
	
}
