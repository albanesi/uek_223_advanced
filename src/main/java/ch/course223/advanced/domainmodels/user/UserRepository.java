package ch.course223.advanced.domainmodels.user;

import ch.course223.advanced.core.ExtendedJpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ExtendedJpaRepository<User> {
}
