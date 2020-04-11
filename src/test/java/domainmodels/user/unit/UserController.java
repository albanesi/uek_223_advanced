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

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

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

        Set<Authority> adminAuthorities = new HashSet<Authority>();
        adminAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        adminAuthorities.add(new Authority().setName("USER_SEE_GLOBAL"));
        adminAuthorities.add(new Authority().setName("USER_CREATE"));
        adminAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));
        adminAuthorities.add(new Authority().setName("USER_MODIFY_GLOBAL"));
        adminAuthorities.add(new Authority().setName("USER_DELETE"));

        Set<Authority> userAuthorities = new HashSet<Authority>();
        userAuthorities.add(new Authority().setName("USER_SEE_OWN"));
        userAuthorities.add(new Authority().setName("USER_MODIFY_OWN"));

        Set<Role> adminRoles = new HashSet<Role>();
        adminRoles.add(new Role().setName("ADMIN").setAuthorities(adminAuthorities));

        Set<Role> userRoles = new HashSet<Role>();
        adminRoles.add(new Role().setName("USER").setAuthorities(userAuthorities));

        User admin = new User().setRoles(adminRoles).setFirstName("john").setLastName("doe").setEmail("john.doe@noseryoung.ch");
        User user = new User().setRoles(adminRoles).setFirstName("jane").setLastName("doe").setEmail("jane.doe@noseryoung.ch");

        given(userService.findById(anyString())).will(invocation -> {
            if ("non-existent".equals(invocation.getArgument(0))) throw new NoSuchElementException();
            return (user);
        });

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



}