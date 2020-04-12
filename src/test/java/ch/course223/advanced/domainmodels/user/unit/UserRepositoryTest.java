package ch.course223.advanced.domainmodels.user.unit;

import ch.course223.advanced.domainmodels.authority.Authority;
import ch.course223.advanced.domainmodels.role.Role;
import ch.course223.advanced.domainmodels.user.User;
import ch.course223.advanced.domainmodels.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    private static User user1;
    private static User user2;

    private static Set<Authority> basicUserAuthorities;
    private static Set<Authority> adminUserAuthorities;

    private static Set<Role> basicUserRoles;
    private static Set<Role> adminUserRoles;
    private static Set<Role> mixedUserRoles;

    @BeforeClass
    public static void setUp(){

        basicUserAuthorities = Stream.of(new Authority().setName("USER_SEE_OWN"), new Authority().setName("USER_MODIFY_OWN")).collect(Collectors.toSet());
        adminUserAuthorities = Stream.of(new Authority().setName("USER_SEE_OWN"), new Authority().setName("USER_SEE_GLOBAL"), new Authority().setName("USER_CREATE"), new Authority().setName("USER_MODIFY_OWN"), new Authority().setName("USER_MODIFY_GLOBAL"), new Authority().setName("USER_DELETE")).collect(Collectors.toSet());

        basicUserRoles = Stream.of(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities)).collect(Collectors.toSet());
        adminUserRoles = Stream.of(new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities)).collect(Collectors.toSet());
        mixedUserRoles = Stream.of(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities), new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities)).collect(Collectors.toSet());


        user1 = new User().setFirstName("John").setLastName("Doe").setEmail("john.doe@noseryoung.ch").setRoles(mixedUserRoles).setEnabled(true).setPassword(new BCryptPasswordEncoder().encode("12345"));
        user1.setId("86330e33-8a0a-4043-b9bf-7766c93ba078");
        user2 = new User().setFirstName("Jane").setLastName("Doe").setEmail("jane.doe@noseryoung.ch").setRoles(mixedUserRoles).setEnabled(true).setPassword(new BCryptPasswordEncoder().encode("12345"));
        user2.setId("ebbf2382-60c8-4358-9f77-f0dc250614c8");

    }

    @Test
    public void findById_requestUserById_returnsUser() {
        testEntityManager.persistAndFlush(user1);

        Assertions.assertThat(userRepository.findById(user1.getId())).isEqualTo(user1.getId());

    }

}
