package net.openid.conformance.openbanking_brasil.testmodules.support.payments;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.testmodule.OIDFJSON;
import net.openid.conformance.util.JWTUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractErrorFromJwtResponseCondition extends AbstractCondition {

	private static final String[] allowedErrors = {"code","title","detail"};
	private static final String[] allowedMetaFields = {"requestDateTime", "totalRecords", "totalPages"};

	protected void validateError(JsonObject response, String errorToExpect) {
		int status = OIDFJSON.getInt(response.get("status"));
		switch(status) {
			case 422:
				try {
					JsonObject jwt = JWTUtil.jwtStringToJsonObjectForEnvironment(OIDFJSON.getString(response.get("body")));
					JsonObject claims = jwt.getAsJsonObject("claims");
					JsonArray errors = claims.getAsJsonArray("errors");
					JsonObject meta = claims.getAsJsonObject("meta");
					validateErrorAndMetaFields(errors, meta);
					AtomicBoolean passed = new AtomicBoolean(false);
					errors.forEach(e -> {
						JsonObject error = (JsonObject) e;
						String errorCode = OIDFJSON.getString(error.get("code"));
						if(errorCode.equals(errorToExpect)) {
							passed.set(true);
						}
						if(!passed.get()) {
							throw error("Error code was not " + errorToExpect, Map.of("error", errorCode));
						} else {
							logSuccess("Successfully found error code  " + errorToExpect);
						}
					});

				} catch (ParseException e) {
					throw error("Could not parse JWT");
				}
				break;
			default:
				log("Response status was not 422 - not taking any action", Map.of("status", status));
				break;
		}
	}

	private void validateErrorAndMetaFields(JsonArray errors, JsonObject meta){
		if(errors == null){
			throw error("Errors not found, failing");
		}

		assertAllowedErrorFields(errors);
		if(meta != null){
			assertAllowedMetaFields(meta);
		}
	}

	private void assertAllowedErrorFields(JsonArray errors) {
		for(JsonElement error: errors){
			assertNoAdditionalErrorFields(error.getAsJsonObject());
		}
	}

	private void assertAllowedMetaFields(JsonObject metaJson) {
		log("Ensure that the 'meta' response " + metaJson + " only contains metadata fields that are defined in the swagger");

		for (Map.Entry<String, JsonElement> meta : metaJson.entrySet())
		{
			log("Checking: " + meta.getKey());
			if ( !ArrayUtils.contains( allowedMetaFields, meta.getKey() ) ) {
				throw error("non-standard meta property '" + meta.getKey() + "'' found in the error response");
			}
		}
	}

	private void assertNoAdditionalErrorFields(JsonObject field){
		log("Ensure that the error response " + field + " only contains error fields that are defined in the swagger");

		for (Map.Entry<String, JsonElement> entry : field.entrySet())
		{
			log("Checking: " + entry.getKey());
			if ( !ArrayUtils.contains( allowedErrors, entry.getKey() ) ) {
				throw error("non-standard error property '" + entry.getKey() + "'' found in the error response");
			}
		}
	}

}
