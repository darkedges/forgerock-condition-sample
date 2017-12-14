package com.darkedges.openam.policyevaluation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mockito;

import com.sun.identity.entitlement.ConditionDecision;
import com.sun.identity.entitlement.EntitlementException;
import com.sun.identity.shared.debug.Debug;

@RunWith(BlockJUnit4ClassRunner.class)
public class DarkEdgesConditionTest {

	DarkEdgesCondition condition;
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		Debug debug = mock(Debug.class);
		condition = new DarkEdgesCondition();
	}

	@Test
	public void conditionStateShouldParseNameLength() {
		// Given

		// When
		condition.setState("{\"nameLength\": 5}");

		// Then
		assertThat(condition.getNameLength(), equalTo(5));
	}

	@Test
	public void conditionStateShouldContainAuthLevel() {

		// Given
		condition.setNameLength(5);

		// When
		String state = condition.getState();

		// Then
		assertThat(state, containsString("\"nameLength\":5"));
	}

	@Test()
	public void conditionShouldReturnFalseWithEmptySubject() throws EntitlementException {

		// Given
		String realm = "REALM";
		Subject subject = new Subject();
		String resourceName = "RESOURCE_NAME";
		Map<String, Set<String>> env = new HashMap<String, Set<String>>();

		// When
		condition.setState("{\"nameLength\": 5}");
		exception.expect(EntitlementException.class);
		exception.expectMessage(containsString("Subjects are required."));
		ConditionDecision cd = condition.evaluate(realm, subject, resourceName, env);

		// Then
		assertThat(cd.isSatisfied(), is(false));
		assertThat(cd.getAdvice(), is(aMapWithSize(0)));

	}

	@Test()
	public void conditionShouldReturnFalseWithSubjectNameLessThan5Subject() throws EntitlementException {

		// Given
		String realm = "REALM";
		Principal p = Mockito.mock(Principal.class);
		Mockito.when(p.getName()).thenReturn("dn=1234,our=people,dc=darkedges,dc=com");

		Subject subject = new Subject();
		subject.getPrincipals().add(p);
		String resourceName = "RESOURCE_NAME";
		Map<String, Set<String>> env = new HashMap<String, Set<String>>();

		// When
		condition.setState("{\"nameLength\": 5}");
		ConditionDecision cd = condition.evaluate(realm, subject, resourceName, env);

		// Then
		assertThat(cd.isSatisfied(), is(false));
		assertThat(cd.getAdvice(), is(aMapWithSize(1)));
	}

	@Test()
	public void conditionShouldReturnTrueWithSubjectNameGreaterThan5Subject() throws EntitlementException {

		// Given
		String realm = "REALM";
		Principal p = Mockito.mock(Principal.class);
		Mockito.when(p.getName()).thenReturn("dn=12345,our=people,dc=darkedges,dc=com");

		Subject subject = new Subject();
		subject.getPrincipals().add(p);
		String resourceName = "RESOURCE_NAME";
		Map<String, Set<String>> env = new HashMap<String, Set<String>>();

		// When
		condition.setState("{\"nameLength\": 5}");
		ConditionDecision cd = condition.evaluate(realm, subject, resourceName, env);

		// Then
		assertThat(cd.isSatisfied(), is(true));
		assertThat(cd.getAdvice(), is(aMapWithSize(0)));
	}

	@Test()
	public void conditionShouldReturnExceptionWithInvalidSubject() throws EntitlementException {

		// Given
		String realm = "REALM";
		Principal p = Mockito.mock(Principal.class);
		Mockito.when(p.getName()).thenReturn("12345");

		Subject subject = new Subject();
		subject.getPrincipals().add(p);
		String resourceName = "RESOURCE_NAME";
		Map<String, Set<String>> env = new HashMap<String, Set<String>>();

		// When
		condition.setState("{\"nameLength\": 5}");
		exception.expect(EntitlementException.class);
		exception.expectMessage(containsString("Condition evaluation fails."));
		ConditionDecision cd = condition.evaluate(realm, subject, resourceName, env);

		// Then

		assertThat(cd.isSatisfied(), is(true));
		assertThat(cd.getAdvice(), is(aMapWithSize(0)));
	}

	@Test
	public void objectsEquals() {
		DarkEdgesCondition condition2 = new DarkEdgesCondition();
		String testString = "darkedges";
		condition.setState("{\"nameLength\": 5}");
		condition2.setState("{\"nameLength\": 5}");
		assertThat(condition.equals(condition2), is(true));
		condition2.setState("{\"nameLength\": 6}");
		assertThat(condition.equals(condition2), is(false));
		assertThat(condition.equals(testString), is(false));

	}

	@Test
	public void validate() throws EntitlementException {
		condition.setState("{\"nameLength\": 5}");
		condition.validate();

		condition.setState("{\"nameLength\": -1}");
		exception.expect(EntitlementException.class);
		exception.expectMessage(containsString("Invalid value {1} for property nameLength"));

		condition.validate();
	}
	
	@Test
	public void hashcode()  {
		condition.setState("{\"nameLength\": 5}");
		assertThat(condition.hashCode(), equalTo(5));
	}
}
