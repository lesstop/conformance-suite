package net.openid.conformance.openid.client.logout;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.as.logout.CreateRPFrontChannelLogoutRequestUrl;
import net.openid.conformance.condition.as.logout.EnsureClientHasFrontChannelLogoutUri;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.testmodule.TestFailureException;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * The difference between this one and OIDCCClientTestFrontChannelLogoutRPInitiated is:
 * - This one does not wait for the RP to call the end_session_endpoint
 * - OIDCCClientTestFrontChannelLogoutRPInitiated renders the front channel logout page when end_session_endpoint is requested
 */
@PublishTestModule(
	testName = "oidcc-client-test-rp-frontchannel-opinitlogout",
	displayName = "OIDCC: Relying party test, OP initiated front channel logout.",
	summary = "The client is expected to make an authorization request " +
		"(also a token request and a optionally a userinfo request when applicable)," +
		" then the OP terminates the session by calling the frontchannel_logout_uri (OP-Initiated Logout)," +
		" then Handle Post Logout URI Redirect." +
		" Corresponds to rp-frontchannel-rpinitlogout in the old test suite.",
	profile = "OIDCC",
	configurationFields = {
	}
)
public class OIDCCClientTestFrontChannelLogoutOPInitiated extends AbstractOIDCCClientFrontChannelLogoutTest
{

	protected boolean isAuthorizationCodeRequestUnexpected() {
		return responseType.includesIdToken();
	}

	@Override
	protected Object handleAuthorizationEndpointRequest(String requestId) {
		if(isAuthorizationCodeRequestUnexpected()) {
			waitAndSendLogoutRequest();
		}
		return super.handleAuthorizationEndpointRequest(requestId);
	}

	@Override
	protected Object authorizationCodeGrantType(String requestId) {
		if(isAuthorizationCodeRequestUnexpected()) {
			throw new TestFailureException(getId(), "Token request is unexpected for this test");
		} else {
			waitAndSendLogoutRequest();
		}
		return super.authorizationCodeGrantType(requestId);
	}

	protected void sendFrontChannelLogoutRequest() {
		createFrontChannelLogoutRequestUrl();
		performRedirect();
	}

	protected void performRedirect() {
		String redirectTo = env.getString("base_url") + "/frontchannel_logout_handler";

		eventLog.log(getName(), args("msg", "Redirecting to front channel logout handler",
			"redirect_to", redirectTo,
			"http", "redirect"));

		setStatus(Status.WAITING);
		browser.goToUrl(redirectTo);
	}

	protected void waitAndSendLogoutRequest() {
		getTestExecutionManager().runInBackground(() -> {
			Thread.sleep(2 * 1000);
			if (getStatus().equals(Status.WAITING)) {
				setStatus(Status.RUNNING);
				sendFrontChannelLogoutRequest();
			}
			return "done";
		});
	}
}
