package domainmodels.user.unit;

import ch.course223.advanced.domainmodels.authority.Authority;
import ch.course223.advanced.domainmodels.role.Role;
import ch.course223.advanced.domainmodels.user.User;
import ch.course223.advanced.domainmodels.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserController {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Before
    public void setUp(){

        //Business Objects (used in findById, findAll)
        Set<Authority> userAuthorities = new HashSet<Authority>();
        userAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        userAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));

        Set<Authority> adminAuthorities = new HashSet<Authority>();
        adminAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        adminAuthorities.add(new Authority().setName("USER_SEE_GLOBAL"));
        adminAuthorities.add(new Authority().setName("USER_CREATE"));
        adminAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));
        adminAuthorities.add(new Authority().setName("USER_MODIFY_GLOBAL"));
        adminAuthorities.add(new Authority().setName("USER_DELETE"));

        Set<Role> userRoles = new HashSet<Role>();
        userRoles.add(new Role().setName("USER").setAuthorities(userAuthorities);

        Set<Role> adminRoles = new HashSet<Role>();
        adminRoles.add(new Role().setName("ADMIN").setAuthorities(adminAuthorities);

        User user = new User().setRoles(adminRoles).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");
        User admin = new User().setRoles(adminRoles).setFirstName("john").setLastName("doe").setEmail("john.doe@noseryoung.ch");

        //Data Transferable Objects (used in create, updateById, deleteById)
        Set<AuthorityDTO> userAuthoritiesDTO = new HashSet<AuthorityDTO>();
        userAuthoritiesDTO.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        userAuthoritiesDTO.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));

        Set<AuthorityDTO> adminAuthoritiesDTO = new HashSet<Authority>();
        adminAuthoritiesDTO.add(new AuthorityDTO().setName("USER_SEE_OWN"));
        adminAuthoritiesDTO.add(new AuthorityDTO().setName("USER_SEE_GLOBAL"));
        adminAuthoritiesDTO.add(new AuthorityDTO().setName("USER_CREATE"));
        adminAuthoritiesDTO.add(new AuthorityDTO().setName("USER_MODIFY_OWN"));
        adminAuthoritiesDTO.add(new AuthorityDTO().setName("USER_MODIFY_GLOBAL"));
        adminAuthoritiesDTO.add(new AuthorityDTO().setName("USER_DELETE"));

        Set<RoleDTO> userRolesDTO = new HashSet<RoleDTO>();
        userRolesDTO.add(new RoleDTO().setName("USER").setAuthorities(userAuthoritiesDTO);

        Set<RoleDTO> adminRolesDTO = new HashSet<RoleDTO>();
        adminRolesDTO.add(new RoleDTO().setName("ADMIN").setAuthorities(adminAuthoritiesDTO);

        User userDTO = new UserDTO().setRoles(adminRoles).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");
        User adminDTO = new UserDTO().setRoles(adminRoles).setFirstName("john").setLastName("doe").setEmail("john.doe@noseryoung.ch");

        //Mocks
        given(userService.findById(anyString())).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new NoSuchElementException();
            return (user);
        });

        given(userService.findAll()).willReturn(Arrays.asList(user, admin));


    }

    @Test
    @WithMockUser
    public void findById_requestUserById_returnsUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        mvc.perform(
                MockMvcRequestBuilders.get("/users/{id}", uuid.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("john"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name").value("USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].[0].name").value("USER_SEE_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].[1].name").value("USER_MODIFY_OWN"));

        verify(userService, times(1)).findById(uuid.toString());
    }

    @Test
    @WithMockUser
    public void findAll_requestAllUsers_returnsAllUsers() throws Exception {
        UUID uuid = UUID.randomUUID();
        mvc.perform(
                MockMvcRequestBuilders.get("/users", uuid.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("john.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("jane.doe@noseryoung.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("john"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].firstName").value("jane"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].lastName").value("doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].name").value("ADMIN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].name").value("USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].[0].name").value("USER_SEE_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].[1].name").value("USER_MODIFY_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[0].name").value("USER_SEE_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[1].name").value("USER_MODIFY_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[2].name").value("USER_CREATE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[3].name").value("USER_MODIFY_OWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[4].name").value("USER_MODIFY_GLOBAL"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].roles[0].[5].name").value("USER_DELETE"));

        verify(userService, times(1)).findById(uuid.toString());
    }

    @Test
    @WithMockUser
    public void create_requestsUser_returnsCreatedUser() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        roles.add(new RoleDTO().setName("rolename"));

        UserDTO userDTO = new UserDTO().setEmail("kevin@in.ch").setFirstName("kevin").setLastName("in")
                .setCountry(new CountryDTO().setName("Germany")).setCity(new CityDTO().setName("Berlin").setPostalCode("9090")).setAddress("Ichweissned 60")
                .setBirthDate("11.07.2000").setPhoneNumber("0930201020")
                .setAvatarSrc("source").setRoles(roles);
        String userAsJsonString = new ObjectMapper().writeValueAsString(userDTO);

        mvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAsJsonString)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("myID"));

        verify(userService, times(1)).save(any(User.class));
    }

}