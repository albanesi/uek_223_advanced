package ch.course223.advanced.domainmodels.role;

import ch.nyp.noa.config.error.BadRequestException;
import ch.nyp.noa.config.generic.ExtendedServiceImpl;
import ch.nyp.noa.webContext.domain.authority.AuthorityService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class RoleServiceImpl extends ExtendedServiceImpl<Role> implements RoleService {

	private AuthorityService authorityService;

	@Autowired
	public RoleServiceImpl(RoleRepository repository, Logger logger, AuthorityService authorityService) {
		super(repository, logger);
		this.authorityService = authorityService;
	}

	@Override
	public Role findByName(String name) {
		return findOrThrow(((RoleRepository) repository).findByName(name));
	}

	@Override
	public void deleteByName(String name) {
		((RoleRepository) repository).deleteByName(name);
	}

	@Override
	public Role updateById(String id, Role entity) throws NoSuchElementException, BadRequestException {
		super.checkUpdatedEntityId(id, entity);

		logger.debug("Updating Role with ID '{}'", id);

		Role oldRole = findById(id);
		entity.setId(oldRole.getId());

		entity.setAuthorities(oldRole.getAuthorities());
		logger.debug("Set updated Roles Authorities to its old Authorities");

		return repository.save(entity);
	}

	@Override
	public void deleteById(String id) throws NoSuchElementException {
		if(!repository.existsById(id)) throw new NoSuchElementException();

		((RoleRepository) repository).deleteRelationsToUsersById(id);

		repository.deleteById(id);
	}

	@Override
	public Role addAuthorityToRole(String roleId, String authorityId) throws NoSuchElementException {
		Role role = findById(roleId);

		role.getAuthorities().add(authorityService.findById(authorityId));
		logger.debug("Added Authority with ID '{}' to Role with ID '{}'", authorityId, roleId);

		return repository.save(role);
	}

	@Override
	public Role removeAuthorityFromRole(String roleId, String authorityId) throws NoSuchElementException {
		Role role = findById(roleId);

		role.getAuthorities().remove(authorityService.findById(authorityId));
		logger.debug("Removed Authority with ID '{}' from Role with ID '{}'", authorityId, roleId);

		return repository.save(role);
	}
}
