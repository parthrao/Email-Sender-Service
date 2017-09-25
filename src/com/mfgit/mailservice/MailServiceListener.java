package com.mfgit.mailservice;

import java.util.Date;
import java.util.HashSet;

import com.mfgit.common.util.StrUtil;
import com.mfgit.common.util.Validate;
import com.mfgit.flowservice.services.ServiceRequestListener;
import com.mfgit.mailservice.payloads.SendMail;

public class MailServiceListener extends ServiceRequestListener {


	@Override
	protected void startupComplete() {
		notifyEvent("MailService Started","MailService has been Started : " + new Date());
		super.startupComplete();
	}

	@Override
	public void shutdown() {
		notifyEvent("MailService Shutdown","MailService has been Shut Down : " + new Date());
		try {
			Thread.sleep(2000); // give it a chance to send.
		} catch (InterruptedException e) {
			
		}
		super.shutdown();
	}
	
	private void notifyEvent(String subject, String body)
	{
		String notifyList = resourceContext.getString("notifyList");
		if(Validate.noNullOrEmpty(true, notifyList))
		{
			SendMail sm = new SendMail();
			HashSet<String> list =StrUtil.getUniqueTokens(notifyList);
			sm.setSubject(subject);
			sm.setEmailBody(body);
			sm.setAddressList(list);
			sm.setCorrID("Internal Message.");
			sm.setRespond(false);
			try {
				messageReceived(sm);
			} catch (Exception e) {
				logger.error("Exception while trying to execute notifyEvent. :",e);
			}
		} else {
			logger.error("Unable to get notifyList from resource Context.");
		}
	}
	
	public static void main(String[] args) {
		if (args.length <= 0) {
			System.err.println("You must Specify Property File location.");
			return;
		}
		MailServiceListener mailList = new MailServiceListener();
		mailList.serviceMain(args[0]);
	}
}
