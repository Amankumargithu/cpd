package com.quodd.cpd;

import static com.quodd.util.JsonUtil.statusToJson;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quodd.cache.AlertCache;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.Constants;
import com.quodd.common.util.QuoddProperty;
import com.quodd.controller.AlertController;
import com.quodd.controller.HistoricalAlertController;
import com.quodd.controller.SystemAlertController;
import com.quodd.controller.UserController;
import com.quodd.dao.AlertCommentsDao;
import com.quodd.dao.AlertDao;
import com.quodd.dao.HistoricalAlertDao;
import com.quodd.dao.SystemAlertDao;
import com.quodd.db.DatabaseManager;
import com.quodd.db.NewsDatabaseManager;
import com.quodd.exception.QuoddException;
import com.quodd.task.AlertCalendar;
import com.quodd.task.AlertDataProcessor;
import com.quodd.task.DatafeedProcessor;
import com.quodd.task.FiredAlertsProcessor;
import com.quodd.task.FundamentalProcessor;
import com.quodd.task.MessageConsumer;
import com.quodd.util.Authenticator;

public class AlertCpd {
	public static final Logger logger = QuoddLogger.getInstance("alertCpd").getLogger();
	public static final QuoddProperty dbProperties = new QuoddProperty("/db.properties");
	public static final QuoddProperty environmentProperties = new QuoddProperty("/environment.properties");
	public static final Gson gson = new Gson();
	public static final Type hashTypeToken = new TypeToken<HashMap<String, Object>>() {
	}.getType();
	public static final AlertCache alertCache = new AlertCache();
	public static final DatabaseManager dbManager = new DatabaseManager();
	public static final AlertDao alertDao = new AlertDao();
	public static final HistoricalAlertDao historicalAlertDao = new HistoricalAlertDao();
	public static final AlertCommentsDao alertCommentDao = new AlertCommentsDao();
	public static final SystemAlertDao systemAlertDao = new SystemAlertDao();
	public static final NewsDatabaseManager newsDatabaseManager = new NewsDatabaseManager();
	public static boolean doProcessStreamingAlerts = false;
	private static final Integer MAX_HELPER_THREAD = 12;
	public static final AlertDataProcessor[] alertDataProcessorArray = new AlertDataProcessor[MAX_HELPER_THREAD];
	private final Thread[] helperThreads = new Thread[MAX_HELPER_THREAD];
	private static final AlertCalendar alertCalendar = new AlertCalendar();
	public static final FiredAlertsProcessor firedProcessor = new FiredAlertsProcessor();
	public static final DatafeedProcessor datafeedProcessor = new DatafeedProcessor();
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private final MessageConsumer consumer = new MessageConsumer();

	public AlertCpd() {
		Thread loadingThread = new Thread(() -> {
			alertCache.loadUserEmailMap();
			alertDao.loadAllActiveUserAlerts();
			systemAlertDao.loadActiveSystemAlert();
		}, "LoadingThread");
		loadingThread.start();
		for (int i = 0; i < MAX_HELPER_THREAD; i++) {
			AlertDataProcessor amh = new AlertDataProcessor(String.valueOf(i));
			Thread t = new Thread(amh, "DataProcessor-" + i);
			helperThreads[i] = t;
			alertDataProcessorArray[i] = amh;
			t.start();
		}
		Thread fundamentalDataThread = new Thread(new FundamentalProcessor(), "fundamentalDataThread");
		fundamentalDataThread.start();
		Thread firedProcessorThread = new Thread(firedProcessor, "FiredAlertThread");
		firedProcessorThread.start();
		Thread datafeedThread = new Thread(datafeedProcessor, "StreamThread");
		datafeedThread.start();
		consumer.connectToIbus(environmentProperties.getProperty("JMS_QUOTES_PRIMARY_IP"),
				environmentProperties.getProperty("JMS_QUOTES_TOPIC_NAME"),
				environmentProperties.getProperty("JMS_QUOTES_QOS"), "Alert Data");
		this.service.scheduleWithFixedDelay(alertCalendar::computeProcess, 30, 30, TimeUnit.SECONDS);
		this.service.scheduleWithFixedDelay(alertCalendar::processTimeAlert, 10, 10, TimeUnit.SECONDS);
		this.service.scheduleWithFixedDelay(consumer::printIbusStat, 1, 1, TimeUnit.MINUTES);
		this.service.scheduleWithFixedDelay(systemAlertDao::loadActiveSystemAlert, 5, 5, TimeUnit.MINUTES);
		this.service.scheduleWithFixedDelay(alertCache::loadDowJonesCache, 5, 20, TimeUnit.MINUTES);
		this.service.scheduleWithFixedDelay(alertCache::loadNewsEdgeCache, 5, 20, TimeUnit.MINUTES);
		this.service.scheduleWithFixedDelay(alertCache::processEarningsAlert, 5, 20, TimeUnit.MINUTES);
	}

	private void startWeb() {
		port(environmentProperties.getIntProperty("PORT", 8909));

		path("alert/app", () -> {
			path("/user", () -> {
//				get("/firedAlerts", UserController.getFiredUserAlerts);
				post("/firedAlerts", UserController.getFiredUserAlerts);
				get("/firedAlerts/json", UserController.getFiredUserAlertsJson);
			});
			path("/alerts", () -> {
				get("/list", AlertController.listAlertsByUserId, gson::toJson);
				get("/delete", AlertController.deleteUserAlertByName, gson::toJson);
				post("/add", AlertController.addUserAlert, gson::toJson);
				get("/list/active", AlertController.listActiveAlertsByUserId, gson::toJson);
				get("/list/fired", AlertController.getFiredUserAlerts, gson::toJson);

			});

			path("/systemalert", () -> {
				before("/*", Authenticator.authenticateSystemAlertApi);
				get("/list/active", SystemAlertController.getActiveSystemAlert, gson::toJson);
				get("/delete", SystemAlertController.deleteSystemAlert, gson::toJson);
				post("/add", SystemAlertController.addSystemAlert, gson::toJson);
				post("/update", SystemAlertController.updateSystemAlert, gson::toJson);
				get("/list/fired", SystemAlertController.getFiredUserAlerts, gson::toJson);
			});

			path("/historical", () -> {
				get("/list", HistoricalAlertController.listHistoricalAlert, gson::toJson);
				get("/delete", HistoricalAlertController.deleteHistoricalAlert, gson::toJson);
				get("/count", HistoricalAlertController.countHistoricalAlertsByUser, gson::toJson);
//				get("/EquityPlusManageUserAlert.do", HistoricalAlertController.listhistoricalAlertbyAlertName,
//						gson::toJson);
//				get("/EquityPlusLoadActiveAlerts.do",
//						HistoricalAlertController.listhistoricalAlertsbyAlertNameandNewsSource, gson::toJson);
//				post("/EquityPlusAddUserAlert", HistoricalAlertController.addhistoricalAlert);

			});

		});

		before("*", (request, response) -> {
			response.type(Constants.REQUEST_TYPE_JSON);
		});
		after("*", (request, response) -> {
			if (response.type() == null) {
				String gzip = request.headers("Content-Encoding");
				if (gzip != null && "gzip".equalsIgnoreCase(gzip))
					response.header("Content-Encoding", "gzip");
				response.type(Constants.REQUEST_TYPE_JSON);
			}
		});
		notFound((req, res) -> {
			res.type(Constants.REQUEST_TYPE_JSON);
			return Constants.ERROR_PAGE_NOT_FOUND;
		});
		internalServerError((req, res) -> {
			res.type(Constants.REQUEST_TYPE_JSON);
			return Constants.ERROR_INTERNAL_SERVER_ERROR;
		});
		exception(IllegalArgumentException.class, (exception, request, response) -> {
			response.type(Constants.REQUEST_TYPE_JSON);
			logger.log(Level.WARNING, exception.getMessage(), exception);
			response.status(Constants.HTTP_RESPONSE_BAD_REQUEST);
			String errorMsg = exception.getMessage() == null ? "Bad parameters" : exception.getMessage();
			response.body(statusToJson("FAILURE", errorMsg));
		});
		exception(SQLException.class, (exception, request, response) -> {
			response.type(Constants.REQUEST_TYPE_JSON);
			logger.log(Level.WARNING, exception.getMessage(), exception);
			response.status(Constants.HTTP_RESPONSE_INTERNAL_ERROR);
			String errorMsg = exception.getMessage() == null ? "Server Error: database query issue"
					: exception.getMessage();
			response.body(statusToJson("FAILURE", errorMsg));
		});

		exception(SQLIntegrityConstraintViolationException.class, (exception, request, response) -> {
			response.type(Constants.REQUEST_TYPE_JSON);
			logger.log(Level.WARNING, exception.getMessage(), exception);
			response.status(Constants.HTTP_RESPONSE_BAD_REQUEST);
			response.body(statusToJson("FAILURE", exception.getMessage()));
		});

		exception(QuoddException.class, (exception, request, response) -> {
			response.type(Constants.REQUEST_TYPE_JSON);
			logger.log(Level.WARNING, exception.getMessage(), exception);
			response.status(Constants.HTTP_RESPONSE_BAD_REQUEST);
			String errorMsg = exception.getMessage() == null ? "Bad parameters" : exception.getMessage();
			response.body(statusToJson("FAILURE", errorMsg));
		});

	}

	public static void main(String[] args) {
		AlertCpd cpd = new AlertCpd();
		cpd.startWeb();
	}
}
