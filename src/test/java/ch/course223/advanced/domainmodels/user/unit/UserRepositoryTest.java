package ch.course223.advanced.domainmodels.user.unit;

import ch.course223.advanced.domainmodels.authority.Authority;
import ch.course223.advanced.domainmodels.role.Role;
import ch.course223.advanced.domainmodels.user.User;
import ch.course223.advanced.domainmodels.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
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

    private  User user1;
    private  User user2;

    private  String uuidUser1;
    private  String uuidUser2;

    private  Set<Authority> basicUserAuthorities;
    private  Set<Authority> adminUserAuthorities;

    private  Set<Role> basicUserRoles;
    private  Set<Role> adminUserRoles;
    private  Set<Role> mixedUserRoles;

    @Before
    public static void setUp(){

        Set<Authority> basicUserAuthorities = Stream.of(new Authority().setName("USER_SEE_OWN"), new Authority().setName("USER_MODIFY_OWN")).collect(Collectors.toSet());
        Set<Authority> adminUserAuthorities = Stream.of(new Authority().setName("USER_SEE_OWN"), new Authority().setName("USER_SEE_GLOBAL"), new Authority().setName("USER_CREATE"), new Authority().setName("USER_MODIFY_OWN"), new Authority().setName("USER_MODIFY_GLOBAL"), new Authority().setName("USER_DELETE")).collect(Collectors.toSet());

        Set<Role> basicUserRoles = Stream.of(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities)).collect(Collectors.toSet());
        Set<Role> adminUserRoles = Stream.of(new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities)).collect(Collectors.toSet());
        Set<Role> mixedUserRoles = Stream.of(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities), new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities)).collect(Collectors.toSet());

        user1 = new User().setFirstName("John").setLastName("Doe").setEmail("john.doe@noseryoung.ch").setRoles(mixedUserRoles).setEnabled(true).setPassword(new BCryptPasswordEncoder().encode("12345"));
        user2 = new User().setFirstName("Jane").setLastName("Doe").setEmail("jane.doe@noseryoung.ch").setRoles(mixedUserRoles).setEnabled(true).setPassword(new BCryptPasswordEncoder().encode("12345"));

    }

    @Test
    public void findById_requestUserById_returnsUser() {
        String userIdFromDB = testEntityManager.persistAndGetId(user1, String.class);

        Optional<User> userFromDB = userRepository.findById(userIdFromDB);

        Assertions.assertThat(userFromDB).isEqualTo(Optional.ofNullable(user1));
        Assertions.assertThat(userFromDB.get().getId()).isEqualTo(userIdFromDB);
        Assertions.assertThat(userFromDB.get().getFirstName()).isEqualTo(user1.getFirstName());
        Assertions.assertThat(userFromDB.get().getLastName()).isEqualTo(user1.getLastName());
        Assertions.assertThat(userFromDB.get().getEmail()).isEqualTo(user1.getEmail());
        Assertions.assertThat(userFromDB.get().getEnabled()).isEqualTo(user1.getEnabled());
        Assertions.assertThat(userFromDB.get().getPassword()).isEqualTo(user1.getPassword());
        Assertions.assertThat(userFromDB.get().getRoles()).isEqualTo(user1.getRoles());

    }

    @Test
    public void findAll_requestAllUsers_returnsAllUsers() {
        testEntityManager.persistAndFlush(user1);
        testEntityManager.persistAndFlush(user2);

        List<User> usersToReturn = Arrays.asList(user1, user2);

        List<User> usersFromDB = userRepository.findAll();

        Assertions.assertThat(usersFromDB.stream().map(User::getFirstName).toArray()).isEqualTo(usersToReturn.stream().map(User::getFirstName).toArray());
        Assertions.assertThat(usersFromDB.stream().map(User::getLastName).toArray()).isEqualTo(usersToReturn.stream().map(User::getLastName).toArray());
        Assertions.assertThat(usersFromDB.stream().map(User::getEmail).toArray()).isEqualTo(usersToReturn.stream().map(User::getEmail).toArray());
        Assertions.assertThat(usersFromDB.stream().map(User::getEnabled).toArray()).isEqualTo(usersToReturn.stream().map(User::getEnabled).toArray());
        Assertions.assertThat(usersFromDB.stream().map(User::getPassword).toArray()).isEqualTo(usersToReturn.stream().map(User::getPassword).toArray());
        Assertions.assertThat(usersFromDB.stream().map(User::getRoles).toArray()).isEqualTo(usersToReturn.stream().map(User::getRoles).toArray());

    }

    @Test
    public void create_deliverUserToCreate_returnCreatedUser(){

        User savedUser = userRepository.save(user1);

        Assertions.assertThat(testEntityManager.find(User.class, savedUser.getId())).isEqualTo(user1);
        Assertions.assertThat(savedUser.getFirstName()).isEqualTo(user1.getFirstName());
        Assertions.assertThat(savedUser.getLastName()).isEqualTo(user1.getLastName());
        Assertions.assertThat(savedUser.getEmail()).isEqualTo(user1.getEmail());
        Assertions.assertThat(savedUser.getEnabled()).isEqualTo(user1.getEnabled());
        Assertions.assertThat(savedUser.getPassword()).isEqualTo(user1.getPassword());
        Assertions.assertThat(savedUser.getRoles()).isEqualTo(user1.getRoles());

    }

    @Test
    public void updateUserById_requestUserToBeUpdated_returnUpdatedUser(){

        User userToUpdate = new User().setFirstName("Jane").setLastName("Herbert").setEmail("jane.herbert@noseryoung.ch").setRoles(mixedUserRoles).setEnabled(true).setPassword(new BCryptPasswordEncoder().encode("newPassword"));

        User updatedUser = userRepository.save(userToUpdate);

        Assertions.assertThat(testEntityManager.find(User.class, updatedUser.getId())).isEqualTo(userToUpdate);
        Assertions.assertThat(updatedUser.getFirstName()).isEqualTo(userToUpdate.getFirstName());
        Assertions.assertThat(updatedUser.getLastName()).isEqualTo(userToUpdate.getLastName());
        Assertions.assertThat(updatedUser.getEmail()).isEqualTo(userToUpdate.getEmail());
        Assertions.assertThat(updatedUser.getEnabled()).isEqualTo(userToUpdate.getEnabled());
        Assertions.assertThat(updatedUser.getPassword()).isEqualTo(userToUpdate.getPassword());
        Assertions.assertThat(updatedUser.getRoles()).isEqualTo(userToUpdate.getRoles());

        testEntityManager.remove(updatedUser);
    }

    @Test
    public void deleteUserById_requestADeletionOfUserById_returnAppropriateState(){

        String userIdFromDB = testEntityManager.persistAndGetId(user1, String.class);

        userRepository.deleteById(userIdFromDB);

        Assertions.assertThat(userRepository.findById(userIdFromDB)).isEmpty();

    }

}
