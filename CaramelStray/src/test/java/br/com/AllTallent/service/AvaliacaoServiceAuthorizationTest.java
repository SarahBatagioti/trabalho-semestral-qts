package br.com.AllTallent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.AllTallent.dto.AvaliacaoDetalhadaDTO;
import br.com.AllTallent.dto.AvaliacaoFuncionarioResponseDTO;
import br.com.AllTallent.dto.AvaliacaoResponseDTO;
import br.com.AllTallent.dto.RespostaColaboradorResponseDTO;
import br.com.AllTallent.exception.ResourceNotFoundException;
import br.com.AllTallent.exception.UnauthorizedActionException;
import br.com.AllTallent.model.Avaliacao;
import br.com.AllTallent.model.AvaliacaoFuncionario;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.Pergunta;
import br.com.AllTallent.model.RespostaColaborador;
import br.com.AllTallent.repository.AvaliacaoFuncionarioRepository;
import br.com.AllTallent.repository.AvaliacaoRepository;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.repository.PerguntaOpcaoRepository;
import br.com.AllTallent.repository.PerguntaRepository;
import br.com.AllTallent.repository.RespostaColaboradorRepository;
import br.com.AllTallent.support.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceAuthorizationTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private PerguntaRepository perguntaRepository;
    @Mock
    private AvaliacaoFuncionarioRepository avaliacaoFuncionarioRepository;
    @Mock
    private RespostaColaboradorRepository respostaColaboradorRepository;
    @Mock
    private PerguntaOpcaoRepository perguntaOpcaoRepository;

    private AvaliacaoService avaliacaoService;

    @BeforeEach
    void setUp() {
        avaliacaoService = new AvaliacaoService(
                avaliacaoRepository,
                funcionarioRepository,
                perguntaRepository,
                avaliacaoFuncionarioRepository,
                respostaColaboradorRepository,
                perguntaOpcaoRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectWhenAuthenticationIsMissingOrInvalid() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> avaliacaoService.listarTodasAvaliacoes())
                .isInstanceOf(UnauthorizedActionException.class);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("invalid", null));

        assertThatThrownBy(() -> avaliacaoService.listarTodasAvaliacoes())
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldListAvaliacoesForAdminAreaOnly() {
        Funcionario admin = TestDataFactory.funcionario(1, "Admin", 1, 10);
        Funcionario criadorMesmoSetor = TestDataFactory.funcionario(2, "Gestor", 2, 10);
        Funcionario criadorOutroSetor = TestDataFactory.funcionario(3, "Outro", 2, 20);
        Funcionario criadorSemArea = TestDataFactory.funcionario(4, "Sem Area", 2, null);
        Avaliacao avaliacaoValida = TestDataFactory.avaliacao(1, criadorMesmoSetor);
        Avaliacao semCriador = TestDataFactory.avaliacao(2, null);
        Avaliacao outroSetor = TestDataFactory.avaliacao(3, criadorOutroSetor);
        Avaliacao semArea = TestDataFactory.avaliacao(4, criadorSemArea);
        authenticateAs(admin);
        when(avaliacaoRepository.findAll()).thenReturn(List.of(avaliacaoValida, semCriador, outroSetor, semArea));

        List<AvaliacaoResponseDTO> response = avaliacaoService.listarTodasAvaliacoes();

        assertThat(response).extracting(AvaliacaoResponseDTO::codigo).containsExactly(1);
    }

    @Test
    void shouldListOnlyOwnAvaliacoesForGestorAndEmptyForUser() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario outroGestor = TestDataFactory.funcionario(2, "Outro Gestor", 2, 10);
        Funcionario gestorOutroSetor = TestDataFactory.funcionario(5, "Gestor Externo", 2, 20);
        Funcionario criadorSemArea = TestDataFactory.funcionario(4, "Sem Area", 2, null);
        Avaliacao propria = TestDataFactory.avaliacao(1, gestor);
        Avaliacao deOutro = TestDataFactory.avaliacao(2, outroGestor);
        Avaliacao deOutroSetor = TestDataFactory.avaliacao(5, gestorOutroSetor);
        Avaliacao semCriador = TestDataFactory.avaliacao(3, null);
        Avaliacao semArea = TestDataFactory.avaliacao(4, criadorSemArea);
        when(avaliacaoRepository.findAll()).thenReturn(List.of(propria, deOutro, deOutroSetor, semCriador, semArea));

        authenticateAs(gestor);
        assertThat(avaliacaoService.listarTodasAvaliacoes()).extracting(AvaliacaoResponseDTO::codigo).containsExactly(1);

        authenticateAs(TestDataFactory.funcionario(3, "Colaborador", 3, 10));
        assertThat(avaliacaoService.listarTodasAvaliacoes()).isEmpty();
    }

    @Test
    void shouldEvaluatePodeAvaliarBranchesDirectly() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario admin = TestDataFactory.funcionario(2, "Admin", 1, 10);
        Funcionario alvoColaboradorMesmoSetor = TestDataFactory.funcionario(3, "Colab", 3, 10);
        Funcionario alvoColaboradorOutroSetor = TestDataFactory.funcionario(4, "Outro Setor", 3, 20);
        Funcionario alvoGestorMesmoSetor = TestDataFactory.funcionario(5, "Gestor 2", 2, 10);
        Funcionario alvoAdminMesmoSetor = TestDataFactory.funcionario(6, "Admin 2", 1, 10);
        Funcionario alvoSemArea = TestDataFactory.funcionario(7, "Sem Area", 3, null);

        assertThat((Boolean) ReflectionTestUtils.invokeMethod(avaliacaoService, "podeAvaliar", new br.com.AllTallent.config.CustomUserDetails(gestor), alvoColaboradorMesmoSetor))
                .isEqualTo(true);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(avaliacaoService, "podeAvaliar", new br.com.AllTallent.config.CustomUserDetails(gestor), alvoColaboradorOutroSetor))
                .isEqualTo(false);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(avaliacaoService, "podeAvaliar", new br.com.AllTallent.config.CustomUserDetails(admin), alvoGestorMesmoSetor))
                .isEqualTo(true);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(avaliacaoService, "podeAvaliar", new br.com.AllTallent.config.CustomUserDetails(admin), alvoAdminMesmoSetor))
                .isEqualTo(false);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(avaliacaoService, "podeAvaliar", new br.com.AllTallent.config.CustomUserDetails(admin), alvoSemArea))
                .isEqualTo(false);
    }

    @Test
    void shouldAllowAdminToSeeEvaluationWithCreatorAreaAndRejectMissingCreatorArea() {
        Funcionario admin = TestDataFactory.funcionario(1, "Admin", 1, 10);
        Funcionario criador = TestDataFactory.funcionario(2, "Gestor", 2, 10);
        Funcionario criadorSemArea = TestDataFactory.funcionario(3, "Gestor", 2, null);
        Avaliacao permitida = TestDataFactory.avaliacao(1, criador);
        Avaliacao semArea = TestDataFactory.avaliacao(2, criadorSemArea);
        when(avaliacaoRepository.findById(1)).thenReturn(Optional.of(permitida));
        when(avaliacaoRepository.findById(2)).thenReturn(Optional.of(semArea));
        authenticateAs(admin);

        assertThat(avaliacaoService.buscarAvaliacaoDetalhada(1).codigo()).isEqualTo(1);
        assertThatThrownBy(() -> avaliacaoService.buscarAvaliacaoDetalhada(2))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldAllowGestorToSeeOwnEvaluationFromSameArea() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, gestor);
        when(avaliacaoRepository.findById(1)).thenReturn(Optional.of(avaliacao));
        authenticateAs(gestor);

        assertThat(avaliacaoService.buscarAvaliacaoDetalhada(1).codigo()).isEqualTo(1);
    }

    @Test
    void shouldValidateAccessRulesDirectly() {
        Funcionario gestorCriador = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario gestorMesmoSetor = TestDataFactory.funcionario(2, "Gestor 2", 2, 10);
        Funcionario adminMesmoSetor = TestDataFactory.funcionario(3, "Admin", 1, 10);
        Funcionario colaboradorMesmoSetor = TestDataFactory.funcionario(4, "Colab", 3, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, gestorCriador);

        ReflectionTestUtils.invokeMethod(avaliacaoService, "validarPermissaoDeAcesso",
                new br.com.AllTallent.config.CustomUserDetails(adminMesmoSetor), avaliacao);
        ReflectionTestUtils.invokeMethod(avaliacaoService, "validarPermissaoDeAcesso",
                new br.com.AllTallent.config.CustomUserDetails(gestorCriador), avaliacao);
        ReflectionTestUtils.invokeMethod(avaliacaoService, "validarPermissaoDeAcesso",
                new br.com.AllTallent.config.CustomUserDetails(colaboradorMesmoSetor), avaliacao);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(avaliacaoService, "validarPermissaoDeAcesso",
                new br.com.AllTallent.config.CustomUserDetails(gestorMesmoSetor), avaliacao))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldFindDetailedAvaliacaoWhenAuthorized() {
        Funcionario admin = TestDataFactory.funcionario(1, "Admin", 1, 10);
        Funcionario criador = TestDataFactory.funcionario(2, "Gestor", 2, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, criador);
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Como foi?", TestDataFactory.competencia(1, "Tech"), "texto");
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(10L, TestDataFactory.funcionario(4, "Ana", 3, 10), avaliacao);
        TestDataFactory.addPerguntas(avaliacao, pergunta);
        TestDataFactory.addInstancias(avaliacao, instancia);
        when(avaliacaoRepository.findById(1)).thenReturn(Optional.of(avaliacao));
        authenticateAs(admin);

        AvaliacaoDetalhadaDTO response = avaliacaoService.buscarAvaliacaoDetalhada(1);

        assertThat(response.codigo()).isEqualTo(1);
        assertThat(response.perguntas()).hasSize(1);
        assertThat(response.instancias()).hasSize(1);
    }

    @Test
    void shouldRejectDetailedAvaliacaoWhenNotFoundOrUnauthorized() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario criadorOutroSetor = TestDataFactory.funcionario(2, "Outro", 2, 20);
        Funcionario outroGestorMesmoSetor = TestDataFactory.funcionario(3, "Outro Gestor", 2, 10);
        Avaliacao semCriador = TestDataFactory.avaliacao(1, null);
        Avaliacao outraArea = TestDataFactory.avaliacao(2, criadorOutroSetor);
        Avaliacao mesmaAreaOutroCriador = TestDataFactory.avaliacao(3, outroGestorMesmoSetor);
        when(avaliacaoRepository.findById(99)).thenReturn(Optional.empty());
        when(avaliacaoRepository.findById(1)).thenReturn(Optional.of(semCriador));
        when(avaliacaoRepository.findById(2)).thenReturn(Optional.of(outraArea));
        when(avaliacaoRepository.findById(3)).thenReturn(Optional.of(mesmaAreaOutroCriador));
        authenticateAs(gestor);

        assertThatThrownBy(() -> avaliacaoService.buscarAvaliacaoDetalhada(99))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> avaliacaoService.buscarAvaliacaoDetalhada(1))
                .isInstanceOf(UnauthorizedActionException.class);
        assertThatThrownBy(() -> avaliacaoService.buscarAvaliacaoDetalhada(2))
                .isInstanceOf(UnauthorizedActionException.class);
        assertThatThrownBy(() -> avaliacaoService.buscarAvaliacaoDetalhada(3))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldFindInstanciasAndRespostasWhenAuthorized() {
        Funcionario admin = TestDataFactory.funcionario(1, "Admin", 1, 10);
        Funcionario criador = TestDataFactory.funcionario(2, "Gestor", 2, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, criador);
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(10L, TestDataFactory.funcionario(4, "Ana", 3, 10), avaliacao);
        RespostaColaborador resposta = TestDataFactory.resposta(5L, instancia, TestDataFactory.pergunta(1L, "Pergunta", null, "texto"), null);
        when(avaliacaoRepository.findById(1)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoFuncionarioRepository.findByAvaliacaoCodigo(1)).thenReturn(List.of(instancia));
        when(avaliacaoFuncionarioRepository.findById(10L)).thenReturn(Optional.of(instancia));
        when(respostaColaboradorRepository.findByAvaliacaoFuncionarioCodigo(10L)).thenReturn(List.of(resposta));
        authenticateAs(admin);

        List<AvaliacaoFuncionarioResponseDTO> instancias = avaliacaoService.buscarInstanciasPorAvaliacao(1);
        List<RespostaColaboradorResponseDTO> respostas = avaliacaoService.buscarRespostasPorInstancia(10L);

        assertThat(instancias).hasSize(1);
        assertThat(respostas).hasSize(1);
    }

    @Test
    void shouldRejectMissingInstanciasAndRespostas() {
        when(avaliacaoRepository.findById(1)).thenReturn(Optional.empty());
        when(avaliacaoFuncionarioRepository.findById(10L)).thenReturn(Optional.empty());
        authenticateAs(TestDataFactory.funcionario(1, "Admin", 1, 10));

        assertThatThrownBy(() -> avaliacaoService.buscarInstanciasPorAvaliacao(1))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> avaliacaoService.buscarRespostasPorInstancia(10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private void authenticateAs(Funcionario funcionario) {
        SecurityContextHolder.getContext().setAuthentication(TestDataFactory.authenticationFor(funcionario));
    }
}
