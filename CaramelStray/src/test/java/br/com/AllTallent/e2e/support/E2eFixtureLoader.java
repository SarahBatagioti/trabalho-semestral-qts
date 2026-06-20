package br.com.AllTallent.e2e.support;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestComponent
public class E2eFixtureLoader {

    private static final String DEFAULT_PASSWORD = "senha123";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public E2eFixtureLoader(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public SeedData resetAndSeed() {
        truncateAll();

        OffsetDateTime agora = OffsetDateTime.now().withNano(0);
        LocalDate hoje = LocalDate.now();
        String senhaHash = passwordEncoder.encode(DEFAULT_PASSWORD);

        insertArea(101, "Plataforma", "Área A - Plataforma");
        insertArea(102, "Produto", "Área B - Produto");

        insertPerfil(1, "Diretoria", "Perfil administrador");
        insertPerfil(2, "Supervisão", "Perfil gestor");
        insertPerfil(3, "Colaborador", "Perfil colaborador");

        insertFuncionario(1001, "Ana Diretora", "ana.admin@alltallent.test", "11111111111", "11911111111",
                senhaHash, agora.minusDays(10), "Diretora de Plataforma", "São Paulo", "Liderança da área A",
                "CR-1001", hoje.minusMonths(12), 101, 1, null);
        insertFuncionario(1002, "Bruno Gestor", "bruno.gestor@alltallent.test", "22222222222", "11922222222",
                senhaHash, agora.minusDays(9), "Gestor de Plataforma", "São Paulo", "Gestor da área A",
                "CR-1002", hoje.minusMonths(10), 101, 2, 1001);
        insertFuncionario(1003, "Carla Colaboradora", "carla.colab@alltallent.test", "33333333333", "11933333333",
                senhaHash, agora.minusDays(8), "Dev Backend", "Campinas", "Colaboradora da área A",
                "CR-1003", hoje.minusMonths(8), 101, 3, 1002);
        insertFuncionario(1004, "Diego Gestor", "diego.gestor@alltallent.test", "44444444444", "11944444444",
                senhaHash, agora.minusDays(7), "Gestor de Produto", "Rio de Janeiro", "Gestor da área B",
                "CR-1004", hoje.minusMonths(9), 102, 2, null);
        insertFuncionario(1005, "Eva Colaboradora", "eva.colab@alltallent.test", "55555555555", "11955555555",
                senhaHash, agora.minusDays(6), "Product Analyst", "Rio de Janeiro", "Colaboradora da área B",
                "CR-1005", hoje.minusMonths(6), 102, 3, 1004);

        insertCompetencia(201, "Java", "hard-skill");
        insertCompetencia(202, "Spring", "hard-skill");
        insertCompetencia(203, "Comunicação", "soft-skill");

        insertFuncionarioCompetencia(1003, 201);
        insertFuncionarioCompetencia(1003, 202);
        insertFuncionarioCompetencia(1005, 202);
        insertFuncionarioCompetencia(1005, 203);

        insertCertificado(701, "AWS Practitioner", 1003);
        insertExperiencia(801, "Desenvolvedora Java", "AllTallent", "Atuou em APIs Spring Boot",
                hoje.minusYears(2), hoje.minusYears(1), 1003);

        insertPergunta(301L, "Descreva sua principal contribuição no último ciclo.", 201, "TEXTO");
        insertPergunta(302L, "Como você avalia seu domínio de Spring Boot?", 202, "OBJETIVA");
        insertPergunta(303L, "Seu time tem colaborado bem com outras áreas?", 203, "OBJETIVA");

        insertPerguntaOpcao(401L, 302L, "Básico", false);
        insertPerguntaOpcao(402L, 302L, "Avançado", true);
        insertPerguntaOpcao(403L, 303L, "Sim", true);
        insertPerguntaOpcao(404L, 303L, "Não", false);

        insertAvaliacao(501, "Avaliação pendente área A", "Rascunho", hoje.minusDays(2), hoje.plusDays(7), 1001);
        insertAvaliacaoPergunta(501, 301L);
        insertAvaliacaoPergunta(501, 302L);
        insertAvaliacaoFuncionario(601L, 1003, 501, null, null, "PENDENTE", null);

        insertAvaliacao(502, "Avaliação aguardando revisão área A", "Rascunho", hoje.minusDays(5), hoje.plusDays(3), 1002);
        insertAvaliacaoPergunta(502, 301L);
        insertAvaliacaoPergunta(502, 302L);
        insertAvaliacaoFuncionario(602L, 1003, 502, "Entreguei os objetivos do trimestre.", null, "AGUARDANDO_REVISAO", 8);
        insertResposta(901L, 602L, 301L, "Automatizei entregas críticas da squad.", null);
        insertResposta(902L, 602L, 302L, null, 402L);

        insertAvaliacao(503, "Avaliação concluída área B", "Rascunho", hoje.minusDays(8), hoje.plusDays(1), 1004);
        insertAvaliacaoPergunta(503, 301L);
        insertAvaliacaoPergunta(503, 303L);
        insertAvaliacaoFuncionario(603L, 1005, 503, "Concluí as entregas planejadas.", "Aprovado pelo gestor", "APROVADO", 9);
        insertResposta(903L, 603L, 301L, "Entreguei análises e documentação.", null);
        insertResposta(904L, 603L, 303L, null, 403L);

        syncSequences();

        return new SeedData(
                DEFAULT_PASSWORD,
                101,
                102,
                1,
                2,
                3,
                1001,
                "ana.admin@alltallent.test",
                1002,
                "bruno.gestor@alltallent.test",
                1003,
                "carla.colab@alltallent.test",
                1004,
                "diego.gestor@alltallent.test",
                1005,
                "eva.colab@alltallent.test",
                201,
                202,
                203,
                301L,
                302L,
                303L,
                401L,
                402L,
                403L,
                404L,
                501,
                502,
                503,
                601L,
                602L,
                603L,
                701,
                801
        );
    }

    private void truncateAll() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    tb_cad_resposta_colaborador,
                    tb_cad_funcionario_avalicacao,
                    tb_cad_avaliacao_pergunta,
                    tb_cad_avaliacao,
                    tb_cad_pergunta_opcao,
                    tb_cad_pergunta,
                    tb_cad_funcionario_competencia,
                    tb_cad_funcionario_certificado,
                    tb_cad_funcionario_historico_experiencia,
                    tb_cad_funcionario,
                    tb_cad_competencia,
                    tb_cad_perfil,
                    tb_cad_area
                RESTART IDENTITY CASCADE
                """);
    }

    private void insertArea(int codigo, String nome, String descricao) {
        jdbcTemplate.update("INSERT INTO tb_cad_area (codigo, nome, descricao) VALUES (?, ?, ?)",
                codigo, nome, descricao);
    }

    private void insertPerfil(int codigo, String nome, String descricao) {
        jdbcTemplate.update("INSERT INTO tb_cad_perfil (codigo, nome, descricao) VALUES (?, ?, ?)",
                codigo, nome, descricao);
    }

    private void insertFuncionario(
            int codigo,
            String nomeCompleto,
            String email,
            String cpf,
            String telefone,
            String senhaHash,
            OffsetDateTime dataCadastro,
            String tituloProfissional,
            String localizacao,
            String resumo,
            String idCracha,
            LocalDate dataAdmissao,
            Integer codigoArea,
            Integer codigoPerfil,
            Integer codigoGestor) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_funcionario
                (codigo, nome_completo, email, cpf, telefone, senha_hash, data_cadastro,
                 titulo_profissional, localizacao, resumo, id_cracha, data_admissao,
                 codigo_area, codigo_perfil, codigo_gestor)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                codigo, nomeCompleto, email, cpf, telefone, senhaHash, dataCadastro,
                tituloProfissional, localizacao, resumo, idCracha, dataAdmissao,
                codigoArea, codigoPerfil, codigoGestor);
    }

    private void insertCompetencia(int codigo, String nome, String categoria) {
        jdbcTemplate.update("INSERT INTO tb_cad_competencia (codigo, nome, categoria) VALUES (?, ?, ?)",
                codigo, nome, categoria);
    }

    private void insertFuncionarioCompetencia(int codigoFuncionario, int codigoCompetencia) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_funcionario_competencia (codigo_funcionario, codigo_competencia)
                VALUES (?, ?)
                """,
                codigoFuncionario, codigoCompetencia);
    }

    private void insertCertificado(int codigo, String certificado, int codigoFuncionario) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_funcionario_certificado (codigo, certificado, codigo_funcionario)
                VALUES (?, ?, ?)
                """,
                codigo, certificado, codigoFuncionario);
    }

    private void insertExperiencia(
            int codigo,
            String cargo,
            String empresa,
            String descricao,
            LocalDate dataInicio,
            LocalDate dataFim,
            int codigoFuncionario) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_funcionario_historico_experiencia
                (codigo, cargo, empresa, descricao, data_inicio, data_fim, codigo_funcionario)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                codigo, cargo, empresa, descricao, dataInicio, dataFim, codigoFuncionario);
    }

    private void insertPergunta(long codigo, String pergunta, int codigoCompetencia, String tipoPergunta) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_pergunta (codigo, pergunta, codigo_competencia, tipo_pergunta)
                VALUES (?, ?, ?, ?)
                """,
                codigo, pergunta, codigoCompetencia, tipoPergunta);
    }

    private void insertPerguntaOpcao(long codigo, long codigoPergunta, String descricao, boolean correta) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_pergunta_opcao (codigo, codigo_pergunta, descricao_opcao, is_correta)
                VALUES (?, ?, ?, ?)
                """,
                codigo, codigoPergunta, descricao, correta);
    }

    private void insertAvaliacao(int codigo, String titulo, String status, LocalDate dataCriacao, LocalDate dataPrazo, int codigoCriador) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_avaliacao (codigo, titulo, status, data_criacao, data_prazo, codigo_criador)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                codigo, titulo, status, dataCriacao, dataPrazo, codigoCriador);
    }

    private void insertAvaliacaoPergunta(int codigoAvaliacao, long codigoPergunta) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_avaliacao_pergunta (codigo_avaliacao, codigo_pergunta)
                VALUES (?, ?)
                """,
                codigoAvaliacao, codigoPergunta);
    }

    private void insertAvaliacaoFuncionario(
            long codigo,
            int codigoFuncionario,
            int codigoAvaliacao,
            String comentarioColaborador,
            String comentarioSupervisao,
            String resultadoStatus,
            Integer nota) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_funcionario_avalicacao
                (codigo, codigo_funcionario_avalidado, codigo_avalicacao,
                 comentario_colaborador, comentario_supervisao, resultado_status, nota)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                codigo, codigoFuncionario, codigoAvaliacao,
                comentarioColaborador, comentarioSupervisao, resultadoStatus, nota);
    }

    private void insertResposta(long codigo, long codigoFuncionarioAvaliacao, long codigoPergunta, String respostaTexto, Long codigoPerguntaOpcaoSelecionada) {
        jdbcTemplate.update("""
                INSERT INTO tb_cad_resposta_colaborador
                (codigo, codigo_funcionario_avaliacao, codigo_pergunta, resposta_texto, codigo_pergunta_opcao_selecionada)
                VALUES (?, ?, ?, ?, ?)
                """,
                codigo, codigoFuncionarioAvaliacao, codigoPergunta, respostaTexto, codigoPerguntaOpcaoSelecionada);
    }

    private void syncSequences() {
        updateSequence("tb_cad_area", "codigo");
        updateSequence("tb_cad_perfil", "codigo");
        updateSequence("tb_cad_funcionario", "codigo");
        updateSequence("tb_cad_competencia", "codigo");
        updateSequence("tb_cad_funcionario_certificado", "codigo");
        updateSequence("tb_cad_funcionario_historico_experiencia", "codigo");
        updateSequence("tb_cad_pergunta", "codigo");
        updateSequence("tb_cad_pergunta_opcao", "codigo");
        updateSequence("tb_cad_avaliacao", "codigo");
        updateSequence("tb_cad_funcionario_avalicacao", "codigo");
        updateSequence("tb_cad_resposta_colaborador", "codigo");
    }

    private void updateSequence(String table, String column) {
        String sequenceName = jdbcTemplate.queryForObject(
                "SELECT pg_get_serial_sequence(?, ?)",
                String.class,
                table,
                column
        );

        if (sequenceName != null) {
            jdbcTemplate.execute(
                    "SELECT setval('" + sequenceName + "', COALESCE((SELECT MAX(" + column + ") FROM " + table + "), 1), true)"
            );
        }
    }

    public record SeedData(
            String defaultPassword,
            Integer areaAId,
            Integer areaBId,
            Integer adminPerfilId,
            Integer gestorPerfilId,
            Integer colaboradorPerfilId,
            Integer adminAId,
            String adminAEmail,
            Integer gestorAId,
            String gestorAEmail,
            Integer colaboradorAId,
            String colaboradorAEmail,
            Integer gestorBId,
            String gestorBEmail,
            Integer colaboradorBId,
            String colaboradorBEmail,
            Integer competenciaJavaId,
            Integer competenciaSpringId,
            Integer competenciaComunicacaoId,
            Long perguntaTextoId,
            Long perguntaOpcaoId,
            Long perguntaComunicacaoId,
            Long opcaoBasicoId,
            Long opcaoAvancadoId,
            Long opcaoSimId,
            Long opcaoNaoId,
            Integer avaliacaoPendenteId,
            Integer avaliacaoRevisaoId,
            Integer avaliacaoConcluidaId,
            Long instanciaPendenteId,
            Long instanciaRevisaoId,
            Long instanciaConcluidaId,
            Integer certificadoId,
            Integer experienciaId
    ) {
    }
}
