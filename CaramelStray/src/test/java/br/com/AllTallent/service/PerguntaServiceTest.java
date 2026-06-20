package br.com.AllTallent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.AllTallent.dto.OpcaoRequest;
import br.com.AllTallent.dto.PerguntaRequestDTO;
import br.com.AllTallent.dto.PerguntaResponseDTO;
import br.com.AllTallent.model.Competencia;
import br.com.AllTallent.model.Pergunta;
import br.com.AllTallent.repository.CompetenciaRepository;
import br.com.AllTallent.repository.PerguntaRepository;
import br.com.AllTallent.support.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PerguntaServiceTest {

    @Mock
    private PerguntaRepository perguntaRepository;
    @Mock
    private CompetenciaRepository competenciaRepository;

    private PerguntaService perguntaService;

    @BeforeEach
    void setUp() {
        perguntaService = new PerguntaService(perguntaRepository, competenciaRepository);
    }

    @Test
    void shouldCreateOpenQuestionWithoutOptions() {
        Competencia competencia = TestDataFactory.competencia(1, "Comunicação");
        PerguntaRequestDTO dto = new PerguntaRequestDTO("Pergunta", 1, "dissertativa", null);
        when(competenciaRepository.findById(1)).thenReturn(Optional.of(competencia));
        when(perguntaRepository.save(any(Pergunta.class))).thenAnswer(invocation -> {
            Pergunta pergunta = invocation.getArgument(0);
            pergunta.setCodigo(10L);
            return pergunta;
        });

        PerguntaResponseDTO response = perguntaService.criarPergunta(dto);

        assertThat(response.codigo()).isEqualTo(10L);
        ArgumentCaptor<Pergunta> captor = ArgumentCaptor.forClass(Pergunta.class);
        verify(perguntaRepository).save(captor.capture());
        assertThat(captor.getValue().getOpcoes()).isNull();
    }

    @Test
    void shouldCreateMultipleChoiceQuestionWithValidOptions() {
        Competencia competencia = TestDataFactory.competencia(1, "Comunicação");
        PerguntaRequestDTO dto = new PerguntaRequestDTO(
                "Pergunta",
                1,
                "Múltipla Escolha",
                List.of(new OpcaoRequest(" A ", true), new OpcaoRequest("", false), new OpcaoRequest("B", false)));
        when(competenciaRepository.findById(1)).thenReturn(Optional.of(competencia));
        when(perguntaRepository.save(any(Pergunta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerguntaResponseDTO response = perguntaService.criarPergunta(dto);

        assertThat(response.pergunta()).isEqualTo("Pergunta");
        ArgumentCaptor<Pergunta> captor = ArgumentCaptor.forClass(Pergunta.class);
        verify(perguntaRepository).save(captor.capture());
        assertThat(captor.getValue().getOpcoes()).hasSize(2);
        assertThat(captor.getValue().getOpcoes()).allMatch(opcao -> opcao.getPergunta() == captor.getValue());
    }

    @Test
    void shouldIgnoreMissingOptionsForMultipleChoiceQuestion() {
        Competencia competencia = TestDataFactory.competencia(1, "Comunicação");
        PerguntaRequestDTO dto = new PerguntaRequestDTO("Pergunta", 1, "multipla", List.of());
        when(competenciaRepository.findById(1)).thenReturn(Optional.of(competencia));
        when(perguntaRepository.save(any(Pergunta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        perguntaService.criarPergunta(dto);

        ArgumentCaptor<Pergunta> captor = ArgumentCaptor.forClass(Pergunta.class);
        verify(perguntaRepository).save(captor.capture());
        assertThat(captor.getValue().getOpcoes()).isNull();
    }

    @Test
    void shouldIgnoreNullOptionListForMultipleChoiceQuestion() {
        Competencia competencia = TestDataFactory.competencia(1, "ComunicaÃ§Ã£o");
        PerguntaRequestDTO dto = new PerguntaRequestDTO("Pergunta", 1, "multipla", null);
        when(competenciaRepository.findById(1)).thenReturn(Optional.of(competencia));
        when(perguntaRepository.save(any(Pergunta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        perguntaService.criarPergunta(dto);

        ArgumentCaptor<Pergunta> captor = ArgumentCaptor.forClass(Pergunta.class);
        verify(perguntaRepository).save(captor.capture());
        assertThat(captor.getValue().getOpcoes()).isNull();
    }

    @Test
    void shouldHandleNullQuestionTypeAndNullOptionDescription() {
        Competencia competencia = TestDataFactory.competencia(1, "Comunicação");
        PerguntaRequestDTO dto = new PerguntaRequestDTO(
                "Pergunta",
                1,
                null,
                List.of(new OpcaoRequest(null, true)));
        when(competenciaRepository.findById(1)).thenReturn(Optional.of(competencia));
        when(perguntaRepository.save(any(Pergunta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerguntaResponseDTO response = perguntaService.criarPergunta(dto);

        assertThat(response.pergunta()).isEqualTo("Pergunta");
    }

    @Test
    void shouldIgnoreBlankAndNullOptionsInsideMultipleChoiceQuestion() {
        Competencia competencia = TestDataFactory.competencia(1, "ComunicaÃ§Ã£o");
        PerguntaRequestDTO dto = new PerguntaRequestDTO(
                "Pergunta",
                1,
                "multipla escolha",
                List.of(new OpcaoRequest(null, true), new OpcaoRequest("  ", false), new OpcaoRequest("Valida", false)));
        when(competenciaRepository.findById(1)).thenReturn(Optional.of(competencia));
        when(perguntaRepository.save(any(Pergunta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        perguntaService.criarPergunta(dto);

        ArgumentCaptor<Pergunta> captor = ArgumentCaptor.forClass(Pergunta.class);
        verify(perguntaRepository).save(captor.capture());
        assertThat(captor.getValue().getOpcoes()).hasSize(1);
    }

    @Test
    void shouldFailWhenCompetenciaDoesNotExist() {
        PerguntaRequestDTO dto = new PerguntaRequestDTO("Pergunta", 1, "dissertativa", null);
        when(competenciaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perguntaService.criarPergunta(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Competência");
    }

    @Test
    void shouldListFindAndDeleteQuestions() {
        Pergunta pergunta = TestDataFactory.pergunta(1L, "Pergunta", TestDataFactory.competencia(2, "Tech"), "texto");
        when(perguntaRepository.findAll()).thenReturn(List.of(pergunta));
        when(perguntaRepository.findById(1L)).thenReturn(Optional.of(pergunta));
        when(perguntaRepository.existsById(1L)).thenReturn(true);

        assertThat(perguntaService.listarTodas()).hasSize(1);
        assertThat(perguntaService.buscarPorId(1L).codigo()).isEqualTo(1L);
        perguntaService.deletarPergunta(1L);
        verify(perguntaRepository).deleteById(1L);
    }

    @Test
    void shouldFailToDeleteMissingQuestion() {
        when(perguntaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> perguntaService.deletarPergunta(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pergunta");
    }
}
