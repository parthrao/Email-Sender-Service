package com.mfgit.mailservice.payloads;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.mfgit.common.gson.GMessage;
import com.mfgit.common.gson.GsonUtil;
import com.mfgit.flowservice.data.ServicePayload;

public class SendMail extends GMessage implements ServicePayload {

	private String emailBody=null;
	private HashSet<String> addressList = new HashSet<String>();
	private String subject=null;
	
	
	@Override
	public String getPayloadType() {
		return SendMail.class.getSimpleName();
	}

	
	public static void main(String[] args) {
		System.out.println(GsonUtil.getGson().toJson(new SendMail()));
	}


	public HashSet<String> getAddressList() {
		return addressList;
	}


	public void setAddressList(HashSet<String> addressList) {
		this.addressList = addressList;
	}
	
	public void addAddress(String address)
	{
		addressList.add(address);
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getEmailBody() {
		return emailBody;
	}


	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

}
