package br.com.AllTallent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.AllTallent.dto.CadastroRequestDTO;
import br.com.AllTallent.model.Area;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.Perfil;
import br.com.AllTallent.repository.AreaRepository;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.repository.PerfilRepository;
import br.com.AllTallent.support.TestDataFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(funcionarioRepository, areaRepository, perfilRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterFuncionarioSuccessfully() {
        CadastroRequestDTO request = TestDataFactory.cadastroRequest();
        Area area = TestDataFactory.area(10, "Tecnologia");
        Perfil perfil = TestDataFactory.perfil(3, "Colaborador");
        Funcionario gestor = TestDataFactory.funcionario(99, "Gestor", 2, 10);
        when(funcionarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(areaRepository.findById(10)).thenReturn(Optional.of(area));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(perfil));
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(funcionarioRepository.findById(99)).thenReturn(Optional.of(gestor));
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        request.setCodigoGestor(99);

        Funcionario result = authService.register(request);

        ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(funcionarioRepository).save(captor.capture());
        Funcionario saved = captor.getValue();
        assertThat(result).isSameAs(saved);
        assertThat(saved.getNomeCompleto()).isEqualTo("Maria Silva");
        assertThat(saved.getSenhaHash()).isEqualTo("encoded");
        assertThat(saved.getArea()).isSameAs(area);
        assertThat(saved.getPerfil()).isSameAs(perfil);
        assertThat(saved.getGestor()).isSameAs(gestor);
        assertThat(saved.getDataCadastro()).isNotNull();
    }

    @Test
    void shouldRegisterWithoutGestor() {
        CadastroRequestDTO request = TestDataFactory.cadastroRequest();
        when(funcionarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(areaRepository.findById(10)).thenReturn(Optional.of(TestDataFactory.area(10, "Tecnologia")));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(TestDataFactory.perfil(3, "Colaborador")));
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Funcionario result = authService.register(request);

        assertThat(result.getGestor()).isNull();
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        CadastroRequestDTO request = TestDataFactory.cadastroRequest();
        when(funcionarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(TestDataFactory.funcionario(1, "Ana", 3, 10)));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void shouldRejectMissingArea() {
        CadastroRequestDTO request = TestDataFactory.cadastroRequest();
        when(funcionarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(areaRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Área");
    }

    @Test
    void shouldRejectMissingPerfil() {
        CadastroRequestDTO request = TestDataFactory.cadastroRequest();
        when(funcionarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(areaRepository.findById(10)).thenReturn(Optional.of(TestDataFactory.area(10, "Tecnologia")));
        when(perfilRepository.findById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Perfil");
    }

    @Test
    void shouldRejectMissingGestorWhenProvided() {
        CadastroRequestDTO request = TestDataFactory.cadastroRequest();
        request.setCodigoGestor(40);
        when(funcionarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(areaRepository.findById(10)).thenReturn(Optional.of(TestDataFactory.area(10, "Tecnologia")));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(TestDataFactory.perfil(3, "Colaborador")));
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(funcionarioRepository.findById(40)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gestor");
    }
}
