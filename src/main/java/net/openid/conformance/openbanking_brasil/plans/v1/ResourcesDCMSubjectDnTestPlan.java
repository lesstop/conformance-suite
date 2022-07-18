package net.openid.conformance.openbanking_brasil.plans.v1;

import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.testmodules.ResourcesDcmSubjectDnTestModule;
import net.openid.conformance.plan.PublishTestPlan;
import net.openid.conformance.plan.TestPlan;
import net.openid.conformance.variant.FAPI1FinalOPProfile;
import net.openid.conformance.variant.VariantSelection;

import java.util.List;
import java.util.Map;

@PublishTestPlan(
	testPlanName = "resources-dcm-subjectdn-test-plan",
	profile = OBBProfile.OBB_PROFILE_DCR,
	displayName = "Resources DCM test for changing subjectdn",
	summary = "For servers that support MTLS client authentication, check that subjectdn can be updated using the dynamic client management endpoint."
)
public class ResourcesDCMSubjectDnTestPlan implements TestPlan {
	public static List<ModuleListEntry> testModulesWithVariants() {
		return List.of(
			new ModuleListEntry(
				List.of(
					ResourcesDcmSubjectDnTestModule.class
				),
				List.of(
					new Variant(FAPI1FinalOPProfile.class, "openbanking_brazil")
				)
			)
		);
	}

	public static String certificationProfileName(VariantSelection variant) {

		Map<String, String> v = variant.getVariant();
		String clientAuth = v.get("client_auth_type");

		if(clientAuth.equals("private_key_jwt")) {
			throw new RuntimeException("This test is set to be executed only with client authentication type of type tls_client_auth. The scope of the test, " +
				"updating the tls_client_auth_subject_dn, has no defined effect when using private_key_jwt client authentication.");
		}

		return null;
	}
}
