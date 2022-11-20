package com.b4utrade.bean;

import java.util.Properties;

public class QuoddConfigurationBean {
	// production
	private static String userConfigurationURL = "https://eqplus.quodd.com/b4utrade/app/QuoddLoadStartupConfiguration.do";

	private static final String URLKeyCheckUserEntitlementURL = "checkUserEntitlementURL";
	private static final String URLKeyCheckUserSessionURL = "checkUserSessionURL";
	private static final String URLKeyDetailedNewsURL = "detailedNewsURL";
	private static final String URLKeyDetailedQuoteJsonURL = "detailedQuoteJsonURL";
	private static final String URLKeyDetailedQuoteListJsonURL = "detailedQuoteJsonURLList";
	private static final String URLKeyExpiredOtherUserURL = "expireOtherUserURL";
	private static final String URLKeyCusipLookUp = "cusipLookUpURL";
	private static final String URLKeyMarketScannerURL = "marketScannerURL";
	private static final String URLKeyMoverAndShakerURL = "moverAndShakerURL";
	private static final String URLKeyLoadUserAlertURL = "loadUserAlertURL";
	private static final String URLKeyManageUserAlertURL = "manageUserAlertURL";
	private static final String URLKeyLoadUserHistoricalAlertURL = "loadUserHistoricalAlertURL";
	private static final String URLKeyloadUserHistoricalAlertCountURL = "loadUserHistoricalAlertCountURL";
	private static final String URLKeyDeleteUserHistoricalAlertURL = "deleteHistoricalAlertURL";
	private static final String URLKeyLoadUserActiveAlertURL = "loadUserActiveAlertURL";
	private static final String URLKeyLoadUserCustomSettingURL = "loadUserCustomSettingURL";
	private static final String URLKeySaveUserCustomSettingURL = "saveUserCustomSettingURL";
	private static final String URLKeyLoadTopTwentyNewsHeadlineURL = "loadTopTwentyNewsHeadlineURL";
	private static final String URLKeyLoadAllCompanyNewsHeadlineURL = "loadAllCompanyNewsHeadlineURL";
	private static final String URLKeySearchDJNewsURL = "searchDJNewsURL";
	private static final String URLKeyGetUserDJNewsCriteriaURL = "getUserDJNewsCriteriaURL";
	private static final String URLKeyDeleteUserDJNewsCriteriaURL = "deleteUserDJNewsCriteriaURL";
	private static final String URLKeySaveUserDJNewsCriteriaURL = "saveUserDJNewsCriteriaURL";
	private static final String URLKeyGetUserEdgeNewsCriteriaURL = "getUserEdgeNewsCriteriaURL";
	private static final String URLKeySaveUserEdgeNewsCriteriaURL = "saveUserEdgeNewsCriteriaURL";
	private static final String URLKeyDeleteUserEdgeNewsCriteriaURL = "deleteUserEdgeNewsCriteriaURL";
	private static final String URLKeySearchEdgeNewsURL = "searchEdgeNewsURL";
	private static final String URLKeyNasdaqBasicStreamingURL = "nasdaqBasicStreamingURL";
	private static final String URLKeyNasdaqBasicPoolingURL = "nasdaqBasicPoolingURL";
	private static final String URLKeyOptionURL = "optionURL";
	private static final String URLKeyOptionRegionalURL = "optionRegionalURL";
	private static final String URLKeyEquityMontageURL = "equityMontageURL";
	private static final String URLKeyCorporateURL = "corporateURL";
	private static final String URLKeySplitAndBuybackURL = "splitAndBuybackURL";
	private static final String URLKeyFrequentAskQuestionsURL = "frequentAskQuestionsURL";
	private static final String URLKeyQuickReferenceURL = "quickReferenceURL";
	private static final String URLKeySystemRequirementURL = "systemRequirementsURL";
	private static final String URLKeyTeamviewerSupportURL = "teamviewerSupportURL";
	private static final String URLKeyDJTreasuryURL = "dowJonesTreasuryURL";
	private static final String URLKeySearchTickerURL = "searchTickerURL";
//	private static final String URLKeyTickerLookUpURL = "tickerLookUpURL";
	private static final String URLKeyAllDJNewswireCodeURL = "allDJNewswireCode";
	private static final String URLKeyTimeAndSaleCriteriaDateURL = "milliTimeAndSaleCriteriaDateURL";
	private static final String URLKeyUserLoginURL = "userLoginURL";
	private static final String URLKeyUserLogoutURL = "userLogoutURL";
	private static final String URLKeyViewAnalystCoverageURL = "viewAnalystCoverageURL";
	private static final String URLKeyViewCompanyEarningURL = "viewCompanyEarningURL";
	private static final String URLKeyViewCompanyFinancialHighlightURL = "viewCompanyFinancialHighlightURL";
	private static final String URLKeyViewCompanyFinancialStatementURL = "viewCompanyFinancialStatementURL";
	private static final String URLKeyViewCompanyKeyStatURL = "viewCompanyKeyStatURL";
	private static final String URLKeyViewCompanySnapshotURL = "viewCompanySnapshotURL";
	private static final String URLKeyViewInsiderActivityURL = "viewInsiderActivityURL";
	private static final String URLKeyViewInstOwnershipURL = "viewInstOwnershipURL";
	private static final String URLKeySecFilingsURL = "viewSecFilingsURL";
	private static final String URLKeyLaunchDowJonesNewsPlusURL = "launchDJNewsPlusURL";
	private static final String URLKeyFutureAndCommoditySelectionListURL = "loadFutureAndCommoditySelectionListURL";
	private static final String URLKeyFutureAndCommodityLookupURL = "lookupFutureAndCommodityURL";
	private static final String URLKeyFutureAndCommodityDetailedQuoteURL = "getFutureAndCommodityDetailedQuoteURL";
	private static final String URLKeyBondsDetailedQuoteURL = "getBondsDetailedQuoteURL";
	private static final String URLKeyFutureAndCommoditySpotSymbolMapURL = "getFutureAndCommoditySpotSymbolMapURL";
	private static final String URLKeyMpidMapURL = "getMpidMapURL";
	private static final String URLKeyPIRCorporateProfileURL = "pirCorporateProfile";
	private static final String URLKeySearchTSQURL = "milliSearchTSQURL";
	private static final String URLKeySearchTSQOptionsURL = "milliSearchOPTIONTSQURL";
	private static final String URLKeyViewAnnualBalanceSheetURL = "viewAnnualBalanceSheetURL";
	private static final String URLKeyViewAnnualIncomeStatementURL = "viewAnnualIncomeStatementURL";
	private static final String URLKeyViewAnnualCashFlowStatementURL = "viewAnnualCashFlowStatementURL";
	private static final String URLKeyViewQuarterlyBalanceSheetURL = "viewQuarterlyBalanceSheetURL";
	private static final String URLKeyViewQuarterlyIncomeStatementURL = "viewQuarterlyIncomeStatementURL";
	private static final String URLKeyViewStockScreenerURL = "viewStockScreenerURL";
	private static final String URLKeyViewMutualFundScreenerURL = "viewMutualFundScreenerURL";
	private static final String URLKeyHistoricalPricesURL = "historicalPricesURL";
	private static final String URLMarketGraderURL = "marketGraderURL";
	private static final String URLEconomicCalendarPlusURL = "economicCalendarPlusURL";
	private static final String URLEconomicCalendarURL = "economicCalendarURL";
	private static final String URLEconomicCalendarEmailDomain = "economicCalendarEmailDomain";
	private static final String URLRTDDOWNLOADLINK = "rtdDownloadLink";
	private static final String URLKeyStandardAndPoorConfig = "standardAndPoorConfigURL";
	private static final String URLKeyTickerReorganizationData = "tickerReorgnisationURL";
	private static final String URLKeyTickerExclusionData = "tickerExclusionDataURL";
	private static final String URLKeyPinkSheetTickerData = "pinkSheetTickerDataURL";
	private static final String URLKeyOTCDuallyQuotedTickerData = "otcDuallyQuotedSymbolURL";
	private static final String URLKeyTsqOptionsChain = "tsqOptionsChainURL";
	private static final String chaikinRequestParams = "chaikinRequestParams";
	private static final String chaikinAuthenticationURL = "chaikinAuthenticationURL";
	private static final String chaikinAuthorisedUserURL = "chaikinAuthorisedUserURL";
	private static final String chaikinBrowserLaunchURL = "chaikinBrowserLaunchURL";
	private static final String chaikinLogoutURL = "chaikinLogoutURL";
	private static final String chaikinRefreshURL = "chaikinRefreshURL";
	private static final String MAX_SYMBOLS = "MAX_SYMBOLS";
	private static final String SYNC_LAYOUT_TIMER = "SYNC_LAYOUT_TIMER";
	private static final String URLEXEDownload = "exeDownloadURL";
	private static final String URLEXEVesrionCHECK = "exeVersionCheckURL";
	private static final String URLOPTIONSVOLUMESTATS = "getOptionsVolumeStatsURL";
	private static final String URLEQUITYVOLUMESTATS = "getEquityVolumeStatsURL";
	private static final String URLNonProAgreementStatus = "getNonProAgreementStatusURL";
	private static final String URLNonProAgreement = "getNonProAgreementURL";
	private static final String URLClickWrap = "getClickWrapURL";
	private static final String URLPriceViewer = "priceViewerURL";
	private static final String URLQuoddPriceViewerTokenGenerator = "quoddPriceViewerTokenGeneratorURL";
	private static final String URLBlackboxStocks = "blackboxStocksURL";
	private static final String URLQuoddBlackbox = "quoddblackboxURL";
	private static final String URLQuoddBlackboxTokenGenerator = "quoddBlackboxTokenGeneratorURL";
	private static final String URL_BRILEY_LINK = "brileyUrl";
	private static final String futureProtocols = "futureProtocols";

	private static final String quoteMediaFundOverviewUrl = "quoteMediaFundOverviewUrl";
	private static final String quoteMediaFundPerformanceUrl = "quoteMediaFundPerformanceUrl";
	private static final String quoteMediaFundPerformanceEtfUrl = "quoteMediaFundPerformanceEtfUrl";
	private static final String quoteMediaFundPerformanceMfUrl = "quoteMediaFundPerformanceMfUrl";
	private static final String quoteMediaFundProfileUrl = "quoteMediaFundProfileUrl";

	private static final String preciseFactorUrl = "preciseFactorUrl";
	private static final String allegoNewsUrl = "allegoNewsUrl";

	private static final String quoteMediaAuthenticateUrl = "quoteMediaAuthenticateUrl";
	private static final String quoteMediaUnauthenticateUrl = "quoteMediaUnauthenticateUrl";
	private static final String vorTokenGenerationUrl = "vorTokenGenerationUrl";

	private static final String mixpanelProjectToken = "mixpanelProjectToken";

	private boolean configurationLoadedFlag = false;
	private Properties userConfigurationProperties;

	public QuoddConfigurationBean() {
	}

	public String getViewMutualFundScreenerURL() {
		return (getParameter(URLKeyViewMutualFundScreenerURL));
	}

	public String getNonProAgreementStatusURL() {
		return getParameter(URLNonProAgreementStatus);
	}

	public String getNonProAgreementURL() {
		return getParameter(URLNonProAgreement);
	}

	public String getViewStockScreenerURL() {
		return (getParameter(URLKeyViewStockScreenerURL));
	}

	public String getBrileyLinkUrl() {
		return (getParameter(URL_BRILEY_LINK));
	}

	public String getViewQuarterlyIncomeStatementURL() {
		return (getParameter(URLKeyViewQuarterlyIncomeStatementURL));
	}

	public String getViewQuarterlyBalanceSheetURL() {
		return (getParameter(URLKeyViewQuarterlyBalanceSheetURL));
	}

	public String getViewAnnualCashFlowStatementURL() {
		return (getParameter(URLKeyViewAnnualCashFlowStatementURL));
	}

	public String getViewAnnualBalanceSheetURL() {
		return (getParameter(URLKeyViewAnnualBalanceSheetURL));
	}

	public String getViewAnnualIncomeStatementURL() {
		return (getParameter(URLKeyViewAnnualIncomeStatementURL));
	}

	public String getHistoricalPricesURL() {
		return (getParameter(URLKeyHistoricalPricesURL));
	}

	public String getCusipLookUpURL() {
		return (getParameter(URLKeyCusipLookUp));
	}

	public void setUserConfigurationProperties(Properties prop) {
		this.userConfigurationProperties = prop;
	}

	public static String getLoadUserConfigurationURL() {
		return (QuoddConfigurationBean.userConfigurationURL);
	}

	public static void setLoadUserConfigurationURL(String s) {
		QuoddConfigurationBean.userConfigurationURL = s;
	}

	public String getCheckUserEntitlementURL() {
		return (getParameter(URLKeyCheckUserEntitlementURL));
	}

	public String getDetailNewsURL() {
		return (getParameter(URLKeyDetailedNewsURL));
	}

	public String getDetailedQuoteJsonURL() {
		return (getParameter(URLKeyDetailedQuoteJsonURL));
	}

	public String getDetailedQuoteListJsonURL() {
		return (getParameter(URLKeyDetailedQuoteListJsonURL));
	}

	public String getExpireOtherUserURL() {
		return (getParameter(URLKeyExpiredOtherUserURL));
	}

	public String getCheckUserSessionURL() {
		return (getParameter(URLKeyCheckUserSessionURL));
	}

	public String getMarketScannerURL() {
		return (getParameter(URLKeyMarketScannerURL));
	}

	public String getMoverAndShakerURL() {
		return (getParameter(URLKeyMoverAndShakerURL));
	}

	public String getNewsURL() {
		return ("");
	}

	public String getNasdaqBasicStreamingURL() {
		return (getParameter(URLKeyNasdaqBasicStreamingURL));
	}

	public String getNasdaqBasicPoolingURL() {
		return (getParameter(URLKeyNasdaqBasicPoolingURL));
	}

	public String getOptionURL() {
		return (getParameter(URLKeyOptionURL));
	}

	public String getOptionRegionalURL() {
		return (getParameter(URLKeyOptionRegionalURL));
	}

	public String getEquityMontageURL() {
		return (getParameter(URLKeyEquityMontageURL));
	}

	public String getCorporateURL() {
		return (getParameter(URLKeyCorporateURL));
	}

	public String getSplitAndBuybackURL() {
		return (getParameter(URLKeySplitAndBuybackURL));
	}

	public String getFrequentAskQuestionsURL() {
		return (getParameter(URLKeyFrequentAskQuestionsURL));
	}

	public String getQuickReferenceURL() {
		return (getParameter(URLKeyQuickReferenceURL));
	}

	public String getSystemRequirementURL() {
		return (getParameter(URLKeySystemRequirementURL));
	}

	public String getDJTreasuryURL() {
		return (getParameter(URLKeyDJTreasuryURL));
	}

	public String getPirCorporateProfileURL() {
		return (getParameter(URLKeyPIRCorporateProfileURL));
	}

	public String getSearchTickerURL() {
		return (getParameter(URLKeySearchTickerURL));
	}

//	public String getTickerLookUpURL() {
//		return (getParameter(URLKeyTickerLookUpURL));
//	}

	public String getTimeAndSaleCriteriaDateURL() {
		return (getParameter(URLKeyTimeAndSaleCriteriaDateURL));
	}

	public String getUserLoginURL() {
		return (getParameter(URLKeyUserLoginURL));
	}

	public String getUserLogoutURL() {
		return (getParameter(URLKeyUserLogoutURL));
	}

	public String getLoadUserAlertURL() {
		return (getParameter(URLKeyLoadUserAlertURL));
	}

	// Aashu
	public String getLoadUserHistoricalAlertURL() {
		return (getParameter(URLKeyLoadUserHistoricalAlertURL));
	}

	public String getDeleteUserHistoricalAlertURL() {
		return (getParameter(URLKeyDeleteUserHistoricalAlertURL));
	}

	public String getLoadUserActiveAlertURL() {
		return (getParameter(URLKeyLoadUserActiveAlertURL));
	}

	public String getManageUserAlertURL() {
		return (getParameter(URLKeyManageUserAlertURL));
	}

	public String getLoadUserCustomSettingURL() {
		return (getParameter(URLKeyLoadUserCustomSettingURL));
	}

	public String getSaveUserCustomSettingURL() {
		return (getParameter(URLKeySaveUserCustomSettingURL));
	}

	public String getLoadTopTwentyNewsHeadlineURL() {
		return (getParameter(URLKeyLoadTopTwentyNewsHeadlineURL));
	}

	public String getLoadAllCompanyNewsHeadlineURL() {
		return (getParameter(URLKeyLoadAllCompanyNewsHeadlineURL));
	}

	public String getSearchTSQURL() {
		return (getParameter(URLKeySearchTSQURL));
	}

	public String getSearchTSQOptionsURL() {
		return (getParameter(URLKeySearchTSQOptionsURL));
	}

	public String getAllDJNewswireCodeURL() {
		return (getParameter(URLKeyAllDJNewswireCodeURL));
	}

	public String getSearchDJNewsURL() {
		return (getParameter(URLKeySearchDJNewsURL));
	}

	public String getUserDJNewsCriteriaURL() {
		return (getParameter(URLKeyGetUserDJNewsCriteriaURL));
	}

	public String getSaveUserDJNewsCriteriaURL() {
		return (getParameter(URLKeySaveUserDJNewsCriteriaURL));
	}

	public String getDeleteUserDJNewsCriteriaURL() {
		return (getParameter(URLKeyDeleteUserDJNewsCriteriaURL));
	}

	public String getSearchEdgeNewsURL() {
		return (getParameter(URLKeySearchEdgeNewsURL));
	}

	public String getUserEdgeNewsCriteriaURL() {
		return (getParameter(URLKeyGetUserEdgeNewsCriteriaURL));
	}

	public String getSaveUserEdgeNewsCriteriaURL() {
		return (getParameter(URLKeySaveUserEdgeNewsCriteriaURL));
	}

	public String getDeleteUserEdgeNewsCriteriaURL() {
		return (getParameter(URLKeyDeleteUserEdgeNewsCriteriaURL));
	}

	public String getViewAnalystCoverageURL() {
		return (getParameter(URLKeyViewAnalystCoverageURL));
	}

	public String getViewCompanyEarningURL() {
		return (getParameter(URLKeyViewCompanyEarningURL));
	}

	public String getViewCompanyFinancialHighlightURL() {
		return (getParameter(URLKeyViewCompanyFinancialHighlightURL));
	}

	public String getViewCompanyKeyStatURL() {
		return (getParameter(URLKeyViewCompanyKeyStatURL));
	}

	public String getViewCompanyFinancialStatementURL() {
		return (getParameter(URLKeyViewCompanyFinancialStatementURL));
	}

	public String getViewCompanySnapshotURL() {
		return (getParameter(URLKeyViewCompanySnapshotURL));
	}

	public String getViewInsiderActivityURL() {
		return (getParameter(URLKeyViewInsiderActivityURL));
	}

	public String getViewInstOwnershipURL() {
		return (getParameter(URLKeyViewInstOwnershipURL));
	}

	public String getViewSecFilingsURL() {
		return (getParameter(URLKeySecFilingsURL));
	}

	public String getLaunchDowJonesNewsPlusURL() {
		return (getParameter(URLKeyLaunchDowJonesNewsPlusURL));
	}

	public String getMarketGraderURL() {
		return (getParameter(URLMarketGraderURL));
	}

	public String getEconomicCalendarPlusURL() {
		return (getParameter(URLEconomicCalendarPlusURL));
	}

	public String getEconomicCalendarURL() {
		return (getParameter(URLEconomicCalendarURL));
	}

	public String getEconomicCalendarEmailDomain() {
		return (getParameter(URLEconomicCalendarEmailDomain));
	}

	public String getRTDDownlaodLink() {
		return (getParameter(URLRTDDOWNLOADLINK));
	}

	public String getTeamviewerSupportURL() {
		return (getParameter(URLKeyTeamviewerSupportURL));
	}

	public boolean isConfigurationLoaded() {
		return (configurationLoadedFlag);
	}

	public boolean getConfigurationLoadedFlag() {
		return (this.configurationLoadedFlag);
	}

	public void setConfigurationLoadedFlag(boolean flag) {
		this.configurationLoadedFlag = flag;
	}

	public String getParameter(String key) {
		return (userConfigurationProperties == null ? null : userConfigurationProperties.getProperty(key));
	}

	public Properties getUserConfigurationProperties() {
		return (this.userConfigurationProperties);
	}

	public String getFutureAndCommoditySelectionListURL() {
		return (getParameter(URLKeyFutureAndCommoditySelectionListURL));
	}

	public String getFutureAndCommodityLookupURL() {
		return (getParameter(URLKeyFutureAndCommodityLookupURL));
	}

	public String getFutureAndCommodityDetailedQuoteURL() {
		return (getParameter(URLKeyFutureAndCommodityDetailedQuoteURL));
	}

	public String getBondsDetailedQuoteURL() {
		return (getParameter(URLKeyBondsDetailedQuoteURL));
	}

	public String getFutureAndCommoditySpotSymbolMapURL() {
		return (getParameter(URLKeyFutureAndCommoditySpotSymbolMapURL));
	}

	public String getMpidMapURL() {
		return (getParameter(URLKeyMpidMapURL));
	}

	public String getLoadUserHistoricalAlertCountURL() {
		return getParameter(URLKeyloadUserHistoricalAlertCountURL);
	}

	public String getStandardAndPoorConfigURL() {
		return (getParameter(URLKeyStandardAndPoorConfig));
	}

	public String getTickerReorganizationDataUrl() {
		return (getParameter(URLKeyTickerReorganizationData));
	}

	public String getTickerExclusionDataUrl() {
		return (getParameter(URLKeyTickerExclusionData));
	}

	public String getPinkSheetTickerListURL() {
		return (getParameter(URLKeyPinkSheetTickerData));
	}

	public String getOTCDuallyQuotedTickerListURL() {
		return (getParameter(URLKeyOTCDuallyQuotedTickerData));
	}

	public String getMaxSymbols() {
		return (getParameter(MAX_SYMBOLS));
	}

	public String getChaikinRequestParams() {
		return (getParameter(chaikinRequestParams));
	}

	public String getChaikinAuthenticationUrl() {
		return (getParameter(chaikinAuthenticationURL));
	}

	public String getChaikinAuthorisedUserUrl() {
		return (getParameter(chaikinAuthorisedUserURL));
	}

	public String getChaikinBrowserlaunchUrl() {
		return (getParameter(chaikinBrowserLaunchURL));
	}

	public String getChaikinLogoutUrl() {
		return (getParameter(chaikinLogoutURL));
	}

	public String getChaikinRefreshUrl() {
		return (getParameter(chaikinRefreshURL));
	}

	public String getSyncLayoutTimer() {
		return getParameter(SYNC_LAYOUT_TIMER);
	}

	public String getUrlTsqOptionchain() {
		return getParameter(URLKeyTsqOptionsChain);
	}

	public String getFutureProtocols() {
		return getParameter(futureProtocols);
	}

	public String getEXEDownloadURL() {
		return getParameter(URLEXEDownload);
	}

	public String getEXEVersionMessageURL() {
		return getParameter(URLEXEVesrionCHECK);
	}

	public String getOptionsVolumeStatsURL() {
		return getParameter(URLOPTIONSVOLUMESTATS);
	}

	public String getEquityVolumeStatsURL() {
		return getParameter(URLEQUITYVOLUMESTATS);
	}

	public String getClickWrapURL() {
		return getParameter(URLClickWrap);
	}

	public String getPriceViewerURL() {
		return getParameter(URLPriceViewer);
	}

	public String getQuoddPriceViewerTokenGeneratorURL() {
		return getParameter(URLQuoddPriceViewerTokenGenerator);
	}

	public String getBlackboxStocksURL() {
		return getParameter(URLBlackboxStocks);
	}

	public String getQuoddBlackboxURL() {
		return getParameter(URLQuoddBlackbox);
	}

	public String getQuoddBlackboxTokenGeneratorURL() {
		return getParameter(URLQuoddBlackboxTokenGenerator);
	}

	public String getQuoteMediaFundOverviewUrl() {
		return getParameter(quoteMediaFundOverviewUrl);
	}

	public String getQuoteMediaFundPerformanceUrl() {
		return getParameter(quoteMediaFundPerformanceUrl);
	}

	public String getQuoteMediaFundPerformanceEtfUrl() {
		return getParameter(quoteMediaFundPerformanceEtfUrl);
	}

	public String getQuoteMediaFundPerformanceMfUrl() {
		return getParameter(quoteMediaFundPerformanceMfUrl);
	}

	public String getQuoteMediaFundProfileUrl() {
		return getParameter(quoteMediaFundProfileUrl);
	}

	public String getPreciseFactorUrl() {
		return getParameter(preciseFactorUrl);
	}

	public String getAllegoNewsUrl() {
		return getParameter(allegoNewsUrl);
	}

	public String getQuoteMediaAuthenticateUrl() {
		return getParameter(quoteMediaAuthenticateUrl);
	}

	public String getQuoteMediaUnauthenticateUrl() {
		return getParameter(quoteMediaUnauthenticateUrl);
	}

	public String getVorTokenGenerationUrl() {
		return getParameter(vorTokenGenerationUrl);
	}

	public String getMixpanelProjectToken() {
		return getParameter(mixpanelProjectToken);
	}
}
