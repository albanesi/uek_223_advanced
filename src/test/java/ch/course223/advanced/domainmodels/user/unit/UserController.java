package ch.course223.advanced.domainmodels.user.unit;

import ch.course223.advanced.domainmodels.authority.Authority;
import ch.course223.advanced.domainmodels.authority.AuthorityDTO;
import ch.course223.advanced.domainmodels.role.Role;
import ch.course223.advanced.domainmodels.role.RoleDTO;
import ch.course223.advanced.domainmodels.user.User;
import ch.course223.advanced.domainmodels.user.UserDTO;
import ch.course223.advanced.domainmodels.user.UserService;
import ch.course223.advanced.error.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class UserController {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    private static Set<Authority> basicUserAuthorities;
    private static Set<Authority> adminUserAuthorities;
    private static Set<AuthorityDTO> basicUserAuthoritiesDTOS;
    private static Set<AuthorityDTO> adminUserAuthoritiesDTOS;

    private static Set<Role> basicUserRoles;
    private static Set<Role> adminUserRoles;
    private static Set<Role> mixedUserRoles;
    private static Set<RoleDTO> mixedUserRolesDTOS;

    @BeforeClass
    public static void setUp(){

        basicUserAuthorities = Stream.of(new Authority().setName("USER_SEE_OWN"), new Authority().setName("USER_MODIFY_OWN")).collect(Collectors.toSet());
        adminUserAuthorities = Stream.of(new Authority().setName("USER_SEE_OWN"), new Authority().setName("USER_SEE_GLOBAL"), new Authority().setName("USER_CREATE"), new Authority().setName("USER_MODIFY_OWN"), new Authority().setName("USER_MODIFY_GLOBAL"), new Authority().setName("USER_DELETE")).collect(Collectors.toSet());
        basicUserAuthoritiesDTOS = Stream.of(new AuthorityDTO().setName("USER_SEE_OWN"), new AuthorityDTO().setName("USER_MODIFY_OWN")).collect(Collectors.toSet());
        adminUserAuthoritiesDTOS = Stream.of(new AuthorityDTO().setName("USER_SEE_OWN"), new AuthorityDTO().setName("USER_SEE_GLOBAL"), new AuthorityDTO().setName("USER_CREATE"), new AuthorityDTO().setName("USER_MODIFY_OWN"), new AuthorityDTO().setName("USER_MODIFY_GLOBAL"), new AuthorityDTO().setName("USER_DELETE")).collect(Collectors.toSet());

        basicUserRoles = Stream.of(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities)).collect(Collectors.toSet());
        adminUserRoles = Stream.of(new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities)).collect(Collectors.toSet());
        mixedUserRoles = Stream.of(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities), new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities)).collect(Collectors.toSet());
        mixedUserRolesDTOS = Stream.of(new RoleDTO().setName("BASIC_USER").setAuthorities(basicUserAuthoritiesDTOS), new RoleDTO().setName("ADMIN_USER").setAuthorities(adminUserAuthoritiesDTOS)).collect(Collectors.toSet());

    }

    @Test
    @WithMockUser
    public void findById_requestUserById_returnsUser() throws Exception {

        User adminUser = new User().setRoles(mixedUserRoles).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

        given(userService.findById(anyString())).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new BadRequestException();
            return (adminUser);
        });

        UUID uuid = UUID.randomUUID();

        mvc.perform(
                MockMvcRequestBuilders.get("/users/{id}", uuid.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(adminUser.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(adminUser.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(adminUser.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].name").value(Matchers.containsInAnyOrder(mixedUserRoles.stream().map(Role::getName).toArray())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].authorities[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(adminUserAuthorities.stream().map(Authority::getName).toArray(), basicUserAuthorities.stream().map(Authority::getName).toArray()))));

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).findById(stringCaptor.capture());
        Assert.assertEquals(uuid.toString(), stringCaptor.getValue());
    }

    @Test
    @WithMockUser
    public void findAll_requestAllUsers_returnsAllUsers() throws Exception {

        User basicUser = new User().setRoles(basicUserRoles).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");
        User adminUser = new User().setRoles(adminUserRoles).setFirstName("john").setLastName("doe").setEmail("john.doe@noseryoung.ch");

        given(userService.findAll()).willReturn(Arrays.asList(adminUser, basicUser));

        mvc.perform(
                MockMvcRequestBuilders.get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].firstName").value(Matchers.containsInAnyOrder(basicUser.getFirstName(),adminUser.getFirstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].lastName").value(Matchers.containsInAnyOrder(basicUser.getLastName(),adminUser.getLastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].email").value(Matchers.containsInAnyOrder(basicUser.getEmail(),adminUser.getEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].roles[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(basicUserRoles.stream().map(Role::getName).toArray(), adminUserRoles.stream().map(Role::getName).toArray()))))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].roles[*].authorities[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(adminUserAuthorities.stream().map(Authority::getName).toArray(), basicUserAuthorities.stream().map(Authority::getName).toArray()))));

        verify(userService, times(1)).findAll();

    }

    @Test
    @WithMockUser
    public void create_deliverUserDTOToCreate_returnCreatedUserDTO() throws Exception {

        UserDTO userDTO = new UserDTO().setRoles(mixedUserRolesDTOS).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

        String userDTOAsJsonString = new ObjectMapper().writeValueAsString(userDTO);

        given(userService.save(any(User.class))).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new BadRequestException();
            UUID uuid = UUID.randomUUID();
            User user = invocation.getArgument(0);
            return user.setId(uuid.toString());
        });

        mvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content(userDTOAsJsonString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].name").value(Matchers.containsInAnyOrder(mixedUserRolesDTOS.stream().map(RoleDTO::getName).toArray())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].authorities[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(basicUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray()))));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).save(userCaptor.capture());
        Assert.assertEquals(userDTO.getFirstName(), userCaptor.getValue().getFirstName());
        Assert.assertEquals(userDTO.getLastName(), userCaptor.getValue().getLastName());
        Assert.assertEquals(userDTO.getEmail(), userCaptor.getValue().getEmail());
        Assert.assertThat(mixedUserRolesDTOS.stream().map(RoleDTO::getName).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getName).toArray()));
        Assert.assertThat(Arrays.stream(ArrayUtils.addAll(basicUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray())).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getAuthorities).flatMap(Collection::stream).map(Authority::getName).toArray()));
    }

    @Test
    @WithMockUser
    public void create_requestUserDTOWithMissingEmailToBeCreated_returnBadRequest() throws Exception {

        UserDTO adminUserDTO = new UserDTO().setRoles(mixedUserRolesDTOS).setFirstName("jane").setLastName("doe");
        String userDTOAsJsonString = new ObjectMapper().writeValueAsString(adminUserDTO);

        given(userService.save(any(User.class))).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new BadRequestException();
            UUID uuid = UUID.randomUUID();
            User mirrorOfGivenDTO = invocation.getArgument(0);
            return mirrorOfGivenDTO.setId(uuid.toString());
        });

        mvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content(userDTOAsJsonString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser
    public void updateUserById_requestUserDTOToBeUpdated_returnUpdatedUserDTO() throws Exception {

        UserDTO userDTO = new UserDTO().setRoles(mixedUserRolesDTOS).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

        String userDTOAsJsonString = new ObjectMapper().writeValueAsString(userDTO);

        given(userService.updateById(anyString(), any(User.class))).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0)) || "non-existent".equals(invocation.getArgument(1))) throw new BadRequestException();
            return ((User) invocation.getArgument(1)).setId(invocation.getArgument(0));
        });

        UUID uuid = UUID.randomUUID();

        mvc.perform(
                MockMvcRequestBuilders.put("/users/{id}", uuid.toString())
                        .content(userDTOAsJsonString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].name").value(Matchers.containsInAnyOrder(mixedUserRolesDTOS.stream().map(RoleDTO::getName).toArray())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].authorities[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(basicUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray()))));

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).updateById(stringCaptor.capture(), userCaptor.capture());
        Assert.assertEquals(uuid.toString(), stringCaptor.getValue());
        Assert.assertEquals(userDTO.getFirstName(), userCaptor.getValue().getFirstName());
        Assert.assertEquals(userDTO.getLastName(), userCaptor.getValue().getLastName());
        Assert.assertEquals(userDTO.getEmail(), userCaptor.getValue().getEmail());
        Assert.assertThat(mixedUserRolesDTOS.stream().map(RoleDTO::getName).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getName).toArray()));
        Assert.assertThat(Arrays.stream(ArrayUtils.addAll(basicUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray())).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getAuthorities).flatMap(Collection::stream).map(Authority::getName).toArray()));
    }

    @Test
    @WithMockUser
    public void updateUserById_requestUserDTOWithFalseEmail_returnBadRequest() throws Exception {

        UserDTO adminUserDTO = new UserDTO().setRoles(mixedUserRolesDTOS).setFirstName("jane").setLastName("doe");
        String userDTOAsJsonString = new ObjectMapper().writeValueAsString(adminUserDTO);

        given(userService.updateById(anyString(), any(User.class))).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0)) || "non-existent".equals(invocation.getArgument(1))) throw new BadRequestException();
            return ((User) invocation.getArgument(1)).setId(invocation.getArgument(0));
        });

        mvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content(userDTOAsJsonString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(userService, never()).updateById(anyString(), any(User.class));

    }


    @Test
    @WithMockUser
    public void deleteUserById_requestADeletionOfUserById_returnAppropriateState() throws Exception {

        given(userService.deleteById(anyString())).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new BadRequestException();
            return null;
        });

        UUID uuid = UUID.randomUUID();

        mvc.perform(
                MockMvcRequestBuilders.delete("/users/{id}", uuid.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).deleteById(stringCaptor.capture());
        Assert.assertEquals(uuid.toString(),stringCaptor.getValue());
    }

}