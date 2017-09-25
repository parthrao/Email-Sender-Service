package com.mfgit.mailservice.flows;

import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Pattern;

import com.mfgit.common.util.PropHelper;
import com.mfgit.common.util.StrUtil;
import com.mfgit.common.util.Validate;
import com.mfgit.flowservice.flows.ServiceFlow;
import com.mfgit.mailservice.MailConfig;
import com.mfgit.mailservice.commands.SendMailCommand;
import com.mfgit.mailservice.payloads.SendMail;

public class SendMailFlow extends ServiceFlow {
	private Pattern pattern = Pattern.compile("^" + SendMail.class.getSimpleName() + "$");
	private MailConfig mCfg = new MailConfig();
	private String emailHostName=null;
	private int emailPort=0;
	private String emailAccount=null;
	private String emailAccountPassword=null;
	private boolean emailDebug=false;
	private String emailFrom=null;

	@Override
	public boolean filterMatch(Object data) {
		return true;
	}

	@Override
	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public void constructFlow() {
		if (req instanceof SendMail) {
			SendMailCommand smc = new SendMailCommand();
			smc.setRequest((SendMail) req);
			smc.setResponder(this);
			smc.setmCfg(mCfg);

			smc.setEmailAccount(emailAccount);
			smc.setEmailAccountPassword(emailAccountPassword);
			smc.setEmailDebug(emailDebug);
			smc.setEmailFrom(emailFrom);
			smc.setEmailHostName(emailHostName);
			smc.setEmailPort(emailPort);
		
			addCommand(smc);
			setValidFlow(true);
		} else {
			logger.error("This flow received a payload that didn't belong.... not Valid flow.");
			setValidFlow(false);
		}
	}

	@Override
	public boolean initProperties(Properties props) {

		String prefix = getPropertyPath();
		String alias = null;
		String addressList = null;
		int c = 1;
		while (true) {
			alias = props.getProperty(prefix + ".alias." + c);

			if (Validate.isNullOrEmpty(true, alias)) {
				logger.info("End of Alias Configs.");
				break;
			} else {
				addressList = props.getProperty(prefix + ".list." + c);
				if (Validate.isNullOrEmpty(true, addressList)) {
					logger.error("Found Alias : {} with empty address list, can't continue.", alias);
					return false;
				}

				HashSet<String> tokens = StrUtil.getUniqueTokens(addressList);

				if (tokens.size() < 1) {
					logger.error("Found Alias : {} with empty address list, can't continue.", alias);
					return false;
				}

				for (String address : tokens) {

					if (!mCfg.addtoAlias(alias, address)) {
						logger.error("Unable to add address : {} to alias : {}. Check Config.", address, alias);
						return false;
					}
				}
				
				logger.info("Final List : Alias : {} : List : {}",alias,mCfg.getList(alias));
			}

			c++;
		}
		
		String notify = props.getProperty(prefix + ".notify");
		if(Validate.isNullOrEmpty(true, notify))
		{
			logger.error("Unable to Start MailService notify list was missing : {}",(prefix + ".notify"));
			return false;
		}
		this.getResourceContext().putString("notifyList", notify);
		
				
		emailHostName = PropHelper.getPropString(prefix + ".emailHostName", props, null);
		emailPort = PropHelper.getPropInteger(prefix + ".emailPort", props, 0);
		emailAccount = PropHelper.getPropString(prefix + ".emailAccount", props, null);
		emailAccountPassword = PropHelper.getPropString(prefix + ".emailAccountPassword", props, null);
		emailDebug = PropHelper.getPropBoolean(prefix + ".emailDebug", props, false);
		emailFrom = PropHelper.getPropString(prefix + ".emailFrom", props, null);
		
		if(Validate.isNullOrEmpty(true, emailHostName,emailAccount,emailAccountPassword,emailFrom) || emailPort == 0)
		{
			logger.error("All Required fields are not set using prefix {} : emailPort,emailHostName,emailAccount,emailAccountPassword,emailFrom",prefix);
			return false;
		}
		

		return super.initProperties(props);
	}

}
