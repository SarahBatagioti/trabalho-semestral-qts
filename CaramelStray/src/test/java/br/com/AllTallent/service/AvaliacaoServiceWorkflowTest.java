package br.com.AllTallent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.AllTallent.dto.AvaliacaoParaResponderDTO;
import br.com.AllTallent.dto.AvaliacaoRequestDTO;
import br.com.AllTallent.dto.AvaliacaoResponseDTO;
import br.com.AllTallent.dto.AvaliacaoRevisaoDTO;
import br.com.AllTallent.dto.RespostaColaboradorRequestDTO;
import br.com.AllTallent.dto.RespostaColaboradorResponseDTO;
import br.com.AllTallent.dto.RevisaoDetalhadaDTO;
import br.com.AllTallent.dto.RevisaoSupervisorRequestDTO;
import br.com.AllTallent.exception.UnauthorizedActionException;
import br.com.AllTallent.model.Avaliacao;
import br.com.AllTallent.model.AvaliacaoFuncionario;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.Pergunta;
import br.com.AllTallent.model.PerguntaOpcao;
import br.com.AllTallent.model.RespostaColaborador;
import br.com.AllTallent.repository.AvaliacaoFuncionarioRepository;
import br.com.AllTallent.repository.AvaliacaoRepository;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.repository.PerguntaOpcaoRepository;
import br.com.AllTallent.repository.PerguntaRepository;
import br.com.AllTallent.repository.RespostaColaboradorRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceWorkflowTest {

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
    void shouldCreateAvaliacaoForGestorSameAreaColaborador() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario alvo = TestDataFactory.funcionario(2, "Colaborador", 3, 10);
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", TestDataFactory.competencia(1, "Tech"), "texto");
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO("Nova", LocalDate.of(2025, 2, 1), List.of(2), List.of(5L));
        Avaliacao avaliacaoSalva = TestDataFactory.avaliacao(10, gestor);
        when(funcionarioRepository.getReferenceById(1)).thenReturn(gestor);
        when(perguntaRepository.findAllById(List.of(5L))).thenReturn(List.of(pergunta));
        when(funcionarioRepository.findAllById(List.of(2))).thenReturn(List.of(alvo));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenReturn(avaliacaoSalva);
        when(avaliacaoFuncionarioRepository.save(any(AvaliacaoFuncionario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        authenticateAs(gestor);

        AvaliacaoResponseDTO response = avaliacaoService.criarAvaliacaoCompleta(dto);

        assertThat(response.codigo()).isEqualTo(10);
        verify(avaliacaoFuncionarioRepository).save(any(AvaliacaoFuncionario.class));
    }

    @Test
    void shouldCreateAvaliacaoForAdminInSameArea() {
        Funcionario admin = TestDataFactory.funcionario(1, "Admin", 1, 10);
        Funcionario gestorAlvo = TestDataFactory.funcionario(2, "Gestor", 2, 10);
        Funcionario colaboradorAlvo = TestDataFactory.funcionario(3, "Colaborador", 3, 10);
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", TestDataFactory.competencia(1, "Tech"), "texto");
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO("Nova", LocalDate.of(2025, 2, 1), List.of(2, 3), List.of(5L));
        Avaliacao avaliacaoSalva = TestDataFactory.avaliacao(10, admin);
        when(funcionarioRepository.getReferenceById(1)).thenReturn(admin);
        when(perguntaRepository.findAllById(List.of(5L))).thenReturn(List.of(pergunta));
        when(funcionarioRepository.findAllById(List.of(2, 3))).thenReturn(List.of(gestorAlvo, colaboradorAlvo));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenReturn(avaliacaoSalva);
        when(avaliacaoFuncionarioRepository.save(any(AvaliacaoFuncionario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        authenticateAs(admin);

        AvaliacaoResponseDTO response = avaliacaoService.criarAvaliacaoCompleta(dto);

        assertThat(response.codigo()).isEqualTo(10);
    }

    @Test
    void shouldRejectCreationWhenQuestionOrFuncionarioIsMissing() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        authenticateAs(gestor);
        when(funcionarioRepository.getReferenceById(1)).thenReturn(gestor);
        when(perguntaRepository.findAllById(List.of(5L))).thenReturn(List.of());

        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(2), List.of(5L))))
                .isInstanceOf(EntityNotFoundException.class);

        when(perguntaRepository.findAllById(List.of(5L)))
                .thenReturn(List.of(TestDataFactory.pergunta(5L, "Pergunta", null, "texto")));
        when(funcionarioRepository.findAllById(List.of(2))).thenReturn(List.of());

        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(2), List.of(5L))))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldRejectCreationForUnauthorizedTargets() {
        Funcionario user = TestDataFactory.funcionario(1, "User", 3, 10);
        Funcionario self = TestDataFactory.funcionario(1, "User", 3, 10);
        Funcionario alvoSemPerfil = TestDataFactory.funcionario(2, "Sem Perfil", null, 10);
        Funcionario gestor = TestDataFactory.funcionario(3, "Gestor", 2, 10);
        Funcionario alvoSupervisor = TestDataFactory.funcionario(4, "Supervisor", 2, 10);
        Funcionario alvoOutroSetor = TestDataFactory.funcionario(7, "Outro Setor", 3, 20);
        Funcionario admin = TestDataFactory.funcionario(8, "Admin", 1, 10);
        Funcionario alvoAdmin = TestDataFactory.funcionario(9, "Outro Admin", 1, 10);
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", null, "texto");
        when(perguntaRepository.findAllById(List.of(5L))).thenReturn(List.of(pergunta));

        authenticateAs(user);
        when(funcionarioRepository.getReferenceById(1)).thenReturn(user);
        when(funcionarioRepository.findAllById(List.of(1))).thenReturn(List.of(self));
        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(1), List.of(5L))))
                .isInstanceOf(UnauthorizedActionException.class);

        when(funcionarioRepository.findAllById(List.of(2))).thenReturn(List.of(alvoSemPerfil));
        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(2), List.of(5L))))
                .isInstanceOf(UnauthorizedActionException.class);

        authenticateAs(gestor);
        when(funcionarioRepository.getReferenceById(3)).thenReturn(gestor);
        when(funcionarioRepository.findAllById(List.of(4))).thenReturn(List.of(alvoSupervisor));
        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(4), List.of(5L))))
                .isInstanceOf(UnauthorizedActionException.class);
        when(funcionarioRepository.findAllById(List.of(7))).thenReturn(List.of(alvoOutroSetor));
        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(7), List.of(5L))))
                .isInstanceOf(UnauthorizedActionException.class);

        Funcionario adminSemArea = TestDataFactory.funcionario(5, "Admin", 1, null);
        Funcionario alvoSemArea = TestDataFactory.funcionario(6, "Sem Area", 3, null);
        authenticateAs(adminSemArea);
        when(funcionarioRepository.getReferenceById(5)).thenReturn(adminSemArea);
        when(funcionarioRepository.findAllById(List.of(6))).thenReturn(List.of(alvoSemArea));
        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(6), List.of(5L))))
                .isInstanceOf(UnauthorizedActionException.class);

        authenticateAs(admin);
        when(funcionarioRepository.getReferenceById(8)).thenReturn(admin);
        when(funcionarioRepository.findAllById(List.of(9))).thenReturn(List.of(alvoAdmin));
        assertThatThrownBy(() -> avaliacaoService.criarAvaliacaoCompleta(
                new AvaliacaoRequestDTO("Nova", LocalDate.now(), List.of(9), List.of(5L))))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldSaveOrUpdateResposta() {
        Funcionario colaborador = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, TestDataFactory.funcionario(10, "Gestor", 2, 10));
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(20L, colaborador, avaliacao);
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", null, "texto");
        PerguntaOpcao opcao = TestDataFactory.opcao(6L, pergunta, "A", true);
        when(avaliacaoFuncionarioRepository.findById(20L)).thenReturn(Optional.of(instancia));
        when(perguntaRepository.findById(5L)).thenReturn(Optional.of(pergunta));
        when(respostaColaboradorRepository.findByFuncionarioAvaliacaoCodigoAndPerguntaCodigo(20L, 5L))
                .thenReturn(Optional.empty());
        when(respostaColaboradorRepository.save(any(RespostaColaborador.class)))
                .thenAnswer(invocation -> {
                    RespostaColaborador resposta = invocation.getArgument(0);
                    if (resposta.getCodigo() == null) {
                        resposta.setCodigo(99L);
                    }
                    return resposta;
                });
        authenticateAs(colaborador);

        RespostaColaboradorResponseDTO novaResposta = avaliacaoService.salvarOuAtualizarResposta(
                new RespostaColaboradorRequestDTO(20L, 5L, "Texto", null));
        assertThat(novaResposta.codigo()).isEqualTo(99L);

        RespostaColaborador existente = TestDataFactory.resposta(88L, instancia, pergunta, null);
        when(respostaColaboradorRepository.findByFuncionarioAvaliacaoCodigoAndPerguntaCodigo(20L, 5L))
                .thenReturn(Optional.of(existente));
        when(perguntaOpcaoRepository.findById(6L)).thenReturn(Optional.of(opcao));
        RespostaColaboradorResponseDTO atualizada = avaliacaoService.salvarOuAtualizarResposta(
                new RespostaColaboradorRequestDTO(20L, 5L, "Texto", 6L));
        assertThat(atualizada.codigo()).isEqualTo(88L);
    }

    @Test
    void shouldRejectInvalidRespostaOperations() {
        Funcionario colaborador = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Funcionario outro = TestDataFactory.funcionario(2, "Bia", 3, 10);
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(20L, outro, TestDataFactory.avaliacao(1, outro));
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", null, "texto");
        Pergunta outraPergunta = TestDataFactory.pergunta(7L, "Outra", null, "texto");
        PerguntaOpcao opcaoInvalida = TestDataFactory.opcao(6L, outraPergunta, "A", true);
        authenticateAs(colaborador);
        when(avaliacaoFuncionarioRepository.findById(20L)).thenReturn(Optional.of(instancia));
        when(perguntaRepository.findById(5L)).thenReturn(Optional.of(pergunta));
        when(perguntaOpcaoRepository.findById(6L)).thenReturn(Optional.of(opcaoInvalida));

        assertThatThrownBy(() -> avaliacaoService.salvarOuAtualizarResposta(
                new RespostaColaboradorRequestDTO(20L, 5L, "Texto", null)))
                .isInstanceOf(UnauthorizedActionException.class);

        when(avaliacaoFuncionarioRepository.findById(21L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> avaliacaoService.salvarOuAtualizarResposta(
                new RespostaColaboradorRequestDTO(21L, 5L, "Texto", null)))
                .isInstanceOf(EntityNotFoundException.class);

        when(avaliacaoFuncionarioRepository.findById(20L))
                .thenReturn(Optional.of(TestDataFactory.avaliacaoFuncionario(20L, colaborador, TestDataFactory.avaliacao(1, outro))));
        when(perguntaRepository.findById(50L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> avaliacaoService.salvarOuAtualizarResposta(
                new RespostaColaboradorRequestDTO(20L, 50L, "Texto", null)))
                .isInstanceOf(EntityNotFoundException.class);

        when(perguntaRepository.findById(5L)).thenReturn(Optional.of(pergunta));
        when(perguntaOpcaoRepository.findById(60L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> avaliacaoService.salvarOuAtualizarResposta(
                new RespostaColaboradorRequestDTO(20L, 5L, "Texto", 60L)))
                .isInstanceOf(EntityNotFoundException.class);

        when(perguntaOpcaoRepository.findById(6L)).thenReturn(Optional.of(opcaoInvalida));
        assertThatThrownBy(() -> avaliacaoService.salvarOuAtualizarResposta(
                new RespostaColaboradorRequestDTO(20L, 5L, "Texto", 6L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReviewAndExposePendingFlows() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario colaborador = TestDataFactory.funcionario(2, "Colab", 3, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, gestor);
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(20L, colaborador, avaliacao);
        when(avaliacaoFuncionarioRepository.findById(20L)).thenReturn(Optional.of(instancia));
        when(avaliacaoFuncionarioRepository.save(any(AvaliacaoFuncionario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(avaliacaoFuncionarioRepository.findByFuncionarioCodigo(2))
                .thenReturn(List.of(instancia, TestDataFactory.avaliacaoFuncionario(21L, colaborador, avaliacao)));
        authenticateAs(gestor);

        assertThat(avaliacaoService.salvarRevisaoSupervisor(20L,
                new RevisaoSupervisorRequestDTO("ok", "feedback", "APROVADO")).getResultadoStatus())
                .isEqualTo("APROVADO");

        authenticateAs(colaborador);
        instancia.setResultadoStatus("PENDENTE");
        avaliacaoService.finalizarPeloColaborador(20L);
        assertThat(instancia.getResultadoStatus()).isEqualTo("AGUARDANDO_REVISAO");
        assertThat(avaliacaoService.buscarPendentesPorFuncionario(2)).hasSize(1);
    }

    @Test
    void shouldRejectInvalidReviewAndFinalizeFlows() {
        Funcionario gestor = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Funcionario supervisor = TestDataFactory.funcionario(2, "Supervisor", 2, 10);
        Funcionario colaborador = TestDataFactory.funcionario(3, "Colab", 3, 10);
        AvaliacaoFuncionario instanciaSupervisor = TestDataFactory.avaliacaoFuncionario(20L, supervisor, TestDataFactory.avaliacao(1, gestor));
        AvaliacaoFuncionario instanciaColaborador = TestDataFactory.avaliacaoFuncionario(21L, colaborador, TestDataFactory.avaliacao(1, gestor));
        authenticateAs(gestor);
        when(avaliacaoFuncionarioRepository.findById(20L)).thenReturn(Optional.of(instanciaSupervisor));

        assertThatThrownBy(() -> avaliacaoService.salvarRevisaoSupervisor(20L,
                new RevisaoSupervisorRequestDTO("ok", "feedback", "APROVADO")))
                .isInstanceOf(UnauthorizedActionException.class);

        authenticateAs(colaborador);
        when(avaliacaoFuncionarioRepository.findById(21L)).thenReturn(Optional.of(instanciaColaborador));
        instanciaColaborador.setResultadoStatus("CONCLUIDO");
        assertThatThrownBy(() -> avaliacaoService.finalizarPeloColaborador(21L))
                .isInstanceOf(IllegalStateException.class);

        when(avaliacaoFuncionarioRepository.findById(22L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> avaliacaoService.finalizarPeloColaborador(22L))
                .isInstanceOf(EntityNotFoundException.class);

        authenticateAs(TestDataFactory.funcionario(4, "Outro", 3, 10));
        instanciaColaborador.setResultadoStatus("PENDENTE");
        when(avaliacaoFuncionarioRepository.findById(23L)).thenReturn(Optional.of(instanciaColaborador));
        assertThatThrownBy(() -> avaliacaoService.finalizarPeloColaborador(23L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void shouldProvideAvaliacaoForResponderAndRevisao() {
        Funcionario colaborador = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, TestDataFactory.funcionario(10, "Gestor", 2, 10));
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", TestDataFactory.competencia(1, "Tech"), "multipla");
        PerguntaOpcao opcao = TestDataFactory.opcao(6L, pergunta, "A", true);
        TestDataFactory.addOpcoes(pergunta, opcao);
        TestDataFactory.addPerguntas(avaliacao, pergunta);
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(20L, colaborador, avaliacao);
        RespostaColaborador resposta = TestDataFactory.resposta(1L, instancia, pergunta, opcao);
        TestDataFactory.addRespostas(instancia, resposta);
        when(avaliacaoFuncionarioRepository.findById(20L)).thenReturn(Optional.of(instancia));
        authenticateAs(colaborador);

        AvaliacaoParaResponderDTO responder = avaliacaoService.buscarParaResponder(20L);
        AvaliacaoRevisaoDTO revisao = avaliacaoService.buscarParaRevisao(20L);

        assertThat(responder.perguntas()).hasSize(1);
        assertThat(revisao.perguntasComRespostas()).hasSize(1);
    }

    @Test
    void shouldRejectInvalidResponderAndRevisaoLookups() {
        Funcionario colaborador = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Funcionario outro = TestDataFactory.funcionario(2, "Bia", 3, 10);
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(20L, outro, TestDataFactory.avaliacao(1, outro));
        authenticateAs(colaborador);
        when(avaliacaoFuncionarioRepository.findById(20L)).thenReturn(Optional.of(instancia));

        assertThatThrownBy(() -> avaliacaoService.buscarParaResponder(20L))
                .isInstanceOf(UnauthorizedActionException.class);

        AvaliacaoFuncionario semBase = TestDataFactory.avaliacaoFuncionario(21L, colaborador, null);
        when(avaliacaoFuncionarioRepository.findById(21L)).thenReturn(Optional.of(semBase));
        assertThatThrownBy(() -> avaliacaoService.buscarParaResponder(21L))
                .isInstanceOf(IllegalStateException.class);

        when(avaliacaoFuncionarioRepository.findById(22L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> avaliacaoService.buscarParaRevisao(22L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldBuildDadosRevisaoAndHandleMissingInstance() {
        Funcionario colaborador = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, TestDataFactory.funcionario(10, "Gestor", 2, 10));
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(20L, colaborador, avaliacao);
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", null, "texto");
        PerguntaOpcao opcao = TestDataFactory.opcao(6L, pergunta, "A", true);
        RespostaColaborador respostaComOpcao = TestDataFactory.resposta(1L, instancia, pergunta, opcao);
        RespostaColaborador respostaSemOpcao = TestDataFactory.resposta(2L, instancia, pergunta, null);
        when(avaliacaoFuncionarioRepository.existsById(20L)).thenReturn(true);
        when(respostaColaboradorRepository.findByAvaliacaoFuncionarioCodigo(20L))
                .thenReturn(List.of(respostaComOpcao, respostaSemOpcao));

        List<RevisaoDetalhadaDTO> response = avaliacaoService.buscarDadosRevisao(20L);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getOpcaoSelecionadaId()).isEqualTo(6L);
        assertThat(response.get(1).getOpcaoSelecionadaId()).isNull();

        when(avaliacaoFuncionarioRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> avaliacaoService.buscarDadosRevisao(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private void authenticateAs(Funcionario funcionario) {
        SecurityContextHolder.getContext().setAuthentication(TestDataFactory.authenticationFor(funcionario));
    }
}
