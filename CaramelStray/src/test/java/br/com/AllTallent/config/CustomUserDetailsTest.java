package br.com.AllTallent.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.support.TestDataFactory;
import org.junit.jupiter.api.Test;

class CustomUserDetailsTest {

    @Test
    void shouldCreateDefaultUserWhenProfileIsMissing() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", null, null);

        CustomUserDetails details = new CustomUserDetails(funcionario);

        assertThat(details.getUsername()).isEqualTo(funcionario.getEmail());
        assertThat(details.getAreaId()).isNull();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void shouldCreateAdminAuthoritiesForProfileOne() {
        CustomUserDetails details = new CustomUserDetails(TestDataFactory.funcionario(1, "Ana", 1, 10));

        assertThat(details.getCodigo()).isEqualTo(1);
        assertThat(details.getAreaId()).isEqualTo(10);
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_ADMIN", "ROLE_GESTOR", "ROLE_USER");
    }

    @Test
    void shouldCreateGestorAuthoritiesForProfileTwo() {
        CustomUserDetails details = new CustomUserDetails(TestDataFactory.funcionario(2, "Bia", 2, 20));

        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_GESTOR", "ROLE_USER");
    }

    @Test
    void shouldCreateColaboradorAuthoritiesForOtherProfiles() {
        CustomUserDetails details = new CustomUserDetails(TestDataFactory.funcionario(3, "Caio", 3, 30));

        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }
}
