/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package io.fintechlabs.testframework.condition;

import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.fintechlabs.testframework.logging.EventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class GetDynamicServerConfiguration extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public GetDynamicServerConfiguration(String testId, EventLog log, boolean optional) {
		super(testId, log, optional);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment, java.lang.String, io.fintechlabs.testframework.logging.EventLog)
	 */
	@Override
	public Environment evaluate(Environment env) {
		
		if (!env.containsObj("config")) {
			return error("Couldn't find a configuration");
		}
		
		String discoveryUrl = env.getString("config", "server.discoveryUrl");
		
		if (Strings.isNullOrEmpty(discoveryUrl)) {
			String iss = env.getString("config", "server.issuer");
			if (Strings.isNullOrEmpty(iss)) {
				return error("Couldn't find discoveryUrl or issuer field for discovery purposes");
			}
			
			discoveryUrl = iss + "/.well-known/openid-configuration";
		}
		
		// get out the server configuration component
		if (!Strings.isNullOrEmpty(discoveryUrl)) {
			// do an auto-discovery here
			
			RestTemplate restTemplate = new RestTemplate();

			// fetch the value
			String jsonString;
			try {
				jsonString = restTemplate.getForObject(discoveryUrl, String.class);
			} catch (RestClientResponseException e) {
				return error("Unable to fetch server configuration from " + discoveryUrl, e);
			}

			log(ImmutableMap.of("msg", "Downloaded server configuration", 
					"server_config_string", jsonString));

			if (!Strings.isNullOrEmpty(jsonString)) {
				try {
					JsonObject serverConfig = new JsonParser().parse(jsonString).getAsJsonObject();
					
					log("Successfully parsed server configuration", serverConfig);
					
					env.put("server", serverConfig);
					
					logSuccess();
					return env;
				} catch (JsonSyntaxException e) {
					return error(e);
				}
				
				
			} else {
				return error("empty server configuration");
			}
			
		} else {
			// check for manual configuration here
			// TODO!
			return error("Static configuration not yet implemented");
		}

		
		
	}

}
