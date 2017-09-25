package com.mfgit.mailservice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

public class MailConfig {
	public org.slf4j.Logger logger = LoggerFactory.getLogger(MailConfig.class);
	private HashMap<String,HashSet<String>> aliasList = new HashMap<String,HashSet<String>>();
	private Pattern pattern= Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
 
	public boolean validate(String address) {
 
		Matcher matcher = pattern.matcher(address);
		return matcher.matches();
 
	}
	
	public boolean aliasExists(String alias)
	{
		return aliasList.containsKey(alias);
	}
	
	public HashSet<String> getList(String alias)
	{
		return aliasList.get(alias);
	}
	
	public boolean addtoAlias(String alias, String address) {
		synchronized (aliasList) {
			HashSet<String> list = aliasList.get(alias);
			if (validate(address)) {
				logger.debug("Valid address : {}", address);
				if (list == null) {
					list = new HashSet<String>();
					aliasList.put(alias, list);
				}

				logger.info("Adding Address : {} to alias list : {}", address, alias);
				list.add(address);
				return true;
			} else {
				if (aliasList.containsKey(address)) {
					for (String currAdd : aliasList.get(address)) {
						if (validate(currAdd)) {
							logger.debug("Valid address : {}", currAdd);
							if (list == null) {
								list = new HashSet<String>();
								aliasList.put(alias, list);
							}
							logger.info("Adding Address : {} to alias list : {}", currAdd, alias);
							list.add(currAdd);
						} else {
							logger.error("Email or Alias Address : {} is invalid.", currAdd);
							return false;
						}

					}

				} else {
					logger.error("Email or Alias Address : {} is invalid.", address);
					return false;
				}
				return true;
			}

		}
	}

}
