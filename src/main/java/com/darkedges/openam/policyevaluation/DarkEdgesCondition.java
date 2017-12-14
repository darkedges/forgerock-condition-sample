/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2006 Sun Microsystems Inc.
 */
/*
 * Portions Copyright 2014-2015 ForgeRock AS.
 */
package com.darkedges.openam.policyevaluation;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.forgerock.openam.core.CoreWrapper;
import org.forgerock.openam.utils.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.identity.entitlement.ConditionDecision;
import com.sun.identity.entitlement.EntitlementConditionAdaptor;
import com.sun.identity.entitlement.EntitlementException;
import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.shared.debug.Debug;

/**
 * An implementation of an
 * {@link com.sun.identity.entitlement.EntitlementCondition} that will check
 * whether the requested auth level is greater than or equal to the auth level
 * set in the condition.
 *
 * @since 12.0.0
 */
public class DarkEdgesCondition extends EntitlementConditionAdaptor {
	private String displayType;
	public static final String LENGTH_FIELD = "nameLength";
	private static final String DARKEDGES_CONDITION_ADVICE = "DarkEdgesConditionAdvice";
	private int nameLength = 0; // Default minimum length

	// Name for the debug-log
	private final String DEBUG_NAME = "DarkEdgesCondition";
	private Debug debug = Debug.getInstance(DEBUG_NAME);
	private final CoreWrapper coreWrapper;

	/**
	 * Constructs a new AuthLevelCondition instance.
	 */
	public DarkEdgesCondition() {
		this(PrivilegeManager.debug, new CoreWrapper());
	}

	/**
	 * Constructs a new AuthLevelCondition instance.
	 *
	 * @param debug
	 *            A Debug instance.
	 * @param coreWrapper
	 *            An instance of the CoreWrapper.
	 */
	DarkEdgesCondition(Debug debug, CoreWrapper coreWrapper) {
		this.debug = debug;
		this.coreWrapper = coreWrapper;
	}

	/**
	 * Configuration Methods
	 * 
	 * @return
	 */
	public int getNameLength() {
		debug.error("getNameLength");
		return nameLength;
	}

	public void setNameLength(int nameLength) {
		debug.error("setNameLength: {}", nameLength);
		this.nameLength = nameLength;
	}

	protected boolean isAllowed(Principal principal, Map<String, Set<String>> advices) throws EntitlementException {

		String userDn = principal.getName();

		int start = userDn.indexOf('=');
		int end = userDn.indexOf(',');
		if (end <= start) {
			throw new EntitlementException(EntitlementException.CONDITION_EVALUATION_FAILED,
					"Name is not a valid DN: " + userDn);
		}

		String userName = userDn.substring(start + 1, end);

		if (userName.length() >= getNameLength()) {
			return true;
		}

		Set<String> adviceMessages = new HashSet<String>(1);
		adviceMessages.add("" + nameLength);
		advices.put(DARKEDGES_CONDITION_ADVICE, adviceMessages);
		return false;
	}

	/**
	 * required
	 */
	public ConditionDecision evaluate(String realm, Subject subject, String resource,
			Map<String, Set<String>> environment) throws EntitlementException {
		debug.error("evaluate:\n\trealm {}.\n\tsubject: {}\n\tresource: {}\n\tenvironment: {}", realm, subject,
				resource, environment);
		boolean authorized = false;
		Map<String, Set<String>> advices = new HashMap<String, Set<String>>();
		if (!subject.getPrincipals().isEmpty()) {
			Principal principal = subject.getPrincipals().iterator().next();
			authorized = isAllowed(principal, advices);
		} else {
			throw new EntitlementException(EntitlementException.SUBJECT_REQUIRED, "No Subject Supplied");
		}
		return new ConditionDecision(authorized, advices);
	}

	public String getState() {
		debug.error("getState");
		try {
			JSONObject json = new JSONObject();
			json.put(LENGTH_FIELD, getNameLength());
			return json.toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public void setState(String state) {
		debug.error("setState: {}", state);
		try {
			JSONObject json = new JSONObject(state);
			setState(json);
			setNameLength(json.getInt(LENGTH_FIELD));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public void validate() throws EntitlementException {
		debug.error("validate");
		if (getNameLength() < 0) {
			throw new EntitlementException(EntitlementException.INVALID_PROPERTY_VALUE, LENGTH_FIELD);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		DarkEdgesCondition other = (DarkEdgesCondition) obj;
		return CollectionUtils.genericCompare(this.nameLength, other.nameLength);
	}

	@Override
	public int hashCode() {
		int hc = super.hashCode();
		hc = 31 * hc + nameLength;
		return hc;
	}

	/**
	 * Returns the name of the condition for use in logging.
	 *
	 * @return The name of the condition.
	 */
	protected String getConditionName() {
		return DEBUG_NAME;
	}

}
