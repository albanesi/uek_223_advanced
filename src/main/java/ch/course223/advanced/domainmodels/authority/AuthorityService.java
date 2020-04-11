package ch.course223.advanced.domainmodels.authority;

import ch.course223.advanced.core.ExtendedService;

public interface AuthorityService extends ExtendedService<Authority> {

	Authority findByName(String name);

	void deleteByName(String name);
}
