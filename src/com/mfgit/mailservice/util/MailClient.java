package com.mfgit.mailservice.util;

import java.util.HashSet;

import javax.jms.JMSException;

import org.slf4j.LoggerFactory;

import com.mfgit.common.util.StrUtil;
import com.mfgit.common.util.Validate;
import com.mfgit.flowter.client.FlowClient;
import com.mfgit.mailservice.payloads.SendMail;

public class MailClient {
	final org.slf4j.Logger logger = LoggerFactory.getLogger(MailClient.class);
	protected static MailClient instance=null;
	protected FlowClient client=null;
	protected boolean ready=false;
	protected int sendTimeout=20000;
	protected boolean enabled=true;
	private HashSet<String> defaultList = null;
	
	protected MailClient() {
	}
	
	public static synchronized MailClient getInstance()
	{
		if(instance == null)
		{
			instance=new MailClient();
		}
		return instance;
	}

	public boolean isReady() {
		return ready;
	}
	
	public boolean init(String jmsUrl, String queueName, int sendTimeout) {
		if (ready) {
			logger.warn("init called again... already ready.. doing nothing.");
			return true;
		}
		this.sendTimeout=sendTimeout;

		if (Validate.isNullOrEmpty(true, jmsUrl, queueName)) {
			logger.error("jmsUrl ({}) or queueName({}) was null or empty.", jmsUrl, queueName);
			return false;
		}

		client = new FlowClient(jmsUrl, queueName);

		try {
			client.initClient();
		} catch (JMSException e) {
			logger.error("Exception while trying to connect MailClient FlowClient :", e);
			return false;
		}

		logger.info("MailClient init Success.");
		this.ready=true;
		return true;
	}
	
	public void shutdown()
	{
		this.ready=false;
		client.shutdown();
	}
	
	
	
	
	public void simpleSend(String subject, String body) {
		simpleSend(defaultList, subject, body);
	}
	
	public void simpleSend(String strList, String subject, String body) {
		HashSet<String> list = StrUtil.getUniqueTokens(strList);
		simpleSend(list,subject,body);
	}
	
	public void simpleSend(HashSet<String> list, String subject, String body) {
		SendMail sm = new SendMail();
		
		if (list == null || list.size() <= 0) {
			logger.error("Simple Send called with empty or null list... doing nothing.");
			sm.setSuccess(false);
			sm.setMessage("Simple Send called with empty or null list... doing nothing.");
			return;
		}

		for (String addr : list) {
			sm.addAddress(addr);
		}
		sm.setSubject(subject);
		sm.setEmailBody(body);
		sendEmail(sm);
	}
	
	public SendMail simpleSendRequest(String subject, String body) {		
		return simpleSendRequest(defaultList,subject,body);		
	}
	
	public SendMail simpleSendRequest(String strList, String subject, String body) {		
		HashSet<String> list = StrUtil.getUniqueTokens(strList);
		return simpleSendRequest(list,subject,body);		
	}
	
	public SendMail simpleSendRequest(HashSet<String> list, String subject, String body) {
		SendMail sm = new SendMail();
		
		if (list == null || list.size() <= 0) {
			logger.error("Simple Send called with empty or null list... doing nothing.");
			sm.setSuccess(false);
			sm.setMessage("Simple Send called with empty or null list... doing nothing.");
			return sm;
		}

		for (String addr : list) {
			sm.addAddress(addr);
		}
		sm.setSubject(subject);
		sm.setEmailBody(body);
		return sendEmailRequest(sm);
	}
	
	public void sendEmail(SendMail mail)
	{
		mail.setRespond(false);
		sendit(mail);
	}
	
	public SendMail sendEmailRequest(SendMail mail)
	{
		mail.setRespond(true);
		return sendit(mail);
	}
	
	private SendMail sendit(SendMail payload) {
		
		if(!enabled)
		{
			logger.warn("MailCient is disabled.");
			payload.setSuccess(false);
			payload.setMessage("MailClient is currently Disabled.");
			return payload;
		}
		if (ready && client.isReady()) {

			if (payload.respond()) {
				payload = client.sendRequest(payload,sendTimeout);
			} else {
				client.send(payload);
			}

		} else {
			logger.error("Unable to send, not ready or client was null.");
			payload.setSuccess(false);
			payload.setMessage("Unable to send.");
		}
		
		return payload;
	}

	public int getSendTimeout() {
		return sendTimeout;
	}

	public void setSendTimeout(int sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public HashSet<String> getDefaultList() {
		return defaultList;
	}

	public void setDefaultList(HashSet<String> defaultList) {
		this.defaultList = defaultList;
	}
	
	

}
