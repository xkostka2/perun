package cz.metacentrum.perun.registrar;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;

/**
 * Interface for all registrar modules. They extend core registrar functionality and are
 * called after same functions in registrar.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface RegistrarModule {

	/**
	 * Sets registrar manager for usage in a module code.
	 *
	 * @param registrar Registrar bean
	 */
	void setRegistrar(RegistrarManager registrar);

	/**
	 * Creates a new application.
	 *
	 * <p>The method triggers approval for VOs with auto-approved applications.
	 * @param user user present in session
	 * @param application application
	 * @param data data
	 * @return stored app data
	 */
	List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException;

	/**
	 * Manually approves an application. Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param session who approves the application
	 * @param app application
	 */
	Application approveApplication(PerunSession session, Application app) throws UserNotExistsException, PrivilegeException, AlreadyAdminException, InternalErrorException, GroupNotExistsException, VoNotExistsException, MemberNotExistsException, AlreadyMemberException, ExternallyManagedException, WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, RegistrarException, ExtendMembershipException, ExtSourceNotExistsException, NotGroupMemberException;

	/**
	 * Manually rejects an application. Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param session who rejects the application
	 * @param app application
	 * @param reason optional reason of rejection displayed to user
	 */
	Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException;

	/**
	 * Calls custom logic before approving of application starts -> e.g do not validate passwords when this fails
	 *
	 * @param session who approves the application
	 * @param app application
	 * @throws CantBeApprovedException When application can't be approved
	 * @throws InternalErrorException When implementation fails
	 * @throws RegistrarException When implementation fails
	 * @throws PrivilegeException When caller is not authorized
	 */
	Application beforeApprove(PerunSession session, Application app) throws CantBeApprovedException, InternalErrorException, RegistrarException, PrivilegeException;

	/**
	 * Custom logic for checking method before application approval from GUI.
	 * Also called if application is set to auto-approval mode. In such case,
	 * if CantBeApprovedException is thrown, approval is stopped.
	 *
	 * @param session who approves the application
	 * @param app application
	 */
	void canBeApproved(PerunSession session, Application app) throws PerunException;

	/**
	 * Custom logic for checking method before application submission (retrieval of registration form) from GUI
	 *
	 * @param session who approves the application
	 * @param params custom params
	 */
	void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException;

}
