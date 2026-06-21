package br.com.AllTallent.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.AllTallent.controller.AreaController;
import br.com.AllTallent.controller.AuthController;
import br.com.AllTallent.controller.FuncionarioController;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.repository.AreaRepository;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.service.AuthService;
import br.com.AllTallent.service.FuncionarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(controllers = {
        AreaController.class,
        AuthController.class,
        FuncionarioController.class
})
@AutoConfigureMockMvc(addFilters = true)
@Import({
        SecurityConfig.class,
        SecurityConfigTest.SecurityTestConfig.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AreaRepository areaRepository;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private FuncionarioRepository funcionarioRepository;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private FuncionarioService funcionarioService;
    @MockBean
    private AuthService authService;

    @Test
    void shouldReturnUnauthorizedForProtectedRouteWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/area"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenJwtAttributeMarksRequestAsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/area").header("Authorization", "Bearer flagged"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenAuthenticatedUserLacksRequiredRole() throws Exception {
        mockMvc.perform(post("/api/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeCompleto": "Ana",
                                  "email": "ana@mail.com",
                                  "cpf": "123",
                                  "telefone": "11999999999",
                                  "senhaHash": "senha123",
                                  "areaId": 1,
                                  "perfilId": 1,
                                  "gestorId": 2,
                                  "tituloProfissional": "Dev",
                                  "localizacao": "SP",
                                  "resumo": "Resumo"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowPublicAuthRouteWithoutAuthentication() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setCodigo(99);
        when(authService.register(any())).thenReturn(funcionario);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeCompleto": "Ana",
                                  "email": "ana@mail.com",
                                  "senha": "senha123",
                                  "telefone": "11999999999",
                                  "idCracha": "ABC123",
                                  "dataAdmissao": "2024-01-01",
                                  "resumo": "Resumo",
                                  "codigoArea": 1,
                                  "codigoPerfil": 1,
                                  "cpf": "12345678900",
                                  "localizacao": "SP",
                                  "tituloProfissional": "Dev",
                                  "codigoGestor": 2
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        AuthenticationProvider authenticationProvider() {
            return org.mockito.Mockito.mock(AuthenticationProvider.class);
        }

        @Bean
        JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(
                    org.mockito.Mockito.mock(JwtService.class),
                    org.mockito.Mockito.mock(UserDetailsService.class)
            ) {
                @Override
                protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain
                ) throws ServletException, IOException {
                    if ("Bearer flagged".equals(request.getHeader("Authorization"))) {
                        request.setAttribute(JWT_AUTHENTICATED_ATTRIBUTE, Boolean.TRUE);
                    }
                    filterChain.doFilter(request, response);
                }
            };
        }
    }
}
