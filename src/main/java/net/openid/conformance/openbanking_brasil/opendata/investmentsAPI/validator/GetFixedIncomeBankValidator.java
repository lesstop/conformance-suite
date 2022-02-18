package net.openid.conformance.openbanking_brasil.opendata.investmentsAPI.validator;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.jsonAsserting.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.openbanking_brasil.productsNServices.ProductNServicesCommonFields;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.IntField;
import net.openid.conformance.util.field.ObjectArrayField;
import net.openid.conformance.util.field.ObjectField;
import net.openid.conformance.util.field.StringField;

import java.util.Set;

/**
 * Api url: https://github.com/OpenBanking-Brasil/areadesenvolvedor/blob/gh-pages/swagger/swagger_investments_apis.yaml
 * Api endpoint: /bank-fixed-incomes
 * Git hash: c90e531a2693825fe55fd28a076367cefcb01ad8
 */

@ApiName("Investments Bank Fixed Incomes")
public class GetFixedIncomeBankValidator extends AbstractJsonAssertingCondition {
	private static class Fields extends ProductNServicesCommonFields {
	}

	private static final Set<String> PRODUCT_TYPE = Sets.newHashSet("CDB", "RDB", "LCI", "LCA");
	private static final Set<String> REDEMPTION_TERM = Sets.newHashSet("DIARIA", "DATA_DE_VENCIMENTO", "DIARIA_APOS_PRAZO_DE_CARENCIA");
	private static final Set<String> INDEXER = Sets.newHashSet("CDI", "DI", "TR", "IPCA", "IGP_M", "IGP_DI", "INPC", "BCP", "TLC", "SELIC", "OUTROS");
	private static final Set<String> INTERVAL = Sets.newHashSet("1_FAIXA", "2_FAIXA", "3_FAIXA", "4_FAIXA");

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {
		JsonElement body = bodyFrom(environment);

		assertField(body,
			new ObjectArrayField
				.Builder("data")
				.setValidator(this::assertData)
				.mustNotBeEmpty()
				.build());

		logFinalStatus();
		return environment;
	}

	private void assertData(JsonObject data) {
		assertField(data,
			new ObjectField
				.Builder("participant")
				.setValidator(this::assertParticipant)
				.build());

		assertField(data,
			new StringField
				.Builder("issuerInstitutionCnpjNumber")
				.setMaxLength(14)
				.setPattern("^\\d{14}$")
				.build());

		assertField(data,
			new StringField
				.Builder("investimentType")
				.setMaxLength(3)
				.setEnums(PRODUCT_TYPE)
				.build());

		assertField(data,
			new ObjectField
				.Builder("index")
				.setValidator(index -> {
					assertField(index,
						new StringField
							.Builder("indexer")
							.setMaxLength(6)
							.setEnums(INDEXER)
							.build());

					assertField(index,
						new StringField
							.Builder("indexerAdditionalInfo")
							.setMaxLength(50)
							.setOptional()
							.build());

					assertField(index,
						new ObjectField
							.Builder("issueRemunerationRate")
							.setValidator(this::assertIssueRemunerationRate)
							.build());
				})
				.build());

		assertField(data,
			new ObjectField
				.Builder("investmentConditions")
				.setValidator(this::assertInvestmentConditions)
				.build());
	}

	private void assertInvestmentConditions(JsonObject investmentConditions) {
		assertField(investmentConditions,
			new StringField
				.Builder("minimumAmount")
				.setMinLength(4)
				.setMaxLength(19)
				.setPattern("^\\d{1,16}\\.\\d{2}$")
				.build());

		assertField(investmentConditions,
			new StringField.
				Builder("redemptionTerm")
				.setMaxLength(29)
				.setEnums(REDEMPTION_TERM)
				.build());

		assertField(investmentConditions,
			new IntField
				.Builder("minimumExpirationTerm")
				.setMinValue(1)
				.build());

		assertField(investmentConditions,
			new IntField
				.Builder("maximumExpirationTerm")
				.setMinValue(1)
				.build());

		assertField(investmentConditions,
			new IntField
				.Builder("minimumGracePeriod")
				.setMinValue(0)
				.build());

		assertField(investmentConditions,
			new IntField
				.Builder("maximumGracePeriod")
				.setMinValue(0)
				.build());
	}

	private void assertIssueRemunerationRate(JsonObject issueRemunerationRate) {
		assertField(issueRemunerationRate,
			new ObjectArrayField
				.Builder("prices")
				.setValidator(this::assertPrices)
				.setMinItems(4)
				.setMaxItems(4)
				.build());

		assertField(issueRemunerationRate,
			new StringField
				.Builder("minimum")
				.setMaxLength(8)
				.setPattern("^\\d{1}\\.\\d{1,6}$")
				.build());

		assertField(issueRemunerationRate,
			new StringField
				.Builder("maximum")
				.setMaxLength(8)
				.setPattern("^\\d{1}\\.\\d{1,6}$")
				.build());
	}

	private void assertPrices(JsonObject prices) {
		assertField(prices,
			new StringField.
				Builder("interval")
				.setMaxLength(7)
				.setEnums(INTERVAL)
				.build());

		assertField(prices,
			new StringField.
				Builder("value")
				.setMaxLength(8)
				.setPattern("^\\d{1}\\.\\d{1,6}$")
				.build());

		assertField(prices,
			new StringField.
				Builder("customerRate")
				.setMaxLength(8)
				.setPattern("^\\d{1}\\.\\d{1,6}$")
				.build());
	}

	private void assertParticipant(JsonObject participantIdentification) {
		assertField(participantIdentification,
			new StringField
				.Builder("brand")
				.setMaxLength(80)
				.build());

		assertField(participantIdentification, Fields.name().setMaxLength(80).build());
		assertField(participantIdentification, Fields.cnpjNumber().setPattern("^\\d{14}$").build());

		assertField(participantIdentification,
			new StringField
				.Builder("urlComplementaryList")
				.setMaxLength(1024)
				.setPattern("^(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)$")
				.setOptional()
				.build());
	}

}
