/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
 *
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

package io.bspk.testframework.strawman;

import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
public interface TestModule {
	
	public enum Status { CREATED, CONFIGURED, RUNNING }

	/**
	 * @param config
	 * @param eventLog 
	 */
	void configure(JsonObject config, EventLog eventLog);

	/**
	 * @return
	 */
	String getId();

	/**
	 * @return
	 */
	Status getStatus();

	/**
	 * 
	 */
	void start();

}
