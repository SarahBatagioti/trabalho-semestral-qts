package br.com.AllTallent.dto;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.AllTallent.exception.ResourceNotFoundException;
import br.com.AllTallent.exception.UnauthorizedActionException;
import br.com.AllTallent.model.Avaliacao;
import br.com.AllTallent.model.AvaliacaoFuncionario;
import br.com.AllTallent.model.Competencia;
import br.com.AllTallent.model.Experiencia;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.FuncionarioCertificado;
import br.com.AllTallent.model.Pergunta;
import br.com.AllTallent.model.PerguntaOpcao;
import br.com.AllTallent.model.RespostaColaborador;
import br.com.AllTallent.support.TestDataFactory;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DtoAndModelCoverageTest {

    @Test
    void shouldCoverRequestAndSimpleDtos() {
        AreaQuantidadeDTO areaQuantidadeDTO = new AreaQuantidadeDTO("Tech", 2L);
        CompetenciaQuantidadeDTO competenciaQuantidadeDTO = new CompetenciaQuantidadeDTO("Java", 3L);
        DashboardResponseDTO dashboardResponseDTO = DashboardResponseDTO.builder()
                .totalColaboradores(10L)
                .avaliacoesConcluidasMes(3)
                .metaMensal(75.0)
                .totalPendencias(2)
                .evolucaoMensal(List.of(new MesQuantidadeDTO("2025-01", 2)))
                .totalColaboradoresArea(List.of(areaQuantidadeDTO))
                .totalColaboradoresCompetencia(List.of(competenciaQuantidadeDTO))
                .top5CompetenciasMaisAvaliadas(List.of(competenciaQuantidadeDTO))
                .build();
        CadastroRequestDTO cadastroRequestDTO = TestDataFactory.cadastroRequest();
        FuncionarioResumoUpdateDTO resumoUpdateDTO = new FuncionarioResumoUpdateDTO();
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("ana@mail.com", "secret");
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO("token", 1, "Ana");
        FuncionarioCompetenciaUpdateDTO competenciaUpdateDTO = new FuncionarioCompetenciaUpdateDTO(List.of(1, 2));
        FuncionarioRequestDTO funcionarioRequestDTO = new FuncionarioRequestDTO("Ana", "ana@mail.com", "123", "119", "secret", 1, 2, 3, "Dev", "SP", "Resumo");
        CertificadoRequestDTO certificadoRequestDTO = new CertificadoRequestDTO("AWS");
        ExperienciaRequestDTO experienciaRequestDTO = new ExperienciaRequestDTO("Dev", "OpenAI", LocalDate.now(), LocalDate.now(), "Desc");
        PerguntaRequestDTO perguntaRequestDTO = new PerguntaRequestDTO("Pergunta", 1, "texto", List.of(new OpcaoRequest("A", true)));
        RespostaColaboradorRequestDTO respostaRequestDTO = new RespostaColaboradorRequestDTO(1L, 2L, "Texto", 3L);
        RevisaoSupervisorRequestDTO revisaoSupervisorRequestDTO = new RevisaoSupervisorRequestDTO("interno", "externo", "APROVADO");
        AvaliacaoRequestDTO avaliacaoRequestDTO = new AvaliacaoRequestDTO("Avaliação", LocalDate.now(), List.of(1), List.of(2L));
        PerguntaResponseDTO.OpcaoRequest nestedOption = new PerguntaResponseDTO.OpcaoRequest("B", false);

        assertThat(areaQuantidadeDTO.getNomeArea()).isEqualTo("Tech");
        assertThat(competenciaQuantidadeDTO.getQuantidade()).isEqualTo(3L);
        assertThat(dashboardResponseDTO.getTotalColaboradores()).isEqualTo(10L);
        assertThat(cadastroRequestDTO.getEmail()).isEqualTo("maria@mail.com");
        assertThat(resumoUpdateDTO).isNotNull();
        assertThat(loginRequestDTO.email()).isEqualTo("ana@mail.com");
        assertThat(loginResponseDTO.userId()).isEqualTo(1);
        assertThat(competenciaUpdateDTO.codigosCompetencia()).containsExactly(1, 2);
        assertThat(funcionarioRequestDTO.gestorId()).isEqualTo(3);
        assertThat(certificadoRequestDTO.nome()).isEqualTo("AWS");
        assertThat(experienciaRequestDTO.empresa()).isEqualTo("OpenAI");
        assertThat(perguntaRequestDTO.opcoes()).hasSize(1);
        assertThat(respostaRequestDTO.opcaoSelecionadaCodigo()).isEqualTo(3L);
        assertThat(revisaoSupervisorRequestDTO.resultadoStatus()).isEqualTo("APROVADO");
        assertThat(avaliacaoRequestDTO.codigosPerguntas()).containsExactly(2L);
        assertThat(nestedOption.descricao()).isEqualTo("B");
    }

    @Test
    void shouldCoverDtoMappingsWithNullAndNonNullValues() {
        Funcionario gestor = TestDataFactory.funcionario(9, "Gestor", 2, 10);
        Funcionario funcionario = TestDataFactory.funcionario(1, "Ana", 3, 10);
        funcionario.setGestor(gestor);
        Competencia competencia = TestDataFactory.competencia(3, "Java");
        FuncionarioCertificado certificado = TestDataFactory.certificado(4, funcionario);
        Experiencia experiencia = TestDataFactory.experiencia(5, funcionario);
        TestDataFactory.addCompetencias(funcionario, competencia);
        TestDataFactory.addCertificados(funcionario, certificado);
        TestDataFactory.addExperiencias(funcionario, experiencia);

        FuncionarioResponseDTO funcionarioResponseDTO = new FuncionarioResponseDTO(funcionario);
        FuncionarioPerfilDTO funcionarioPerfilDTO = new FuncionarioPerfilDTO(funcionario);
        FuncionarioCompetenciasResponseDTO competenciasResponseDTO = new FuncionarioCompetenciasResponseDTO(funcionario);
        FuncionarioExperienciasResponseDTO experienciasResponseDTO = new FuncionarioExperienciasResponseDTO(funcionario);
        CompetenciaDTO competenciaDTO = new CompetenciaDTO(competencia);
        CertificadoDTO certificadoDTO = new CertificadoDTO(certificado);
        ExperienciaDTO experienciaDTO = new ExperienciaDTO(experiencia);

        assertThat(funcionarioResponseDTO.nomeGestor()).isEqualTo("Gestor");
        assertThat(funcionarioResponseDTO.certificados()).hasSize(1);
        assertThat(funcionarioPerfilDTO.competencias()).hasSize(1);
        assertThat(competenciasResponseDTO.competencias()).hasSize(1);
        assertThat(experienciasResponseDTO.experiencias()).hasSize(1);
        assertThat(competenciaDTO.nome()).isEqualTo("Java");
        assertThat(certificadoDTO.nome()).isEqualTo("Java");
        assertThat(experienciaDTO.cargo()).isEqualTo("Developer");

        Funcionario funcionarioNulo = TestDataFactory.funcionario(2, "Bia", null, null);
        funcionarioNulo.setCompetencias(null);
        funcionarioNulo.setCertificados(null);
        funcionarioNulo.setExperiencias(null);
        funcionarioNulo.setGestor(null);
        assertThat(new FuncionarioResponseDTO(funcionarioNulo).certificados()).isEmpty();
        assertThat(new FuncionarioPerfilDTO(funcionarioNulo).certificados()).isEmpty();
        assertThat(new FuncionarioCompetenciasResponseDTO(funcionarioNulo).competencias()).isEmpty();
        assertThat(new FuncionarioExperienciasResponseDTO(funcionarioNulo).experiencias()).isEmpty();
    }

    @Test
    void shouldCoverAvaliacaoAndPerguntaDtos() {
        Competencia competencia = TestDataFactory.competencia(1, "Tech");
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", competencia, "multipla");
        PerguntaOpcao opcao = TestDataFactory.opcao(6L, pergunta, "A", true);
        TestDataFactory.addOpcoes(pergunta, opcao);
        Funcionario criador = TestDataFactory.funcionario(1, "Gestor", 2, 10);
        Avaliacao avaliacao = TestDataFactory.avaliacao(2, criador);
        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(7L, TestDataFactory.funcionario(3, "Ana", 3, 10), avaliacao);
        TestDataFactory.addPerguntas(avaliacao, pergunta);
        TestDataFactory.addInstancias(avaliacao, instancia);
        RespostaColaborador resposta = TestDataFactory.resposta(8L, instancia, pergunta, opcao);
        TestDataFactory.addRespostas(instancia, resposta);

        PerguntaResponseDTO perguntaResponseDTO = new PerguntaResponseDTO(pergunta);
        PerguntaOpcaoDTO perguntaOpcaoDTO = new PerguntaOpcaoDTO(opcao);
        PerguntaParaResponderDTO perguntaParaResponderDTO = new PerguntaParaResponderDTO(pergunta);
        PerguntaComRespostaDTO perguntaComRespostaDTO = new PerguntaComRespostaDTO(pergunta, resposta);
        PerguntaComRespostaDTO perguntaComListaRespostaDTO = new PerguntaComRespostaDTO(
                TestDataFactory.pergunta(10L, "Outra", null, "texto"),
                List.of(resposta, TestDataFactory.resposta(9L, instancia, null, null)));
        AvaliacaoResponseDTO avaliacaoResponseDTO = new AvaliacaoResponseDTO(avaliacao);
        AvaliacaoDetalhadaDTO avaliacaoDetalhadaDTO = new AvaliacaoDetalhadaDTO(avaliacao);
        AvaliacaoFuncionarioResponseDTO avaliacaoFuncionarioResponseDTO = new AvaliacaoFuncionarioResponseDTO(instancia);
        AvaliacaoParaResponderDTO avaliacaoParaResponderDTO = new AvaliacaoParaResponderDTO(instancia, avaliacao);
        AvaliacaoRevisaoDTO avaliacaoRevisaoDTO = new AvaliacaoRevisaoDTO(instancia, avaliacao);
        RespostaColaboradorResponseDTO respostaColaboradorResponseDTO = new RespostaColaboradorResponseDTO(resposta);
        RevisaoDetalhadaDTO revisaoDetalhadaDTO = RevisaoDetalhadaDTO.builder()
                .perguntaId(5L)
                .perguntaTexto("Pergunta")
                .respostaDada("Resposta")
                .opcaoSelecionadaId(6L)
                .build();

        assertThat(perguntaResponseDTO.competenciaCodigo()).isEqualTo(1);
        assertThat(perguntaOpcaoDTO.codigo()).isEqualTo(6L);
        assertThat(perguntaParaResponderDTO.opcoes()).hasSize(1);
        assertThat(perguntaComRespostaDTO.opcaoSelecionadaCodigo()).isEqualTo(6L);
        assertThat(perguntaComListaRespostaDTO.respostaTexto()).isNull();
        assertThat(avaliacaoResponseDTO.nomeCriador()).isEqualTo("Gestor");
        assertThat(avaliacaoDetalhadaDTO.instancias()).hasSize(1);
        assertThat(avaliacaoFuncionarioResponseDTO.getFuncionarioCodigo()).isEqualTo(3);
        assertThat(avaliacaoParaResponderDTO.perguntas()).hasSize(1);
        assertThat(avaliacaoRevisaoDTO.perguntasComRespostas()).hasSize(1);
        assertThat(respostaColaboradorResponseDTO.opcaoSelecionadaCodigo()).isEqualTo(6L);
        assertThat(revisaoDetalhadaDTO.getOpcaoSelecionadaId()).isEqualTo(6L);

        Pergunta semCompetencia = TestDataFactory.pergunta(11L, "Sem categoria", null, "texto");
        semCompetencia.setOpcoes(null);
        Avaliacao semCriador = TestDataFactory.avaliacao(3, null);
        semCriador.setPerguntas(null);
        semCriador.setInstanciasAvaliacao(null);
        AvaliacaoFuncionario instanciaNula = TestDataFactory.avaliacaoFuncionario(12L, null, null);
        instanciaNula.setFuncionario(null);
        instanciaNula.setAvaliacao(null);
        instanciaNula.setRespostas(null);
        RespostaColaborador respostaNula = TestDataFactory.resposta(13L, null, null, null);
        respostaNula.setAvaliacaoFuncionario(null);
        respostaNula.setPergunta(null);
        respostaNula.setOpcaoSelecionada(null);

        assertThat(new PerguntaResponseDTO(semCompetencia).competenciaNome()).isNull();
        assertThat(new PerguntaParaResponderDTO(semCompetencia).competenciaNome()).isEqualTo("Sem Categoria");
        assertThat(new PerguntaComRespostaDTO(semCompetencia, (RespostaColaborador) null).opcoes()).isEmpty();
        RespostaColaborador respostaSemOpcaoSelecionada = TestDataFactory.resposta(14L, instancia, pergunta, null);
        assertThat(new PerguntaComRespostaDTO(pergunta, respostaSemOpcaoSelecionada).opcaoSelecionadaCodigo()).isNull();
        AvaliacaoFuncionario instanciaSemFuncionario = TestDataFactory.avaliacaoFuncionario(16L, null, avaliacao);
        instanciaSemFuncionario.setFuncionario(null);
        instanciaSemFuncionario.setRespostas(null);
        assertThat(new AvaliacaoRevisaoDTO(instanciaSemFuncionario, avaliacao).nomeFuncionario()).isNull();
        assertThat(new AvaliacaoResponseDTO(semCriador).nomeCriador()).isEqualTo("Sistema");
        assertThat(new AvaliacaoDetalhadaDTO(semCriador).perguntas()).isEmpty();
        assertThat(new AvaliacaoFuncionarioResponseDTO(instanciaNula).getFuncionarioCodigo()).isNull();
        assertThat(new AvaliacaoParaResponderDTO(TestDataFactory.avaliacaoFuncionario(14L, TestDataFactory.funcionario(1, "A", 3, 10), semCriador), semCriador).perguntas()).isEmpty();
        assertThat(new AvaliacaoRevisaoDTO(TestDataFactory.avaliacaoFuncionario(15L, TestDataFactory.funcionario(1, "A", 3, 10), semCriador), semCriador).perguntasComRespostas()).isEmpty();
        assertThat(new AvaliacaoRevisaoDTO(TestDataFactory.avaliacaoFuncionario(17L, TestDataFactory.funcionario(1, "A", 3, 10), avaliacao), avaliacao).perguntasComRespostas()).hasSize(1);
        PerguntaComRespostaDTO perguntaSemCorrespondencia = new PerguntaComRespostaDTO(
                pergunta,
                List.of(TestDataFactory.resposta(15L, instancia, TestDataFactory.pergunta(50L, "Outra", null, "texto"), null)));
        assertThat(perguntaSemCorrespondencia.opcaoSelecionadaCodigo()).isNull();
        PerguntaComRespostaDTO perguntaComRespostaSemOpcao = new PerguntaComRespostaDTO(
                pergunta,
                List.of(TestDataFactory.resposta(16L, instancia, pergunta, null)));
        assertThat(perguntaComRespostaSemOpcao.opcaoSelecionadaCodigo()).isNull();
        assertThat(new RespostaColaboradorResponseDTO(respostaNula).perguntaCodigo()).isNull();
    }

    @Test
    void shouldCoverModelsAndExceptions() {
        Avaliacao avaliacao = TestDataFactory.avaliacao(1, TestDataFactory.funcionario(1, "Gestor", 2, 10));
        avaliacao.setStatus(null);
        ReflectionTestUtils.invokeMethod(avaliacao, "onCreate");
        assertThat(avaliacao.getStatus()).isEqualTo("Rascunho");
        avaliacao.setStatus("PENDENTE");
        ReflectionTestUtils.invokeMethod(avaliacao, "onCreate");
        assertThat(avaliacao.getStatus()).isEqualTo("PENDENTE");
        avaliacao.setStatus("   ");
        ReflectionTestUtils.invokeMethod(avaliacao, "onCreate");
        assertThat(avaliacao.getStatus()).isEqualTo("Rascunho");

        AvaliacaoFuncionario instancia = TestDataFactory.avaliacaoFuncionario(2L, TestDataFactory.funcionario(2, "Ana", 3, 10), avaliacao);
        RespostaColaborador resposta = TestDataFactory.resposta(3L, instancia, TestDataFactory.pergunta(4L, "Pergunta", null, "texto"), null);
        avaliacao.setInstanciasAvaliacao(null);
        avaliacao.addInstancia(instancia);
        avaliacao.addInstancia(TestDataFactory.avaliacaoFuncionario(4L, TestDataFactory.funcionario(4, "Nova", 3, 10), avaliacao));
        avaliacao.removeInstancia(instancia);
        avaliacao.setInstanciasAvaliacao(null);
        avaliacao.removeInstancia(instancia);
        avaliacao.setPerguntas(null);
        Pergunta pergunta = TestDataFactory.pergunta(5L, "Pergunta", null, "texto");
        avaliacao.addPergunta(pergunta);
        avaliacao.addPergunta(TestDataFactory.pergunta(6L, "Outra", null, "texto"));
        avaliacao.removePergunta(pergunta);
        avaliacao.setPerguntas(null);
        avaliacao.removePergunta(pergunta);

        AvaliacaoFuncionario novaInstancia = new AvaliacaoFuncionario(TestDataFactory.funcionario(3, "Bia", 3, 10), avaliacao);
        assertThat(novaInstancia.getResultadoStatus()).isEqualTo("PENDENTE");
        novaInstancia.setRespostas(null);
        novaInstancia.addResposta(resposta);
        novaInstancia.addResposta(TestDataFactory.resposta(4L, novaInstancia, pergunta, null));
        novaInstancia.removeResposta(resposta);
        novaInstancia.setRespostas(null);
        novaInstancia.removeResposta(resposta);

        PerguntaOpcao opcao = TestDataFactory.opcao(6L, pergunta, "A", false);
        opcao.setIsCorreta(true);
        RespostaColaborador respostaColaborador = new RespostaColaborador();
        respostaColaborador.setOpcaoSelecionada(opcao);
        assertThat(respostaColaborador.getCodigoPerguntaOpcaoSelecionada()).isEqualTo(6L);
        respostaColaborador.setOpcaoSelecionada(null);
        assertThat(respostaColaborador.getCodigoPerguntaOpcaoSelecionada()).isNull();

        MesQuantidadeDTO quantidadeDTO = new MesQuantidadeDTO("2025-01", null);
        assertThat(quantidadeDTO.getQuantidade()).isEqualTo(0);
        quantidadeDTO.setQuantidade(10);
        assertThat(quantidadeDTO.getQuantidade()).isEqualTo(10);

        assertThat(new ResourceNotFoundException("erro")).hasMessage("erro");
        assertThat(new UnauthorizedActionException("erro")).hasMessage("erro");
    }
}
