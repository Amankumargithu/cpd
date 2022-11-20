package ntp.ejb;

import java.util.HashMap;
import java.util.LinkedList;

import javax.ejb.Remote;
@Remote
public interface EquityMontageData {
	public LinkedList getEquityMontageQuote(String equityMontageTickers) ;
	public void subscribeEquityMontageSymbol(String id, Object[] keys);
	public HashMap getEquityMontageExchanges(String rootSymbol);
}
