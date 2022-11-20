package com.quodd.util;

import java.util.HashMap;
import java.util.Map;

public class RecorderHelper {

	public static final Map<Integer, String> metaCodeMap = new HashMap<>(); // 3
	public static final Map<String, String> authenticity = new HashMap<>(); // 4.1
	public static final Map<String, String> calculationMethod = new HashMap<>(); // 4.3
	public static final Map<String, String> channelEventMap = new HashMap<>(); // 4.7
	public static final Map<String, String> contractMultiplierType = new HashMap<>(); // 4.8
	public static final Map<String, String> disseminationFrequency = new HashMap<>(); // 4.12
	public static final Map<String, String> financialStatus = new HashMap<>(); // 4.18
	public static final Map<String, String> issueType = new HashMap<>(); // 4.23
	public static final Map<Byte, String> imbalanceType = new HashMap<>(); // 4.21
	public static final Map<String, String> luldPriceBandIndicator = new HashMap<>(); // 4.25
	public static final Map<String, String> marketTier = new HashMap<>(); // 4.29
	public static final Map<String, String> matchingAlgo = new HashMap<>(); // 4.30
	public static final Map<Byte, String> priceRangeIndicationType = new HashMap<>(); // 4.41
	public static final Map<String, String> productType = new HashMap<>(); // 4.44
	public static final Map<String, String> settlementTime = new HashMap<>(); // 4.51
	public static final Map<Byte, String> statusType = new HashMap<>(); // 4.55.1
	public static final Map<Byte, String> statusCodeTrading = new HashMap<>(); // 4.55.2.1
	public static final Map<Byte, String> statusCodeSHO = new HashMap<>(); // 4.55.2.2
	public static final Map<Byte, String> reasonCodeTrading = new HashMap<>(); // 4.55.3.1
	public static final Map<Byte, String> reasonCodeSHO = new HashMap<>(); // 4.55.3.2
	public static final Map<Byte, String> statusFlagTrading = new HashMap<>(); // 4.55.4.1
	public static final Map<String, String> strategy = new HashMap<>(); // 4.56
	public static final Map<Byte, String> symbologyType = new HashMap<>(); // 4.57
	public static final Map<Byte, String> valueUpdateFlag = new HashMap<>(); // 4.63
	public static final Map<String, String> primaryMarketCode = new HashMap<>(); // 4.63

	static {
		// Custom Meta - Section 4.3.3
		metaCodeMap.put(0xfe, "Country");
		metaCodeMap.put(0xfd, "Currency");
		metaCodeMap.put(0xfc, "MIC");
		metaCodeMap.put(0xfb, "ProductType");
		metaCodeMap.put(0xfa, "AlternateSymbol");
		metaCodeMap.put(0xf9, "SymbologyType");
		metaCodeMap.put(0xf8, "PrimaryMarketCode");
		//
		metaCodeMap.put(0x01, "RootSymbol"); // 1
		metaCodeMap.put(0x02, "Put_CALL"); // 2
		metaCodeMap.put(0x03, "ExpirationDate"); // 3
		metaCodeMap.put(0x04, "StrikePrice"); // 4
		metaCodeMap.put(0x05, "CommodityCode"); // 5
		metaCodeMap.put(0x06, "MaturityDate"); // 6
		metaCodeMap.put(0x07, "Coupon"); // 7
		metaCodeMap.put(0x08, "FaceValue"); // 8
		metaCodeMap.put(0x09, "IssueDate");// 9
		metaCodeMap.put(0x0a, "StrikeCurrency"); // 10
		metaCodeMap.put(0x0b, "CurrencyM"); // 11
		metaCodeMap.put(0x0c, "Cusip/Isin"); // 12
		metaCodeMap.put(0x0d, "MarketTier"); // 13
		metaCodeMap.put(0x0e, "FinancialStatus"); // 14
		metaCodeMap.put(0x0f, "Authenticity"); // 15
		metaCodeMap.put(0x10, "ShortSaleThresholdIndicator"); // 16
		metaCodeMap.put(0x11, "DisclosureStatus"); // 17
		metaCodeMap.put(0x12, "ContractMultiplier"); // 18
		metaCodeMap.put(0x13, "ContractMultiplierType"); // 19
		metaCodeMap.put(0x14, "DisplayFactor"); // 20
		metaCodeMap.put(0x15, "ExpirationCycle"); // 21
		metaCodeMap.put(0x16, "FlowSchedule"); // 22
		metaCodeMap.put(0x17, "ImpliedIndicator"); // 23
		metaCodeMap.put(0x18, "MatchAlgo"); // 24
		metaCodeMap.put(0x19, "ListingMarket"); // 25
		metaCodeMap.put(0x1a, "RoundLot"); // 26
		metaCodeMap.put(0x1b, "Reserved1"); // 27
		metaCodeMap.put(0x1c, "Reserved2"); // 28
		metaCodeMap.put(0x1d, "Reserved3"); // 29
		metaCodeMap.put(0x1e, "CabinetPrice"); // 30
		metaCodeMap.put(0x1f, "MinLotSize"); // 31
		metaCodeMap.put(0x20, "MinPriceIncrement"); // 32
		metaCodeMap.put(0x21, "MinTradeVolume"); // 33
		metaCodeMap.put(0x22, "MaxTradeVolume"); // 34
		metaCodeMap.put(0x23, "OriginalContractSize"); // 35
		metaCodeMap.put(0x24, "PriceRatio"); // 36
		metaCodeMap.put(0x25, "PricingModel"); // 37
		metaCodeMap.put(0x26, "Strategy"); // 38
		metaCodeMap.put(0x27, "UnitOfMeasure"); // 39
		metaCodeMap.put(0x28, "UserDefined"); // 40
		metaCodeMap.put(0x29, "PiggybackExempt"); // 41
		metaCodeMap.put(0x2a, "CaveatEmptor"); // 42
		metaCodeMap.put(0x2b, "UnsolicitedOnly"); // 43
		metaCodeMap.put(0x2c, "BB_Quoted"); // 44
		metaCodeMap.put(0x2d, "MessagingDisabled"); // 45
		metaCodeMap.put(0x2e, "IssueName"); // 46
		metaCodeMap.put(0x2f, "OldSymbol"); // 47
		metaCodeMap.put(0x30, "IssueType"); // 48
		metaCodeMap.put(0x31, "CalculationMethod"); // 49
		metaCodeMap.put(0x32, "SettlementTime"); // 50
		metaCodeMap.put(0x33, "InstrumentGroup"); // 51
		metaCodeMap.put(0x34, "DisseminationFrequency"); // 52
		metaCodeMap.put(0x37, "Duration"); // 55
		metaCodeMap.put(0x38, "Yield"); // 56
		metaCodeMap.put(0x39, "NAV"); // 57
		metaCodeMap.put(0x3a, "EstimatedCashPerCreationUnit"); // 58
		metaCodeMap.put(0x3b, "TotalCashPerCreationUnit"); // 59
		metaCodeMap.put(0x3c, "Dividend"); // 60
		metaCodeMap.put(0x3d, "TotalSharesOutstanding"); // 61
		metaCodeMap.put(0x3e, "PrimaryMarketMaker"); // 62
		metaCodeMap.put(0x3f, "MarketMakerMode"); // 63
		metaCodeMap.put(0x40, "MarketParticipantState"); // 64
		metaCodeMap.put(0x41, "HighPrice"); // 65
		metaCodeMap.put(0x42, "LowPrice"); // 66
		metaCodeMap.put(0x43, "LastPrice"); // 67
		metaCodeMap.put(0x44, "OpenPrice"); // 68
		metaCodeMap.put(0x45, "ClosePrice"); // 69
		metaCodeMap.put(0x46, "TotalVolume"); // 70
		metaCodeMap.put(0x47, "NetChange"); // 71
		metaCodeMap.put(0x48, "OpenInterest"); // 72
		metaCodeMap.put(0x49, "TickValue/IPVValue"); // 73
		metaCodeMap.put(0x4a, "Bid"); // 74
		metaCodeMap.put(0x4b, "Ask"); // 75
		metaCodeMap.put(0x4c, "SpecialOpeningPrice"); // 76
		metaCodeMap.put(0x4d, "ExpectedVolume"); // 77
		metaCodeMap.put(0x4e, "MWCBDeclineLevel1"); // 78
		metaCodeMap.put(0x4f, "MWCBDeclineLevel2"); // 79
		metaCodeMap.put(0x50, "MWCBDeclineLevel3"); // 80
		metaCodeMap.put(0x51, "ParentSymbol"); // 81
		metaCodeMap.put(0x52, "LULD Price Band Indicator"); // 82
		metaCodeMap.put(0x53, "LULD Price Quote Indicator"); // 83
		metaCodeMap.put(0x54, "LULD NBBO Indicator"); // 84
		metaCodeMap.put(0x55, "EffectiveTime"); // 85
		metaCodeMap.put(0x56, "OpenPrice2"); // 86
		metaCodeMap.put(0x57, "ClosePrice2"); // 87
		metaCodeMap.put(0x58, "SettlementValue"); // 88
		metaCodeMap.put(0x59, "OpeningRangeLow"); // 89
		metaCodeMap.put(0x5a, "OpeningRangeHigh"); // 90
		metaCodeMap.put(0x5b, "SuspensionPrice"); // 91
		metaCodeMap.put(0x5c, "ResumptionPrice"); // 92
		metaCodeMap.put(0x5d, "CloingRangeLow"); // 93
		metaCodeMap.put(0x5e, "ClosingRangeHigh"); // 94
		metaCodeMap.put(0x5f, "TradingLimitsLow"); // 95
		metaCodeMap.put(0x60, "TradingLimitsHigh"); // 96
		metaCodeMap.put(0x61, "FixingPrice"); // 97
		metaCodeMap.put(0x62, "HighBid"); // 98
		metaCodeMap.put(0x63, "LowAsk"); // 99
		metaCodeMap.put(0x64, "UnderlyingPrice"); // 100
		metaCodeMap.put(0x65, "Side"); // 101
		metaCodeMap.put(0x66, "Quantity"); // 102
		metaCodeMap.put(0x67, "OfferWanted/BidWanted"); // 103
		metaCodeMap.put(0x68, "RetailInterest"); // 104
		metaCodeMap.put(0x69, "Last/ClosingMarket"); // 105
		metaCodeMap.put(0x6a, "NumberOfPricedBidParitipants"); // 106
		metaCodeMap.put(0x6b, "NumberOfPricedAskParitipants"); // 107
		metaCodeMap.put(0x6c, "PreviousClosePrice"); // 108
		metaCodeMap.put(0x6d, "PreviousCloseDate"); // 109
		metaCodeMap.put(0x6e, "Reserved4"); // 110
		metaCodeMap.put(0x6f, "VWAP"); // 111
		metaCodeMap.put(0x70, "TotalValue"); // 112
		metaCodeMap.put(0x71, "BidSize"); // 113
		metaCodeMap.put(0x72, "AskSize"); // 114
		metaCodeMap.put(0x73, "52-WeekHigh"); // 115
		metaCodeMap.put(0x74, "52-WeekLow"); // 116
		metaCodeMap.put(0x75, "RedemptionNAV"); // 117
		metaCodeMap.put(0x76, "WrapPrice"); // 118
		metaCodeMap.put(0x77, "ELTR"); // 119
		metaCodeMap.put(0x78, "Interest"); // 120
		metaCodeMap.put(0x79, "DividendFactor"); // 121
		metaCodeMap.put(0x7a, "Avg.Maturity"); // 122
		metaCodeMap.put(0x7b, "Avg.Life"); // 123
		metaCodeMap.put(0x7c, "TotalNetAssets"); // 124
		metaCodeMap.put(0x7d, "Gross7DayYield"); // 125
		metaCodeMap.put(0x7e, "Subsidized7DayYield"); // 126
		metaCodeMap.put(0x7f, "EffectiveAnnualized7DayYield"); // 127
		metaCodeMap.put(0x80, "30DayYield"); // 128
		metaCodeMap.put(0x81, "30DayYieldDate"); // 129
		metaCodeMap.put(0x82, "EntryDate"); // 130
		metaCodeMap.put(0x83, "Non-QualifiedCash"); // 131
		metaCodeMap.put(0x84, "QualifiedCash"); // 132
		metaCodeMap.put(0x85, "Tax-FreeCash"); // 133
		metaCodeMap.put(0x86, "OrdinaryForeignTaxCredit"); // 134
		metaCodeMap.put(0x87, "QualifiedForeignTaxCredit"); // 135
		metaCodeMap.put(0x88, "ReinvestDate"); // 136
		metaCodeMap.put(0x89, "ShortTermGain"); // 137
		metaCodeMap.put(0x8a, "LongTermGain"); // 138
		metaCodeMap.put(0x8b, "UnallocatedDistributions"); // 139
		metaCodeMap.put(0x8c, "ReturnOfCapital"); // 140
		metaCodeMap.put(0x8d, "InstrumentFlag"); // 141
		metaCodeMap.put(0x8e, "InstrumentValueFlag"); // 142
		metaCodeMap.put(0x8f, "UpstreamConditionDetail"); // 143
		metaCodeMap.put(0x90, "SettlementType"); // 144
		metaCodeMap.put(0x91, "TradeReportDetail"); // 145
		metaCodeMap.put(0x92, "ExtendedTradeReportFlags"); // 146
		metaCodeMap.put(0x93, "LULD Band Lower Limit"); // 147
		metaCodeMap.put(0x94, "LULD Band Upper Limit"); // 148
		metaCodeMap.put(0x95, "Bid MMID Locate"); // 149
		metaCodeMap.put(0x96, "Ask MMID Locate"); // 150
		metaCodeMap.put(0x97, "SuspensionRangeLow"); // 151
		metaCodeMap.put(0x98, "SuspensionRangeHigh"); // 152
		metaCodeMap.put(0x99, "ResumptionRangeLow"); // 153
		metaCodeMap.put(0x9a, "ResumptionRangeHigh"); // 154
		metaCodeMap.put(0x9b, "BidYeild"); // 155
		metaCodeMap.put(0x9c, "AskYield"); // 156
		metaCodeMap.put(0x9d, "AllowableTradingRangeLow"); // 157
		metaCodeMap.put(0x9e, "AllowableTradingRangeHigh"); // 158
		metaCodeMap.put(0x9f, "FeeCode"); // 159
		metaCodeMap.put(0xa0, "ActivationDate"); // 160
		metaCodeMap.put(0xa1, "TickOpen"); // 161
		metaCodeMap.put(0xa2, "TickClose"); // 162
		metaCodeMap.put(0xa3, "TickHigh"); // 163
		metaCodeMap.put(0xa4, "TickLow"); // 164
		metaCodeMap.put(0xa5, "TickNetChange"); // 165
		metaCodeMap.put(0xa6, "TickPreviousClose"); // 166
		metaCodeMap.put(0xa7, "PreviousOpenInterest"); // 167
		metaCodeMap.put(0xa8, "PreviousVolume"); // 168
		metaCodeMap.put(0xa9, "LastTradeDate"); // 169
		metaCodeMap.put(0xaa, "Post"); // 170
		metaCodeMap.put(0xab, "Panel"); // 171
		metaCodeMap.put(0xac, "LRP Value"); // 172
		metaCodeMap.put(0xad, "Ex Dividend Date"); // 173
		metaCodeMap.put(0xae, "Trade Dissemination Time"); // 174
		metaCodeMap.put(0xaf, "Reference URL"); // 175
		metaCodeMap.put(0xb0, "Event Trigger Market"); // 176
		metaCodeMap.put(0xb1, "Event Trigger Price"); // 177
		metaCodeMap.put(0xb2, "Event Trigger Volume"); // 178
		metaCodeMap.put(0xb3, "Ticker/Tape"); // 179
		metaCodeMap.put(0xb4, "SettlementDate"); // 180
		metaCodeMap.put(0xb5, "CalculatedOpeningPrice"); // 181
		metaCodeMap.put(0xb6, "ExtendedOrderFlags"); // 182
		metaCodeMap.put(0xb7, "ShortSaleStatus"); // 183
		metaCodeMap.put(0xb8, "DistributionFrequency"); // 184
		metaCodeMap.put(0xb9, "ImbalanceFlags"); // 185
		metaCodeMap.put(0xba, "PairedQuantity"); // 186
		metaCodeMap.put(0xbb, "FarPricce"); // 187
		metaCodeMap.put(0xbc, "NearPRice"); // 188
		metaCodeMap.put(0xbd, "ReferencePrice"); // 189
		metaCodeMap.put(0xbe, "PriceVariationIndicator"); // 190
		metaCodeMap.put(0xbf, "ParValue"); // 191
		metaCodeMap.put(0xc0, "ClearingPrice"); // 192
		metaCodeMap.put(0xc1, "ClosingOnlyClearingPrice"); // 193
		metaCodeMap.put(0xc2, "SSRFilingPrice"); // 194
		metaCodeMap.put(0xc3, "GMFVolume"); // 195
		metaCodeMap.put(0xc4, "IndicativePrice"); // 196
		metaCodeMap.put(0xc5, "ActionOnlyPrice"); // 197
		metaCodeMap.put(0xc6, "QuoteCondition"); // 198
		metaCodeMap.put(0xc7, "MinPostOnlyQuantity"); // 199
		metaCodeMap.put(0xc8, "TotalImbalance"); // 200
		metaCodeMap.put(0xc9, "MarketImbalance"); // 201
		metaCodeMap.put(0xca, "ListingMarketCenterSymbol"); // 202
		metaCodeMap.put(0xcb, "MainFraction"); // 203
		metaCodeMap.put(0xcc, "SubFraction"); // 204
		metaCodeMap.put(0xcd, "TotalReturnValue"); // 205
		metaCodeMap.put(0xce, "TotalReturnValueChange"); // 206
		metaCodeMap.put(0xcf, "Rar/ActualTradeIDFromMarket"); // 207
		metaCodeMap.put(0xd0, "Rule144aFlag"); // 208
		metaCodeMap.put(0xd1, "SaturationEligibleFlag"); // 209
		metaCodeMap.put(0xd2, "UnitOfMeasureQuantity"); // 210
		metaCodeMap.put(0xd3, "NAV Premium"); // 211
		metaCodeMap.put(0xd4, "Original NAV Premium"); // 212
		metaCodeMap.put(0xd5, "NAV Premium Bid"); // 213
		metaCodeMap.put(0xd6, "NAV Premium Ask"); // 214
		metaCodeMap.put(0xd7, "LegPrice"); // 215
		metaCodeMap.put(0xd8, "LegOptionDelta"); // 216
		metaCodeMap.put(0xd9, "CalculationTime"); // 217
		metaCodeMap.put(0xda, "ClearingVolume"); // 218
		metaCodeMap.put(0xdb, "BuyMaximumQuantity"); // 219
		metaCodeMap.put(0xdc, "BuyMinimumQuantity"); // 220
		metaCodeMap.put(0xdd, "SellMaximumQuantity"); // 221
		metaCodeMap.put(0xde, "SellMinimumQuantity"); // 222
		metaCodeMap.put(0xdf, "TestSymbol"); // 223
		metaCodeMap.put(0xe0, "AuctionTime"); // 224
		metaCodeMap.put(0xe1, "BrokerNumber1"); // 225
		metaCodeMap.put(0xe2, "BrokerNumber2"); // 226
		metaCodeMap.put(0xe3, "ECN Eligible Flag"); // 227
		metaCodeMap.put(0xe4, "SecurityID"); // 228
		metaCodeMap.put(0xe5, "IndicativeOpeningPrice"); // 229
		metaCodeMap.put(0xe6, "IndicativeOpeningQuantity"); // 230
		metaCodeMap.put(0xff, "ExtendedValueType"); // 255

		authenticity.put("0", "Unknown");
		authenticity.put("1", "Live");
		authenticity.put("2", "Demo");
		authenticity.put("3", "Test");
		authenticity.put("4", "Deleted");

		calculationMethod.put("0", "Unknown");
		calculationMethod.put("1", "Price Return");
		calculationMethod.put("2", "Total Return");
		calculationMethod.put("3", "Net Total Return");
		calculationMethod.put("4", "Excess Return");
		calculationMethod.put("5", "Excess Total Return");
		calculationMethod.put("6", "Inverse");
		calculationMethod.put("7", "Leveraged");
		calculationMethod.put("8", "World Currency Options");
		calculationMethod.put("9", "Dividend");
		calculationMethod.put("10", "Alpha");
		calculationMethod.put("11", "Volatility");
		calculationMethod.put("12", "Pre-Market Indicator");
		calculationMethod.put("13", "After hours Indicator");
		calculationMethod.put("14", "Unweighted");
		calculationMethod.put("15", "OMRX");
		calculationMethod.put("16", "OMRXON");
		calculationMethod.put("17", "Money Market");
		calculationMethod.put("18", "NOREX");
		calculationMethod.put("19", "SSV");
		calculationMethod.put("20", "Commodity Price");
		calculationMethod.put("21", "Commodity Excess");
		calculationMethod.put("22", "Commodity Excess Total");
		calculationMethod.put("23", "CMFX");
		calculationMethod.put("24", "Commodity Close");
		calculationMethod.put("25", "External Index");
		calculationMethod.put("26", "Settlement");
		calculationMethod.put("27", "Alternative");
		calculationMethod.put("28", "Notional Total Return");
		calculationMethod.put("29", "Hedged Return");
		calculationMethod.put("30", "Bid");
		calculationMethod.put("31", "Ask");
		calculationMethod.put("32", "Last Sale");
		calculationMethod.put("33", "Time Weighted Average Price");

		channelEventMap.put("*SOT", "Start of Transactions");
		channelEventMap.put("*SOD", "Start of Day");
		channelEventMap.put("*SOR", "Start of Reporting");
		channelEventMap.put("*EOD", "End of Day");
		channelEventMap.put("*MKO", "Market Open");
		channelEventMap.put("*MKC", "Market Close");
		channelEventMap.put("*ECD", "Early Close");
		channelEventMap.put("*ERT", "End of Retransmission Requests");
		channelEventMap.put("*EOR", "End of Reporting");
		channelEventMap.put("*EOT", "End of Transactions");
		channelEventMap.put("*ELS", "End of Last Sale Eligibility");
		channelEventMap.put("*ADO", "FINRA ADF Open (US Equities)");
		channelEventMap.put("*ADC", "FINRA ADF Close (US Equities)");
		channelEventMap.put("*SOI", "Start Open Interest");
		channelEventMap.put("*EOI", "End Open Interest");
		channelEventMap.put("*SOS", "Start of Summaries");
		channelEventMap.put("*EOS", "End of Summaries");
		channelEventMap.put("*GDM", "Good Morning");
		channelEventMap.put("*GDN", "Good Night");
		channelEventMap.put("*MW1", "Market Wide Circuit Breaker Level1 Breach (US Equities)");
		channelEventMap.put("*MW2", "Market Wide Circuit Breaker Level2 Breach (US Equities)");
		channelEventMap.put("MW3", "Market Wide Circuit Breaker Level3 Breach (US Equities)");
		channelEventMap.put("*PRO", "Pre-Open (CME,TSX)");
		channelEventMap.put("*OPN", "Opening (TSX)");
		channelEventMap.put("*CLO", "Closing (TSX)");
		channelEventMap.put("*PRC", "Pre=Cross (CME)");
		channelEventMap.put("*CRX", "Cross (CME)");
		channelEventMap.put("*NCX", "No Cancel (CME)");
		channelEventMap.put("*HLT", "Trading halt (CME)");
		channelEventMap.put("*EMH", "Emergency Market Halt (US Equities)");
		channelEventMap.put("*EMQ", "Emergency Market Quoting Period (US Equities)");
		channelEventMap.put("*EMT", "Emergency Market Trading Resume (US Equities)");
		channelEventMap.put("*EHO", "Extended Hours Open (TSX)");
		channelEventMap.put("*EHC", "Extended Hours Close (TSX)");
		channelEventMap.put("*EHX", "Extended Hours Cancels (TSX)");
		channelEventMap.put("*MOC", "MOC Imbalance (TSX)");
		channelEventMap.put("*CCP", "CCP Determination (TSX)");
		channelEventMap.put("*PME", "Price Movement Extension (TSX)");
		channelEventMap.put("*NCD", "(TSX) NAVex Closed");
		channelEventMap.put("*NOE", "(TSX) Order Entry");
		channelEventMap.put("*NOC", "(TSX) Order Entry Close");
		channelEventMap.put("*NEX", "(TSX) NAVex Execution");
		channelEventMap.put("*NCL", "(TSX) NAVex Close");

		contractMultiplierType.put("1", "Hour");
		contractMultiplierType.put("2", "Day");

		disseminationFrequency.put("0", "Unknown");
		disseminationFrequency.put("1", "Tick By Tick");
		disseminationFrequency.put("2", "0.01 Second");
		disseminationFrequency.put("3", "0.1 Second");
		disseminationFrequency.put("4", "Quarter Second");
		disseminationFrequency.put("5", "Half Second");
		disseminationFrequency.put("6", "One Second");
		disseminationFrequency.put("7", "5 Seconds");
		disseminationFrequency.put("8", "15 Seconds");
		disseminationFrequency.put("9", "One Minute");
		disseminationFrequency.put("10", "Once,at open");
		disseminationFrequency.put("11", "Once. at close");
		disseminationFrequency.put("12", "Once, intraday");
		disseminationFrequency.put("13", "Weekly");
		disseminationFrequency.put("14", "Monthly");
		disseminationFrequency.put("15", "Quarterly");
		disseminationFrequency.put("16", "Yearly");
		disseminationFrequency.put("17", "Bi-Weekly");
		disseminationFrequency.put("18", "Daily");
		disseminationFrequency.put("19", "Intraday");
		disseminationFrequency.put("20", "15 Minutes");

		financialStatus.put("0", "Normal");
		financialStatus.put("1", "Bankrupt");
		financialStatus.put("2", "Delinquent (Late Filing)");
		financialStatus.put("3", "Delinquent and bankrupt");
		financialStatus.put("4", "Deficient (Below Listing Standards)");
		financialStatus.put("5", "Deficient and Bankrupt");
		financialStatus.put("6", "Deficient and Delinquent");
		financialStatus.put("7", "Deficient, Delinquent and Bankrupt");
		financialStatus.put("8", "Suspended");
		financialStatus.put("9", "Creation Suspended (ETPs)");
		financialStatus.put("10", "Redemptions Suspended (ETPs)");
		financialStatus.put("11", "Liquidation (ETPs)");

		issueType.put("0", "Not Available");
		issueType.put("1", "Unclassified");
		issueType.put("2", "American Depository Receipt");
		issueType.put("3", "Bond");
		issueType.put("4", "Common Shares");
		issueType.put("5", "Bond Derivative");
		issueType.put("6", "Equity Derivative");
		issueType.put("7", "Depository Receipt");
		issueType.put("8", "Government Bond");
		issueType.put("9", "Corporate Bond");
		issueType.put("10", "Limited Partnership");
		issueType.put("11", "Miscellaneous");
		issueType.put("12", "Note");
		issueType.put("13", "Ordinary Shares");
		issueType.put("14", "Preferred Shares");
		issueType.put("15", "Rights");
		issueType.put("16", "Shares of Benificial Interest");
		issueType.put("17", "Convertible Debenture");
		issueType.put("18", "Unit");
		issueType.put("19", "Units of Benificial Interest");
		issueType.put("20", "Warrant");
		issueType.put("21", "Index Warrant");
		issueType.put("22", "Put Warrant");
		issueType.put("23", "Money Market");
		issueType.put("24", "Coupon");
		issueType.put("25", "Residual");
		issueType.put("26", "T-Bill");
		issueType.put("27", "Money Market - General Purpose");
		issueType.put("28", "Money Market - Government Securities");
		issueType.put("29", "Money Market - Tax Exempt Securities");
		issueType.put("30", "Mutual Fund - Open End");
		issueType.put("31", "Mutual Fund - Close End");
		issueType.put("32", "UIT - Debt Securities");
		issueType.put("33", "UIT - Equity Securities");
		issueType.put("34", "Annuity - Variable");
		issueType.put("35", "Annuity - Equity Indexed");
		issueType.put("36", "Structured Product - Access Transactions");
		issueType.put("37", "Structured Product - Tax Driven Structure");
		issueType.put("38", "Structured Product - Buffered Note");
		issueType.put("39", "Structured Product - Principal Protected Note");
		issueType.put("40", "Structured product - Levered Note");
		issueType.put("41", "Structured Product - Enhanced Income Note");
		issueType.put("42", "AIP - Hedge Funds");
		issueType.put("43", "AIP - Fund of Hedge Funds");
		issueType.put("44", "AIP - Managed Future Funds");
		issueType.put("45", "AIP - Commodity Pool Funds");
		issueType.put("46", "AIP - Non-Traded REITs");
		issueType.put("47", "AIP - Non-Traditional Hedge Funds (Registed)");
		issueType.put("48", "AIP - Private Equity Offerings");
		issueType.put("49", "T-Bond");
		issueType.put("50", "Structured Product (No Information)");
		issueType.put("51", "American Depository - Preferred");
		issueType.put("52", "American Depository - Warrants");
		issueType.put("53", "American Depository - Rights");
		issueType.put("54", "American Depository - Corporate Bond");
		issueType.put("55", "NY Registered Share");
		issueType.put("56", "Index");
		issueType.put("57", "Mutual Fund");
		issueType.put("58", "Basket");
		issueType.put("59", "Liquidating Trust");
		issueType.put("60", "Debenture");
		issueType.put("61", "ETF");
		issueType.put("62", "Foreign");
		issueType.put("63", "American Depository Shares");
		issueType.put("64", "Test");
		issueType.put("65", "Unit 2");
		issueType.put("66", "Exchange Traded Mutual Fund (ETMF)");
		issueType.put("67", "Trust Preferred");
		issueType.put("68", "Alpha Index ETNs");
		issueType.put("69", "Index Linked Note");
		issueType.put("70", "Commodity Based Trust Shares");
		issueType.put("71", "Commodity Futures Trust Shares");
		issueType.put("72", "Commodity Linked Securities");
		issueType.put("73", "Commodity Index Trust Shares");
		issueType.put("74", "Collateralized Mortgage Obligation");
		issueType.put("75", "Currency Trust Shares");
		issueType.put("76", "Commodity-Currency-Linked Securities");
		issueType.put("77", "Currency Warrants");
		issueType.put("78", "Global Depository Shares");
		issueType.put("79", "ETF-Unit Investment Trust");
		issueType.put("80", "Equity Gold Shares");
		issueType.put("81", "ETN-Equity Index-Linked Securities");
		issueType.put("82", "Exchange Traded Note");
		issueType.put("83", "Equity Units");
		issueType.put("84", "ETN-Fixed Income-Linked Securities");
		issueType.put("85", "ETN-Futures-Linked Securities");
		issueType.put("86", "Exchange Traded Debt");
		issueType.put("87", "ETF-Management Investment Company");
		issueType.put("88", "Interest Rate");
		issueType.put("89", "Index-Linked Exchangable Notes");
		issueType.put("90", "Corporate Backed Trust Security");
		issueType.put("91", "Contingent Litigation Right/Warrant");
		issueType.put("92", "Limited Liability Company");
		issueType.put("93", "Equity Linked Note");
		issueType.put("94", "Managed Fund Shares");
		issueType.put("95", "ETN-Multi-Factor Index-Linked Securities");
		issueType.put("96", "Managed Trust Securities");
		issueType.put("97", "Income Depositary Shares");
		issueType.put("98", "Third Party Trust Certificate");
		issueType.put("99", "Poison Pill");
		issueType.put("100", "Partnership Units");
		issueType.put("101", "Closed End Funds");
		issueType.put("102", "Reg-S");
		issueType.put("103", "Commodity-Redeemable Commodity-Linked Securities");
		issueType.put("104", "ETN-Redeemable Futures-Linked Securities");
		issueType.put("105", "REIT");
		issueType.put("106", "Commodity-Redeemable Currency-Linked Securities");
		issueType.put("107", "SEED");
		issueType.put("108", "Spot Rate Closing");
		issueType.put("109", "Spot Rate Intraday");
		issueType.put("110", "Tracking Stock");
		issueType.put("111", "Trust Certificates");
		issueType.put("112", "Trust Units");
		issueType.put("113", "Portal");
		issueType.put("114", "Contingent Value Right");
		issueType.put("115", "Trust issues Receipts");
		issueType.put("116", "World Currency Option");
		issueType.put("117", "Trust");
		issueType.put("118", "Global Depository Receipt");
		issueType.put("119", "Fund");
		issueType.put("120", "Agency Bond");
		issueType.put("121", "Equity Linked Bond");
		issueType.put("122", "Preferred Trust Securities");
		issueType.put("123", "Index Based Derivatives");
		issueType.put("124", "ETF Portfolio Depositary Receipt");
		issueType.put("125", "Global Shares");
		issueType.put("126", "ETF Index Fund Shares");
		issueType.put("127", "Privately Held Security");

		imbalanceType.put((byte) 0x00, "None");
		imbalanceType.put((byte) 0x01, "Market");
		imbalanceType.put((byte) 0x02, "MOC");
		imbalanceType.put((byte) 0x03, "Regulatory Imbalance");
		imbalanceType.put((byte) 0x04, "Opening Imbalance");
		imbalanceType.put((byte) 0x05, "Closing Imbalance");
		imbalanceType.put((byte) 0x06, "IPO Imbalance");

		luldPriceBandIndicator.put("0", "None");
		luldPriceBandIndicator.put("1", "Opening Update");
		luldPriceBandIndicator.put("2", "Intraday Update");
		luldPriceBandIndicator.put("3", "Re-Stated Value");
		luldPriceBandIndicator.put("4", "Suspended during Trading Halt or Trading Pause");
		luldPriceBandIndicator.put("5", "Re-Opening Update");
		luldPriceBandIndicator.put("6", "Outside Price Band Rule Hours");

		marketTier.put("0", "Unknown");
		marketTier.put("1", "NASDAQ Global Select");
		marketTier.put("2", "NASDAQ Global");
		marketTier.put("3", "NASDAQ Capital");
		marketTier.put("4", "NYSE");
		marketTier.put("5", "NYSE Mkts");
		marketTier.put("6", "NYSE Arca");
		marketTier.put("7", "BATS");
		marketTier.put("8", "OTC Markets - no Tier");
		marketTier.put("9", "OTCQX US Premier");
		marketTier.put("10", "OTCQX US");
		marketTier.put("11", "OTCQX International Premier");
		marketTier.put("12", "OTCQX International");
		marketTier.put("13", "OTCQB");
		marketTier.put("14", "OTCBB");
		marketTier.put("15", "OTC PINK - Current");
		marketTier.put("16", "OTC PINK - Limited");
		marketTier.put("17", "OTC PINK - No Information");
		marketTier.put("18", "OTC Grey Market");
		marketTier.put("19", "OTC Yellow");
		marketTier.put("20", "OTC Bonds");
		marketTier.put("21", "Funds - News Media List");
		marketTier.put("22", "Funds - Supplemental List");
		// Mapping Listing Market with tier
		marketTier.put("XNYS", "NYSE");
		marketTier.put("XASE", "NYSE Mkts");
		marketTier.put("ARCX", "NYSE Arca");
		marketTier.put("BATS", "BATS");

		matchingAlgo.put("1", "FIFO");
		matchingAlgo.put("2", "Split FIFO Pro Rata");
		matchingAlgo.put("3", "Pro Rata");
		matchingAlgo.put("4", "Allocation");
		matchingAlgo.put("5", "FIFO with LMM");
		matchingAlgo.put("6", "Threshold Pro Rata");
		matchingAlgo.put("7", "FIFO with TOP and LMM");
		matchingAlgo.put("8", "Threshold Pro Rata With LMM");
		matchingAlgo.put("9", "Eurodollar Options");

		priceRangeIndicationType.put((byte) 0x01, "Limit-Up Limit-Down Band");
		priceRangeIndicationType.put((byte) 0x02, "Opening Range");
		priceRangeIndicationType.put((byte) 0x03, "Closing Range");
		priceRangeIndicationType.put((byte) 0x04, "Suspension Range");
		priceRangeIndicationType.put((byte) 0x05, "Resumption Range");
		priceRangeIndicationType.put((byte) 0x06, "Allowable Trading Range");
		priceRangeIndicationType.put((byte) 0x07, "Mandatory Opening Range");

		productType.put("0", "Unknown");
		productType.put("1", "Equity");
		productType.put("2", "Option");
		productType.put("3", "Index");
		productType.put("4", "ETP");
		productType.put("5", "ETF");
		productType.put("6", "ETN");
		productType.put("7", "Settlement");
		productType.put("8", "Spot");
		productType.put("9", "Subordinated product");
		productType.put("10", "World Currency");
		productType.put("11", "Alpha Index");
		productType.put("12", "Fixed Income");
		productType.put("13", "Mutual Fund");
		productType.put("14", "Futures Contract");
		productType.put("15", "Futures Option");
		productType.put("16", "Futures Spread");
		productType.put("17", "Local Issue");
		productType.put("18", "Money Market Fund");
		productType.put("19", "Unit Investment Trust (UIT)");
		productType.put("20", "Structured Product");
		productType.put("21", "Annuity");
		productType.put("22", "Alternative Investment Product (AIP)");
		productType.put("23", "Option Root");
		productType.put("24", "Exchange Traded Mutual Fund (ETMF)");

		settlementTime.put("0", "Unknown");
		settlementTime.put("1", "Open");
		settlementTime.put("2", "Close");
		settlementTime.put("3", "Mid-Day");

		statusType.put((byte) 0x01, "Trading");
		statusType.put((byte) 0x02, "Reg SHO");

		statusCodeTrading.put((byte) 0x01, "Trading");
		statusCodeTrading.put((byte) 0x02, "Quoting (Orders are accepted but not executed)");
		statusCodeTrading.put((byte) 0x03, "Halted");
		statusCodeTrading.put((byte) 0x04, "Paused");
		statusCodeTrading.put((byte) 0x05, "No Open No Resume");
		statusCodeTrading.put((byte) 0x06, "Pending Resume");
		statusCodeTrading.put((byte) 0x07, "Authorised Frozen");
		statusCodeTrading.put((byte) 0x08, "Inhibited Frozen");
		statusCodeTrading.put((byte) 0x09, "Authorised Delayed");
		statusCodeTrading.put((byte) 0x0a, "Inhibited Delayed");
		statusCodeTrading.put((byte) 0x0b, "Authorised - Price Movement Extension");
		statusCodeTrading.put((byte) 0x0c, "Workup (eSpeed)");
		statusCodeTrading.put((byte) 0x0d, "Active (GIDS)");
		statusCodeTrading.put((byte) 0x0e, "HELD (GIDS)");
		statusCodeTrading.put((byte) 0x0f, "Pending (GIDS)");
		statusCodeTrading.put((byte) 0x11, "Deleted");
		statusCodeTrading.put((byte) 0x12, "Suspended");
		statusCodeTrading.put((byte) 0x13, "PreOpen");
		statusCodeTrading.put((byte) 0x14, "Closed");
		statusCodeTrading.put((byte) 0x15, "New Price Indication");
		statusCodeTrading.put((byte) 0x16, "PreCross");
		statusCodeTrading.put((byte) 0x17, "Cross");
		statusCodeTrading.put((byte) 0x18, "Post Close");

		statusCodeSHO.put((byte) 0x00, "SHO not in Effect");
		statusCodeSHO.put((byte) 0x01, "SHO in Effect");

		strategy.put("0", "Unknown");
		strategy.put("1", "Tradable");
		strategy.put("2", "Benchmark");
		strategy.put("3", "Composite");
		strategy.put("4", "Sector");
		strategy.put("5", "Growth");
		strategy.put("6", "Value");
		strategy.put("7", "Inverse");
		strategy.put("8", "Leverage");
		strategy.put("9", "Thematic");
		strategy.put("10", "Ethical");
		strategy.put("11", "Sustainable");
		strategy.put("12", "Sharia");
		strategy.put("13", "Social Responsible");
		strategy.put("14", "Stability");
		strategy.put("15", "Fundamental");
		strategy.put("16", "Factor");
		strategy.put("17", "Investment Discipline");
		strategy.put("18", "Three Way");
		strategy.put("19", "Three Way Straddle Vs. Call");
		strategy.put("20", "Three Way Straddle Vs. Put");
		strategy.put("21", "Box");
		strategy.put("22", "Butterfly");
		strategy.put("23", "Xmas Tree");
		strategy.put("24", "Conditional Curve");
		strategy.put("25", "Condor");
		strategy.put("26", "Double");
		strategy.put("27", "Horizontal");
		strategy.put("28", "Horizontal Straddle");
		strategy.put("29", "Iron Condor");
		strategy.put("30", "Ratio 1x2");
		strategy.put("31", "Ratio 1x3");
		strategy.put("32", "Ratio 2x3");
		strategy.put("33", "Risk Reversal");
		strategy.put("34", "Straddle Strip");
		strategy.put("35", "Straddle");
		strategy.put("36", "Strip");
		strategy.put("37", "Vertical");
		strategy.put("38", "Jelly Roll");
		strategy.put("39", "Iron Butterfly");
		strategy.put("40", "Guts");
		strategy.put("41", "Generic");
		strategy.put("42", "Calendar Speed");
		strategy.put("43", "FX Calendar Spread");
		strategy.put("44", "Reduced Tick Calendar Spread");
		strategy.put("45", "Equity Calendar Spread");
		strategy.put("46", "Inter commodity Calendar Spread");
		strategy.put("47", "Pack");
		strategy.put("48", "Month Pack");
		strategy.put("49", "Pack Butterfly");
		strategy.put("50", "Double Butterfly");
		strategy.put("51", "Pack Spread");
		strategy.put("52", "Crack 1 to 1");
		strategy.put("53", "Bundle");
		strategy.put("54", "Bundle Spread");
		strategy.put("55", "Implied Treasury Intercommodity Spread");
		strategy.put("56", "TAS Calendar Spread");
		strategy.put("57", "Commodities Intercommodity Spread");
		strategy.put("58", "Bond Index Spread");
		strategy.put("59", "BMD Futures Strip");
		strategy.put("60", "Spread");
		strategy.put("61", "Buy-Write");
		strategy.put("62", "Combo");
		strategy.put("63", "Invoice Swap");
		strategy.put("64", "Futures Strip Spread");
		strategy.put("65", "Intercommodity Strip Spread");
		strategy.put("66", "Unbalanced Strip Spread");
		strategy.put("67", "Balanced Strip Spread");
		strategy.put("68", "Interest Rate Intercommodity Spread");
		strategy.put("69", "Invoice Swap");

		reasonCodeTrading.put((byte) 0x00, "None Provided");
		reasonCodeTrading.put((byte) 0x01, "News Dissemination");
		reasonCodeTrading.put((byte) 0x02, "News Pending");
		reasonCodeTrading.put((byte) 0x03, "Odrer Influx");
		reasonCodeTrading.put((byte) 0x04, "Order Imbalance");
		reasonCodeTrading.put((byte) 0x05, "Volatility");
		reasonCodeTrading.put((byte) 0x06, "Equipment Changeover");
		reasonCodeTrading.put((byte) 0x07, "Sub-Penny Trading");
		reasonCodeTrading.put((byte) 0x08, "Regulatory-Extraordinary Market Activity");
		reasonCodeTrading.put((byte) 0x09, "ETF");
		reasonCodeTrading.put((byte) 0x0a, "Information Requested");
		reasonCodeTrading.put((byte) 0x0b, "Non-Compliance");
		reasonCodeTrading.put((byte) 0x0c, "Filings Not Current");
		reasonCodeTrading.put((byte) 0x0d, "SEC Trading Suspension");
		reasonCodeTrading.put((byte) 0x0e, "Regulatory Concern");
		reasonCodeTrading.put((byte) 0x0f, "Operations");
		reasonCodeTrading.put((byte) 0x10, "Corporate Action");
		reasonCodeTrading.put((byte) 0x11, "Quote Not Aailable");
		reasonCodeTrading.put((byte) 0x12, "Volatility - Straddle State");
		reasonCodeTrading.put((byte) 0x13, "News and Resumption Times");
		reasonCodeTrading.put((byte) 0x14, "Single Stock Pause");
		reasonCodeTrading.put((byte) 0x15, "Single Stock Pause - Quoting");
		reasonCodeTrading.put((byte) 0x16, "Qualifications Resolved");
		reasonCodeTrading.put((byte) 0x17, "Filings Resolved");
		reasonCodeTrading.put((byte) 0x18, "Issuer News Not Forthcoming");
		reasonCodeTrading.put((byte) 0x19, "Requirements Met");
		reasonCodeTrading.put((byte) 0x1a, "Filings Met");
		reasonCodeTrading.put((byte) 0x1b, "Concluded by Other Authority");
		reasonCodeTrading.put((byte) 0x1c, "New Issue Available");
		reasonCodeTrading.put((byte) 0x1d, "Issue Available");
		reasonCodeTrading.put((byte) 0x1e, "IPO -Not Yet Trading");
		reasonCodeTrading.put((byte) 0x1f, "IPO -Release for Quotation");
		reasonCodeTrading.put((byte) 0x20, "IPO -Window Extension");
		reasonCodeTrading.put((byte) 0x21, "Market Wide Circuit Breaker Level 1");
		reasonCodeTrading.put((byte) 0x22, "Market Wide Circuit Breaker Level 2");
		reasonCodeTrading.put((byte) 0x23, "Market Wide Circuit Breaker Level 3");
		reasonCodeTrading.put((byte) 0x24, "Market Wide Circuit Breaker - Carryover from Prior Day");
		reasonCodeTrading.put((byte) 0x25, "Market Wide Circuit Breaker - Resumption");
		reasonCodeTrading.put((byte) 0x26, "Post-Close");
		reasonCodeTrading.put((byte) 0x27, "Pre-Cross");
		reasonCodeTrading.put((byte) 0x28, "Cross");
		reasonCodeTrading.put((byte) 0x29, "SEC Revocation");
		reasonCodeTrading.put((byte) 0x2a, "Foreign Market Reguatory");
		reasonCodeTrading.put((byte) 0x2b, "Component Derivative of Exchange Listed");
		reasonCodeTrading.put((byte) 0x2c, "Extraordinary Events");
		reasonCodeTrading.put((byte) 0x2d, "Security Deletion");
		reasonCodeTrading.put((byte) 0x2e, "Internal Halt");
		reasonCodeTrading.put((byte) 0x2f, "Was Frozen");
		reasonCodeTrading.put((byte) 0x30, "Was Delayed");
		reasonCodeTrading.put((byte) 0x31, "As-Of Update");
		reasonCodeTrading.put((byte) 0x32, "Related Security - News Dissemination");
		reasonCodeTrading.put((byte) 0x33, "Related Security - News Pending");
		reasonCodeTrading.put((byte) 0x34, "Related Security");
		reasonCodeTrading.put((byte) 0x35, "In View Of Common");
		reasonCodeTrading.put((byte) 0x36, "Exchange Specific");
		reasonCodeTrading.put((byte) 0x37, "Security Not Delayed Or Halted");
		reasonCodeTrading.put((byte) 0x38, "LULD Trading Pause");
		reasonCodeTrading.put((byte) 0x39, "No Open No Resume");
		reasonCodeTrading.put((byte) 0x3a, "Not Available for trading");
		reasonCodeTrading.put((byte) 0x3b, "Unnknown or Invalid");

		reasonCodeSHO.put((byte) 0x00, "Not in effect");
		reasonCodeSHO.put((byte) 0x01, "Day 1 -Activated");
		reasonCodeSHO.put((byte) 0x02, "Day 2 -Continued");

		statusFlagTrading.put((byte) 0x00, "");
		statusFlagTrading.put((byte) 0x01, "Market Wide / Regulatory");

		symbologyType.put((byte) 0x01, "NASDAQ OMX");
		symbologyType.put((byte) 0x02, "CQS");
		symbologyType.put((byte) 0x03, "COMSTOCK");
		symbologyType.put((byte) 0x04, "NYSE");
		symbologyType.put((byte) 0x05, "Toronto Stock Exchange L1");
		symbologyType.put((byte) 0x06, "Toronto Stock Exchange L2");
		symbologyType.put((byte) 0x07, "ETP Estimated Cash Per Creation Unit");
		symbologyType.put((byte) 0x08, "ETP Total Cash Per Creation Unit");
		symbologyType.put((byte) 0x09, "ETP Net Asset Value (NAV)");
		symbologyType.put((byte) 0x0a, "ETP Dividend");
		symbologyType.put((byte) 0x0b, "ETP Shares Outstanding");
		symbologyType.put((byte) 0x0c, "ETP Intraday Portfolio Value (IPV)");
		symbologyType.put((byte) 0x0d, "CME");

		primaryMarketCode.put("XOTC", "BB");
		primaryMarketCode.put("XADF", "D");
		primaryMarketCode.put("XNAS", "T");
		primaryMarketCode.put("ARCX", "P");
		primaryMarketCode.put("XNYS", "N");
		primaryMarketCode.put("BATS", "Z");
		primaryMarketCode.put("EDGX", "K");
		primaryMarketCode.put("EDGA", "J");
		primaryMarketCode.put("BATY", "Y");
		primaryMarketCode.put("XASE", "A");
		primaryMarketCode.put("XBOS", "B");
		primaryMarketCode.put("XPHL", "X");
		primaryMarketCode.put("XCHI", "M");
		primaryMarketCode.put("FINN", "D1");
		primaryMarketCode.put("FINY", "D");
		primaryMarketCode.put("XCIS", "C");
		primaryMarketCode.put("XISE", "I");
		primaryMarketCode.put("IEXG", "V");
		primaryMarketCode.put("CBSX", "W");
		primaryMarketCode.put("XTSE", "TO");
		primaryMarketCode.put("XTSX", "TV");
		primaryMarketCode.put("PSGM", "GM");
		primaryMarketCode.put("PINX", "PK");
		primaryMarketCode.put("OTCB", "QB");
		primaryMarketCode.put("OTCQ", "QX");
	}
}
