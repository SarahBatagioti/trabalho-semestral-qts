package br.com.AllTallent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.AllTallent.dto.CertificadoRequestDTO;
import br.com.AllTallent.dto.ExperienciaRequestDTO;
import br.com.AllTallent.dto.FuncionarioRequestDTO;
import br.com.AllTallent.exception.ResourceNotFoundException;
import br.com.AllTallent.exception.UnauthorizedActionException;
import br.com.AllTallent.model.Area;
import br.com.AllTallent.model.Competencia;
import br.com.AllTallent.model.Experiencia;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.FuncionarioCertificado;
import br.com.AllTallent.model.Perfil;
import br.com.AllTallent.repository.AreaRepository;
import br.com.AllTallent.repository.CertificadoRepository;
import br.com.AllTallent.repository.CompetenciaRepository;
import br.com.AllTallent.repository.ExperienciaRepository;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.repository.PerfilRepository;
import br.com.AllTallent.support.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private CompetenciaRepository competenciaRepository;
    @Mock
    private ExperienciaRepository experienciaRepository;
    @Mock
    private CertificadoRepository certificadoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private FuncionarioService funcionarioService;

    @BeforeEach
    void setUp() {
        funcionarioService = new FuncionarioService(
                funcionarioRepository,
                areaRepository,
                perfilRepository,
                competenciaRepository,
                experienciaRepository,
                certificadoRepository,
                passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListWithAndWithoutFilter() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        when(funcionarioRepository.findAll()).thenReturn(List.of(funcionario));
        when(funcionarioRepository.buscarPorTexto("ana")).thenReturn(List.of(funcionario));

        assertThat(funcionarioService.listarTodos(null)).hasSize(1);
        assertThat(funcionarioService.listarTodos("  ")).hasSize(1);
        assertThat(funcionarioService.listarTodos("ana")).hasSize(1);
    }

    @Test
    void shouldFindCreateUpdateAndDeleteFuncionario() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Area area = TestDataFactory.area(10, "Tech");
        Perfil perfil = TestDataFactory.perfil(3, "Colaborador");
        Funcionario gestor = TestDataFactory.funcionario(99, "Gestor", 2, 10);
        FuncionarioRequestDTO request = new FuncionarioRequestDTO(
                "Novo Nome",
                "novo@mail.com",
                "11122233344",
                "11999999999",
                "novaSenha",
                10,
                3,
                99,
                "Dev",
                "SP",
                "Resumo");
        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario));
        when(funcionarioRepository.findById(99)).thenReturn(Optional.of(gestor));
        when(areaRepository.findById(10)).thenReturn(Optional.of(area));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(perfil));
        when(passwordEncoder.encode("novaSenha")).thenReturn("encoded");
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(funcionarioRepository.existsById(1)).thenReturn(true);

        assertThat(funcionarioService.buscarPorId(1).codigo()).isEqualTo(1);
        assertThat(funcionarioService.criar(request).nomeCompleto()).isEqualTo("Novo Nome");
        assertThat(funcionarioService.atualizar(1, request).email()).isEqualTo("novo@mail.com");
        funcionarioService.deletar(1);

        ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(funcionarioRepository, times(2)).save(captor.capture());
        Funcionario ultimoSalvo = captor.getAllValues().get(1);
        assertThat(ultimoSalvo.getArea()).isSameAs(area);
        assertThat(ultimoSalvo.getPerfil()).isSameAs(perfil);
        assertThat(ultimoSalvo.getGestor()).isSameAs(gestor);
        assertThat(ultimoSalvo.getSenhaHash()).isEqualTo("encoded");
        verify(funcionarioRepository).deleteById(1);
    }

    @Test
    void shouldFailWhenRequiredRelationsAreMissing() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        FuncionarioRequestDTO request = new FuncionarioRequestDTO(
                "Novo Nome", "novo@mail.com", "111", "119", "", 10, 3, null, null, null, null);
        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario));
        when(areaRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> funcionarioService.atualizar(1, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Área");
    }

    @Test
    void shouldFailWhenPerfilOrGestorAreMissingAndWhenUpdatingUnknownExperience() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        FuncionarioRequestDTO missingPerfil = new FuncionarioRequestDTO(
                "Novo Nome", "novo@mail.com", "111", "119", "", 10, 3, null, null, null, null);
        FuncionarioRequestDTO missingGestor = new FuncionarioRequestDTO(
                "Novo Nome", "novo@mail.com", "111", "119", "", 10, 3, 99, null, null, null);
        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario));
        when(areaRepository.findById(10)).thenReturn(Optional.of(TestDataFactory.area(10, "Tech")));
        when(perfilRepository.findById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> funcionarioService.atualizar(1, missingPerfil))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Perfil");

        when(perfilRepository.findById(3)).thenReturn(Optional.of(TestDataFactory.perfil(3, "Colaborador")));
        when(funcionarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> funcionarioService.atualizar(1, missingGestor))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Gestor");

        when(experienciaRepository.findById(404)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> funcionarioService.atualizarExperiencia(404,
                new ExperienciaRequestDTO("Lead", "Nova", LocalDate.now(), LocalDate.now(), "Desc")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Experi");
    }

    @Test
    void shouldExposePerfilCompletoAndExperiencias() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Experiencia experiencia = TestDataFactory.experiencia(1, funcionario);
        TestDataFactory.addExperiencias(funcionario, experiencia);
        when(funcionarioRepository.findByIdCompleto(1)).thenReturn(Optional.of(funcionario));

        assertThat(funcionarioService.buscarPerfilPorId(1).nomeCompleto()).isEqualTo("Ana");
        assertThat(funcionarioService.buscarFuncionarioCompleto(1)).isSameAs(funcionario);
        assertThat(funcionarioService.listarExperienciasPorFuncionario(1).experiencias()).hasSize(1);
    }

    @Test
    void shouldManageCertificadosAndExperiencias() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario));
        Experiencia experiencia = TestDataFactory.experiencia(1, funcionario);
        when(experienciaRepository.findById(1)).thenReturn(Optional.of(experiencia));
        when(experienciaRepository.save(any(Experiencia.class))).thenAnswer(invocation -> {
            Experiencia experienciaSalva = invocation.getArgument(0);
            if (experienciaSalva.getCodigo() == null) {
                experienciaSalva.setCodigo(99);
            }
            return experienciaSalva;
        });
        FuncionarioCertificado certificado = TestDataFactory.certificado(5, funcionario);
        when(certificadoRepository.findById(5)).thenReturn(Optional.of(certificado));
        when(certificadoRepository.save(any(FuncionarioCertificado.class))).thenAnswer(invocation -> {
            FuncionarioCertificado certificadoSalvo = invocation.getArgument(0);
            if (certificadoSalvo.getCodigo() == null) {
                certificadoSalvo.setCodigo(55);
            }
            return certificadoSalvo;
        });
        when(certificadoRepository.existsById(5)).thenReturn(true);

        assertThat(funcionarioService.adicionarCertificado(1, new CertificadoRequestDTO("AWS")))
                .extracting("codigo", "nome")
                .containsExactly(55, "AWS");
        assertThat(funcionarioService.adicionarExperiencia(1,
                new ExperienciaRequestDTO("Dev", "Empresa", LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1), "Desc"))
                )
                .extracting("codigo", "empresa")
                .containsExactly(99, "Empresa");
        assertThat(funcionarioService.atualizarExperiencia(1,
                new ExperienciaRequestDTO("Lead", "Nova", LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1), "Nova desc"))
                .cargo()).isEqualTo("Lead");
        assertThat(funcionarioService.usuarioPodeEditarExperiencia(1, 1)).isTrue();
        assertThat(funcionarioService.usuarioPodeRemoverCertificado(5, 1)).isTrue();
        funcionarioService.removerCertificado(5);
        verify(certificadoRepository).deleteById(5);
    }

    @Test
    void shouldInitializeNullCollectionsWhenAddingCertificadoAndExperiencia() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        funcionario.setCertificados(null);
        funcionario.setExperiencias(null);
        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario));
        when(certificadoRepository.save(any(FuncionarioCertificado.class))).thenAnswer(invocation -> {
            FuncionarioCertificado certificadoSalvo = invocation.getArgument(0);
            certificadoSalvo.setCodigo(10);
            return certificadoSalvo;
        });
        when(experienciaRepository.save(any(Experiencia.class))).thenAnswer(invocation -> {
            Experiencia experienciaSalva = invocation.getArgument(0);
            experienciaSalva.setCodigo(20);
            return experienciaSalva;
        });

        assertThat(funcionarioService.adicionarCertificado(1, new CertificadoRequestDTO("AWS")).nome()).isEqualTo("AWS");
        assertThat(funcionarioService.adicionarExperiencia(1,
                new ExperienciaRequestDTO("Dev", "Empresa", LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1), "Desc"))
                .cargo()).isEqualTo("Dev");
    }

    @Test
    void shouldFailForMissingExperienceOrCertificado() {
        when(experienciaRepository.findById(40)).thenReturn(Optional.empty());
        when(certificadoRepository.findById(50)).thenReturn(Optional.empty());
        when(certificadoRepository.existsById(50)).thenReturn(false);

        assertThatThrownBy(() -> funcionarioService.usuarioPodeEditarExperiencia(40, 1))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.usuarioPodeRemoverCertificado(50, 1))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.removerCertificado(50))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldAssociateCompetenciasForSelfEdition() {
        Funcionario alvo = TestDataFactory.funcionario(1, "Ana", 3, 10);
        authenticateAs(alvo);
        when(funcionarioRepository.findByIdCompleto(1)).thenReturn(Optional.of(alvo));
        List<Competencia> competencias = List.of(TestDataFactory.competencia(1, "Java"));
        when(competenciaRepository.findAllById(List.of(1))).thenReturn(competencias);

        funcionarioService.associarCompetencias(1, List.of(1));

        assertThat(alvo.getCompetencias()).hasSize(1);
    }

    @Test
    void shouldRejectAssociationForPlainUser() {
        Funcionario logado = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Funcionario alvo = TestDataFactory.funcionario(2, "Bia", 3, 10);
        authenticateAs(logado);
        when(funcionarioRepository.findByIdCompleto(2)).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> funcionarioService.associarCompetencias(2, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldRejectAssociationWhenTargetHasInvalidStructure() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario alvoSemPerfil = TestDataFactory.funcionario(2, "Bia", null, 10);
        Funcionario alvoSemArea = TestDataFactory.funcionario(3, "Caio", 3, null);
        authenticateAs(gestor);
        when(funcionarioRepository.findByIdCompleto(2)).thenReturn(Optional.of(alvoSemPerfil));
        when(funcionarioRepository.findByIdCompleto(3)).thenReturn(Optional.of(alvoSemArea));

        assertThatThrownBy(() -> funcionarioService.associarCompetencias(2, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
        assertThatThrownBy(() -> funcionarioService.associarCompetencias(3, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldAllowGestorForSameAreaColaboradorAndRejectOtherCases() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario colaboradorMesmoSetor = TestDataFactory.funcionario(2, "Bia", 3, 10);
        Funcionario colaboradorOutroSetor = TestDataFactory.funcionario(3, "Caio", 3, 20);
        Funcionario supervisorMesmoSetor = TestDataFactory.funcionario(4, "Dani", 2, 10);
        authenticateAs(gestor);
        when(funcionarioRepository.findByIdCompleto(2)).thenReturn(Optional.of(colaboradorMesmoSetor));
        when(funcionarioRepository.findByIdCompleto(3)).thenReturn(Optional.of(colaboradorOutroSetor));
        when(funcionarioRepository.findByIdCompleto(4)).thenReturn(Optional.of(supervisorMesmoSetor));
        when(competenciaRepository.findAllById(List.of(1))).thenReturn(List.of(TestDataFactory.competencia(1, "Java")));

        funcionarioService.associarCompetencias(2, List.of(1));

        assertThat(colaboradorMesmoSetor.getCompetencias()).hasSize(1);
        assertThatThrownBy(() -> funcionarioService.associarCompetencias(3, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
        assertThatThrownBy(() -> funcionarioService.associarCompetencias(4, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldAllowAdminForSameAreaTeamAndRejectOthersOrMissingCompetencias() {
        Funcionario admin = TestDataFactory.funcionario(1, "Admin", 1, 10);
        Funcionario gestorMesmoSetor = TestDataFactory.funcionario(2, "Gestor", 2, 10);
        Funcionario colaboradorMesmoSetor = TestDataFactory.funcionario(3, "Colab", 3, 10);
        Funcionario adminMesmoSetor = TestDataFactory.funcionario(4, "Outro Admin", 1, 10);
        Funcionario colaboradorOutroSetor = TestDataFactory.funcionario(5, "Outro Setor", 3, 20);
        authenticateAs(admin);
        when(funcionarioRepository.findByIdCompleto(2)).thenReturn(Optional.of(gestorMesmoSetor));
        when(funcionarioRepository.findByIdCompleto(3)).thenReturn(Optional.of(colaboradorMesmoSetor));
        when(funcionarioRepository.findByIdCompleto(4)).thenReturn(Optional.of(adminMesmoSetor));
        when(funcionarioRepository.findByIdCompleto(5)).thenReturn(Optional.of(colaboradorOutroSetor));
        when(competenciaRepository.findAllById(List.of(1, 2)))
                .thenReturn(List.of(TestDataFactory.competencia(1, "Java")));
        when(competenciaRepository.findAllById(List.of(1)))
                .thenReturn(List.of(TestDataFactory.competencia(1, "Java")));

        funcionarioService.associarCompetencias(2, List.of(1));
        funcionarioService.associarCompetencias(3, List.of(1));

        assertThatThrownBy(() -> funcionarioService.associarCompetencias(4, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
        assertThatThrownBy(() -> funcionarioService.associarCompetencias(5, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
        assertThatThrownBy(() -> funcionarioService.associarCompetencias(3, List.of(1, 2)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("competência");
    }

    @Test
    void shouldHandleNullAreaAndUnknownAuthoritiesDuringCompetenciaAssociation() {
        Funcionario alvo = TestDataFactory.funcionario(2, "Bia", 3, 10);
        CustomUserDetailsMock principalSemArea = new CustomUserDetailsMock(10, null, List.of("ROLE_GESTOR"));
        authenticateAs(principalSemArea);
        when(funcionarioRepository.findByIdCompleto(2)).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> funcionarioService.associarCompetencias(2, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);

        CustomUserDetailsMock principalSemPapeis = new CustomUserDetailsMock(11, 10, List.of());
        authenticateAs(principalSemPapeis);
        assertThatThrownBy(() -> funcionarioService.associarCompetencias(2, List.of(1)))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldFailForMissingFuncionarioDuringOperations() {
        when(funcionarioRepository.findById(9)).thenReturn(Optional.empty());
        when(funcionarioRepository.findByIdCompleto(9)).thenReturn(Optional.empty());
        when(funcionarioRepository.existsById(9)).thenReturn(false);

        assertThatThrownBy(() -> funcionarioService.buscarPorId(9)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.atualizar(9,
                new FuncionarioRequestDTO(null, null, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.deletar(9)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.buscarPerfilPorId(9)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.buscarFuncionarioCompleto(9)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.listarExperienciasPorFuncionario(9)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.adicionarCertificado(9, new CertificadoRequestDTO("AWS")))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> funcionarioService.adicionarExperiencia(9,
                new ExperienciaRequestDTO("Dev", "Empresa", LocalDate.now(), LocalDate.now(), "Desc")))
                .isInstanceOf(ResourceNotFoundException.class);
        authenticateAs(TestDataFactory.funcionario(1, "Ana", 3, 10));
        assertThatThrownBy(() -> funcionarioService.associarCompetencias(9, List.of(1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldMapNullGestorAndSkipPasswordEncodingWhenPasswordIsBlank() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        FuncionarioRequestDTO request = new FuncionarioRequestDTO(
                "Novo Nome", "novo@mail.com", "111", "119", "", 10, 3, null, "Dev", "SP", "Resumo");
        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario));
        when(areaRepository.findById(10)).thenReturn(Optional.of(TestDataFactory.area(10, "Tech")));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(TestDataFactory.perfil(3, "Colaborador")));
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        funcionarioService.atualizar(1, request);

        assertThat(funcionario.getGestor()).isNull();
        Mockito.verify(passwordEncoder, Mockito.never()).encode(any());
    }

    @Test
    void shouldSkipPasswordEncodingWhenPasswordIsNull() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        FuncionarioRequestDTO request = new FuncionarioRequestDTO(
                "Novo Nome", "novo@mail.com", "111", "119", null, 10, 3, null, "Dev", "SP", "Resumo");
        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario));
        when(areaRepository.findById(10)).thenReturn(Optional.of(TestDataFactory.area(10, "Tech")));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(TestDataFactory.perfil(3, "Colaborador")));
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        funcionarioService.atualizar(1, request);

        Mockito.verify(passwordEncoder, Mockito.never()).encode(any());
    }

    private void authenticateAs(Funcionario funcionario) {
        SecurityContextHolder.getContext().setAuthentication(TestDataFactory.authenticationFor(funcionario));
    }

    private void authenticateAs(CustomUserDetailsMock principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }

    private static final class CustomUserDetailsMock extends br.com.AllTallent.config.CustomUserDetails {
        private final Integer codigo;
        private final Integer areaId;
        private final List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities;

        private CustomUserDetailsMock(Integer codigo, Integer areaId, List<String> authorities) {
            super(TestDataFactory.funcionario(codigo, "Mock", 3, areaId));
            this.codigo = codigo;
            this.areaId = areaId;
            this.authorities = authorities.stream()
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .toList();
        }

        @Override
        public Integer getCodigo() {
            return codigo;
        }

        @Override
        public Integer getAreaId() {
            return areaId;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return authorities;
        }
    }
}
