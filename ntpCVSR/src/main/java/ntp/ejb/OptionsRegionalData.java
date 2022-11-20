package ntp.ejb;

import java.util.HashMap;
import java.util.LinkedList;

import javax.ejb.Remote;
@Remote
public interface OptionsRegionalData{

	public LinkedList getOptionsRegionalQuote(String optionRegionalTickers) ;
	public void subscribeOptionsRegionalSymbol(String id, Object[] keys) ;
	public HashMap getOptionsRegionalExchanges() ;

}
