package com.darkedges.openam.policyevaluation;

import org.forgerock.openam.entitlement.EntitlementModule;
import org.forgerock.openam.entitlement.EntitlementRegistry;

public class DarkEdgesEntitlementModule implements EntitlementModule {

	public void registerCustomTypes(EntitlementRegistry entitlementRegistry) {
		entitlementRegistry.registerConditionType("DarkEdgesCondition", DarkEdgesCondition.class);

	}
}
