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

import static org.assertj.core.api.Assertions.assertThat;
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

        //Business Objects (used in findById, findAll)
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

        User adminUser = new User().setRoles(adminUserRoles).setFirstName("john").setLastName("doe").setEmail("john.doe@noseryoung.ch");
        User basicUser = new User().setRoles(basicUserRoles).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

        //Mocks
        given(userService.findById(anyString())).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new BadRequestException();
            return (basicUser);
        });

        given(userService.findAll()).willReturn(Arrays.asList(adminUser, basicUser));

        given(userService.save(any(User.class))).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new BadRequestException();
            UUID uuid = UUID.randomUUID();
            User userDTO = invocation.getArgument(0);
            return userDTO.setId(uuid.toString());
        });

        given(userService.updateById(anyString(), any(User.class))).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0)) || "non-existent".equals(invocation.getArgument(1))) throw new BadRequestException();
            return ((User) invocation.getArgument(1)).setId(invocation.getArgument(0));
        });

        given(userService.deleteById(anyString())).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new BadRequestException();
            return null;
        });

    }

    @Test
    @WithMockUser(roles = {"BASIC_USER"})
    public void findById_requestUserById_returnsUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        mvc.perform(
                MockMvcRequestBuilders.get("/users/{id}", uuid.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("jane"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("jane.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name").value("BASIC_USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].[0].name").value("USER_SEE_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].[1].name").value("USER_MODIFY_OWN"));

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).findById(anyString());
        assertThat(stringCaptor.getValue().equals(uuid.toString()));
    }

    @Test
    @WithMockUser
    public void findAll_requestAllUsers_returnsAllUsers() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("john.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("jane.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("john"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].firstName").value("jane"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].name").value("ADMIN_USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].name").value("BASIC_USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].[0].name").value("USER_SEE_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].[1].name").value("USER_MODIFY_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[0].name").value("USER_SEE_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[1].name").value("USER_MODIFY_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[2].name").value("USER_CREATE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[3].name").value("USER_MODIFY_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[4].name").value("USER_MODIFY_GLOBAL"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[5].name").value("USER_DELETE"));

        verify(userService, times(1)).findAll();
    }

    @Test
    @WithMockUser
    public void create_deliverUserDTOToCreate_returnCreatedUserDTO() throws Exception {
        Set<AuthorityDTO> basicUserAuthorityDTOS = new HashSet<AuthorityDTO>();
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));

        Set<RoleDTO> basicUserRoleDTOS = new HashSet<RoleDTO>();
        basicUserRoleDTOS.add(new RoleDTO().setName("BASIC_USER").setAuthorities(basicUserAuthorityDTOS));

        UserDTO userDTO = new UserDTO().setRoles(basicUserRoleDTOS).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

        String userDTOAsJsonString = new ObjectMapper().writeValueAsString(userDTO);

        mvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content(userDTOAsJsonString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("jane"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("jane.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name").value("BASIC_USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].authorities[0].name").value("USER_MODIFY_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].authorities[1].name").value("USER_SEE_OWN"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFirstName().equals("jane"));
        assertThat(userCaptor.getValue().getLastName().equals("doe"));
        assertThat(userCaptor.getValue().getEmail().equals("jane.doe@noseryoung.ch"));
        assertThat(userCaptor.getValue().getRoles().iterator().next().getName().equals("USER_SEE_OWN"));
        assertThat(userCaptor.getValue().getRoles().iterator().next().getName().equals("USER_MODIFY_OWN"));
        //check if Roles contain values from above
    }

    @Test
    @WithMockUser
    public void updateUserById_deliverUserDTOToUpdate_returnUpdatedUserDTO() throws Exception {
        UUID uuid = UUID.randomUUID();
        Set<AuthorityDTO> basicUserAuthorityDTOS = new HashSet<AuthorityDTO>();
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        basicUserAuthorityDTOS.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));

        Set<RoleDTO> basicUserRoleDTOS = new HashSet<RoleDTO>();
        basicUserRoleDTOS.add(new RoleDTO().setName("USER").setAuthorities(basicUserAuthorityDTOS));

        UserDTO userDTO = new UserDTO().setRoles(basicUserRoleDTOS).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

        String userDTOAsJsonString = new ObjectMapper().writeValueAsString(userDTO);

        mvc.perform(
                MockMvcRequestBuilders.put("/users/{id}", uuid.toString())
                        .content(userDTOAsJsonString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("john"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name").value("USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].[0].name").value("USER_SEE_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].[1].name").value("USER_MODIFY_OWN"));

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).updateById(anyString(), any(User.class));
        assertThat(userCaptor.getValue().getFirstName().equals("john"));
        assertThat(userCaptor.getValue().getLastName().equals("doe"));
        assertThat(userCaptor.getValue().getEmail().equals("john.doe@noseryoung.ch"));
        //check if Roles contain values from above
        assertThat(stringCaptor.getValue().equals(uuid.toString()));

    }

    @Test
    @WithMockUser
    public void deleteUserById_requestADeletionOfUserById_returnAppropriateState() throws Exception {
        UUID uuid = UUID.randomUUID();

        mvc.perform(
                MockMvcRequestBuilders.delete("/users/{id}", uuid.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).deleteById(anyString());
        assertThat(stringCaptor.getValue().equals(uuid.toString()));

    }

}