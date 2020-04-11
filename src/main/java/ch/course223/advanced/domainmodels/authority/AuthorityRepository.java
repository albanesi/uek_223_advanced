package ch.course223.advanced.domainmodels.authority;

import ch.course223.advanced.core.ExtendedJpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends ExtendedJpaRepository<Authority> {

	Optional<Authority> findByName(String name);

	void deleteByName(String name);

	@Modifying
	@Transactional
	@Query(nativeQuery = true, value = "delete from role_authority where authority_id = ?")
	void deleteRelationsToRolesById(String id);
}
