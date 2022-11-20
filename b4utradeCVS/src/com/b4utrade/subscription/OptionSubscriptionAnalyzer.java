package com.b4utrade.subscription;

import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import com.b4utrade.ejb.OptionSubscriber;
import com.equitymontage.ejb.EquityMontageData;
import com.optionsregional.ejb.OptionsRegionalData;
import com.tacpoint.publisher.TAnalyzer;
import com.tacpoint.publisher.TDispatcher;
import com.tacpoint.publisher.TPublisherConfigBean;

public class OptionSubscriptionAnalyzer implements Runnable, TAnalyzer
{
	private InitialContext jndiContext = null;
	private Object reference = null;
	private Object futreference = null;
	private OptionSubscriber optionSubscriber = null;
	private OptionSubscriber futuresSubscriber = null;
	private OptionSubscriber futuresOptionsSubscriber = null;
	private OptionsRegionalData optionsRegionalSubscriber  = null;
	private static final String MONTAGE_IDENTIFIER = "/";
	private EquityMontageData equityMontageSubscriber = null;
	private static final String BLANK = "";
	private static final String OPTIONS_MASK = "O:";
	private static final String FUTURE_OPTIONS_MASK = "FO:";
	private static final String FUTURE_MASK = "/";
	private static Log log = LogFactory.getLog(OptionSubscriptionAnalyzer.class);
	private static ConcurrentHashMap optionSubs = new ConcurrentHashMap(5000);
	private static ConcurrentHashMap futureOptionSubs = new ConcurrentHashMap(5000);
	private static ConcurrentHashMap futureSubs = new ConcurrentHashMap(5000);
	private static ConcurrentHashMap optionsRegionalSubs = new ConcurrentHashMap(5000);
	private static ConcurrentHashMap equityMontageSubs = new ConcurrentHashMap(5000);
	private String subscriberId = null;
	private TPublisherConfigBean configBean;

	public void setConfigBean(TPublisherConfigBean configBean) {
		this.configBean = configBean;
	}

	public void run() {
		try {
			subscriberId = java.net.InetAddress.getLocalHost().getHostName();
		}
		catch (Exception e) {
			subscriberId = String.valueOf(System.currentTimeMillis());
		}
		log.debug("OptionSubscriptionAnalyzer subscriber ID : "+subscriberId);
		System.out.println("OptionSubscriptionAnalyzer subscriber ID : ");

		long curTime = 0;
		long begTime = 0;
		boolean isSubListUpToDate = false;
		boolean isFuturesSubListUpToDate = false;
		boolean isRegionalSubListUpToDate = false;
		boolean isEquityMontageSubListUpToDate = false;
		while (true) 
		{
			try 
			{
				isSubListUpToDate = true;
				isFuturesSubListUpToDate = true;
				isRegionalSubListUpToDate = true;
				isEquityMontageSubListUpToDate = true;
				curTime = System.currentTimeMillis();
				if ( (curTime - begTime) > 2000) 
				{
					log.debug("OptionSubscriptionAnalyzer - comprehensive option subscription list : ");
					begTime = curTime;
					// ticker values not been removed anywhere from MASTER_SUBSCRIPTIONS so using same class for Equity Montage
					/**/
					// force a subscription update in case the options producer is out of sync!
					isSubListUpToDate = false;
					isFuturesSubListUpToDate = false;
					isRegionalSubListUpToDate = false;
					isEquityMontageSubListUpToDate = false;
				}
				try {
					Thread.sleep(1000);
				}
				catch (Exception sleepEx) {}

				Iterator it = TDispatcher.MASTER_SUBSCRIPTIONS.keySet().iterator();
				while (it.hasNext())
				{
					String key = (String)it.next();
					boolean isRegionalSymbol = false;
					boolean isOptionSymbol = false;
					boolean isFutureSymbol = false;
					boolean isFutureOptionSymbol = false;
					boolean isEquityMontageSymbol = false;
					if (isOptionRegionalSymbol(key))
					{
						isRegionalSymbol = true;
					}
					else if (isOptionSymbol(key))
					{
						isOptionSymbol = true;
					}
					else if(isFutureOptionSymbol(key))
						isFutureOptionSymbol = true;	
					else if (key.startsWith(FUTURE_MASK))
					{
						isFutureSymbol = true;
					}
					else if (isMontageSymbol(key))
					{
						isEquityMontageSymbol = true;
					}
					if (isEquityMontageSymbol)
					{
						if (equityMontageSubs.containsKey(key))
							continue;
						log.debug ("Equity Montage symbol " + key);
						equityMontageSubs.put(key, BLANK); // add to the equity montage list
						isEquityMontageSubListUpToDate = false;

					}
					if  (isRegionalSymbol)
					{
						//System.out.println("Regional Symbol name " + key);
						//log.debug("Regional Symbol was " +key);
						if (optionsRegionalSubs.containsKey(key))
							continue;

						optionsRegionalSubs.put(key, BLANK); // add to the regional list
						isRegionalSubListUpToDate = false;
					}
					if(isOptionSymbol)
					{
						if (optionSubs.containsKey(key)) 
						{
							continue;
						}
						else
						{
							System.out.println("Options Ticker requested: " + key);
							optionSubs.put(key,BLANK);
							isSubListUpToDate = false;
						}
					}
					if(isFutureOptionSymbol)
					{
						if (futureOptionSubs.containsKey(key)) 
						{
							continue;
						}
						else
						{
							System.out.println("Future Options Ticker requested: " + key);
							futureOptionSubs.put(key,BLANK);
							isSubListUpToDate = false;
						}
					}
					if(isFutureSymbol)
					{
						if (futureSubs.containsKey(key)) 
						{
							continue;
						}
						else
						{

							futureSubs.put(key,BLANK);
							System.out.println("Futures Ticker requested: " + key);
							isFuturesSubListUpToDate = false;
						}
					}
				}

				Iterator optionsIter = optionSubs.keySet().iterator();
				while (optionsIter.hasNext()) {
					Object key = optionsIter.next();
					if (TDispatcher.MASTER_SUBSCRIPTIONS.containsKey(key))
					{
						continue;
					}

					optionSubs.remove(key);
					isSubListUpToDate = false;
				}
				Iterator futureOptionsIter = futureOptionSubs.keySet().iterator();
				while (futureOptionsIter.hasNext()) {
					Object key = futureOptionsIter.next();
					if (TDispatcher.MASTER_SUBSCRIPTIONS.containsKey(key))
					{
						continue;
					}
					futureOptionSubs.remove(key);
					isSubListUpToDate = false;
				}
				
				Iterator futuresIter = futureSubs.keySet().iterator();
				while (futuresIter.hasNext()) {
					Object key = futuresIter.next();
					if (TDispatcher.MASTER_SUBSCRIPTIONS.containsKey(key))
					{
						continue;
					}

					futureSubs.remove(key);
					isFuturesSubListUpToDate = false;
				}

				Iterator regionalIter = optionsRegionalSubs.keySet().iterator();
				while (regionalIter.hasNext())
				{
					Object key = regionalIter.next();
					if (TDispatcher.MASTER_SUBSCRIPTIONS.containsKey(key))
					{
						continue;
					}
					// log.debug("Removed from regional list " + key);
					optionsRegionalSubs.remove(key);
					isRegionalSubListUpToDate = false;
				}
				Iterator equityMontageIter = equityMontageSubs.keySet().iterator();
				while (equityMontageIter.hasNext())
				{
					Object key = equityMontageIter.next();
					if (TDispatcher.MASTER_SUBSCRIPTIONS.containsKey(key))
					{
						continue;
					}

					equityMontageSubs.remove(key);
					isEquityMontageSubListUpToDate = false;
				}

				if (!isSubListUpToDate && optionSubs.size() >0)
				{
					processSubscriptions();
				}
				if (!isSubListUpToDate && futureOptionSubs.size() >0)
				{
					processFutureOptionsSubscriptions();
				}
				if (!isFuturesSubListUpToDate && futureSubs.size() >0)
				{
					processFuturesSubscriptions();
				}
				if (!isRegionalSubListUpToDate && optionsRegionalSubs.size() >0)
				{
					processRegionalSubscription();
				}
				if (!isEquityMontageSubListUpToDate && equityMontageSubs.size() >0)
				{
					processEquityMontageSubscription();
				}

			}
			catch (Exception e) {
				log.error("Exception encountered during OptionSubscriptionAnalyzer run method - "+e.getMessage(),e);
			}
		}
	}

	private void processRegionalSubscription ()
	{
		try {

			// System.out.println("in processRegionalSubscription");
			if (optionsRegionalSubscriber== null)
				initOptionRegionalSubscriber();

			if (optionsRegionalSubscriber == null) { 
				log.error("Unable to initialize option regional subscriber handle.");
				return;
			}

			System.out.println("Request for REgional symbs: " + optionsRegionalSubs);
			optionsRegionalSubscriber.subscribeOptionsRegionalSymbol(subscriberId, optionsRegionalSubs.keySet().toArray());

			log.debug("Finished updating subscription list on options regional server.");

		}
		catch (Exception e) {
			log.error("Exception in OptionSubscriptionAnalyzer processRegionalSubscriptions method - "+e.getMessage(),e);
			optionsRegionalSubscriber = null;
		}

	}

	// equity monatge
	private void processEquityMontageSubscription ()
	{
		try {

			//  System.out.println("in processEquityMontageSubscription");
			if (equityMontageSubscriber== null)
				initEquityMontageSubscriber();

			if (equityMontageSubscriber == null) { 
				log.error("Unable to initialize Equity Montage subscriber handle.");
				return;
			}
			equityMontageSubscriber.subscribeEquityMontageSymbol(subscriberId, equityMontageSubs.keySet().toArray());

			log.debug("Finished updating subscription list on Equity Montage server.");

		}
		catch (Exception e) {
			log.error("Exception in EquityMontageAnalyzer processEquityMontageSubscriptions method - "+e.getMessage(),e);
			equityMontageSubscriber = null;
		}

	}



	private void processSubscriptions() {

		try {

			// log.debug("IN processSubscriptions");
			// System.out.println("IN processSubscriptions");
			if (optionSubscriber == null)
				initOptionSubscriber();

			if (optionSubscriber == null) { 
				log.debug("Unable to initialize option subscriber handle.");
				System.err.println("Unable to initialize option subscriber handle.");
				log.error("Unable to initialize option subscriber handle.");
				return;
			}

			// System.out.println("Request for options symbs: " + optionSubs.keySet().size());
			optionSubscriber.subscribe(subscriberId, optionSubs.keySet().toArray());

			log.debug("Finished updating subscription list on options server.");

		}
		catch (Exception e) {
			log.error("Exception in OptionSubscriptionAnalyzer processSubscriptions method - "+e.getMessage(),e);
			optionSubscriber = null;
		}
	}
	
	private void processFutureOptionsSubscriptions() {
		try {
			if (futuresOptionsSubscriber == null)
				initFutureOptionSubscriber();
			if (futuresOptionsSubscriber == null) { 
				log.debug("Unable to initialize future option subscriber handle.");
				System.err.println("Unable to initialize future option subscriber handle.");
				log.error("Unable to initialize future option subscriber handle.");
				return;
			}
			futuresOptionsSubscriber.subscribe(subscriberId, optionSubs.keySet().toArray());
			log.debug("Finished updating subscription list on future options server.");
		}
		catch (Exception e) {
			log.error("Exception in OptionSubscriptionAnalyzer processSubscriptions method - "+e.getMessage(),e);
			futuresOptionsSubscriber = null;
		}
	}

	private void processFuturesSubscriptions() {

		try {
			// System.out.println("in processFuturesSubscriptions");

			if (futuresSubscriber == null)
				initFuturesSubscriber();

			if (futuresSubscriber == null) { 
				log.error("Unable to initialize futures subscriber handle.");
				return;
			}

			futuresSubscriber.subscribe(subscriberId, futureSubs.keySet().toArray());

			log.debug("Finished updating subscription list on futures server.");

		}
		catch (Exception e) {
			log.error("Exception in FuturesSubscriptionAnalyzer processSubscriptions method - "+e.getMessage(),e);
			futuresSubscriber = null;
		}
	}

	private void initOptionSubscriber() {
		try {
			// log.debug("IN initOptionSubscriber");
			//System.out.println("IN initOptionSubscriber");
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");

			if (configBean != null && configBean.getOptionsJndiAddress() != null)
				env.put(Context.PROVIDER_URL, configBean.getOptionsJndiAddress());
			else
				env.put(Context.PROVIDER_URL, "192.168.192.114");

			log.debug("Options JNDI Address  " + configBean.getOptionsJndiAddress());
			System.out.println("Options JNDI Address  " + configBean.getOptionsJndiAddress());

			log.info("Options JNDI Address  " + configBean.getOptionsJndiAddress());
			env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			jndiContext = new InitialContext(env);
			reference  = jndiContext.lookup("ejbCache/OptionSubscriber");
			optionSubscriber = (OptionSubscriber) PortableRemoteObject.narrow(reference, OptionSubscriber.class);


			log.debug("Got optionSubscriber " + optionSubscriber);
			System.out.println("Got optionSubscriber " + optionSubscriber);

			// initOptionRegionalSubscriber ();

		}
		catch(Exception e) {
			log.debug("Exception: " + e.getMessage());
			e.printStackTrace();
			log.error("Unable to init OptionSubscriber interface - "+e.getMessage(),e);
			optionSubscriber = null;
		}
	}

	private void initFutureOptionSubscriber() {
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			if (configBean != null && configBean.getFuturesOptionsJndiAddress() != null)
				env.put(Context.PROVIDER_URL, configBean.getFuturesOptionsJndiAddress());
			else
				env.put(Context.PROVIDER_URL, "192.168.192.133");
			log.debug("Futures Options JNDI Address  " + configBean.getFuturesOptionsJndiAddress());
			System.out.println("Futures Options JNDI Address  " + configBean.getFuturesOptionsJndiAddress());
			log.info("Future Options JNDI Address  " + configBean.getFuturesOptionsJndiAddress());
			env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			jndiContext = new InitialContext(env);
			reference  = jndiContext.lookup("ejbCache/OptionSubscriber");
			futuresOptionsSubscriber = (OptionSubscriber) PortableRemoteObject.narrow(reference, OptionSubscriber.class);
			log.debug("Got futureOptionSubscriber " + futuresOptionsSubscriber);
			System.out.println("Got futureOptionSubscriber " + futuresOptionsSubscriber);
		}
		catch(Exception e) {
			log.debug("Exception: " + e.getMessage());
			e.printStackTrace();
			log.error("Unable to init OptionSubscriber interface - "+e.getMessage(),e);
			optionSubscriber = null;
		}
	}

	private void initFuturesSubscriber() {
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");

			if (configBean != null && configBean.getFuturesJndiAddress() != null)
				env.put(Context.PROVIDER_URL, configBean.getFuturesJndiAddress());
			else
				env.put(Context.PROVIDER_URL, "192.168.192.163");

			System.out.println("Future url: " + configBean.getFuturesJndiAddress());

			log.info("Futures JNDI Address  " + configBean.getFuturesJndiAddress());
			env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			jndiContext = new InitialContext(env);
			futreference  = jndiContext.lookup("ejbCache/OptionSubscriber");
			futuresSubscriber = (OptionSubscriber) PortableRemoteObject.narrow(futreference, OptionSubscriber.class);

			System.out.println("Got futures subscriber: " + futuresSubscriber);
		}
		catch(Exception e) {
			e.printStackTrace();
			log.error("Unable to init FutureSubscriber interface - "+e.getMessage(),e);
			futuresSubscriber = null;
		}
	}

	private void initOptionRegionalSubscriber() {
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			//log.debug("Regional Stream IP " + configBean.getOptionsRegionalJndiAddress());

			if (configBean != null && configBean.getOptionsRegionalJndiAddress() != null)
			{
				System.out.println("URL FOR Montage OP: " + configBean.getOptionsRegionalJndiAddress() );
				env.put(Context.PROVIDER_URL, configBean.getOptionsRegionalJndiAddress());
			}
			else
				env.put(Context.PROVIDER_URL, "10.10.192.106");

			log.info("Options Regional JNDI Address  " + configBean.getOptionsRegionalJndiAddress());
			env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			jndiContext = new InitialContext(env);
			reference  = jndiContext.lookup("ejbCache/OptionsRegionalData");
			optionsRegionalSubscriber = (OptionsRegionalData) PortableRemoteObject.narrow(reference, OptionsRegionalData.class);

			System.out.println("GotRegionalsubs");
		}

		catch(Exception e) {
			e.printStackTrace();
			log.error("Unable to init OptionReionalSubscriber interface - "+e.getMessage(),e);
			optionsRegionalSubscriber = null;
		}
	}


	private void initEquityMontageSubscriber() {
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			//log.debug("Regional Stream IP " + configBean.getOptionsRegionalJndiAddress());
			if (configBean != null && configBean.getEquityMontageJndiAddress() != null)
				env.put(Context.PROVIDER_URL, configBean.getEquityMontageJndiAddress());
			else
				env.put(Context.PROVIDER_URL, "172.16.9.93");

			System.out.println("Montage URL: " + env.get(Context.PROVIDER_URL));
			env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			jndiContext = new InitialContext(env);
			reference  = jndiContext.lookup("ejbCache/EquityMontageData");
			equityMontageSubscriber = (EquityMontageData) PortableRemoteObject.narrow(reference, EquityMontageData.class);

			System.out.println("Got montage data: ");
		}
		catch(Exception e) {
			e.printStackTrace();
			log.error("Unable to init OptionSubscriber interface - "+e.getMessage(),e);
			equityMontageSubscriber = null;

		}
	}



	public static boolean isOptionSymbol(String s)
	{
		if (s.startsWith(OPTIONS_MASK) && !isMontageSymbol(s))
			return(true);
		return(false);
	}
	
	public static boolean isFutureOptionSymbol(String s)
	{
		if (s.startsWith(FUTURE_OPTIONS_MASK) && !isMontageSymbol(s))
			return(true);
		return(false);
	}

	// added by kuldeep
	public boolean isOptionRegionalSymbol (String ticker)
	{
		if (ticker.startsWith(OPTIONS_MASK) && isMontageSymbol(ticker)/*ticker.endsWith(OPTION_AMEX_SYMBOL_SUFFIX) || ticker.endsWith(OPTION_BOSTON_SYMBOL_SUFFIX) || ticker.endsWith(OPTION_CBOE_SYMBOL_SUFFIX) || ticker.endsWith(OPTION_INT_SYMBOL_SUFFIX) || ticker.endsWith(OPTION_PACIFIC_SYMBOL_SUFFIX) || ticker.endsWith(OPTION_PHIL_SYMBOL_SUFFIX) || ticker.endsWith(OPTION_NASDAQ_SYMBOL_SUFFIX)*/)
			return true;
		return false;
	}


	// montage work
	private static boolean isMontageSymbol (String ticker)
	{
		ticker = ticker.trim();
		if (ticker == null)
			return false;
		if (ticker.length() < 3)
			return false;
		//Logger.log("Symbol is " + ticker);
		if (!(""+ticker.trim().charAt(ticker.trim().length()-2)).equalsIgnoreCase(MONTAGE_IDENTIFIER))
		{
			return false;
		}
		else
			return true;
	}


}
