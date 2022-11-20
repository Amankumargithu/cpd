package ntp.jmx;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import ntp.equityregional.streamer.EquityRegionalCPD;
import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

public class TickerHouseInitializer implements TickerHouseInitializerMBean, MBeanRegistration {
	public static final String OBJECT_NAME = ":service=TickerHouseInitializer";
	private static final String EQUITY_REGIONAL = "EQR";

	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		try {
			NTPLogger.info("TickerHouseInitializer - about to instantiate Mbean instance!");
			try {
				CPDProperty p = CPDProperty.getInstance();
				String cpd = p.getProperty("CPD");
				if (cpd.equalsIgnoreCase(EQUITY_REGIONAL)) {
					NTPLogger.info("Initializing Regional Equity CPD");
					EquityRegionalCPD.main(null);
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
