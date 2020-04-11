package ch.course223.advanced.domainmodels.authority;

import ch.course223.advanced.core.ExtendedServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
class AuthorityServiceImpl extends ExtendedServiceImpl<Authority> implements AuthorityService {

	@Autowired
	public AuthorityServiceImpl(AuthorityRepository repository) {
		super(repository);
	}

	@Override
	public Authority findByName(String name) {
		return findOrThrow(((AuthorityRepository) repository).findByName(name));
	}

	@Override
	public void deleteByName(String name) {
		((AuthorityRepository) repository).deleteByName(name);
	}

	@Override
	public void deleteById(String id) throws NoSuchElementException {
		if(!repository.existsById(id)) throw new NoSuchElementException();

		((AuthorityRepository) repository).deleteRelationsToRolesById(id);

		repository.deleteById(id);
	}
}
