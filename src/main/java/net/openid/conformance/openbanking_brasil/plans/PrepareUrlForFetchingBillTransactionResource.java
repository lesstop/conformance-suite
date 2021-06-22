package net.openid.conformance.openbanking_brasil.plans;

import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

public class PrepareUrlForFetchingBillTransactionResource extends AbstractCondition {

	@Override
	@PreEnvironment(strings = {"accountId", "billId", "base_resource_url"})
	public Environment evaluate(Environment env) {
		String resourceUrl = env.getString("base_resource_url");
		String accountId = env.getString("accountId");
		String billId = env.getString("billId");
		resourceUrl = String.format("%s/%s/bills/%s/transactions", resourceUrl, accountId, billId);
		env.putString("protected_resource_url", resourceUrl);
		return env;
	}

}
