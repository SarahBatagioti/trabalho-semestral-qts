package br.com.AllTallent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.com.AllTallent.dto.AreaQuantidadeDTO;
import br.com.AllTallent.dto.CompetenciaQuantidadeDTO;
import br.com.AllTallent.dto.DashboardResponseDTO;
import br.com.AllTallent.dto.MesQuantidadeProjection;
import br.com.AllTallent.model.Avaliacao;
import br.com.AllTallent.model.AvaliacaoFuncionario;
import br.com.AllTallent.model.Competencia;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.RespostaColaborador;
import br.com.AllTallent.repository.AvaliacaoFuncionarioRepository;
import br.com.AllTallent.repository.AvaliacaoRepository;
import br.com.AllTallent.repository.FuncionarioRepository;
import br.com.AllTallent.repository.RespostaColaboradorRepository;
import br.com.AllTallent.support.TestDataFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private AvaliacaoRepository avaliacaoRepository;
    @Mock
    private AvaliacaoFuncionarioRepository avaliacaoFuncionarioRepository;
    @Mock
    private RespostaColaboradorRepository respostaColaboradorRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                funcionarioRepository,
                avaliacaoRepository,
                avaliacaoFuncionarioRepository,
                respostaColaboradorRepository);
    }

    @Test
    void shouldBuildDashboardWithoutAreaFilterAndZeroMeta() {
        when(funcionarioRepository.count()).thenReturn(10L);
        when(avaliacaoFuncionarioRepository.countTotalPendentes()).thenReturn(2);
        when(avaliacaoFuncionarioRepository.countConcluidasNoMes(any(), any())).thenReturn(0);
        when(avaliacaoFuncionarioRepository.countAprovadasNoMes(any(), any())).thenReturn(0);
        when(funcionarioRepository.findEvolucaoMensal()).thenReturn(List.of(projection("2025-01", 3)));
        when(funcionarioRepository.countFuncionariosPorCompetencia())
                .thenReturn(List.of(new CompetenciaQuantidadeDTO("Java", 2L)));
        when(avaliacaoFuncionarioRepository.findTopCompetenciasMaisAvaliadas(any()))
                .thenReturn(List.of(new CompetenciaQuantidadeDTO("Java", 4L)));
        when(funcionarioRepository.countFuncionariosPorArea())
                .thenReturn(List.of(new AreaQuantidadeDTO("Tecnologia", 5L)));

        DashboardResponseDTO response = dashboardService.getDashboardData(null);

        assertThat(response.getTotalColaboradores()).isEqualTo(10L);
        assertThat(response.getMetaMensal()).isEqualTo(0.0);
        assertThat(response.getEvolucaoMensal()).hasSize(1);
        assertThat(response.getTop5CompetenciasMaisAvaliadas()).hasSize(1);
    }

    @Test
    void shouldBuildDashboardWithAreaFilterAndCalculatedMeta() {
        when(funcionarioRepository.countByAreaCodigo(10)).thenReturn(8L);
        when(avaliacaoFuncionarioRepository.countTotalPendentesByArea(10)).thenReturn(1);
        when(avaliacaoFuncionarioRepository.countConcluidasNoMesByArea(any(), any(), eq(10))).thenReturn(4);
        when(avaliacaoFuncionarioRepository.countAprovadasNoMesByArea(any(), any(), eq(10))).thenReturn(3);
        when(funcionarioRepository.findEvolucaoMensalByArea(10)).thenReturn(List.of(projection("2025-02", 2)));
        when(funcionarioRepository.countFuncionariosPorCompetencia()).thenReturn(List.of());
        when(avaliacaoFuncionarioRepository.findTopCompetenciasMaisAvaliadas(any())).thenReturn(List.of());
        when(funcionarioRepository.countFuncionariosPorArea()).thenReturn(List.of());

        DashboardResponseDTO response = dashboardService.getDashboardData(10);

        assertThat(response.getTotalColaboradores()).isEqualTo(8L);
        assertThat(response.getMetaMensal()).isEqualTo(75.0);
    }

    @Test
    void shouldGenerateLegacyResumoAndDistributions() {
        Funcionario semArea = TestDataFactory.funcionario(1, "Ana", 3, null);
        Competencia semNome = TestDataFactory.competencia(1, "Java");
        semNome.setNome(" ");
        Competencia java = TestDataFactory.competencia(2, "Java");
        Funcionario comArea = TestDataFactory.funcionario(2, "Bia", 3, 10);
        TestDataFactory.addCompetencias(semArea, semNome);
        TestDataFactory.addCompetencias(comArea, java);
        Avaliacao concluida = TestDataFactory.avaliacao(1, comArea);
        concluida.setStatus("CONCLUIDO");
        Avaliacao pendente = TestDataFactory.avaliacao(2, comArea);
        pendente.setStatus("PENDENTE");
        AvaliacaoFuncionario instanciaSemEntrega = TestDataFactory.avaliacaoFuncionario(11L, semArea, pendente);
        AvaliacaoFuncionario instanciaComEntrega = TestDataFactory.avaliacaoFuncionario(12L, comArea, concluida);
        RespostaColaborador resposta = TestDataFactory.resposta(1L, instanciaComEntrega, null, null);
        when(funcionarioRepository.findAll()).thenReturn(List.of(semArea, comArea));
        when(avaliacaoRepository.findAll()).thenReturn(List.of(concluida, pendente));
        when(avaliacaoFuncionarioRepository.findAll()).thenReturn(List.of(instanciaSemEntrega, instanciaComEntrega));
        when(respostaColaboradorRepository.findByAvaliacaoFuncionarioCodigo(11L)).thenReturn(List.of());
        when(respostaColaboradorRepository.findByAvaliacaoFuncionarioCodigo(12L)).thenReturn(List.of(resposta));

        Map<String, Object> resumo = dashboardService.gerarResumo();
        Map<String, Long> porArea = dashboardService.getDistribuicaoPorArea();
        Map<String, Long> porCompetencia = dashboardService.getDistribuicaoPorCompetencias();

        assertThat(resumo).containsEntry("totalColaboradores", 2L);
        assertThat((List<String>) resumo.get("colaboradoresSemEntrega")).contains("Ana");
        assertThat(porArea).containsEntry("Sem área", 1L);
        assertThat(porCompetencia).containsEntry("Sem nome", 1L).containsEntry("Java", 1L);
    }

    @Test
    void shouldExposeAggregatedQueries() {
        List<AreaQuantidadeDTO> areas = List.of(new AreaQuantidadeDTO("Tech", 5L));
        List<CompetenciaQuantidadeDTO> competencias = List.of(new CompetenciaQuantidadeDTO("Java", 5L));
        when(funcionarioRepository.countFuncionariosPorArea()).thenReturn(areas);
        when(funcionarioRepository.countFuncionariosPorCompetencia()).thenReturn(competencias);
        when(avaliacaoFuncionarioRepository.findTopCompetenciasMaisAvaliadas(any())).thenReturn(competencias);

        assertThat(dashboardService.getTotalColaboradoresArea()).isEqualTo(areas);
        assertThat(dashboardService.getTotalColaboradoresCompetencia()).isEqualTo(competencias);
        assertThat(dashboardService.getTop5CompetenciasMaisAvaliadas()).isEqualTo(competencias);
    }

    @Test
    void shouldKeepMetaZeroWhenConcludedCountIsNullAndHandleNullCompetenciaName() {
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        Competencia competenciaSemNome = TestDataFactory.competencia(1, "Java");
        competenciaSemNome.setNome(null);
        TestDataFactory.addCompetencias(funcionario, competenciaSemNome);
        when(funcionarioRepository.count()).thenReturn(1L);
        when(avaliacaoFuncionarioRepository.countTotalPendentes()).thenReturn(0);
        when(avaliacaoFuncionarioRepository.countConcluidasNoMes(any(), any())).thenReturn(null);
        when(avaliacaoFuncionarioRepository.countAprovadasNoMes(any(), any())).thenReturn(0);
        when(funcionarioRepository.findEvolucaoMensal()).thenReturn(List.of());
        when(funcionarioRepository.countFuncionariosPorCompetencia()).thenReturn(List.of());
        when(avaliacaoFuncionarioRepository.findTopCompetenciasMaisAvaliadas(any())).thenReturn(List.of());
        when(funcionarioRepository.countFuncionariosPorArea()).thenReturn(List.of());
        when(funcionarioRepository.findAll()).thenReturn(List.of(funcionario));

        DashboardResponseDTO response = dashboardService.getDashboardData(null);

        assertThat(response.getMetaMensal()).isEqualTo(0.0);
        assertThat(dashboardService.getDistribuicaoPorCompetencias()).containsEntry("Sem nome", 1L);
    }

    private MesQuantidadeProjection projection(String mes, long quantidade) {
        return new MesQuantidadeProjection() {
            @Override
            public String getMes() {
                return mes;
            }

            @Override
            public Long getQuantidade() {
                return quantidade;
            }
        };
    }
}
