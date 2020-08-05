package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

public class CreationEventProcessor extends AbstractEventProcessor {

	private final static Logger log = LoggerFactory.getLogger(CreationEventProcessor.class);

	@Override
	public void processEvent(String msg, MessageBeans beans) {
		for (int beanFlag : beans.getPresentBeansFlags()) {
			try {
				switch (beanFlag) {
					case MessageBeans.GROUP_F:
						if (beans.getParentGroup() != null) {
							log.debug("Adding subgroup {} to group {}", beans.getGroup(), beans.getParentGroup());
							perunGroup.addGroupAsSubGroup(beans.getGroup(), beans.getParentGroup());
							break;
						}
						log.debug("Adding new group: {}", beans.getGroup());
						perunGroup.addGroup(beans.getGroup());
						break;

					case MessageBeans.RESOURCE_F:
						log.debug("Adding new resource: {}", beans.getResource());
						perunResource.addResource(beans.getResource());
						break;

					case MessageBeans.FACILITY_F:
						log.debug("Adding new facility: {}", beans.getFacility());
						perunFacility.addFacility(beans.getFacility());
						break;

					case MessageBeans.USER_F:
						log.debug("Adding new user: {}", beans.getUser());
						perunUser.addUser(beans.getUser());
						break;

					case MessageBeans.VO_F:
						if (beans.getGroup() != null) {
							break;
						}
						log.debug("Adding new VO: {}", beans.getVo());
						perunVO.addVo(beans.getVo());
						break;

					default:
						break;
				}
			} catch (NamingException | InternalErrorException e) {
				log.error("Error creating new entry: {}", e.getMessage());
			}
		}

	}

}
