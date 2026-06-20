package br.com.AllTallent.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.AllTallent.config.CustomUserDetails;
import br.com.AllTallent.config.JwtService;
import br.com.AllTallent.dto.AvaliacaoDetalhadaDTO;
import br.com.AllTallent.dto.AvaliacaoFuncionarioResponseDTO;
import br.com.AllTallent.dto.AvaliacaoParaResponderDTO;
import br.com.AllTallent.dto.AvaliacaoRequestDTO;
import br.com.AllTallent.dto.AvaliacaoResponseDTO;
import br.com.AllTallent.dto.CadastroRequestDTO;
import br.com.AllTallent.dto.CertificadoDTO;
import br.com.AllTallent.dto.CertificadoRequestDTO;
import br.com.AllTallent.dto.CompetenciaDTO;
import br.com.AllTallent.dto.DashboardResponseDTO;
import br.com.AllTallent.dto.ExperienciaDTO;
import br.com.AllTallent.dto.ExperienciaRequestDTO;
import br.com.AllTallent.dto.FuncionarioCompetenciaUpdateDTO;
import br.com.AllTallent.dto.FuncionarioCompetenciasResponseDTO;
import br.com.AllTallent.dto.FuncionarioExperienciasResponseDTO;
import br.com.AllTallent.dto.FuncionarioPerfilDTO;
import br.com.AllTallent.dto.FuncionarioRequestDTO;
import br.com.AllTallent.dto.FuncionarioResponseDTO;
import br.com.AllTallent.dto.LoginRequestDTO;
import br.com.AllTallent.dto.PerguntaRequestDTO;
import br.com.AllTallent.dto.PerguntaResponseDTO;
import br.com.AllTallent.dto.RespostaColaboradorRequestDTO;
import br.com.AllTallent.dto.RespostaColaboradorResponseDTO;
import br.com.AllTallent.dto.RevisaoDetalhadaDTO;
import br.com.AllTallent.dto.RevisaoSupervisorRequestDTO;
import br.com.AllTallent.exception.ResourceNotFoundException;
import br.com.AllTallent.exception.UnauthorizedActionException;
import br.com.AllTallent.model.Area;
import br.com.AllTallent.model.Competencia;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.Perfil;
import br.com.AllTallent.repository.AreaRepository;
import br.com.AllTallent.repository.CompetenciaRepository;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.repository.PerfilRepository;
import br.com.AllTallent.service.AuthService;
import br.com.AllTallent.service.AvaliacaoService;
import br.com.AllTallent.service.DashboardService;
import br.com.AllTallent.service.FuncionarioService;
import br.com.AllTallent.service.PerguntaService;
import br.com.AllTallent.support.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class ControllerCoverageTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private FuncionarioService funcionarioService;
    @Mock
    private AuthService authService;
    @Mock
    private AvaliacaoService avaliacaoService;
    @Mock
    private DashboardService dashboardService;
    @Mock
    private PerguntaService perguntaService;
    @Mock
    private CompetenciaRepository competenciaRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private PerfilRepository perfilRepository;

    @BeforeEach
    void setUp() {
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldCoverAuthController() {
        AuthController controller = new AuthController(
                authenticationManager, funcionarioRepository, jwtService, funcionarioService, authService);
        LoginRequestDTO request = new LoginRequestDTO("ana@mail.com", "secret");
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                User.withUsername("ana@mail.com").password("secret").authorities("ROLE_USER").build(),
                null,
                List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(funcionarioRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(funcionario));
        when(jwtService.generateToken(any(org.springframework.security.core.userdetails.UserDetails.class))).thenReturn("jwt");
        when(funcionarioService.buscarPorId(1)).thenReturn(new FuncionarioResponseDTO(funcionario));
        when(authService.register(any(CadastroRequestDTO.class))).thenReturn(funcionario);

        assertThat(controller.login(request).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.register(TestDataFactory.cadastroRequest()).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.getMeuPerfil(TestDataFactory.authenticationFor(funcionario)).getStatusCode().value()).isEqualTo(200);

        when(authService.register(any(CadastroRequestDTO.class))).thenThrow(new RuntimeException("erro"));
        assertThat(controller.register(TestDataFactory.cadastroRequest()).getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void shouldCoverDashboardAndCompetenciaControllers() {
        DashboardController dashboardController = new DashboardController(dashboardService, funcionarioRepository);
        Funcionario admin = TestDataFactory.funcionario(1, "Admin", 1, 10);
        Funcionario gestor = TestDataFactory.funcionario(2, "Gestor", 2, 20);
        Funcionario diretoria = TestDataFactory.funcionario(5, "Diretoria", 1, 10);
        Funcionario supervisao = TestDataFactory.funcionario(6, "Supervisao", 2, 20);
        Authentication authDiretoria = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(diretoria),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DIRETORIA")));
        Authentication authSupervisao = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(supervisao),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERVISAO")));
        DashboardResponseDTO dto = DashboardResponseDTO.builder().totalColaboradores(1L).build();
        when(dashboardService.getDashboardData(any())).thenReturn(dto);
        when(funcionarioRepository.findById(2)).thenReturn(Optional.of(gestor));
        when(funcionarioRepository.findById(6)).thenReturn(Optional.of(supervisao));

        assertThat(dashboardController.getDashboardData(99, TestDataFactory.authenticationFor(admin)).getStatusCode().value()).isEqualTo(200);
        assertThat(dashboardController.getDashboardData(99, TestDataFactory.authenticationFor(gestor)).getStatusCode().value()).isEqualTo(200);
        assertThat(dashboardController.getDashboardData(99, authDiretoria).getStatusCode().value()).isEqualTo(200);
        assertThat(dashboardController.getDashboardData(99, authSupervisao).getStatusCode().value()).isEqualTo(200);

        Funcionario gestorSemArea = TestDataFactory.funcionario(3, "Gestor", 2, null);
        when(funcionarioRepository.findById(3)).thenReturn(Optional.of(gestorSemArea));
        assertThat(dashboardController.getDashboardData(77, TestDataFactory.authenticationFor(gestorSemArea)).getStatusCode().value()).isEqualTo(200);

        when(funcionarioRepository.findById(4)).thenReturn(Optional.empty());
        assertThat(dashboardController.getDashboardData(77, TestDataFactory.authenticationFor(TestDataFactory.funcionario(4, "Gestor", 2, 20))).getStatusCode().value()).isEqualTo(500);

        CompetenciaController competenciaController = new CompetenciaController(competenciaRepository);
        Competencia competencia = TestDataFactory.competencia(1, "Java");
        when(competenciaRepository.findAll()).thenReturn(List.of(competencia));
        when(competenciaRepository.findById(1)).thenReturn(Optional.of(competencia));
        when(competenciaRepository.findById(2)).thenReturn(Optional.empty());
        when(competenciaRepository.existsByNomeIgnoreCase("Java")).thenReturn(true);
        when(competenciaRepository.existsByNomeIgnoreCase("Spring")).thenReturn(false);
        when(competenciaRepository.save(any(Competencia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(competenciaRepository.existsById(1)).thenReturn(true);
        when(competenciaRepository.existsById(2)).thenReturn(false);

        assertThat(competenciaController.listar().getStatusCode().value()).isEqualTo(200);
        assertThat(competenciaController.buscarPorId(1).getStatusCode().value()).isEqualTo(200);
        assertThat(competenciaController.buscarPorId(2).getStatusCode().value()).isEqualTo(404);
        assertThat(competenciaController.criar(competencia).getStatusCode().value()).isEqualTo(400);
        Competencia spring = TestDataFactory.competencia(2, "Spring");
        assertThat(competenciaController.criar(spring).getStatusCode().value()).isEqualTo(201);
        assertThat(competenciaController.atualizar(1, spring).getStatusCode().value()).isEqualTo(200);
        assertThat(competenciaController.atualizar(2, spring).getStatusCode().value()).isEqualTo(404);
        assertThat(competenciaController.deletar(1).getStatusCode().value()).isEqualTo(204);
        assertThat(competenciaController.deletar(2).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldCoverAreaPerfilAndPerguntaControllers() {
        AreaController areaController = new AreaController(areaRepository);
        Area area = TestDataFactory.area(1, "Tech");
        when(areaRepository.save(any(Area.class))).thenReturn(area);
        when(areaRepository.findAll()).thenReturn(List.of(area));
        assertThat(areaController.createArea(area).getStatusCode().value()).isEqualTo(201);
        assertThat(areaController.getAllAreas()).hasSize(1);

        PerfilController perfilController = new PerfilController(perfilRepository);
        Perfil perfil = TestDataFactory.perfil(1, "Admin");
        when(perfilRepository.save(any(Perfil.class))).thenReturn(perfil);
        when(perfilRepository.findAll()).thenReturn(List.of(perfil));
        assertThat(perfilController.createPerfil(perfil).getStatusCode().value()).isEqualTo(201);
        assertThat(perfilController.getAllPerfis()).hasSize(1);

        PerguntaController perguntaController = new PerguntaController(perguntaService);
        setCurrentRequest("/api/perguntas");
        PerguntaResponseDTO perguntaResponseDTO = new PerguntaResponseDTO(1L, "Pergunta", 1, "Tech");
        when(perguntaService.criarPergunta(any(PerguntaRequestDTO.class))).thenReturn(perguntaResponseDTO);
        when(perguntaService.listarTodas()).thenReturn(List.of(perguntaResponseDTO));
        when(perguntaService.buscarPorId(1L)).thenReturn(perguntaResponseDTO);
        assertThat(perguntaController.criarPergunta(new PerguntaRequestDTO("Pergunta", 1, "texto", List.of())).getStatusCode().value()).isEqualTo(201);
        when(perguntaService.criarPergunta(any(PerguntaRequestDTO.class))).thenThrow(new EntityNotFoundException("erro"));
        assertThat(perguntaController.criarPergunta(new PerguntaRequestDTO("Pergunta", 1, "texto", List.of())).getStatusCode().value()).isEqualTo(400);
        assertThat(perguntaController.listarTodasPerguntas().getStatusCode().value()).isEqualTo(200);
        assertThat(perguntaController.buscarPerguntaPorId(1L).getStatusCode().value()).isEqualTo(200);
        when(perguntaService.buscarPorId(2L)).thenThrow(new EntityNotFoundException("erro"));
        assertThat(perguntaController.buscarPerguntaPorId(2L).getStatusCode().value()).isEqualTo(404);
        assertThat(perguntaController.deletarPergunta(1L).getStatusCode().value()).isEqualTo(204);
        org.mockito.Mockito.doThrow(new EntityNotFoundException("erro")).when(perguntaService).deletarPergunta(2L);
        assertThat(perguntaController.deletarPergunta(2L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldCoverFuncionarioController() {
        FuncionarioController controller = new FuncionarioController(funcionarioService);
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        FuncionarioResponseDTO funcionarioResponseDTO = new FuncionarioResponseDTO(funcionario);
        FuncionarioPerfilDTO perfilDTO = new FuncionarioPerfilDTO(funcionario);
        FuncionarioCompetenciasResponseDTO competenciasDTO = new FuncionarioCompetenciasResponseDTO(funcionario);
        FuncionarioExperienciasResponseDTO experienciasDTO = new FuncionarioExperienciasResponseDTO(funcionario);
        CertificadoDTO certificadoDTO = new CertificadoDTO(1, "AWS");
        ExperienciaDTO experienciaDTO = new ExperienciaDTO(1, "Dev", "OpenAI", "Desc", LocalDate.now(), LocalDate.now());
        when(funcionarioService.listarTodos(any())).thenReturn(List.of(funcionarioResponseDTO));
        when(funcionarioService.buscarPorId(1)).thenReturn(funcionarioResponseDTO);
        when(funcionarioService.criar(any(FuncionarioRequestDTO.class))).thenReturn(funcionarioResponseDTO);
        when(funcionarioService.atualizar(any(Integer.class), any(FuncionarioRequestDTO.class))).thenReturn(funcionarioResponseDTO);
        when(funcionarioService.buscarPerfilPorId(1)).thenReturn(perfilDTO);
        when(funcionarioService.adicionarCertificado(1, new CertificadoRequestDTO("AWS"))).thenReturn(certificadoDTO);
        when(funcionarioService.buscarFuncionarioCompleto(1)).thenReturn(funcionario);
        when(funcionarioService.listarExperienciasPorFuncionario(1)).thenReturn(experienciasDTO);
        when(funcionarioService.adicionarExperiencia(any(Integer.class), any(ExperienciaRequestDTO.class))).thenReturn(experienciaDTO);
        when(funcionarioService.atualizarExperiencia(any(Integer.class), any(ExperienciaRequestDTO.class))).thenReturn(experienciaDTO);

        assertThat(controller.listarTodos("ana").getStatusCode().value()).isEqualTo(200);
        assertThat(controller.buscarPorId(1).getStatusCode().value()).isEqualTo(200);
        setCurrentRequest("/api/funcionario");
        assertThat(controller.criar(new FuncionarioRequestDTO("Ana", "ana@mail.com", "123", "119", "s", 1, 2, 3, "Dev", "SP", "R")).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.atualizar(1, new FuncionarioRequestDTO("Ana", "ana@mail.com", "123", "119", "s", 1, 2, 3, "Dev", "SP", "R")).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.deletar(1).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.buscarPerfilPorId(1).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.adicionarCertificado(1, new CertificadoRequestDTO("AWS")).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.removerCertificado(1).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.listarCompetenciasPorFuncionario(1).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.listarExperienciasPorFuncionario(1).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.adicionarExperiencia(1, new ExperienciaRequestDTO("Dev", "OpenAI", LocalDate.now(), LocalDate.now(), "Desc")).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.atualizarExperiencia(1, new ExperienciaRequestDTO("Dev", "OpenAI", LocalDate.now(), LocalDate.now(), "Desc")).getStatusCode().value()).isEqualTo(200);

        assertThat(controller.atualizarCompetencias(1, new FuncionarioCompetenciaUpdateDTO(List.of(1))).getStatusCode().value()).isEqualTo(204);
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("erro"))
                .when(funcionarioService).associarCompetencias(1, List.of(1));
        assertThat(controller.atualizarCompetencias(1, new FuncionarioCompetenciaUpdateDTO(List.of(1))).getStatusCode().value()).isEqualTo(404);
        org.mockito.Mockito.doThrow(new UnauthorizedActionException("erro"))
                .when(funcionarioService).associarCompetencias(1, List.of(2));
        assertThat(controller.atualizarCompetencias(1, new FuncionarioCompetenciaUpdateDTO(List.of(2))).getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void shouldCoverAvaliacaoController() {
        AvaliacaoController controller = new AvaliacaoController(avaliacaoService);
        setCurrentRequest("/api/avaliacoes");
        AvaliacaoResponseDTO responseDTO = new AvaliacaoResponseDTO(1, "Aval", "PENDENTE", LocalDate.now(), LocalDate.now(), "Gestor");
        AvaliacaoDetalhadaDTO detalhadaDTO = new AvaliacaoDetalhadaDTO(1, "Aval", "P", LocalDate.now(), LocalDate.now(), "Gestor", List.of(), List.of());
        RespostaColaboradorResponseDTO respostaDTO = new RespostaColaboradorResponseDTO(1L, 2L, 3L, "Texto", null);
        AvaliacaoFuncionarioResponseDTO instanciaDTO = new AvaliacaoFuncionarioResponseDTO(TestDataFactory.avaliacaoFuncionario(1L, TestDataFactory.funcionario(1, "Ana", 3, 10), TestDataFactory.avaliacao(1, TestDataFactory.funcionario(2, "Gestor", 2, 10))));
        AvaliacaoParaResponderDTO responderDTO = new AvaliacaoParaResponderDTO(1L, "Aval", LocalDate.now(), List.of());
        when(avaliacaoService.criarAvaliacaoCompleta(any(AvaliacaoRequestDTO.class))).thenReturn(responseDTO);
        when(avaliacaoService.listarTodasAvaliacoes()).thenReturn(List.of(responseDTO));
        when(avaliacaoService.buscarAvaliacaoDetalhada(1)).thenReturn(detalhadaDTO);
        when(avaliacaoService.buscarInstanciasPorAvaliacao(1)).thenReturn(List.of(instanciaDTO));
        when(avaliacaoService.salvarOuAtualizarResposta(any(RespostaColaboradorRequestDTO.class))).thenReturn(respostaDTO);
        when(avaliacaoService.buscarRespostasPorInstancia(1L)).thenReturn(List.of(respostaDTO));
        when(avaliacaoService.buscarDadosRevisao(1L)).thenReturn(List.of(RevisaoDetalhadaDTO.builder().perguntaTexto("P").build()));
        when(avaliacaoService.salvarRevisaoSupervisor(any(Long.class), any(RevisaoSupervisorRequestDTO.class))).thenReturn(instanciaDTO);
        when(avaliacaoService.buscarPendentesPorFuncionario(1)).thenReturn(List.of(instanciaDTO));
        when(avaliacaoService.buscarParaResponder(1L)).thenReturn(responderDTO);

        assertThat(controller.criarAvaliacao(new AvaliacaoRequestDTO("Aval", LocalDate.now(), List.of(1), List.of(2L))).getStatusCode().value()).isEqualTo(201);
        when(avaliacaoService.criarAvaliacaoCompleta(any(AvaliacaoRequestDTO.class))).thenThrow(new EntityNotFoundException("erro"));
        assertThat(controller.criarAvaliacao(new AvaliacaoRequestDTO("Aval", LocalDate.now(), List.of(1), List.of(2L))).getStatusCode().value()).isEqualTo(400);
        when(avaliacaoService.criarAvaliacaoCompleta(any(AvaliacaoRequestDTO.class))).thenThrow(new RuntimeException("erro"));
        assertThat(controller.criarAvaliacao(new AvaliacaoRequestDTO("Aval", LocalDate.now(), List.of(1), List.of(2L))).getStatusCode().value()).isEqualTo(500);

        assertThat(controller.listarTodasAvaliacoes().getStatusCode().value()).isEqualTo(200);
        assertThat(controller.buscarAvaliacaoDetalhada(1).getStatusCode().value()).isEqualTo(200);
        when(avaliacaoService.buscarAvaliacaoDetalhada(2)).thenThrow(new ResourceNotFoundException("erro"));
        assertThat(controller.buscarAvaliacaoDetalhada(2).getStatusCode().value()).isEqualTo(404);
        assertThat(controller.buscarInstanciasPorAvaliacao(1).getStatusCode().value()).isEqualTo(200);
        when(avaliacaoService.buscarInstanciasPorAvaliacao(2)).thenThrow(new EntityNotFoundException("erro"));
        assertThat(controller.buscarInstanciasPorAvaliacao(2).getStatusCode().value()).isEqualTo(404);

        assertThat(controller.salvarResposta(new RespostaColaboradorRequestDTO(1L, 2L, "Texto", null)).getStatusCode().value()).isEqualTo(200);
        when(avaliacaoService.salvarOuAtualizarResposta(any(RespostaColaboradorRequestDTO.class))).thenThrow(new EntityNotFoundException("erro"));
        assertThat(controller.salvarResposta(new RespostaColaboradorRequestDTO(1L, 2L, "Texto", null)).getStatusCode().value()).isEqualTo(400);
        when(avaliacaoService.salvarOuAtualizarResposta(any(RespostaColaboradorRequestDTO.class))).thenThrow(new IllegalArgumentException("erro"));
        assertThat(controller.salvarResposta(new RespostaColaboradorRequestDTO(1L, 2L, "Texto", null)).getStatusCode().value()).isEqualTo(400);
        when(avaliacaoService.salvarOuAtualizarResposta(any(RespostaColaboradorRequestDTO.class))).thenThrow(new RuntimeException("erro"));
        assertThat(controller.salvarResposta(new RespostaColaboradorRequestDTO(1L, 2L, "Texto", null)).getStatusCode().value()).isEqualTo(500);

        assertThat(controller.buscarRespostasPorInstancia(1L).getStatusCode().value()).isEqualTo(200);
        when(avaliacaoService.buscarRespostasPorInstancia(2L)).thenThrow(new EntityNotFoundException("erro"));
        assertThat(controller.buscarRespostasPorInstancia(2L).getStatusCode().value()).isEqualTo(404);
        assertThat(controller.getDadosParaRevisao(1L).getStatusCode().value()).isEqualTo(200);
        when(avaliacaoService.buscarDadosRevisao(2L)).thenThrow(new EntityNotFoundException("erro"));
        assertThat(controller.getDadosParaRevisao(2L).getStatusCode().value()).isEqualTo(404);

        assertThat(controller.salvarRevisaoSupervisor(1L, new RevisaoSupervisorRequestDTO("ok", "fb", "APROVADO")).getStatusCode().value()).isEqualTo(200);
        when(avaliacaoService.salvarRevisaoSupervisor(any(Long.class), any(RevisaoSupervisorRequestDTO.class))).thenThrow(new EntityNotFoundException("erro"));
        assertThat(controller.salvarRevisaoSupervisor(2L, new RevisaoSupervisorRequestDTO("ok", "fb", "APROVADO")).getStatusCode().value()).isEqualTo(404);
        when(avaliacaoService.salvarRevisaoSupervisor(any(Long.class), any(RevisaoSupervisorRequestDTO.class))).thenThrow(new RuntimeException("erro"));
        assertThat(controller.salvarRevisaoSupervisor(2L, new RevisaoSupervisorRequestDTO("ok", "fb", "APROVADO")).getStatusCode().value()).isEqualTo(500);

        assertThat(controller.buscarAvaliacoesPendentes(1).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.buscarAvaliacaoParaResponder(1L).getStatusCode().value()).isEqualTo(200);
        when(avaliacaoService.buscarParaResponder(2L)).thenThrow(new EntityNotFoundException("erro"));
        assertThat(controller.buscarAvaliacaoParaResponder(2L).getStatusCode().value()).isEqualTo(404);
        assertThat(controller.finalizarAvaliacaoColaborador(1L).getStatusCode().value()).isEqualTo(204);
        org.mockito.Mockito.doThrow(new EntityNotFoundException("erro")).when(avaliacaoService).finalizarPeloColaborador(2L);
        assertThat(controller.finalizarAvaliacaoColaborador(2L).getStatusCode().value()).isEqualTo(404);
        org.mockito.Mockito.doThrow(new IllegalStateException("erro")).when(avaliacaoService).finalizarPeloColaborador(3L);
        assertThat(controller.finalizarAvaliacaoColaborador(3L).getStatusCode().value()).isEqualTo(409);
    }

    private void setCurrentRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRequestURI(path);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
