package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_resource_attribute_def_def_defaultFilesQuotaTest {

	private urn_perun_resource_attribute_def_def_defaultFilesQuota classInstance;
	private Attribute attributeToCheck;
	private Resource resource = new Resource();
	private PerunSessionImpl sess;
	private Attribute reqAttribute;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_resource_attribute_def_def_defaultFilesQuota();
		attributeToCheck = new Attribute();
		sess = mock(PerunSessionImpl.class);
		reqAttribute = new Attribute();

		PerunBl perunBl = mock(PerunBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(attributesManagerBl.getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultFilesLimit")).thenReturn(reqAttribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testSyntaxWithWrongValue() throws Exception {
		System.out.println("testSyntaxWithWrongValue()");
		attributeToCheck.setValue(-1);

		classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
	}

	@Test
	public void testSyntaxCorrect() throws Exception {
		System.out.println("testSyntaxCorrect()");
		attributeToCheck.setValue(1);

		classInstance.checkAttributeSyntax(sess, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testSemanticsWithValueIncompatibleWithDefaultQuota() throws Exception {
		System.out.println("testSemanticsWithValueIncompatibleWithDefaultQuota()");
		attributeToCheck.setValue(null);
		reqAttribute.setValue(5);

		classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testSemanticsWithValueLesserThanDefaultLimit() throws Exception {
		System.out.println("testSemanticsWithValueLesserThanDefaultLimit()");
		attributeToCheck.setValue(6);
		reqAttribute.setValue(5);

		classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
	}

	@Test
	public void testSemanticsCorrect() throws Exception {
		System.out.println("testSemanticsCorrect()");
		attributeToCheck.setValue(4);
		reqAttribute.setValue(5);

		classInstance.checkAttributeSemantics(sess, resource, attributeToCheck);
	}
}
