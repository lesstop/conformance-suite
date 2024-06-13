package net.openid.conformance.condition;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.openid.conformance.condition.util.MtlsKeystoreBuilder;
import net.openid.conformance.logging.LoggingRequestInterceptor;
import net.openid.conformance.logging.TestInstanceEventLog;
import net.openid.conformance.testmodule.DataUtils;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.testmodule.OIDFJSON;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public abstract class AbstractCondition implements Condition, DataUtils {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCondition.class);

	private String testId;
	private TestInstanceEventLog log;
	private Set<String> requirements;
	private ConditionResult conditionResultOnFailure;
	private int logged = 0;
	private int errorsLogged = 0;
	private boolean loggedSoftLimitMsg = false;

	@Override
	public void setProperties(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		this.testId = testId;
		this.log = log;
		this.conditionResultOnFailure = conditionResultOnFailure;
		this.requirements = Sets.newHashSet(requirements);
	}

	@Override
	public void execute(Environment env) {
		try {
			Method eval = this.getClass().getMethod("evaluate", Environment.class);
			PreEnvironment pre = eval.getAnnotation(PreEnvironment.class);
			if (pre != null) {
				for (String req : pre.required()) {
					if (!env.containsObject(req)) {
						logger.info(testId + ": [pre] Test condition " + this.getClass().getSimpleName() + " failure, couldn't find object in environment: " + req);
						log.log(this.getMessage(), args(
							"msg", "Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find required object in environment before evaluation: " + req,
							"expected", req,
							"result", ConditionResult.FAILURE,
							"mapped", env.isKeyShadowed(req) ? env.getEffectiveKey(req) : null,
							"requirements", this.getRequirements()
							// TODO: log the environment here?
						));
						throw alreadyLoggedPrePostError("[pre] Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find object in environment: " + req);
					}
				}
				for (String s : pre.strings()) {
					if (env.getString(s) == null) {
						logger.info(testId + ": [pre] Test condition " + this.getClass().getSimpleName() + " failure, couldn't find string in environment: " + s);
						log.log(this.getMessage(), args(
							"msg", "Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find required string in environment before evaluation: " + s,
							"expected", s,
							"result", ConditionResult.FAILURE,
							"requirements", this.getRequirements()
							// TODO: log the environment here?
						));
						throw alreadyLoggedPrePostError("[pre] Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find string in environment: " + s);
					}
				}
			}

			// evaluate the condition and assign its results back to our environment
			env = evaluate(env);
			if (logged == 0) {
				log.log(this.getMessage(),
					args("msg", "Condition ran but did not log anything"));
			}
			if (errorsLogged > 0) {
				// the condition has logged a warning/failure so must throw an error, otherwise the test result will
				// not be updated
				throw error("Test logged a non-success result but did not throw an error");
			}

			// check the environment to make sure the condition did what it claimed to
			PostEnvironment post = eval.getAnnotation(PostEnvironment.class);
			if (post != null) {
				for (String req : post.required()) {
					if (!env.containsObject(req)) {
						logger.info(testId + ": [post] Test condition " + this.getClass().getSimpleName() + " failure, couldn't find object in environment: " + req);
						log.log(this.getMessage(), args(
							"msg", "Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find required object in environment after evaluation: " + req,
							"expected", req,
							"result", ConditionResult.FAILURE,
							"mapped", env.isKeyShadowed(req) ? env.getEffectiveKey(req) : null,
							"requirements", this.getRequirements()
							// TODO: log the environment here?
						));
						throw alreadyLoggedPrePostError("[post] Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find object in environment: " + req);
					}
				}
				for (String s : post.strings()) {
					if (env.getString(s) == null) {
						logger.info(testId + ": [post] Test condition " + this.getClass().getSimpleName() + " failure, couldn't find string in environment: " + s);
						log.log(this.getMessage(), args(
							"msg", "Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find required string in environment after evaluation: " + s,
							"expected", s,
							"result", ConditionResult.FAILURE,
							"requirements", this.getRequirements()
							// TODO: log the environment here?
						));
						throw alreadyLoggedPrePostError("[post] Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't find string in environment: " + s);
					}
				}
			}
		} catch (NoSuchMethodException e) {
			logger.error(testId + ": Couldn't create condition object", e);
			log.log(this.getMessage(), args(
				"msg", "Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't get 'evaluate' method for condition '" + this.getClass().getSimpleName() + "'",
				"result", ConditionResult.FAILURE
			));
			throw alreadyLoggedPrePostError("Something unexpected happened (this could be caused by something you did wrong, or it may be an issue in the test suite - please review the instructions and your configuration, if you still see a problem please contact certification@oidf.org with the full details) - couldn't get 'evaluate' method for condition '" + this.getClass().getSimpleName() + "'", e);
		} catch (Environment.UnexpectedTypeException e) {
			throw error(e.getMessage(), e);
		}
	}

	/**
	 * Tests if the condition holds true. Reads from the given environment and returns a potentially modified environment.
	 *
	 * Throws ConditionError when condition isn't met.
	 *
	 * Decorate with @PreEnvironment to ensure objects or strings are in the environment before evaluation.
	 * Decorate with @PostEnvironment to ensure objects or strings are in the environment after evaluation.
	 */
	protected abstract Environment evaluate(Environment env);

	/**
	 * Get the testId for this instance
	 * @return The test Id for the current instance
	 */
	protected String getTestId() {
		return this.testId;
	}

	/**
	 * Get a string from the environment, throwing a condition error if missing/not a string
	 */
	protected String getStringFromEnvironment(Environment env, String key, String path, String friendlyName) {
		JsonElement value = env.getElementFromObject(key, path);

		if (value == null) {
			throw error(friendlyName+" is missing", args(key, env.getObject(key)));
		}

		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
			throw error(friendlyName+" is not a string", args("value", value));
		}

		return OIDFJSON.getString(value);
	}

	/**
	 * Get a string from the environment, throwing a condition error if missing/not a string
	 */
	protected JsonObject getJsonObjectFromEnvironment(Environment env, String key, String path, String friendlyName) {
		JsonElement value = env.getElementFromObject(key, path);

		if (value == null) {
			throw error(friendlyName+" is missing", args(key, env.getObject(key)));
		}

		if (!value.isJsonObject()) {
			throw error(friendlyName+" is not a JSON object", args("value", value));
		}

		return value.getAsJsonObject();
	}

	protected JsonArray getJsonArrayFromEnvironment(Environment env, String key, String path, String friendlyName) {
		JsonElement value = env.getElementFromObject(key, path);

		if (value == null) {
			throw error(friendlyName+" is missing", args(key, env.getObject(key)));
		}

		if (!value.isJsonArray()) {
			throw error(friendlyName+" is not a JSON array", args("value", value));
		}

		return value.getAsJsonArray();
	}

	/*
	 * Logging utilities
	 */

	/**
	 * Do some common processing/checks on log messages
	 *
	 * @param result ConditionResult string (or null if none)
	 * @return true if this message should not be logged
	 */
	private boolean reachedLoggingLimits(String result) {
		final int errorLimit = 50;
		final int logSoftLimit = 1000; // we stop logging here
		final int logHardLimit = 10000; // we abort execution here

		logged++;

		if (result != null &&
			!result.equals(ConditionResult.SUCCESS.toString()) &&
			!result.equals(ConditionResult.INFO.toString()) &&
			!result.equals(ConditionResult.REVIEW.toString())) {
			errorsLogged++;
			if (errorsLogged > errorLimit) {
				// we don't call throw error() or logFailure etc to avoid ending up in an infinite loop
				String msg = "This condition has logged over "+errorLimit+" errors and has been aborted.";
				log.log(getMessage(), args("msg", msg, "result", conditionResultOnFailure));
				throw new ConditionError(testId, getMessage() + ": " + msg);
			}
			// we log upto 50 errors/warnings, even if it would mean exceeding the logSoft/HardLimit
			return false;
		}
		if (logged >= logSoftLimit) {
			if (!loggedSoftLimitMsg) {
				log.log(getMessage(), "This condition has logged over "+logSoftLimit+" log entries. Further entries will be suppressed.");
				loggedSoftLimitMsg = true;
			}
			if (logged >= logHardLimit) {
				// we don't call throw error() or logFailure etc to avoid ending up in an infinite loop
				String msg = "This condition attempted to log over "+logHardLimit+" log entries and has been aborted.";
				log.log(getMessage(), args("msg", msg, "result", conditionResultOnFailure));
				throw new ConditionError(testId, getMessage() + ": " + msg);
			}
			return true;
		}
		return false;
	}

	protected void log(JsonObject obj) {
		String result = null;
		if (obj.has("result")) {
			result = OIDFJSON.getString(obj.get("result"));
		}
		if (reachedLoggingLimits(result)) {
			return;
		}
		log.log(getMessage(), obj);
	}

	protected void log(String msg) {
		if (reachedLoggingLimits(null)) {
			return;
		}
		log.log(getMessage(), msg);
	}

	protected void log(Map<String, Object> map) {
		String result = null;
		if (map.containsKey("result")) {
			result = map.get("result").toString();
		}
		if (reachedLoggingLimits(result)) {
			return;
		}

		log.log(getMessage(), map);
	}

	protected void log(String msg, JsonObject in) {
		JsonObject copy = JsonParser.parseString(in.toString()).getAsJsonObject(); // don't modify the underlying object, round-trip to get a copy
		copy.addProperty("msg", msg);
		log(copy);
	}

	protected void log(String msg, Map<String, Object> map) {
		Map<String, Object> copy = new HashMap<>(map); // don't modify the underlying map
		copy.put("msg", msg);
		log(copy);
	}

	protected void logSuccess(JsonObject in) {
		JsonObject copy = JsonParser.parseString(in.toString()).getAsJsonObject(); // don't modify the underlying object, round-trip to get a copy
		copy.addProperty("result", ConditionResult.SUCCESS.toString());
		if (!getRequirements().isEmpty()) {
			JsonArray arr = new JsonArray();
			for (String req : getRequirements()) {
				arr.add(req);
			}
			copy.add("requirements", arr);
		}
		log(copy);
	}

	protected void logSuccess(String msg) {
		if (getRequirements().isEmpty()) {
			log(args("msg", msg, "result", ConditionResult.SUCCESS));
		} else {
			log(args("msg", msg, "result", ConditionResult.SUCCESS, "requirements", getRequirements()));
		}
	}

	protected void logSuccess(Map<String, Object> map) {
		Map<String, Object> copy = new HashMap<>(map); // don't modify the underlying map
		copy.put("result", ConditionResult.SUCCESS);
		if (!getRequirements().isEmpty()) {
			copy.put("requirements", getRequirements());
		}
		log(copy);
	}

	protected void logSuccess(String msg, JsonObject in) {
		JsonObject copy = JsonParser.parseString(in.toString()).getAsJsonObject(); // don't modify the underlying object, round-trip to get a copy
		copy.addProperty("msg", msg);
		copy.addProperty("result", ConditionResult.SUCCESS.toString());
		if (!getRequirements().isEmpty()) {
			JsonArray reqs = new JsonArray(getRequirements().size());
			for (String req : getRequirements()) {
				reqs.add(req);
			}
			copy.add("requirements", reqs);
		}
		log(copy);
	}

	protected void logSuccess(String msg, Map<String, Object> map) {
		Map<String, Object> copy = new HashMap<>(map); // don't modify the underlying map
		copy.put("msg", msg);
		copy.put("result", ConditionResult.SUCCESS);
		if (!getRequirements().isEmpty()) {
			copy.put("requirements", getRequirements());
		}
		log(copy);
	}

	/*
	 * Automatically log failures or warnings, depending on if this is an optional test
	 *
	 * Note that this does NOT cause the test result to move to warning/failure - it is better for a test condition
	 * to throw error(). If there are a need to call logFailure directly (for example, making multiple checks in
	 * a single condition) then the condition author must ensure it throws an error at the end if any checks have
	 * failed.
	 */

	protected void logFailure(JsonObject in) {
		JsonObject copy = JsonParser.parseString(in.toString()).getAsJsonObject(); // don't modify the underlying object, round-trip to get a copy
		copy.addProperty("result", conditionResultOnFailure.toString());
		if (!getRequirements().isEmpty()) {
			JsonArray arr = new JsonArray();
			for (String req : getRequirements()) {
				arr.add(req);
			}
			copy.add("requirements", arr);
		}
		log(copy);
	}

	protected void logFailure(String msg) {
		if (getRequirements().isEmpty()) {
			log(args("msg", msg, "result", conditionResultOnFailure));
		} else {
			log(args("msg", msg, "result", conditionResultOnFailure, "requirements", getRequirements()));
		}
	}

	protected void logFailure(Map<String, Object> map) {
		Map<String, Object> copy = new HashMap<>(map); // don't modify the underlying map
		copy.put("result", conditionResultOnFailure);
		if (!getRequirements().isEmpty()) {
			copy.put("requirements", getRequirements());
		}
		log(copy);
	}

	protected void logFailure(String msg, JsonObject in) {
		JsonObject copy = JsonParser.parseString(in.toString()).getAsJsonObject(); // don't modify the underlying object, round-trip to get a copy
		copy.addProperty("msg", msg);
		copy.addProperty("result", conditionResultOnFailure.toString());
		if (!getRequirements().isEmpty()) {
			JsonArray reqs = new JsonArray(getRequirements().size());
			for (String req : getRequirements()) {
				reqs.add(req);
			}
			copy.add("requirements", reqs);
		}
		log(copy);
	}

	protected void logFailure(String msg, Map<String, Object> map) {
		Map<String, Object> copy = new HashMap<>(map); // don't modify the underlying map
		copy.put("msg", msg);
		copy.put("result", conditionResultOnFailure);
		if (!getRequirements().isEmpty()) {
			copy.put("requirements", getRequirements());
		}
		log(copy);
	}

	/*
	 * Error utilities
	 */

	/**
	 * Return a ConditionError for failures in the Pre/Post Environment annotations
	 */
	private ConditionError alreadyLoggedPrePostError(String message, Throwable cause) {
		// it is assumed the caller has already written an entry to the event log
		return new ConditionError(testId, getMessage() + ": " + message, true, cause);
	}

	/**
	 * Return a ConditionError for failures in the Pre/Post Environment annotations
	 */
	private ConditionError alreadyLoggedPrePostError(String message) {
		// it is assumed the caller has already written an entry to the event log
		return new ConditionError(testId, getMessage() + ": " + message, true);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(String message, Throwable cause) {
		logFailure(message, ex(cause));
		return new ConditionError(testId, getMessage() + ": " + message, cause);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(String message) {
		logFailure(message);
		return new ConditionError(testId, getMessage() + ": " + message);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(Throwable cause) {
		logFailure(cause.getMessage(), ex(cause));
		return new ConditionError(testId, getMessage(), cause);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(String message, Throwable cause, Map<String, Object> map) {
		logFailure(message, ex(cause, map));
		return new ConditionError(testId, getMessage() + ": " + message, cause);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(String message, Map<String, Object> map) {
		logFailure(message, map);
		return new ConditionError(testId, getMessage() + ": " + message);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(Throwable cause, Map<String, Object> map) {
		logFailure(cause.getMessage(), ex(cause, map));
		return new ConditionError(testId, getMessage(), cause);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(String message, Throwable cause, JsonObject in) {
		logFailure(message, ex(cause, in));
		return new ConditionError(testId, getMessage() + ": " + message, cause);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(String message, JsonObject in) {
		logFailure(message, in);
		return new ConditionError(testId, getMessage() + ": " + message);
	}

	/**
	 * Log a failure then return a ConditionError
	 */
	protected ConditionError error(Throwable cause, JsonObject in) {
		logFailure(cause.getMessage(), ex(cause, in));
		return new ConditionError(testId, getMessage(), cause);
	}

	/**
	 * Get the list of requirements that this test would fulfill if it passed
	 *
	 * @return
	 */
	protected Set<String> getRequirements() {
		return requirements;
	}

	protected String createBrowserInteractionPlaceholder(String msg) {
		String placeholder = RandomStringUtils.randomAlphanumeric(10);
		if (getRequirements().isEmpty()) {
			log(msg, args("upload", placeholder, "result", ConditionResult.REVIEW));
		} else {
			log(msg, args("upload", placeholder, "result", ConditionResult.REVIEW, "requirements", getRequirements()));
		}
		return placeholder;
	}

	protected String createBrowserInteractionPlaceholder() {
		String placeholder = RandomStringUtils.randomAlphanumeric(10);
		if (getRequirements().isEmpty()) {
			log(args("upload", placeholder, "result", ConditionResult.REVIEW));
		} else {
			log(args("upload", placeholder, "result", ConditionResult.REVIEW, "requirements", getRequirements()));
		}
		return placeholder;
	}

	/*
	 * Create an HTTP Client for use in calling outbound to other services
	 */
	protected HttpClient createHttpClient(Environment env, boolean restrictAllowedTLSVersions)
		throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException,
				KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {

		HttpClientBuilder builder = HttpClientBuilder.create()
			.useSystemProperties();

		int timeout = 60; // seconds
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(timeout * 1000)
			.setConnectionRequestTimeout(timeout * 1000)
			.setSocketTimeout(timeout * 1000).build();
		builder.setDefaultRequestConfig(config);

		KeyManager[] km = null;

		// initialize MTLS if it's available
		if (env.containsObject("mutual_tls_authentication")) {

			km = MtlsKeystoreBuilder.configureMtls(env);

		}

		TrustManager[] trustAllCerts = {
			new X509TrustManager() {

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
			}
		};

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(km, trustAllCerts, new java.security.SecureRandom());

		builder.setSSLContext(sc);

		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sc,
			(restrictAllowedTLSVersions ? new String[] { "TLSv1.2", "TLSv1.3" } : null),
			null,
			NoopHostnameVerifier.INSTANCE);

		builder.setSSLSocketFactory(sslConnectionSocketFactory);

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
			.register("https", sslConnectionSocketFactory)
			.register("http", new PlainConnectionSocketFactory())
			.build();

		HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
		builder.setConnectionManager(ccm);

		builder.disableRedirectHandling();

		builder.disableAutomaticRetries();

		HttpClient httpClient = builder.build();
		return httpClient;
	}

	protected RestTemplate createRestTemplate(Environment env) throws UnrecoverableKeyException, KeyManagementException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, IOException {
		return createRestTemplate(env, true);
	}

	protected RestTemplate createRestTemplate(Environment env, boolean restrictAllowedTLSVersions) throws UnrecoverableKeyException, KeyManagementException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, IOException {
		HttpClient httpClient = createHttpClient(env, restrictAllowedTLSVersions);

		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));

		restTemplate.getInterceptors().add(new LoggingRequestInterceptor(getMessage(), log, env.getObject("mutual_tls_authentication")));

		List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();

		// fix the StringHttpMessageConverter, but retaining other default converters, as we do use them,
		// e.g. the map -> urlencoded-form body one
		converters.stream()
			.filter(converter -> converter instanceof StringHttpMessageConverter)
			.forEach(converter -> {
				StringHttpMessageConverter stringHttpMessageConverter = (StringHttpMessageConverter) converter;
				// the default StringHttpMessageConverter will convert to Latin1, so override it
				stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
				// Stop StringHttpMessageConverter from adding a default Accept-Charset header
				stringHttpMessageConverter.setWriteAcceptCharset(false);
			});

		return restTemplate;
	}

	/**
	 * Setup a TCP connection to the given host/port
	 *
	 * @param targetHost The host that will be used to create the socket.
	 * @param targetPort The port that will be used to create the socket.
	 * @return a newly created socket using the system HTTP proxy if one is set.
	 * @throws IOException thrown if there is an issue with the socket connection.
	 */
	protected Socket setupSocket(String targetHost, Integer targetPort) throws IOException {
		// For most operations we rely on HttpClientBuilder useSystemProperties() method to support proxies, however
		// as here we are creating the socket ourselves to perform TLS cipher/version tests, we need to explicitly
		// process the proxy configuration keys.
		String proxyHost = System.getProperty("https.proxyHost", "");
		int proxyPort = Integer.parseInt(System.getProperty("https.proxyPort", "0"));
		String noProxyStr = System.getProperty("https.noProxy", "");
		boolean noProxyFlag = false;
		Path targetPath = Path.of(targetHost);

		for (String proxyExc : noProxyStr.split(",")) {
			PathMatcher matcher =  FileSystems.getDefault().getPathMatcher("glob:" + proxyExc);

			if (matcher.matches(targetPath)) {
				noProxyFlag = true;
				break;
			}
		}

		Socket socket;
		if (!noProxyFlag && Strings.isNullOrEmpty(proxyHost) && proxyPort != 0) {

			// see https://gitlab.com/openid/conformance-suite/merge_requests/218#note_74098367
			log("Creating socket through system HTTPS proxy; this may cause incorrect test results", args(
					"proxy_host", proxyHost,
					"proxy_port", proxyPort,
					"target_host", targetHost,
					"target_port", targetPort,
					"result", ConditionResult.WARNING
				));
			// Note that the above 'log' doesn't make the test result be a warning; it would be better if it did but
			// that's not simple to achieve from here

			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
			socket = new Socket(proxy);
			socket.connect(new InetSocketAddress(targetHost, targetPort));
		} else {
			socket = new Socket(InetAddress.getByName(targetHost), targetPort);
		}
		return socket;
	}

	protected JsonObject convertResponseForEnvironment(String endpointName, ResponseEntity<String> response) {
		JsonObject responseInfo = new JsonObject();
		responseInfo.addProperty("status", response.getStatusCode().value());
		responseInfo.addProperty("endpoint_name", endpointName); // for use in further logging
		JsonObject responseHeaders = mapToJsonObject(response.getHeaders(), true);

		responseInfo.add("headers", responseHeaders);

		responseInfo.addProperty("body", response.getBody());
		return responseInfo;
	}

	protected JsonObject convertJsonResponseForEnvironment(String endpointName, ResponseEntity<String> response) {
		return convertJsonResponseForEnvironment(endpointName, response, false);
	}

	protected JsonObject convertJsonResponseForEnvironment(String endpointName, ResponseEntity<String> response, boolean allowParseFailure) {
		JsonObject responseInfo = convertResponseForEnvironment(endpointName, response);

		String jsonString = response.getBody();
		if (Strings.isNullOrEmpty(jsonString)) {
			if (allowParseFailure) {
				return responseInfo;
			}
			throw error("Empty response from the "+endpointName+" endpoint");
		}

		try {
			JsonElement jsonRoot = JsonParser.parseString(jsonString);
			if (jsonRoot == null || !jsonRoot.isJsonObject()) {
				if (allowParseFailure) {
					return responseInfo;
				}

				throw error(endpointName + " endpoint did not return a JSON object.",
					args("response", jsonString));
			}

			JsonObject bodyJson = jsonRoot.getAsJsonObject();

			responseInfo.add("body_json", bodyJson);

		} catch (JsonParseException e) {
			if (allowParseFailure) {
				return responseInfo;
			}
			throw error("Response from "+endpointName+" endpoint does not appear to be JSON.", e,
				args("response", jsonString));
		}

		return responseInfo;
	}
}
