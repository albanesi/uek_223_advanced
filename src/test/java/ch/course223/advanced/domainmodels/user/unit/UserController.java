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
import org.junit.Before;
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

    @Before
    public void setUp(){
    }

    @Test
    @WithMockUser
    public void findById_requestUserById_returnsUser() throws Exception {

        Set<Authority> basicUserAuthorities = new HashSet<Authority>();
        basicUserAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        basicUserAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));

        Set<Authority> adminUserAuthorities = new HashSet<Authority>();
        adminUserAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        adminUserAuthorities.add(new Authority().setName("USER_SEE_GLOBAL"));
        adminUserAuthorities.add(new Authority().setName("USER_CREATE"));
        adminUserAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));
        adminUserAuthorities.add(new Authority().setName("USER_MODIFY_GLOBAL"));
        adminUserAuthorities.add(new Authority().setName("USER_DELETE"));

        Set<Role> userRoles = new HashSet<Role>();
        userRoles.add(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities));
        userRoles.add(new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities));

        User adminUser = new User().setRoles(userRoles).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].name").value(Matchers.containsInAnyOrder(userRoles.stream().map(Role::getName).toArray())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].authorities[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(adminUserAuthorities.stream().map(Authority::getName).toArray(), basicUserAuthorities.stream().map(Authority::getName).toArray()))));
                //.andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].name").value(Matchers.contains("BASIC_USER", "BASIC_USER")));

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).findById(stringCaptor.capture());
        Assert.assertEquals(uuid.toString(), stringCaptor.getValue());
    }

    @Test
    @WithMockUser
    public void findAll_requestAllUsers_returnsAllUsers() throws Exception {
        Set<Authority> basicUserAuthorities = new HashSet<Authority>();
        basicUserAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        basicUserAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));

        Set<Authority> adminUserAuthorities = new HashSet<Authority>();
        adminUserAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        adminUserAuthorities.add(new Authority().setName("USER_SEE_GLOBAL"));
        adminUserAuthorities.add(new Authority().setName("USER_CREATE"));
        adminUserAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));
        adminUserAuthorities.add(new Authority().setName("USER_MODIFY_GLOBAL"));
        adminUserAuthorities.add(new Authority().setName("USER_DELETE"));

        Set<Role> basicUserRoles = new HashSet<Role>();
        basicUserRoles.add(new Role().setName("BASIC_USER").setAuthorities(basicUserAuthorities));

        Set<Role> adminUserRoles = new HashSet<Role>();
        adminUserRoles.add(new Role().setName("ADMIN_USER").setAuthorities(adminUserAuthorities));

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

        Set<AuthorityDTO> basicUserAuthorityDTOS = new HashSet<>();
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));

        Set<AuthorityDTO> adminUserAuthoritiesDTOS = new HashSet<>();
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_SEE_GLOBAL"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_CREATE"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_MODIFY_GLOBAL"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_DELETE"));

        Set<RoleDTO> userRoleDTOS = new HashSet<RoleDTO>();
        userRoleDTOS.add(new RoleDTO().setName("BASIC_USER").setAuthorities(basicUserAuthorityDTOS));
        userRoleDTOS.add(new RoleDTO().setName("ADMIN_USER").setAuthorities(adminUserAuthoritiesDTOS));


        UserDTO userDTO = new UserDTO().setRoles(userRoleDTOS).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].name").value(Matchers.containsInAnyOrder(userRoleDTOS.stream().map(RoleDTO::getName).toArray())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].authorities[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(basicUserAuthorityDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray()))));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).save(userCaptor.capture());
        Assert.assertEquals(userDTO.getFirstName(), userCaptor.getValue().getFirstName());
        Assert.assertEquals(userDTO.getLastName(), userCaptor.getValue().getLastName());
        Assert.assertEquals(userDTO.getEmail(), userCaptor.getValue().getEmail());
        Assert.assertThat(userRoleDTOS.stream().map(RoleDTO::getName).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getName).toArray()));
        Assert.assertThat(Arrays.stream(ArrayUtils.addAll(basicUserAuthorityDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray())).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getAuthorities).flatMap(Collection::stream).map(Authority::getName).toArray()));
    }

    @Test
    @WithMockUser
    public void create_requestUserDTOWithMissingEmailToBeCreated_returnBadRequest() throws Exception {
        Set<AuthorityDTO> basicUserAuthorityDTOS = new HashSet<>();
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));

        Set<AuthorityDTO> adminUserAuthoritiesDTOS = new HashSet<>();
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_SEE_GLOBAL"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_CREATE"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_MODIFY_GLOBAL"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_DELETE"));

        Set<RoleDTO> userRoleDTOS = new HashSet<RoleDTO>();
        userRoleDTOS.add(new RoleDTO().setName("BASIC_USER").setAuthorities(basicUserAuthorityDTOS));
        userRoleDTOS.add(new RoleDTO().setName("ADMIN_USER").setAuthorities(adminUserAuthoritiesDTOS));

        UserDTO adminUserDTO = new UserDTO().setRoles(userRoleDTOS).setFirstName("jane").setLastName("doe");
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

        Set<AuthorityDTO> basicUserAuthorityDTOS = new HashSet<>();
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));

        Set<AuthorityDTO> adminUserAuthoritiesDTOS = new HashSet<>();
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_SEE_GLOBAL"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_CREATE"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_MODIFY_GLOBAL"));
        adminUserAuthoritiesDTOS.add(new AuthorityDTO().setName("USER_DELETE"));

        Set<RoleDTO> userRoleDTOS = new HashSet<RoleDTO>();
        userRoleDTOS.add(new RoleDTO().setName("BASIC_USER").setAuthorities(basicUserAuthorityDTOS));
        userRoleDTOS.add(new RoleDTO().setName("ADMIN_USER").setAuthorities(adminUserAuthoritiesDTOS));

        UserDTO userDTO = new UserDTO().setRoles(userRoleDTOS).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].name").value(Matchers.containsInAnyOrder(userRoleDTOS.stream().map(RoleDTO::getName).toArray())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[*].authorities[*].name").value(Matchers.containsInAnyOrder(ArrayUtils.addAll(basicUserAuthorityDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray()))));

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).updateById(stringCaptor.capture(), userCaptor.capture());
        Assert.assertEquals(uuid.toString(), stringCaptor.getValue());
        Assert.assertEquals(userDTO.getFirstName(), userCaptor.getValue().getFirstName());
        Assert.assertEquals(userDTO.getLastName(), userCaptor.getValue().getLastName());
        Assert.assertEquals(userDTO.getEmail(), userCaptor.getValue().getEmail());
        Assert.assertThat(userRoleDTOS.stream().map(RoleDTO::getName).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getName).toArray()));
        Assert.assertThat(Arrays.stream(ArrayUtils.addAll(basicUserAuthorityDTOS.stream().map(AuthorityDTO::getName).toArray(), adminUserAuthoritiesDTOS.stream().map(AuthorityDTO::getName).toArray())).collect(Collectors.toList()), Matchers.containsInAnyOrder(userCaptor.getValue().getRoles().stream().map(Role::getAuthorities).flatMap(Collection::stream).map(Authority::getName).toArray()));
    }

    /*@Test
    @WithMockUser
    public void updateUserById_requestUserDTOWithFalseEmail_returnBadRequest() throws Exception {

        mvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(userService, never()).updateById(anyString(), any(User.class));

    }*/


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