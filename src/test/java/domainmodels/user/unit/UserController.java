package domainmodels.user.unit;


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

    }

    @Test
    @WithMockUser
    public void findById_requestUserById_returnsUser() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.get("/users/{id}", "a")
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("peter@muster.ch"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("peter"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("muster"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.avatarSrc").value("source"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name").value("name"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value("11.07.2000"))
                .andExpect((MockMvcResultMatchers.jsonPath("$.phoneNumber").value("0749281929")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.address").value("Keineahnungstrasse 35"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.city.name").value("Zürich"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.city.postalCode").value("8000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.country.name").value("Switzerland"));

        verify(userService, times(1)).findById("a");
    }

}