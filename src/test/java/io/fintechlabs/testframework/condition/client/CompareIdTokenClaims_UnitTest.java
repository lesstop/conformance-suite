package io.fintechlabs.testframework.condition.client;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CompareIdTokenClaims_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private CompareIdTokenClaims cond;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		cond = new CompareIdTokenClaims();
		cond.setProperties("UNIT-TEST", eventLog, Condition.ConditionResult.INFO);
	}

	private JsonObject createClaimsJsonObject(String iss, String sub, String aud, long iat, long authTime, String azp) {
		JsonObject obj = new JsonObject();
		obj.addProperty("iss", iss);
		obj.addProperty("sub", sub);
		obj.addProperty("aud", aud);
		obj.addProperty("iat", iat);
		obj.addProperty("auth_time", authTime);
		if (azp != null) {
			obj.addProperty("azp", azp);
		}
		return obj;
	}

	@Test
	public void testEvaluate_success() {
		JsonObject first = createClaimsJsonObject("https://example.com/iss", "subject", "audience", 1560600719, 1560600619, "client");
		JsonObject second = createClaimsJsonObject("https://example.com/iss", "subject", "audience", 1560600819, 1560600619, "client");

		env.putObject("first_id_token_claims", first);
		env.putObject("second_id_token_claims", second);

		cond.evaluate(env);
	}

	/**
	 * its azp Claim Value MUST be the same as in the ID Token issued when the original authentication occurred;
	 * if no azp Claim was present in the original ID Token, one MUST NOT be present in the new ID Token, and
	 */
	@Test (expected = ConditionError.class)
	public void testEvaluate_diffentAzpInSecond() {
		JsonObject first = createClaimsJsonObject("https://example.com/iss", "subject", "audience", 1560600719, 1560600619, null);
		JsonObject second = createClaimsJsonObject("https://example.com/iss", "subject", "audience", 1560600819, 1560600619, "client");

		env.putObject("first_id_token_claims", first);
		env.putObject("second_id_token_claims", second);

		cond.evaluate(env);
	}

	@Test
	public void testEvaluate_successWithArrayAud() {
		JsonObject first = createClaimsJsonObject("https://example.com/iss", "subject", "audience", 1560600719, 1560600619, "client");
		JsonObject second = createClaimsJsonObject("https://example.com/iss", "subject", "audience", 1560600819, 1560600619, "client");
		first.remove("aud");
		second.remove("aud");

		JsonArray audArray1 = new JsonArray();
		audArray1.add("aud1");
		audArray1.add("aud2");
		audArray1.add("aud3");
		first.add("aud", audArray1);

		JsonArray audArray2 = new JsonArray();
		audArray2.add("aud1");
		audArray2.add("aud3");
		audArray2.add("aud2");	//note the different ordering of elements
		second.add("aud", audArray2);

		env.putObject("first_id_token_claims", first);
		env.putObject("second_id_token_claims", second);

		cond.evaluate(env);
	}
}
