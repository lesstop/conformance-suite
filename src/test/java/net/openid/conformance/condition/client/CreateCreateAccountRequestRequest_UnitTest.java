package net.openid.conformance.condition.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.openid.conformance.condition.Condition.ConditionResult;
import net.openid.conformance.logging.TestInstanceEventLog;
import net.openid.conformance.testmodule.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CreateCreateAccountRequestRequest_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private CreateCreateAccountRequestRequest cond;

	@Before
	public void setUp() throws Exception {

		cond = new CreateCreateAccountRequestRequest();
		cond.setProperties("UNIT-TEST", eventLog, ConditionResult.INFO);

	}

	@Test
	public void testEvaluate() {

		cond.execute(env);

		JsonElement permissions = env.getElementFromObject("account_requests_endpoint_request", "Data.Permissions");
		assertThat(permissions).isNotNull();
		assertThat(permissions.isJsonArray()).isTrue();
		assertThat(permissions.getAsJsonArray().contains(new JsonPrimitive("ReadAccountsBasic"))).isTrue();

	}

	@Test
	public void testEvaluateExtra() {

		env.putObjectFromJsonString("config", """
				{ "server": { "additionalOpenBankingUkAccountRequestData" : { "SupplementaryData": {
				      "DesiredStatus": "Authorised",
				      "UserID": "flibble"
				    } } } }""");

		cond.execute(env);

		JsonElement supData = env.getElementFromObject("account_requests_endpoint_request", "Data.SupplementaryData");
		assertThat(supData).isNotNull();
		assertThat(supData.isJsonObject()).isTrue();
		assertThat((supData.getAsJsonObject().get("UserID"))).isEqualTo(new JsonPrimitive("flibble"));

		JsonElement permissions = env.getElementFromObject("account_requests_endpoint_request", "Data.Permissions");
		assertThat(permissions).isNotNull();
		assertThat(permissions.isJsonArray()).isTrue();
		assertThat(permissions.getAsJsonArray().contains(new JsonPrimitive("ReadAccountsBasic"))).isTrue();

	}

}
