package com.b4utrade.ejb;

import java.util.ArrayList;

import javax.ejb.Remote;

import com.b4utrade.bean.NewsBean;
import com.b4utrade.bean.NewsCriteriaDetailBean;
@Remote
public interface NewsData
{

   public ArrayList getNews(NewsCriteriaDetailBean bean);
   public ArrayList getNews(NewsCriteriaDetailBean bean, int maxRows) ;
   public byte[] getCompressedLatestCompanyNews(NewsCriteriaDetailBean bean);
   public byte[] getCompressedNews(NewsCriteriaDetailBean bean);
   public byte[] getCompressedNews(NewsCriteriaDetailBean bean, int maxRows);
   public NewsBean getNewsByID(long id);
}
