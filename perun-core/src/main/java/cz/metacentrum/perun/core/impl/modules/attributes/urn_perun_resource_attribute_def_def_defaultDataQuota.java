package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_resource_attribute_def_def_defaultDataQuota extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final String A_R_defaultDataLimit = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultDataLimit";
	private static final String A_F_readyForNewQuotas = AttributesManager.NS_FACILITY_ATTR_DEF + ":readyForNewQuotas";
	private static final Pattern testingPattern = Pattern.compile("^[0-9]+([.][0-9]+)?[KMGTPE]$");

	//Definition of K = KB, M = MB etc.
	final long K = 1024;
	final long M = K * 1024;
	final long G = M * 1024;
	final long T = G * 1024;
	final long P = T * 1024;
	final long E = P * 1024;

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException {
		String defaultDataQuotaNumber = attribute.valueAsString();

		//Check if attribute value has the right exp pattern (can be null)
		if (defaultDataQuotaNumber == null) return;

		Matcher testMatcher = testingPattern.matcher(attribute.valueAsString());
		if (!testMatcher.find())
			throw new WrongAttributeValueException(attribute, resource, "Format of quota must be something like ex.: 1.30M or 2500K, but it is " + attribute.getValue());

		//Get DefaultDataQuota value
		BigDecimal quotaNumber = ModulesUtilsBlImpl.getNumberAndUnitFromString(defaultDataQuotaNumber).getLeft();

		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongAttributeValueException(attribute, resource, null, attribute + " can't be less than 0.");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Attribute attrDefaultDataLimit;
		String defaultDataLimit = null;

		//Get DefaultDataQuota value
		Pair<BigDecimal, String> quotaNumberAndLetter = ModulesUtilsBlImpl.getNumberAndUnitFromString(attribute.valueAsString());
		BigDecimal quotaNumber = quotaNumberAndLetter.getLeft();
		String defaultDataQuotaLetter = quotaNumberAndLetter.getRight();

		//Get DefaultDataLimit attribute
		try {
			attrDefaultDataLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultDataLimit);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute with defaultDataLimit from resource " + resource.getId() + " could not obtained.", ex);
		}

		if (attrDefaultDataLimit != null) defaultDataLimit = attrDefaultDataLimit.valueAsString();

		//Get DefaultDataLimit value
		Pair<BigDecimal, String> limitNumberAndLetter = ModulesUtilsBlImpl.getNumberAndUnitFromString(defaultDataLimit);
		BigDecimal limitNumber = limitNumberAndLetter.getLeft();
		String defaultDataLimitLetter = limitNumberAndLetter.getRight();

		if (limitNumber.compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new WrongReferenceAttributeValueException(attribute, attrDefaultDataLimit, resource, null, resource, null, attrDefaultDataLimit + " cant be less than 0.");
		}

		//Compare DefaultDataQuota with DefaultDataLimit
		if (quotaNumber.compareTo(BigDecimal.valueOf(0)) == 0) {
			if (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDefaultDataLimit, resource, null, resource, null, "Try to set unlimited quota, but limit is still " + limitNumber + defaultDataLimitLetter);
			}
		} else if (limitNumber.compareTo(BigDecimal.valueOf(0)) != 0) {

			switch (defaultDataLimitLetter) {
				case "K":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(K));
					break;
				case "M":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(M));
					break;
				case "T":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(T));
					break;
				case "P":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(P));
					break;
				case "E":
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(E));
					break;
				default:
					limitNumber = limitNumber.multiply(BigDecimal.valueOf(G));
					break;
			}

			switch (defaultDataQuotaLetter) {
				case "K":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(K));
					break;
				case "M":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(M));
					break;
				case "T":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(T));
					break;
				case "P":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(P));
					break;
				case "E":
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(E));
					break;
				default:
					quotaNumber = quotaNumber.multiply(BigDecimal.valueOf(G));
					break;
			}

			if (limitNumber.compareTo(quotaNumber) < 0) {
				throw new WrongReferenceAttributeValueException(attribute, attrDefaultDataLimit, resource, null, resource, null, attribute + " must be less than or equals to " + defaultDataLimit);
			}
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		//if this is setting of the new attribute value, check if old quota attributes are supported on the facility
		if(attribute.getValue() != null) {
			try {
				Facility facility = session.getPerunBl().getResourcesManagerBl().getFacility(session, resource);
				Attribute readyForNewQuotasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, facility, A_F_readyForNewQuotas);
				//You shouldn't be allowed to set old quota attributes if facility is set for new quotas attributes (to prevent wrong setting of quotas)
				if(readyForNewQuotasAttribute.getValue() != null && readyForNewQuotasAttribute.valueAsBoolean()) {
					throw new WrongReferenceAttributeValueException(attribute, readyForNewQuotasAttribute, resource, null, facility, null, "For this facility the new quotas attributes are used! You are trying to set the old ones.");
				}
			} catch (AttributeNotExistsException ex) {
				//if attribute not exists, it is the same like it was set on false, which is ok
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(A_R_defaultDataLimit);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("defaultDataQuota");
		attr.setDisplayName("Default data quota");
		attr.setType(String.class.getName());
		attr.setDescription("Soft quota including units (M, G, T, ...), G is default.");
		return attr;
	}
}
