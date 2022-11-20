package com.b4utrade.ejb;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.NewsBean;
import com.b4utrade.memorydb.EdgeNewsMemoryDB;


@Stateless(mappedName="ejbCache/EdgeNewsData")
@Remote(EdgeNewsData.class)
public class EdgeNewsDataBean implements EdgeNewsData
{
	public ArrayList getNews(EdgeNewsCriteriaBean bean, int maxRows) {
		return EdgeNewsMemoryDB.instance().getNews(bean, maxRows);
	}

	public HashMap getNewsByTickers(EdgeNewsCriteriaBean bean, int maxRows)
	{
		return EdgeNewsMemoryDB.instance().getNewsByTickers(bean, maxRows);
	}

	public byte[] getCompressedNewsByTickers(EdgeNewsCriteriaBean bean, int maxRows)
	{
		return EdgeNewsMemoryDB.instance().getCompressedNews(bean, maxRows);
	}

	public NewsBean getNewsByID(long id)
	{
		return EdgeNewsMemoryDB.instance().getNewsByID(id);
	}

	public byte[] getCompressedNews(EdgeNewsCriteriaBean bean, int maxRows) {
		System.out.println("In getCompressedNews method");
		return EdgeNewsMemoryDB.instance().getCompressedNews(bean, maxRows);
	}

	public HashMap getLatestNewsByTickers(String tickers, String sources)
	{
		return EdgeNewsMemoryDB.instance().getLatestNewsByTickers(tickers, sources);
	}

	public void ejbCreate() {}
	public void ejbPostCreate() {}
	public void ejbRemove() {}
	public void ejbActivate() {}
	public void ejbPassivate() {}
	public void setSessionContext(SessionContext sc) {}

}
