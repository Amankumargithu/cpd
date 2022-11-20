package com.quodd.util;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;

public interface ConditionCodeGenerator {
	static final Logger logger = QuoddLogger.getInstance().getLogger();

	// Unmapped - 10,22,50,51
	public static Set<String> generateUtpConditionCode(String upstreamCode, String ticker, long tradeId, String name,
			long timestamp) {
		Set<String> codeList = new HashSet<>();
		if (upstreamCode != null) {
			char[] charArr = upstreamCode.toCharArray();
			if (charArr.length == 4) {
				switch (charArr[0]) {
				case '@':
					codeList.add("0");
					break;
				case 'C':
					codeList.add("3");
					break;
				case 'N':
					codeList.add("14");
					break;
				case 'R':
					codeList.add("18");
					break;
				case 'Y':
					codeList.add("25");
					break;
				default:
					logger.warning(name + " Upstream code 0 not mapped " + charArr[0] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				codeList.add("R6");
				switch (charArr[1]) {
				case 'F':
					codeList.add("6");
					break;
				case 'O':
					codeList.add("15");
					break;
				case '4':
					codeList.add("52");
					break;
				case '5':
					codeList.add("53");
					break;
				case '6':
					codeList.add("54");
					break;
				case '7':
//					codeList.add("25");
					break;
//				case '8':
//					codeList.add("R6");
//					break;
				case '9':
					codeList.add("9");
					break;
				case ' ':
					codeList.remove("R6");
					break;
				default:
					logger.warning(name + " Upstream code 1 not mapped " + charArr[1] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				switch (charArr[2]) {
				case 'T':
					codeList.add("20");
					break;
				case 'L':
					codeList.add("12");
					break;
				case 'Z':
					codeList.add("26");
					break;
				case 'U':
					codeList.add("21");
					break;
				case ' ':
					break;
				default:
					logger.warning(name + " Upstream code 2 not mapped " + charArr[2] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				switch (charArr[3]) {
				case '1':
					codeList.add("49");
					break;
				case 'A':
					codeList.add("1");
					break;
				case 'B':
					codeList.add("2");
					break;
				case 'D':
					codeList.add("4");
					break;
				case 'E':
					codeList.add("5");
					break;
				case 'G':
					codeList.add("7");
					break;
				case 'H':
					codeList.add("8");
					break;
				case 'I':
					codeList.add("27");
					break;
				case 'K':
					codeList.add("11");
					break;
				case 'M':
					codeList.add("13");
					codeList.add("54");
					break;
				case 'P':
					codeList.add("16");
					break;
				case 'Q':
					codeList.add("17");
					codeList.add("15");
					break;
				case 'S':
					codeList.add("19");
					break;
				case 'V':
//					codeList.add("25");
					break;
				case 'W':
					codeList.add("23");
					break;
				case 'X':
					codeList.add("24");
					break;
				case ' ':
					break;
				default:
					logger.warning(name + " Upstream code 3 not mapped " + charArr[3] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}

			} else {
				logger.warning(
						name + " ustream code not 4 " + upstreamCode + "," + ticker + "," + tradeId + "," + timestamp);
			}
		} else {
			logger.warning(name + " Null Upstream code" + "," + ticker + "," + tradeId + "," + timestamp);
		}
		return codeList;
	}

	// 8,9,22
	public static Set<String> generateCtaaConditionCode(String upstreamCode, String ticker, long tradeId, String name,
			long timestamp) {
		Set<String> codeList = new HashSet<>();
		if (upstreamCode != null) {
			char[] charArr = upstreamCode.toCharArray();
			if (charArr.length == 4) {
				switch (charArr[0]) {
				case ' ':
					codeList.add("0");
					break;
				case 'C':
					codeList.add("3");
					break;
				case 'N':
					codeList.add("14");
					break;
				case 'R':
					codeList.add("18");
					break;
				default:
					logger.warning(name + " Upstream code 0 not mapped " + charArr[0] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				codeList.add("R6");
				switch (charArr[1]) {
				case 'F':
					codeList.add("6");
					break;
				case 'O':
					codeList.add("15");
					break;
				case '4':
					codeList.add("52");
					break;
				case '5':
					codeList.add("53");
					break;
				case '6':
					codeList.add("54");
					break;
				case '7':
//					codeList.add("25");
					break;
//				case '8':
//					break;
				case '9':
					codeList.add("9");
					break;
				case ' ':
					codeList.remove("R6");
					break;
				default:
					logger.warning(name + " Upstream code 1 not mapped " + charArr[1] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				switch (charArr[2]) {
				case 'T':
					codeList.add("20");
					break;
				case 'L':
					codeList.add("12");
					break;
				case 'Z':
					codeList.add("26");
					break;
				case 'U':
					codeList.add("21");
					break;
				case ' ':
					break;
				default:
					logger.warning(name + " Upstream code 2 not mapped " + charArr[2] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				switch (charArr[3]) {
				case 'B':
					codeList.add("2");
					break;
				case 'E':
					codeList.add("5");
					break;
				case 'H':
					codeList.add("8");
					break;
				case 'I':
					codeList.add("27");
					break;
				case 'K':
					codeList.add("10");
					break;
				case 'M':
					codeList.add("13");
					codeList.add("54");
					break;
				case 'P':
					codeList.add("16");
					break;
				case 'Q':
					codeList.add("17");
					codeList.add("15");
					break;
				case 'V':
//					codeList.add("25");
					break;
				case 'X':
					codeList.add("24");
					break;
				case ' ':
					break;
				default:
					logger.warning(name + " Upstream code 3 not mapped " + charArr[3] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}

			} else {
				logger.warning(
						name + " ustream code not 4 " + upstreamCode + "," + ticker + "," + tradeId + "," + timestamp);
			}
		} else {
			codeList.add("0");
		}
		return codeList;
	}

	// 8,9,22
	public static Set<String> generateCtabConditionCode(String upstreamCode, String ticker, long tradeId, String name,
			long timestamp) {
		Set<String> codeList = new HashSet<>();
		if (upstreamCode != null) {
			char[] charArr = upstreamCode.toCharArray();
			if (charArr.length == 4) {
				switch (charArr[0]) {
				case ' ':
					codeList.add("0");
					break;
				case 'C':
					codeList.add("3");
					break;
				case 'N':
					codeList.add("14");
					break;
				case 'R':
					codeList.add("18");
					break;
				default:
					logger.warning(name + " Upstream code 0 not mapped " + charArr[0] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				codeList.add("R6");
				switch (charArr[1]) {
				case 'F':
					codeList.add("6");
					break;
				case 'O':
					codeList.add("15");
					break;
				case '4':
					codeList.add("52");
					break;
				case '5':
					codeList.add("53");
					break;
				case '6':
					codeList.add("54");
					break;
					case '7':
//						codeList.add("25");
						break;
//					case '8':
//						break;
					case '9':
						codeList.add("9");
						break;
				case ' ':
					codeList.remove("R6");
					break;
				default:
					logger.warning(name + " Upstream code 1 not mapped " + charArr[1] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				switch (charArr[2]) {
				case 'T':
					codeList.add("20");
					break;
				case 'L':
					codeList.add("12");
					break;
				case 'Z':
					codeList.add("26");
					break;
				case 'U':
					codeList.add("21");
					break;
				case ' ':
					break;
				default:
					logger.warning(name + " Upstream code 2 not mapped " + charArr[2] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}
				switch (charArr[3]) {
				case 'B':
					codeList.add("2");
					break;
				case 'E':
					codeList.add("5");
					break;
				case 'H':
					codeList.add("8");
					break;
				case 'I':
					codeList.add("27");
					break;
				case 'K':
					codeList.add("11");
					break;
				case 'M':
					codeList.add("13");
					codeList.add("54");
					break;
				case 'P':
					codeList.add("16");
					break;
				case 'Q':
					codeList.add("17");
					codeList.add("15");
					break;
					case 'V':
//						codeList.add("25");
						break;
				case 'X':
					codeList.add("24");
					break;
				case ' ':
					break;
				default:
					logger.warning(name + " Upstream code 3 not mapped " + charArr[3] + "," + ticker + "," + tradeId
							+ "," + timestamp);
					break;
				}

			} else {
				logger.warning(
						name + " ustream code not 4 " + upstreamCode + "," + ticker + "," + tradeId + "," + timestamp);
			}
		} else {
			codeList.add("0");
		}
		return codeList;
	}

}
