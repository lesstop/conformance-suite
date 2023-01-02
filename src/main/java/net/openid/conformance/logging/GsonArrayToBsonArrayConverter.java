package net.openid.conformance.logging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import org.bson.BsonArray;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.Map;

public class GsonArrayToBsonArrayConverter implements Converter<JsonArray, BsonArray> {

	private Gson gson = new GsonBuilder().serializeNulls().create();

	/* (non-Javadoc)
	 * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
	 */
	@Override
	public BsonArray convert(JsonArray source) {
		if (source == null) {
			return null;
		} else {
			String json = gson.toJson(GsonObjectToBsonDocumentConverter.convertFieldsToStructure(source));
			return BsonArray.parse(json);
		}
	}

	public static Map<String, Object> convertUnloggableValuesInMap(Map<String, Object> map) {
		Map<String, Object> convertedMap = new HashMap<>();
		if (map != null) {
			map.forEach((key, value) -> {
				if (value instanceof JsonElement && ((JsonElement) value).isJsonArray()) {
					convertedMap.put(key, new GsonArrayToBsonArrayConverter().convert(((JsonElement) value).getAsJsonArray()));
				} else if (value instanceof JWK) {
					// letting this through to the default mongo converter results in stackoverflows if the jwk
					// contains an x5c entry; explicitly convert it to it's more helpful JSON representation
					String json = ((JWK) value).toJSONString();
					convertedMap.put(key, JsonParser.parseString(json));
				} else if (value instanceof JWKSet) {
					String json = ((JWKSet) value).toString();
					convertedMap.put(key, JsonParser.parseString(json));
				} else if (value instanceof JWTClaimsSet) {
					String json = ((JWTClaimsSet) value).toString();
					convertedMap.put(key, JsonParser.parseString(json));
				} else if (value instanceof JWSHeader) {
					String json = ((JWSHeader) value).toString();
					convertedMap.put(key, JsonParser.parseString(json));
				} else {
					convertedMap.put(key, value);
				}
			});
			return convertedMap;
		}
		return null;
	}

}
