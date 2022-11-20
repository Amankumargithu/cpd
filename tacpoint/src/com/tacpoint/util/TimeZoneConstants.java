package com.tacpoint.util;

import java.util.Hashtable;

/**
 *Description of the Class
 *
 * @author     Tacpoint, Inc.
 * @created    December 17, 2001
 */
public final class TimeZoneConstants {

	private static Hashtable mTimeZoneHash = null;

	private static TimeZoneConstants instance = null;


	/**
	 *Constructor for the TimeZoneConstants object
	 *
	 * @since
	 */
	private TimeZoneConstants() {
		initialize();
	}


	/**
	 *Gets the timeZoneHash attribute of the TimeZoneConstants class
	 *
	 * @return    The timeZoneHash value
	 * @since
	 */
	public static Hashtable getTimeZoneHash() {

		if (instance == null) {
			init();
		}

		return mTimeZoneHash;
	}


	/**
	 *Description of the Method
	 *
	 * @since
	 */
	public static synchronized void init() {
		if (instance == null) {
			instance = new TimeZoneConstants();
		}
	}


	/**
	 *Description of the Method
	 *
	 * @since
	 */
	public static synchronized void initialize() {

		mTimeZoneHash = new Hashtable();

		// West Samoa Time
		mTimeZoneHash.put(new Integer(1), "MIT");
		// West Samoa Time
		mTimeZoneHash.put(new Integer(2), "Pacific/Apia");
		// Niue Time
		mTimeZoneHash.put(new Integer(3), "Pacific/Niue");
		// Samoa Standard Time
		mTimeZoneHash.put(new Integer(4), "Pacific/Pago_Pago");
		// Hawaii-Aleutian Standard Time
		mTimeZoneHash.put(new Integer(5), "America/Adak");
		// Hawaii Standard Time
		mTimeZoneHash.put(new Integer(6), "HST");
		// Tokelau Time
		mTimeZoneHash.put(new Integer(7), "Pacific/Fakaofo");
		// Hawaii Standard Time
		mTimeZoneHash.put(new Integer(8), "Pacific/Honolulu");
		// Cook Is. Time
		mTimeZoneHash.put(new Integer(9), "Pacific/Rarotonga");
		// Tahiti Time
		mTimeZoneHash.put(new Integer(10), "Pacific/Tahiti");
		// Marquesas Time
		mTimeZoneHash.put(new Integer(11), "Pacific/Marquesas");
		// Alaska Standard Time
		mTimeZoneHash.put(new Integer(12), "AST");
		// Alaska Standard Time
		mTimeZoneHash.put(new Integer(13), "America/Anchorage");
		// Gambier Time
		mTimeZoneHash.put(new Integer(14), "Pacific/Gambier");
		// Pacific Standard Time
		mTimeZoneHash.put(new Integer(15), "America/Los_Angeles");
		// Pacific Standard Time
		mTimeZoneHash.put(new Integer(16), "America/Tijuana");
		// Pacific Standard Time
		mTimeZoneHash.put(new Integer(17), "America/Vancouver");
		// Pacific Standard Time
		mTimeZoneHash.put(new Integer(18), "PST");
		// Pitcairn Standard Time
		mTimeZoneHash.put(new Integer(19), "Pacific/Pitcairn");
		// Mountain Standard Time
		mTimeZoneHash.put(new Integer(20), "America/Dawson_Creek");
		// Mountain Standard Time
		mTimeZoneHash.put(new Integer(21), "America/Denver");
		// Mountain Standard Time
		mTimeZoneHash.put(new Integer(22), "America/Edmonton");
		// Mountain Standard Time
		mTimeZoneHash.put(new Integer(23), "America/Mazatlan");
		// Mountain Standard Time
		mTimeZoneHash.put(new Integer(24), "America/Phoenix");
		// Mountain Standard Time
		mTimeZoneHash.put(new Integer(25), "MST");
		// Mountain Standard Time
		mTimeZoneHash.put(new Integer(26), "PNT");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(27), "America/Belize");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(28), "America/Chicago");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(29), "America/Costa_Rica");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(30), "America/El_Salvador");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(31), "America/Guatemala");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(32), "America/Managua");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(33), "America/Mexico_City");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(34), "America/Regina");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(35), "America/Tegucigalpa");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(36), "America/Winnipeg");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(37), "CST");
		// Easter Is. Time
		mTimeZoneHash.put(new Integer(38), "Pacific/Easter");
		// Galapagos Time
		mTimeZoneHash.put(new Integer(39), "Pacific/Galapagos");
		// Colombia Time
		mTimeZoneHash.put(new Integer(40), "America/Bogota");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(41), "America/Cayman");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(42), "America/Grand_Turk");
		// Ecuador Time
		mTimeZoneHash.put(new Integer(43), "America/Guayaquil");
		// Central Standard Time
		mTimeZoneHash.put(new Integer(44), "America/Havana");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(45), "America/Indianapolis");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(46), "America/Jamaica");
		// Peru Time
		mTimeZoneHash.put(new Integer(47), "America/Lima");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(48), "America/Montreal");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(49), "America/Nassau");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(50), "America/New_York");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(51), "America/Panama");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(52), "America/Port-au-Prince");
		// Acre Time
		mTimeZoneHash.put(new Integer(53), "America/Porto_Acre");
		// GMT-05:00
		mTimeZoneHash.put(new Integer(54), "America/Rio_Branco");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(55), "EST");
		// Eastern Standard Time
		mTimeZoneHash.put(new Integer(56), "IET");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(57), "America/Anguilla");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(58), "America/Antigua");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(59), "America/Aruba");
		// Paraguay Time
		mTimeZoneHash.put(new Integer(60), "America/Asuncion");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(61), "America/Barbados");
		// Venezuela Time
		mTimeZoneHash.put(new Integer(62), "America/Caracas");
		// Amazon Standard Time
		mTimeZoneHash.put(new Integer(63), "America/Cuiaba");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(64), "America/Curacao");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(65), "America/Dominica");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(66), "America/Grenada");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(67), "America/Guadeloupe");
		// Guyana Time
		mTimeZoneHash.put(new Integer(68), "America/Guyana");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(69), "America/Halifax");
		// Bolivia Time
		mTimeZoneHash.put(new Integer(70), "America/La_Paz");
		// Amazon Standard Time
		mTimeZoneHash.put(new Integer(71), "America/Manaus");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(72), "America/Martinique");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(73), "America/Montserrat");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(74), "America/Port_of_Spain");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(75), "America/Puerto_Rico");
		// Chile Time
		mTimeZoneHash.put(new Integer(76), "America/Santiago");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(77), "America/Santo_Domingo");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(78), "America/St_Kitts");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(79), "America/St_Lucia");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(80), "America/St_Thomas");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(81), "America/St_Vincent");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(82), "America/Thule");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(83), "America/Tortola");
		// Chile Time
		mTimeZoneHash.put(new Integer(84), "Antarctica/Palmer");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(85), "Atlantic/Bermuda");
		// Falkland Is. Time
		mTimeZoneHash.put(new Integer(86), "Atlantic/Stanley");
		// Atlantic Standard Time
		mTimeZoneHash.put(new Integer(87), "PRT");
		// Newfoundland Standard Time
		mTimeZoneHash.put(new Integer(88), "America/St_Johns");
		// Newfoundland Standard Time
		mTimeZoneHash.put(new Integer(89), "CNT");
		// Argentine Time
		mTimeZoneHash.put(new Integer(90), "AGT");
		// Argentine Time
		mTimeZoneHash.put(new Integer(91), "America/Buenos_Aires");
		// French Guiana Time
		mTimeZoneHash.put(new Integer(92), "America/Cayenne");
		// Brazil Time
		mTimeZoneHash.put(new Integer(93), "America/Fortaleza");
		// Western Greenland Time
		mTimeZoneHash.put(new Integer(94), "America/Godthab");
		// Pierre & Miquelon Standard Time
		mTimeZoneHash.put(new Integer(95), "America/Miquelon");
		// Uruguay Time
		mTimeZoneHash.put(new Integer(96), "America/Montevideo");
		// Suriname Time
		mTimeZoneHash.put(new Integer(97), "America/Paramaribo");
		// Brazil Time
		mTimeZoneHash.put(new Integer(98), "America/Sao_Paulo");
		// Brazil Time
		mTimeZoneHash.put(new Integer(99), "BET");
		// Fernando de Noronha Time
		mTimeZoneHash.put(new Integer(100), "America/Noronha");
		// South Georgia Standard Time
		mTimeZoneHash.put(new Integer(101), "Atlantic/South_Georgia");
		// Eastern Greenland Time
		mTimeZoneHash.put(new Integer(102), "America/Scoresbysund");
		// Azores Time
		mTimeZoneHash.put(new Integer(103), "Atlantic/Azores");
		// Cape Verde Time
		mTimeZoneHash.put(new Integer(104), "Atlantic/Cape_Verde");
		// Eastern Greenland Time
		mTimeZoneHash.put(new Integer(105), "Atlantic/Jan_Mayen");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(106), "Africa/Abidjan");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(107), "Africa/Accra");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(108), "Africa/Banjul");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(109), "Africa/Bissau");
		// Western European Time
		mTimeZoneHash.put(new Integer(110), "Africa/Casablanca");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(111), "Africa/Conakry");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(112), "Africa/Dakar");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(113), "Africa/Freetown");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(114), "Africa/Lome");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(115), "Africa/Monrovia");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(116), "Africa/Nouakchott");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(117), "Africa/Ouagadougou");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(118), "Africa/Sao_Tome");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(119), "Africa/Timbuktu");
		// Western European Time
		mTimeZoneHash.put(new Integer(120), "Atlantic/Canary");
		// Western European Time
		mTimeZoneHash.put(new Integer(121), "Atlantic/Faeroe");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(122), "Atlantic/Reykjavik");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(123), "Atlantic/St_Helena");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(124), "Europe/Dublin");
		// Western European Time
		mTimeZoneHash.put(new Integer(125), "Europe/Lisbon");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(126), "Europe/London");
		// Greenwich Mean Time
		mTimeZoneHash.put(new Integer(127), "GMT");
		// Coordinated Universal Time
		mTimeZoneHash.put(new Integer(128), "UTC");
		// Western European Time
		mTimeZoneHash.put(new Integer(129), "WET");
		// Central European Time
		mTimeZoneHash.put(new Integer(130), "Africa/Algiers");
		// Western African Time
		mTimeZoneHash.put(new Integer(131), "Africa/Bangui");
		// Western African Time
		mTimeZoneHash.put(new Integer(132), "Africa/Douala");
		// Western African Time
		mTimeZoneHash.put(new Integer(133), "Africa/Kinshasa");
		// Western African Time
		mTimeZoneHash.put(new Integer(134), "Africa/Lagos");
		// Western African Time
		mTimeZoneHash.put(new Integer(135), "Africa/Libreville");
		// Western African Time
		mTimeZoneHash.put(new Integer(136), "Africa/Luanda");
		// Western African Time
		mTimeZoneHash.put(new Integer(137), "Africa/Malabo");
		// Western African Time
		mTimeZoneHash.put(new Integer(138), "Africa/Ndjamena");
		// Western African Time
		mTimeZoneHash.put(new Integer(139), "Africa/Niamey");
		// Western African Time
		mTimeZoneHash.put(new Integer(140), "Africa/Porto-Novo");
		// Central European Time
		mTimeZoneHash.put(new Integer(141), "Africa/Tunis");
		// Western African Time
		mTimeZoneHash.put(new Integer(142), "Africa/Windhoek");
		// Central European Time
		mTimeZoneHash.put(new Integer(143), "ECT");
		// Central European Time
		mTimeZoneHash.put(new Integer(144), "Europe/Amsterdam");
		// Central European Time
		mTimeZoneHash.put(new Integer(145), "Europe/Andorra");
		// Central European Time
		mTimeZoneHash.put(new Integer(146), "Europe/Belgrade");
		// Central European Time
		mTimeZoneHash.put(new Integer(147), "Europe/Berlin");
		// Central European Time
		mTimeZoneHash.put(new Integer(148), "Europe/Brussels");
		// Central European Time
		mTimeZoneHash.put(new Integer(149), "Europe/Budapest");
		// Central European Time
		mTimeZoneHash.put(new Integer(150), "Europe/Copenhagen");
		// Central European Time
		mTimeZoneHash.put(new Integer(151), "Europe/Gibraltar");
		// Central European Time
		mTimeZoneHash.put(new Integer(152), "Europe/Luxembourg");
		// Central European Time
		mTimeZoneHash.put(new Integer(153), "Europe/Madrid");
		// Central European Time
		mTimeZoneHash.put(new Integer(154), "Europe/Malta");
		// Central European Time
		mTimeZoneHash.put(new Integer(155), "Europe/Monaco");
		// Central European Time
		mTimeZoneHash.put(new Integer(156), "Europe/Oslo");
		// Central European Time
		mTimeZoneHash.put(new Integer(157), "Europe/Paris");
		// Central European Time
		mTimeZoneHash.put(new Integer(158), "Europe/Prague");
		// Central European Time
		mTimeZoneHash.put(new Integer(159), "Europe/Rome");
		// Central European Time
		mTimeZoneHash.put(new Integer(160), "Europe/Stockholm");
		// Central European Time
		mTimeZoneHash.put(new Integer(161), "Europe/Tirane");
		// Central European Time
		mTimeZoneHash.put(new Integer(162), "Europe/Vaduz");
		// Central European Time
		mTimeZoneHash.put(new Integer(163), "Europe/Vienna");
		// Central European Time
		mTimeZoneHash.put(new Integer(164), "Europe/Warsaw");
		// Central European Time
		mTimeZoneHash.put(new Integer(165), "Europe/Zurich");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(166), "ART");
		// Central African Time
		mTimeZoneHash.put(new Integer(167), "Africa/Blantyre");
		// Central African Time
		mTimeZoneHash.put(new Integer(168), "Africa/Bujumbura");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(169), "Africa/Cairo");
		// Central African Time
		mTimeZoneHash.put(new Integer(170), "Africa/Gaborone");
		// Central African Time
		mTimeZoneHash.put(new Integer(171), "Africa/Harare");
		// South Africa Standard Time
		mTimeZoneHash.put(new Integer(172), "Africa/Johannesburg");
		// Central African Time
		mTimeZoneHash.put(new Integer(173), "Africa/Kigali");
		// Central African Time
		mTimeZoneHash.put(new Integer(174), "Africa/Lubumbashi");
		// Central African Time
		mTimeZoneHash.put(new Integer(175), "Africa/Lusaka");
		// Central African Time
		mTimeZoneHash.put(new Integer(176), "Africa/Maputo");
		// South Africa Standard Time
		mTimeZoneHash.put(new Integer(177), "Africa/Maseru");
		// South Africa Standard Time
		mTimeZoneHash.put(new Integer(178), "Africa/Mbabane");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(179), "Africa/Tripoli");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(180), "Asia/Amman");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(181), "Asia/Beirut");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(182), "Asia/Damascus");
		// Israel Standard Time
		mTimeZoneHash.put(new Integer(183), "Asia/Jerusalem");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(184), "Asia/Nicosia");
		// Central African Time
		mTimeZoneHash.put(new Integer(185), "CAT");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(186), "EET");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(187), "Europe/Athens");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(188), "Europe/Bucharest");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(189), "Europe/Chisinau");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(190), "Europe/Helsinki");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(191), "Europe/Istanbul");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(192), "Europe/Kaliningrad");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(193), "Europe/Kiev");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(194), "Europe/Minsk");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(195), "Europe/Riga");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(196), "Europe/Simferopol");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(197), "Europe/Sofia");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(198), "Europe/Tallinn");
		// Eastern European Time
		mTimeZoneHash.put(new Integer(199), "Europe/Vilnius");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(200), "Africa/Addis_Ababa");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(201), "Africa/Asmera");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(202), "Africa/Dar_es_Salaam");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(203), "Africa/Djibouti");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(204), "Africa/Kampala");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(205), "Africa/Khartoum");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(206), "Africa/Mogadishu");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(207), "Africa/Nairobi");
		// Arabia Standard Time
		mTimeZoneHash.put(new Integer(208), "Asia/Aden");
		// Arabia Standard Time
		mTimeZoneHash.put(new Integer(209), "Asia/Baghdad");
		// Arabia Standard Time
		mTimeZoneHash.put(new Integer(210), "Asia/Bahrain");
		// Arabia Standard Time
		mTimeZoneHash.put(new Integer(211), "Asia/Kuwait");
		// Arabia Standard Time
		mTimeZoneHash.put(new Integer(212), "Asia/Qatar");
		// Arabia Standard Time
		mTimeZoneHash.put(new Integer(213), "Asia/Riyadh");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(214), "EAT");
		// Moscow Standard Time
		mTimeZoneHash.put(new Integer(215), "Europe/Moscow");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(216), "Indian/Antananarivo");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(217), "Indian/Comoro");
		// Eastern African Time
		mTimeZoneHash.put(new Integer(218), "Indian/Mayotte");
		// Iran Time
		mTimeZoneHash.put(new Integer(219), "Asia/Tehran");
		// Iran Time
		mTimeZoneHash.put(new Integer(220), "MET");
		// Aqtau Time
		mTimeZoneHash.put(new Integer(221), "Asia/Aqtau");
		// Azerbaijan Time
		mTimeZoneHash.put(new Integer(222), "Asia/Baku");
		// Gulf Standard Time
		mTimeZoneHash.put(new Integer(223), "Asia/Dubai");
		// Gulf Standard Time
		mTimeZoneHash.put(new Integer(224), "Asia/Muscat");
		// Georgia Time
		mTimeZoneHash.put(new Integer(225), "Asia/Tbilisi");
		// Armenia Time
		mTimeZoneHash.put(new Integer(226), "Asia/Yerevan");
		// Samara Time
		mTimeZoneHash.put(new Integer(227), "Europe/Samara");
		// Seychelles Time
		mTimeZoneHash.put(new Integer(228), "Indian/Mahe");
		// Mauritius Time
		mTimeZoneHash.put(new Integer(229), "Indian/Mauritius");
		// Reunion Time
		mTimeZoneHash.put(new Integer(230), "Indian/Reunion");
		// Armenia Time
		mTimeZoneHash.put(new Integer(231), "NET");
		// Afghanistan Time
		mTimeZoneHash.put(new Integer(232), "Asia/Kabul");
		// Aqtobe Time
		mTimeZoneHash.put(new Integer(233), "Asia/Aqtobe");
		// Turkmenistan Time
		mTimeZoneHash.put(new Integer(234), "Asia/Ashgabat");
		// Turkmenistan Time
		mTimeZoneHash.put(new Integer(235), "Asia/Ashkhabad");
		// Kirgizstan Time
		mTimeZoneHash.put(new Integer(236), "Asia/Bishkek");
		// Tajikistan Time
		mTimeZoneHash.put(new Integer(237), "Asia/Dushanbe");
		// Pakistan Time
		mTimeZoneHash.put(new Integer(238), "Asia/Karachi");
		// Uzbekistan Time
		mTimeZoneHash.put(new Integer(239), "Asia/Tashkent");
		// Yekaterinburg Time
		mTimeZoneHash.put(new Integer(240), "Asia/Yekaterinburg");
		// Indian Ocean Territory Time
		mTimeZoneHash.put(new Integer(241), "Indian/Chagos");
		// French Southern & Antarctic Lands Time
		mTimeZoneHash.put(new Integer(242), "Indian/Kerguelen");
		// Maldives Time
		mTimeZoneHash.put(new Integer(243), "Indian/Maldives");
		// Pakistan Time
		mTimeZoneHash.put(new Integer(244), "PLT");
		// India Standard Time
		mTimeZoneHash.put(new Integer(245), "Asia/Calcutta");
		// India Standard Time
		mTimeZoneHash.put(new Integer(246), "IST");
		// Nepal Time
		mTimeZoneHash.put(new Integer(247), "Asia/Katmandu");
		// Mawson Time
		mTimeZoneHash.put(new Integer(248), "Antarctica/Mawson");
		// Alma-Ata Time
		mTimeZoneHash.put(new Integer(249), "Asia/Almaty");
		// Sri Lanka Time
		mTimeZoneHash.put(new Integer(250), "Asia/Colombo");
		// Bangladesh Time
		mTimeZoneHash.put(new Integer(251), "Asia/Dacca");
		// Bangladesh Time
		mTimeZoneHash.put(new Integer(252), "Asia/Dhaka");
		// Novosibirsk Time
		mTimeZoneHash.put(new Integer(253), "Asia/Novosibirsk");
		// Bhutan Time
		mTimeZoneHash.put(new Integer(254), "Asia/Thimbu");
		// Bhutan Time
		mTimeZoneHash.put(new Integer(255), "Asia/Thimphu");
		// Bangladesh Time
		mTimeZoneHash.put(new Integer(256), "BST");
		// Myanmar Time
		mTimeZoneHash.put(new Integer(257), "Asia/Rangoon");
		// Cocos Islands Time
		mTimeZoneHash.put(new Integer(258), "Indian/Cocos");
		// Indochina Time
		mTimeZoneHash.put(new Integer(259), "Asia/Bangkok");
		// Java Time
		mTimeZoneHash.put(new Integer(260), "Asia/Jakarta");
		// Krasnoyarsk Time
		mTimeZoneHash.put(new Integer(261), "Asia/Krasnoyarsk");
		// Indochina Time
		mTimeZoneHash.put(new Integer(262), "Asia/Phnom_Penh");
		// Indochina Time
		mTimeZoneHash.put(new Integer(263), "Asia/Saigon");
		// Indochina Time
		mTimeZoneHash.put(new Integer(264), "Asia/Vientiane");
		// Christmas Island Time
		mTimeZoneHash.put(new Integer(265), "Indian/Christmas");
		// Indochina Time
		mTimeZoneHash.put(new Integer(266), "VST");
		// Western Standard Time (Australia)
		mTimeZoneHash.put(new Integer(267), "Antarctica/Casey");
		// Brunei Time
		mTimeZoneHash.put(new Integer(268), "Asia/Brunei");
		// Hong Kong Time
		mTimeZoneHash.put(new Integer(269), "Asia/Hong_Kong");
		// Irkutsk Time
		mTimeZoneHash.put(new Integer(270), "Asia/Irkutsk");
		// Malaysia Time
		mTimeZoneHash.put(new Integer(271), "Asia/Kuala_Lumpur");
		// China Standard Time
		mTimeZoneHash.put(new Integer(272), "Asia/Macao");
		// Philippines Time
		mTimeZoneHash.put(new Integer(273), "Asia/Manila");
		// China Standard Time
		mTimeZoneHash.put(new Integer(274), "Asia/Shanghai");
		// Singapore Time
		mTimeZoneHash.put(new Integer(275), "Asia/Singapore");
		// China Standard Time
		mTimeZoneHash.put(new Integer(276), "Asia/Taipei");
		// Borneo Time
		mTimeZoneHash.put(new Integer(277), "Asia/Ujung_Pandang");
		// Ulaanbaatar Time
		mTimeZoneHash.put(new Integer(278), "Asia/Ulaanbaatar");
		// Ulaanbaatar Time
		mTimeZoneHash.put(new Integer(279), "Asia/Ulan_Bator");
		// Western Standard Time (Australia)
		mTimeZoneHash.put(new Integer(280), "Australia/Perth");
		// China Standard Time
		mTimeZoneHash.put(new Integer(281), "CTT");
		// Jayapura Time
		mTimeZoneHash.put(new Integer(282), "Asia/Jayapura");
		// Korea Standard Time
		mTimeZoneHash.put(new Integer(283), "Asia/Pyongyang");
		// Korea Standard Time
		mTimeZoneHash.put(new Integer(284), "Asia/Seoul");
		// Japan Standard Time
		mTimeZoneHash.put(new Integer(285), "Asia/Tokyo");
		// Yakutsk Time
		mTimeZoneHash.put(new Integer(286), "Asia/Yakutsk");
		// Japan Standard Time
		mTimeZoneHash.put(new Integer(287), "JST");
		// Palau Time
		mTimeZoneHash.put(new Integer(288), "Pacific/Palau");
		// Central Standard Time (Northern Territory)
		mTimeZoneHash.put(new Integer(289), "ACT");
		// Central Standard Time (South Australia)
		mTimeZoneHash.put(new Integer(290), "Australia/Adelaide");
		// Central Standard Time (South Australia/New South Wales)
		mTimeZoneHash.put(new Integer(291), "Australia/Broken_Hill");
		// Central Standard Time (Northern Territory)
		mTimeZoneHash.put(new Integer(292), "Australia/Darwin");
		// Eastern Standard Time (New South Wales)
		mTimeZoneHash.put(new Integer(293), "AET");
		// Dumont-d'Urville Time
		mTimeZoneHash.put(new Integer(294), "Antarctica/DumontDUrville");
		// Vladivostok Time
		mTimeZoneHash.put(new Integer(295), "Asia/Vladivostok");
		// Eastern Standard Time (Queensland)
		mTimeZoneHash.put(new Integer(296), "Australia/Brisbane");
		// Eastern Standard Time (Tasmania)
		mTimeZoneHash.put(new Integer(297), "Australia/Hobart");
		// Eastern Standard Time (New South Wales)
		mTimeZoneHash.put(new Integer(298), "Australia/Sydney");
		// Chamorro Standard Time
		mTimeZoneHash.put(new Integer(299), "Pacific/Guam");
		// Papua New Guinea Time
		mTimeZoneHash.put(new Integer(300), "Pacific/Port_Moresby");
		// Chamorro Standard Time
		mTimeZoneHash.put(new Integer(301), "Pacific/Saipan");
		// Truk Time
		mTimeZoneHash.put(new Integer(302), "Pacific/Truk");
		// Load Howe Standard Time
		mTimeZoneHash.put(new Integer(303), "Australia/Lord_Howe");
		// Magadan Time
		mTimeZoneHash.put(new Integer(304), "Asia/Magadan");
		// Vanuatu Time
		mTimeZoneHash.put(new Integer(305), "Pacific/Efate");
		// Solomon Is. Time
		mTimeZoneHash.put(new Integer(306), "Pacific/Guadalcanal");
		// Kosrae Time
		mTimeZoneHash.put(new Integer(307), "Pacific/Kosrae");
		// New Caledonia Time
		mTimeZoneHash.put(new Integer(308), "Pacific/Noumea");
		// Ponape Time
		mTimeZoneHash.put(new Integer(309), "Pacific/Ponape");
		// Solomon Is. Time
		mTimeZoneHash.put(new Integer(310), "SST");
		// Norfolk Time
		mTimeZoneHash.put(new Integer(311), "Pacific/Norfolk");
		// New Zealand Standard Time
		mTimeZoneHash.put(new Integer(312), "Antarctica/McMurdo");
		// Anadyr Time
		mTimeZoneHash.put(new Integer(313), "Asia/Anadyr");
		// Petropavlovsk-Kamchatski Time
		mTimeZoneHash.put(new Integer(314), "Asia/Kamchatka");
		// New Zealand Standard Time
		mTimeZoneHash.put(new Integer(315), "NST");
		// New Zealand Standard Time
		mTimeZoneHash.put(new Integer(316), "Pacific/Auckland");
		// Fiji Time
		mTimeZoneHash.put(new Integer(317), "Pacific/Fiji");
		// Tuvalu Time
		mTimeZoneHash.put(new Integer(318), "Pacific/Funafuti");
		// Marshall Islands Time
		mTimeZoneHash.put(new Integer(319), "Pacific/Majuro");
		// Nauru Time
		mTimeZoneHash.put(new Integer(320), "Pacific/Nauru");
		// Gilbert Is. Time
		mTimeZoneHash.put(new Integer(321), "Pacific/Tarawa");
		// Wake Time
		mTimeZoneHash.put(new Integer(322), "Pacific/Wake");
		// Wallis & Futuna Time
		mTimeZoneHash.put(new Integer(323), "Pacific/Wallis");
		// Chatham Standard Time
		mTimeZoneHash.put(new Integer(324), "Pacific/Chatham");
		// Phoenix Is. Time
		mTimeZoneHash.put(new Integer(325), "Pacific/Enderbury");
		// Tonga Time
		mTimeZoneHash.put(new Integer(326), "Pacific/Tongatapu");
		// Line Is. Time
		mTimeZoneHash.put(new Integer(327), "Pacific/Kiritimati");
	}

}

