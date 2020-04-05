import java.nio.ByteBuffer;

import kr.ac.konkuk.ccslab.cm.event.handler.CMMqttEventHandler;
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
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;

public class CMMqttEventTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CMMqttEventTest tester = new CMMqttEventTest();
		
		/*
		tester.testCONNECT();
		tester.testCONNACK();
		tester.testPUBLISH();
		tester.testPUBACK();
		tester.testPUBREC();
		tester.testPUBREL();
		tester.testPUBCOMP();
		*/
		tester.testSUBSCRIBE();
		/*
		tester.testSUBACK();
		tester.testUNSUBSCRIBE();
		tester.testUNSUBACK();
		tester.testPINGREQ();
		tester.testPINGRESP();
		tester.testDISCONNECT();
		*/
		
		/*
		boolean bRet = CMMqttManager.isTopicMatch("/test/temp", "/test/temp");
		System.out.println("isTopicMatch(\"/test/temp\", \"/test/temp\"): "+bRet);
		bRet = CMMqttManager.isTopicMatch("/test/temp", "/+/temp");
		System.out.println("isTopicMatch(\"/test/temp\", \"/+/temp\"): "+bRet);
		bRet = CMMqttManager.isTopicMatch("/test/temp", "#");
		System.out.println("isTopicMatch(\"/test/temp\", \"#\"): "+bRet);
		bRet = CMMqttManager.isTopicMatch("/test/temp", "/test/");
		System.out.println("isTopicMatch(\"/test/temp\", \"/test/\"): "+bRet);
		bRet = CMMqttManager.isTopicMatch("/test/temp", "/test/temp/konkuk");
		System.out.println("isTopicMatch(\"/test/temp\", \"/test/temp/konkuk\"): "+bRet);
		*/
	}

	private void testCONNECT()
	{
		System.out.println("=================== test CONNECT");
		CMMqttEventCONNECT mqttCONNECTEvent = new CMMqttEventCONNECT();
		mqttCONNECTEvent.setUserNameFlag(true);
		mqttCONNECTEvent.setWillQoS((byte)2);
		mqttCONNECTEvent.setWillFlag(true);
		mqttCONNECTEvent.setKeepAlive(100);
		
		mqttCONNECTEvent.setClientID("mqtt-test-client");
		mqttCONNECTEvent.setWillTopic("/CM/mqtt");
		mqttCONNECTEvent.setWillMessage("mqtt-connect-test-message");
		mqttCONNECTEvent.setUserName("ccslab");
		mqttCONNECTEvent.setPassword("ccslab");

		System.out.println("------------------- after setting member variables");
		System.out.println(mqttCONNECTEvent.toString());
		
		//ByteBuffer buf = mqttCONNECTEvent.marshall();
		CMMqttEvent mqttEvent = (CMMqttEvent)mqttCONNECTEvent;
		ByteBuffer buf = mqttEvent.marshall();
		CMMqttEventCONNECT mqttCONNECTEvent2 = new CMMqttEventCONNECT(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttCONNECTEvent2.toString());		
	}
	
	private void testCONNACK()
	{
		System.out.println("=================== test CONNACK");
		CMMqttEventCONNACK mqttConnack = new CMMqttEventCONNACK();
		mqttConnack.setVarHeader(true, (byte)5);
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttConnack.toString());
		
		ByteBuffer buf = mqttConnack.marshall();
		CMMqttEventCONNACK mqttConnack2 = new CMMqttEventCONNACK(buf);
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttConnack2.toString());
	}
	
	private void testPUBLISH()
	{
		System.out.println("=================== test PUBLISH");
		CMMqttEventPUBLISH mqttPublish = new CMMqttEventPUBLISH();
		mqttPublish.setDupFlag(false);
		mqttPublish.setQoS((byte)1);
		mqttPublish.setRetainFlag(true);
		mqttPublish.setTopicName("CM/mqtt/test");
		mqttPublish.setPacketID(1);
		mqttPublish.setAppMessage("test app message");
		
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttPublish.toString());
		
		ByteBuffer buf = mqttPublish.marshall();
		CMMqttEventPUBLISH mqttPublish2 = new CMMqttEventPUBLISH(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttPublish2.toString());
	}
	
	private void testPUBACK()
	{
		System.out.println("=================== test PUBACK");
		CMMqttEventPUBACK mqttPuback = new CMMqttEventPUBACK();
		mqttPuback.setPacketID(5);
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttPuback.toString());
		
		ByteBuffer buf = mqttPuback.marshall();
		CMMqttEventPUBACK mqttPuback2 = new CMMqttEventPUBACK(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttPuback2.toString());
	}
	
	private void testPUBREC()
	{
		System.out.println("=================== test PUBREC");
		CMMqttEventPUBREC mqttPubrec = new CMMqttEventPUBREC();
		mqttPubrec.setPacketID(3);
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttPubrec.toString());
		
		ByteBuffer buf = mqttPubrec.marshall();
		CMMqttEventPUBREC mqttPubrec2 = new CMMqttEventPUBREC(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttPubrec2.toString());
	}

	private void testPUBREL()
	{
		System.out.println("=================== test PUBREL");
		CMMqttEventPUBREL mqttPubrel = new CMMqttEventPUBREL();
		mqttPubrel.setPacketID(7);
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttPubrel.toString());
		
		ByteBuffer buf = mqttPubrel.marshall();
		CMMqttEventPUBREL mqttPubrel2 = new CMMqttEventPUBREL(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttPubrel2.toString());
	}

	private void testPUBCOMP()
	{
		System.out.println("=================== test PUBCOMP");
		CMMqttEventPUBCOMP mqttPubcomp = new CMMqttEventPUBCOMP();
		mqttPubcomp.setPacketID(127);
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttPubcomp.toString());
		
		ByteBuffer buf = mqttPubcomp.marshall();
		CMMqttEventPUBCOMP mqttPubcomp2 = new CMMqttEventPUBCOMP(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttPubcomp2.toString());
	}
	
	private void testSUBSCRIBE()
	{
		System.out.println("=================== test SUBSCRIBE");
		CMMqttEventSUBSCRIBE mqttSubscribe = new CMMqttEventSUBSCRIBE();
		mqttSubscribe.setPacketID(63439);
		mqttSubscribe.addTopicQoS("CM/mqtt", (byte)0);
		mqttSubscribe.addTopicQoS("test/to be deleted", (byte)1);
		mqttSubscribe.addTopicQoS("CM/iot/temp", (byte)2);
		mqttSubscribe.removeTopicQoS("test/to be deleted");
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttSubscribe.toString());
		
		ByteBuffer buf = mqttSubscribe.marshall();
		CMMqttEventSUBSCRIBE mqttSubscribe2 = new CMMqttEventSUBSCRIBE(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttSubscribe2.toString());		
	}
	
	private void testSUBACK()
	{
		System.out.println("=================== test SUBACK");
		CMMqttEventSUBACK mqttSuback = new CMMqttEventSUBACK();
		mqttSuback.setPacketID(0);
		mqttSuback.addReturnCode((byte)0);
		mqttSuback.addReturnCode((byte)2);
		mqttSuback.addReturnCode((byte)128);
		mqttSuback.removeReturnCode((byte)128);

		System.out.println("------------------- after setting member variables");
		System.out.println(mqttSuback.toString());
		
		ByteBuffer buf = mqttSuback.marshall();
		CMMqttEventSUBACK mqttSuback2 = new CMMqttEventSUBACK(buf);

		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttSuback2.toString());
	}

	private void testUNSUBSCRIBE()
	{
		System.out.println("=================== test UNSUBSCRIBE");
		CMMqttEventUNSUBSCRIBE mqttUnsubsribe = new CMMqttEventUNSUBSCRIBE();
		mqttUnsubsribe.setPacketID(0);
		mqttUnsubsribe.addTopic("CM/mqtt");
		mqttUnsubsribe.addTopic("CM/iot/location");
		mqttUnsubsribe.addTopic("test/delTopic");
		mqttUnsubsribe.removeTopic("test/delTopic");

		System.out.println("------------------- after setting member variables");
		System.out.println(mqttUnsubsribe.toString());
		
		ByteBuffer buf = mqttUnsubsribe.marshall();
		CMMqttEventUNSUBSCRIBE mqttUnsubscribe2 = new CMMqttEventUNSUBSCRIBE(buf);

		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttUnsubscribe2.toString());
	}
	
	private void testUNSUBACK()
	{
		System.out.println("=================== test UNSUBACK");
		CMMqttEventUNSUBACK mqttUnsuback = new CMMqttEventUNSUBACK();
		mqttUnsuback.setPacketID(0);
		
		System.out.println("------------------- after setting member variables");
		System.out.println(mqttUnsuback.toString());
		
		ByteBuffer buf = mqttUnsuback.marshall();
		CMMqttEventUNSUBACK mqttUnsuback2 = new CMMqttEventUNSUBACK(buf);
		
		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttUnsuback2.toString());
	}
	
	private void testPINGREQ()
	{
		System.out.println("=================== test PINGREQ");
		CMMqttEventPINGREQ mqttPingreq = new CMMqttEventPINGREQ();
		System.out.println(mqttPingreq.toString());
		
		ByteBuffer buf = mqttPingreq.marshall();
		CMMqttEventPINGREQ mqttPingreq2 = new CMMqttEventPINGREQ(buf);

		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttPingreq2.toString());
	}
	
	private void testPINGRESP()
	{
		System.out.println("=================== test PINGRESP");
		CMMqttEventPINGRESP mqttPingresp = new CMMqttEventPINGRESP();
		System.out.println(mqttPingresp.toString());
		
		ByteBuffer buf = mqttPingresp.marshall();
		CMMqttEventPINGRESP mqttPingresp2 = new CMMqttEventPINGRESP(buf);

		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttPingresp2.toString());
	}

	private void testDISCONNECT()
	{
		System.out.println("=================== test DISCONNECT");
		CMMqttEventDISCONNECT mqttDisconnect = new CMMqttEventDISCONNECT();
		System.out.println(mqttDisconnect.toString());
		
		ByteBuffer buf = mqttDisconnect.marshall();
		CMMqttEventDISCONNECT mqttDisconnect2 = new CMMqttEventDISCONNECT(buf);

		System.out.println("------------------- after marshalling/unmarshalling the event");
		System.out.println(mqttDisconnect2.toString());
	}

}
