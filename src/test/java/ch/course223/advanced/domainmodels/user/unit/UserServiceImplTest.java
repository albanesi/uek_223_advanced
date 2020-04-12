package ch.course223.advanced.domainmodels.user.unit;

import ch.course223.advanced.domainmodels.authority.Authority;
import ch.course223.advanced.domainmodels.role.Role;
import ch.course223.advanced.domainmodels.user.User;
import ch.course223.advanced.domainmodels.user.UserRepository;
import ch.course223.advanced.domainmodels.user.UserServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test.properties")
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
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

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.ofNullable(user1));

        User foundUser = userService.findById(user1.getId());

        Assertions.assertThat(foundUser.getFirstName()).isEqualTo(user1.getFirstName());
        Assertions.assertThat(foundUser.getLastName()).isEqualTo(user1.getLastName());
        Assertions.assertThat(foundUser.getEmail()).isEqualTo(user1.getEmail());
        Assertions.assertThat(foundUser.getRoles()).isEqualTo(user1.getRoles());
        Assertions.assertThat(foundUser.getPassword()).isEqualTo(user1.getPassword());

        verify(userRepository, times(1)).findById(user1.getId());

    }

    @Test
    public void findAll_requestAllUsers_returnsAllUsers() {

        List<User> usersToReturn = Arrays.asList(user1, user2);

        Mockito.when(userRepository.findAll()).thenReturn(usersToReturn);

        List<User> users = userService.findAll();

        Assertions.assertThat(users.size()).isEqualTo(usersToReturn.size());
        Assertions.assertThat(users.stream().map(User::getFirstName).toArray()).isEqualTo(usersToReturn.stream().map(User::getFirstName).toArray());
        Assertions.assertThat(users.stream().map(User::getLastName).toArray()).isEqualTo(usersToReturn.stream().map(User::getLastName).toArray());
        Assertions.assertThat(users.stream().map(User::getEmail).toArray()).isEqualTo(usersToReturn.stream().map(User::getEmail).toArray());
        Assertions.assertThat(users.stream().map(User::getEnabled).toArray()).isEqualTo(usersToReturn.stream().map(User::getEnabled).toArray());

        verify(userRepository, times(1)).findAll();

    }

    @Test
    public void create_deliverUserToCreate_returnCreatedUser(){

        Mockito.when(userRepository.save(user1)).thenReturn(user1);

        User savedUser = userService.save(user1);

        Assertions.assertThat(savedUser).isEqualTo(user1);
        Assertions.assertThat(savedUser.getFirstName()).isEqualTo(user1.getFirstName());
        Assertions.assertThat(savedUser.getLastName()).isEqualTo(user1.getLastName());
        Assertions.assertThat(savedUser.getEmail()).isEqualTo(user1.getEmail());
        Assertions.assertThat(savedUser.getRoles()).isEqualTo(user1.getRoles());

        verify(userRepository, times(1)).save(user1);
    }

    @Test
    public void updateUserById_requestUserToBeUpdated_returnUpdatedUser(){

        User userToUpdate = new User().setFirstName("Jane").setLastName("Herbert").setEmail("jane.herbert@noseryoung.ch").setRoles(mixedUserRoles).setEnabled(true).setPassword(new BCryptPasswordEncoder().encode("newPassword"));

        Mockito.when(userRepository.existsById(user2.getId())).thenReturn(true);
        Mockito.when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        User updatedUser = userService.updateById(user2.getId(), userToUpdate);

        Assertions.assertThat(updatedUser.getFirstName()).isEqualTo(userToUpdate.getFirstName());
        Assertions.assertThat(updatedUser.getLastName()).isEqualTo(userToUpdate.getLastName());
        Assertions.assertThat(updatedUser.getEmail()).isEqualTo(userToUpdate.getEmail());
        Assertions.assertThat(updatedUser.getPassword()).isEqualTo(userToUpdate.getPassword());

    }

    @Test
    public void deleteUserById_requestADeletionOfUserById_returnAppropriateState(){

        Mockito.when(userRepository.existsById(user1.getId())).thenReturn(true);
        Mockito.doNothing().when(userRepository).deleteById(user1.getId());

        Assertions.assertThat(userService.deleteById(user1.getId())).isNull();

    }

























}
