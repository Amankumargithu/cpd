package ntp.jmx;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import ntp.equity.EquityCPD;
import ntp.equityregional.streamer.EquityRegionalCPD;
import ntp.futureOptions.streamer.FutureOptionsCPD;
import ntp.futures.streamer.FuturesCPD;
import ntp.logger.NTPLogger;
import ntp.options.streamer.OptionsCPD;
import ntp.optionsregional.streamer.OptionsRegionalCPD;
import ntp.tsqstr.TSQCPD;
import ntp.util.CPDProperty;

public class TickerHouseInitializer implements TickerHouseInitializerMBean, MBeanRegistration {
	public static final String OBJECT_NAME = ":service=TickerHouseInitializer";
	private static final String EQUITY = "EQ";
	private static final String EQUITY_REGIONAL = "EQR";
	private static final String OPTIONS = "OP";
	private static final String OPTIONS_REGIONAL = "OPR";
	private static final String FUTURES = "FTR";
	private static final String FUTURES_OPTIONS = "FTOPR";
	private static final String TSQ = "TSQ";

	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		try {
			NTPLogger.info("TickerHouseInitializer - about to instantiate Mbean instance!");
			try {
				CPDProperty p = CPDProperty.getInstance();
				String cpd = p.getProperty("CPD");
				if (cpd.equalsIgnoreCase(EQUITY)) {
					NTPLogger.info("Initializing Equity CPD");
					EquityCPD.main(null);
				} else if (cpd.equalsIgnoreCase(OPTIONS)) {
					NTPLogger.info("Initializing Options CPD");
					OptionsCPD.main(null);
				} else if (cpd.equalsIgnoreCase(FUTURES)) {
					NTPLogger.info("Initializing Futures CPD");
					FuturesCPD.main(null);
				} else if (cpd.equalsIgnoreCase(FUTURES_OPTIONS)) {
					NTPLogger.info("Initializing Futures Options CPD");
					FutureOptionsCPD.main(null);
				} else if (cpd.equalsIgnoreCase(OPTIONS_REGIONAL)) {
					NTPLogger.info("Initializing Regional Options CPD");
					OptionsRegionalCPD.main(null);
				} else if (cpd.equalsIgnoreCase(EQUITY_REGIONAL)) {
					NTPLogger.info("Initializing Regional Equity CPD");
					EquityRegionalCPD.main(null);
				} else if (cpd.equalsIgnoreCase(TSQ)) {
					NTPLogger.info("Initializing TSQ CPD");
					TSQCPD.main(null);
				} else {
					NTPLogger.error("Please specify CPD in cpd.properties file");
					NTPLogger.error("CPD=OP or EQ ... etc");
					NTPLogger.error("Exiting now..");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			;
			NTPLogger.info("TickerHouseInitializer - successfully instantiated Mbean instance!");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ObjectName(OBJECT_NAME);
	}

	public void postRegister(Boolean registrationDone) {
		NTPLogger.info("In Mbean postRegister");
	}

	public void preDeregister() throws Exception {
		NTPLogger.info("In Mbean preDeregister");
	}

	public void postDeregister() {
		NTPLogger.info("In Mbean postDeregister");
	}

}
