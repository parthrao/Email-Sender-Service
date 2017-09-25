package com.mfgit.mailservice.commands;

import java.util.HashSet;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.mfgit.FlowResponder;
import com.mfgit.common.util.Validate;
import com.mfgit.flowter.flow.BaseCommand;
import com.mfgit.flowter.flow.Context;
import com.mfgit.mailservice.MailConfig;
import com.mfgit.mailservice.payloads.SendMail;

public class SendMailCommand extends BaseCommand {
	private SendMail request = null;
	private FlowResponder responder = null;
	private MailConfig mCfg=null;
	private String body=null;
	
	private String emailHostName=null;
	private int emailPort=0;
	private String emailAccount=null;
	private String emailAccountPassword=null;
	private boolean emailDebug=false;
	private String emailFrom=null;
	
	@Override
	public Boolean execute(Context ctx) {
		
		if(request == null)
		{
			return doClientResponse(false,"Unable to do anything, request was null.");
		}
		if(mCfg == null)
		{
			return doClientResponse(false,"Unable to do anything, MailConfig was null.");
		}
		
		if(Validate.isNullOrEmpty(true, emailHostName,emailAccount,emailAccountPassword,emailFrom) || emailPort == 0)
		{
			return doClientResponse(false,"All Required fields are not set : emailPort,emailHostName,emailAccount,emailAccountPassword,emailFrom");
		}
		
		HashSet<String> reqList = request.getAddressList();
		
		HashSet<String> addresses = new HashSet<String>();
		body=request.getEmailBody();
		String subject=request.getSubject();
		
		addresses=resolveAliases(reqList, addresses);
		
		if(addresses.isEmpty())
		{
			return doClientResponse(false,"The Address List was empty!");
		}
				
		Email email = new SimpleEmail();
		email.setHostName(emailHostName);
		email.setSmtpPort(emailPort);
		email.setAuthenticator(new DefaultAuthenticator(emailAccount, emailAccountPassword));
		//email.setSSLOnConnect(true);
		email.setDebug(emailDebug);
		//No longer works with TLS enabled... 
//		email.setStartTLSEnabled(true);
//		email.setStartTLSRequired(true);
		try {
			email.setFrom(emailFrom);
			
			if(subject != null)
			{
				email.setSubject(subject);
			}
			
			if(body != null)
			{
				email.setContent(body,"text/html");
			}
			
			for(String address : addresses)
			{
				email.addTo(address);
			}
			
			logger.info("Sending to : {} : subject : {} : body : {}", addresses, subject, body);
			
			email.send();
		} catch (EmailException e) {
			logger.error("Exception while trying to send mail :",e);
			return doClientResponse(false,"Send Failed :" + e.getMessage());
		}
		
		return doClientResponse(true,"SUCCESS");
	}
	
	private boolean doClientResponse(boolean success, String message)
	{
		if(responder != null && request.respond())
		{
			request.setSuccess(success);
			request.setMessage(message);
			responder.respond(request);
		}
		
		if(success)
		{
			return success();
		} else {
			return fail(message);
		}
	}
	
	private HashSet<String> resolveAliases(HashSet<String> seedList,HashSet<String> addresses)
	{
		if(seedList == null)
		{
			return addresses;
		}
		
		for (String item : seedList) {
			if (mCfg.validate(item)) {
				logger.info("Found Valid Email to add to address list : {}", item);
				addresses.add(item);
			} else {
				if(mCfg.aliasExists(item))
				{
					resolveAliases(mCfg.getList(item),addresses);
				} else {
					logger.error("Item was not a valid email address or a configured list : {}", item);
					if (body == null) {
						body = new String();
					}

					body = "ERROR : Partial Send.  Notify Admin. Email was sent to this Alias which is invalid : " + item + "\n\n" + body;

				}
			}
		}
		
		return addresses;
	}

	@Override
	public String getDescription() {
		return "Sends email based on configuration.";
	}

	public SendMail getRequest() {
		return request;
	}

	public void setRequest(SendMail request) {
		this.request = request;
	}

	public FlowResponder getResponder() {
		return responder;
	}

	public void setResponder(FlowResponder responder) {
		this.responder = responder;
	}

	public MailConfig getmCfg() {
		return mCfg;
	}

	public void setmCfg(MailConfig mCfg) {
		this.mCfg = mCfg;
	}

	public String getEmailHostName() {
		return emailHostName;
	}

	public void setEmailHostName(String emailHostName) {
		this.emailHostName = emailHostName;
	}

	public int getEmailPort() {
		return emailPort;
	}

	public void setEmailPort(int emailPort) {
		this.emailPort = emailPort;
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	public void setEmailAccount(String emailAccount) {
		this.emailAccount = emailAccount;
	}

	public String getEmailAccountPassword() {
		return emailAccountPassword;
	}

	public void setEmailAccountPassword(String emailAccountPassword) {
		this.emailAccountPassword = emailAccountPassword;
	}

	public boolean isEmailDebug() {
		return emailDebug;
	}

	public void setEmailDebug(boolean emailDebug) {
		this.emailDebug = emailDebug;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

}
