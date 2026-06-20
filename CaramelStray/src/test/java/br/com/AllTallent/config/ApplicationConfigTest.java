package br.com.AllTallent.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.support.TestDataFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

class ApplicationConfigTest {

    private FuncionarioRepository funcionarioRepository;
    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        funcionarioRepository = mock(FuncionarioRepository.class);
        applicationConfig = new ApplicationConfig(funcionarioRepository);
    }

    @Test
    void shouldLoadUserDetailsFromRepository() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 1, 10);
        when(funcionarioRepository.findByEmailForSecurity("ana@mail.com")).thenReturn(Optional.of(funcionario));

        UserDetailsService userDetailsService = applicationConfig.userDetailsService();

        CustomUserDetails details = (CustomUserDetails) userDetailsService.loadUserByUsername("ana@mail.com");
        assertThat(details.getCodigo()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenUserIsMissing() {
        when(funcionarioRepository.findByEmailForSecurity("missing@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationConfig.userDetailsService().loadUserByUsername("missing@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@mail.com");
    }

    @Test
    void shouldCreateAuthenticationInfrastructure() throws Exception {
        AuthenticationConfiguration configuration = mock(AuthenticationConfiguration.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(configuration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationProvider provider = applicationConfig.authenticationProvider();
        PasswordEncoder encoder = applicationConfig.passwordEncoder();

        assertThat(provider).isNotNull();
        assertThat(encoder.matches("senha123", encoder.encode("senha123"))).isTrue();
        assertThat(applicationConfig.authenticationManager(configuration)).isSameAs(authenticationManager);
    }
}
