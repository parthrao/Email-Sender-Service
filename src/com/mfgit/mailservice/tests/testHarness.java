package com.mfgit.mailservice.tests;

import javax.jms.JMSException;

import com.mfgit.flowter.client.FlowClient;
import com.mfgit.mailservice.payloads.SendMail;
import com.mfgit.mailservice.util.MailClient;

public class testHarness {
	private FlowClient fc = null;
	public void init()
	{
		fc = new FlowClient("tcp://localhost:61616","MailService");
		try {
			fc.initClient();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shutdown()
	{
		MailClient.getInstance().shutdown();
		fc.shutdown();
	}
	
	
	public void test_1()
	{
		SendMail sm = new SendMail();
		sm.setEmailBody("<html>Hello,<br><br>[WADR] Template was not found !<br><br>"
		+ "Program did not find the [WADR] Excel Template on Vendor's FTP. Please place the Template<br><br>"
		+"<table border='1'><tr bgcolor='#008080'align='center'><FONT COLOR='WHITE'><th>WORK WEEK</th><th>DATE-TIME(IST)</th><th>VENDOR</th><th>RECORDS IMPORTED</th></font></tr><tr align='center'><td>FY16Q4WW50</td><td>12-06-2016 19:11:38</td><td>ASE-CL</td><FONT COLOR='WHITE'><td bgcolor='red'>0</td></FONT></tr></table><br><br>"
		+ "In case of any issues / queries please email to - <a href='some@example.com'>some@example.com</a><br><br>"
		+ "- </html>");
		sm.setSubject("EMPTY or NO File Found");
		sm.addAddress("some@example.com");
		fc.sendRequest(sm);
	}
	
	public void test_2()
	{
		MailClient mc = MailClient.getInstance();
		
		mc.init("tcp://localhost:61616", "MailService", 20000);
		
		mc.simpleSend("parthrao8@gmail.com", "singleton Test", "This is the reminder to provide the file");
	}
	
	
	public static void main(String[] args) {
		testHarness th = new testHarness();
		
		th.init();
		th.test_1();
		th.shutdown();
	}

}
