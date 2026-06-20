package br.com.AllTallent.support;

import br.com.AllTallent.config.CustomUserDetails;
import br.com.AllTallent.dto.CadastroRequestDTO;
import br.com.AllTallent.model.Area;
import br.com.AllTallent.model.Avaliacao;
import br.com.AllTallent.model.AvaliacaoFuncionario;
import br.com.AllTallent.model.Competencia;
import br.com.AllTallent.model.Experiencia;
import br.com.AllTallent.model.Funcionario;
import br.com.AllTallent.model.FuncionarioCertificado;
import br.com.AllTallent.model.Perfil;
import br.com.AllTallent.model.Pergunta;
import br.com.AllTallent.model.PerguntaOpcao;
import br.com.AllTallent.model.RespostaColaborador;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Area area(Integer codigo, String nome) {
        Area area = new Area();
        area.setCodigo(codigo);
        area.setNome(nome);
        area.setDescricao(nome + " description");
        return area;
    }

    public static Perfil perfil(Integer codigo, String nome) {
        Perfil perfil = new Perfil();
        perfil.setCodigo(codigo);
        perfil.setNome(nome);
        perfil.setDescricao(nome + " description");
        return perfil;
    }

    public static Competencia competencia(Integer codigo, String nome) {
        Competencia competencia = new Competencia();
        competencia.setCodigo(codigo);
        competencia.setNome(nome);
        competencia.setCategoria("soft-skill");
        return competencia;
    }

    public static Funcionario funcionario(Integer codigo, String nome, Integer perfilCodigo, Integer areaCodigo) {
        Funcionario funcionario = new Funcionario();
        funcionario.setCodigo(codigo);
        funcionario.setNomeCompleto(nome);
        funcionario.setEmail(nome.toLowerCase().replace(" ", ".") + "@mail.com");
        funcionario.setCpf("00000000000");
        funcionario.setTelefone("11999999999");
        funcionario.setSenhaHash("encoded-password");
        funcionario.setDataCadastro(OffsetDateTime.parse("2025-01-01T10:00:00Z"));
        funcionario.setTituloProfissional("Developer");
        funcionario.setLocalizacao("Sao Paulo");
        funcionario.setResumo("Resumo");
        funcionario.setIdCracha("CR-" + codigo);
        funcionario.setDataAdmissao(LocalDate.of(2024, 1, 10));
        funcionario.setArea(areaCodigo == null ? null : area(areaCodigo, "Area " + areaCodigo));
        funcionario.setPerfil(perfilCodigo == null ? null : perfil(perfilCodigo, "Perfil " + perfilCodigo));
        funcionario.setCompetencias(new HashSet<>());
        funcionario.setCertificados(new HashSet<>());
        funcionario.setExperiencias(new HashSet<>());
        return funcionario;
    }

    public static Pergunta pergunta(Long codigo, String texto, Competencia competencia, String tipoPergunta) {
        Pergunta pergunta = new Pergunta();
        pergunta.setCodigo(codigo);
        pergunta.setPergunta(texto);
        pergunta.setCompetencia(competencia);
        pergunta.setTipoPergunta(tipoPergunta);
        return pergunta;
    }

    public static PerguntaOpcao opcao(Long codigo, Pergunta pergunta, String descricao, boolean correta) {
        PerguntaOpcao opcao = new PerguntaOpcao();
        opcao.setCodigo(codigo);
        opcao.setPergunta(pergunta);
        opcao.setDescricaoOpcao(descricao);
        opcao.setIsCorreta(correta);
        return opcao;
    }

    public static Avaliacao avaliacao(Integer codigo, Funcionario criador) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCodigo(codigo);
        avaliacao.setTitulo("Avaliacao " + codigo);
        avaliacao.setStatus("PENDENTE");
        avaliacao.setDataCriacao(LocalDate.of(2025, 1, 1));
        avaliacao.setDataPrazo(LocalDate.of(2025, 1, 31));
        avaliacao.setCriador(criador);
        avaliacao.setPerguntas(new HashSet<>());
        avaliacao.setInstanciasAvaliacao(new HashSet<>());
        return avaliacao;
    }

    public static AvaliacaoFuncionario avaliacaoFuncionario(Long codigo, Funcionario funcionario, Avaliacao avaliacao) {
        AvaliacaoFuncionario instancia = new AvaliacaoFuncionario();
        instancia.setCodigo(codigo);
        instancia.setFuncionario(funcionario);
        instancia.setAvaliacao(avaliacao);
        instancia.setComentarioColaborador("Comentario colaborador");
        instancia.setComentarioSupervisao("Comentario supervisao");
        instancia.setResultadoStatus("PENDENTE");
        instancia.setNota(8);
        instancia.setRespostas(new HashSet<>());
        return instancia;
    }

    public static RespostaColaborador resposta(Long codigo, AvaliacaoFuncionario instancia, Pergunta pergunta, PerguntaOpcao opcao) {
        RespostaColaborador resposta = new RespostaColaborador();
        resposta.setCodigo(codigo);
        resposta.setAvaliacaoFuncionario(instancia);
        resposta.setPergunta(pergunta);
        resposta.setRespostaTexto("Resposta " + codigo);
        resposta.setOpcaoSelecionada(opcao);
        return resposta;
    }

    public static Experiencia experiencia(Integer codigo, Funcionario funcionario) {
        Experiencia experiencia = new Experiencia();
        experiencia.setCodigo(codigo);
        experiencia.setCargo("Developer");
        experiencia.setEmpresa("OpenAI");
        experiencia.setDescricao("Descricao");
        experiencia.setDataInicio(LocalDate.of(2020, 1, 1));
        experiencia.setDataFim(LocalDate.of(2021, 1, 1));
        experiencia.setFuncionario(funcionario);
        return experiencia;
    }

    public static FuncionarioCertificado certificado(Integer codigo, Funcionario funcionario) {
        FuncionarioCertificado certificado = new FuncionarioCertificado();
        certificado.setCodigo(codigo);
        certificado.setCertificado("Java");
        certificado.setFuncionario(funcionario);
        return certificado;
    }

    public static CadastroRequestDTO cadastroRequest() {
        CadastroRequestDTO request = new CadastroRequestDTO();
        request.setNomeCompleto("Maria Silva");
        request.setEmail("maria@mail.com");
        request.setSenha("senha123");
        request.setTelefone("11988887777");
        request.setIdCracha("CH-123");
        request.setDataAdmissao(LocalDate.of(2024, 2, 1));
        request.setResumo("Resumo");
        request.setCodigoArea(10);
        request.setCodigoPerfil(3);
        request.setCpf("12345678900");
        request.setLocalizacao("Campinas");
        request.setTituloProfissional("Analyst");
        return request;
    }

    public static Authentication authenticationFor(Funcionario funcionario) {
        CustomUserDetails userDetails = new CustomUserDetails(funcionario);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public static void addCompetencias(Funcionario funcionario, Competencia... competencias) {
        funcionario.setCompetencias(new HashSet<>(List.of(competencias)));
    }

    public static void addCertificados(Funcionario funcionario, FuncionarioCertificado... certificados) {
        funcionario.setCertificados(new HashSet<>(List.of(certificados)));
    }

    public static void addExperiencias(Funcionario funcionario, Experiencia... experiencias) {
        funcionario.setExperiencias(new HashSet<>(List.of(experiencias)));
    }

    public static void addOpcoes(Pergunta pergunta, PerguntaOpcao... opcoes) {
        pergunta.setOpcoes(new HashSet<>(List.of(opcoes)));
    }

    public static void addPerguntas(Avaliacao avaliacao, Pergunta... perguntas) {
        avaliacao.setPerguntas(new HashSet<>(List.of(perguntas)));
    }

    public static void addInstancias(Avaliacao avaliacao, AvaliacaoFuncionario... instancias) {
        avaliacao.setInstanciasAvaliacao(new HashSet<>(List.of(instancias)));
    }

    public static void addRespostas(AvaliacaoFuncionario instancia, RespostaColaborador... respostas) {
        instancia.setRespostas(new HashSet<>(List.of(respostas)));
    }
}
