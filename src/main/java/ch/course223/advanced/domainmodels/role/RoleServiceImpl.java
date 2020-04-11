package ch.course223.advanced.domainmodels.role;

import ch.course223.advanced.core.ExtendedServiceImpl;
import ch.course223.advanced.domainmodels.authority.AuthorityService;
import ch.course223.advanced.error.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class RoleServiceImpl extends ExtendedServiceImpl<Role> implements RoleService {

	private AuthorityService authorityService;

	@Autowired
	public RoleServiceImpl(RoleRepository repository, AuthorityService authorityService) {
		super(repository);
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

		Role oldRole = findById(id);
		entity.setId(oldRole.getId());

		entity.setAuthorities(oldRole.getAuthorities());

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

		return repository.save(role);
	}

	@Override
	public Role removeAuthorityFromRole(String roleId, String authorityId) throws NoSuchElementException {
		Role role = findById(roleId);

		role.getAuthorities().remove(authorityService.findById(authorityId));

		return repository.save(role);
	}
}
