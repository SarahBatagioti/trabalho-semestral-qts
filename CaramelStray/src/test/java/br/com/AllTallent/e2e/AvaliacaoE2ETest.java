package br.com.AllTallent.e2e;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

import br.com.AllTallent.e2e.support.BaseApiE2ETest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AvaliacaoE2ETest extends BaseApiE2ETest {

    @Test
    void deveCriarAvaliacaoComoAdminEConsultarDetalhesEInstancias() {
        String token = loginAsAdminA();

        Integer avaliacaoId = auth(token)
                .body(Map.of(
                        "titulo", "Avaliação criada no E2E",
                        "dataPrazo", LocalDate.now().plusDays(10).toString(),
                        "codigosFuncionarios", List.of(seed.colaboradorAId()),
                        "codigosPerguntas", List.of(seed.perguntaTextoId(), seed.perguntaOpcaoId())
                ))
                .when()
                .post("/api/avaliacoes")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-response.schema.json"))
                .body("titulo", equalTo("Avaliação criada no E2E"))
                .extract()
                .jsonPath()
                .getInt("codigo");

        auth(token)
                .when()
                .get("/api/avaliacoes/{id}", avaliacaoId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-detalhada.schema.json"))
                .body("codigo", equalTo(avaliacaoId))
                .body("perguntas.codigo", hasItems(seed.perguntaTextoId().intValue(), seed.perguntaOpcaoId().intValue()));

        auth(token)
                .when()
                .get("/api/avaliacoes/{id}/instancias", avaliacaoId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-instancia-list.schema.json"))
                .body("funcionarioCodigo", hasItem(seed.colaboradorAId()));
    }

    @Test
    void deveRespeitarPermissoesNaCriacaoEListagemDeAvaliacoes() {
        auth(loginAsGestorB())
                .body(Map.of(
                        "titulo", "Avaliação indevida",
                        "dataPrazo", LocalDate.now().plusDays(5).toString(),
                        "codigosFuncionarios", List.of(seed.colaboradorAId()),
                        "codigosPerguntas", List.of(seed.perguntaTextoId())
                ))
                .when()
                .post("/api/avaliacoes")
                .then()
                .statusCode(403);

        auth(loginAsAdminA())
                .when()
                .get("/api/avaliacoes")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-response-list.schema.json"))
                .body("codigo", hasItems(seed.avaliacaoPendenteId(), seed.avaliacaoRevisaoId()))
                .body("codigo", not(hasItem(seed.avaliacaoConcluidaId())));

        auth(loginAsGestorA())
                .when()
                .get("/api/avaliacoes")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-response-list.schema.json"))
                .body("codigo", hasItem(seed.avaliacaoRevisaoId()))
                .body("codigo", not(hasItem(seed.avaliacaoPendenteId())));
    }

    @Test
    void deveResponderAtualizarFinalizarEAuditarInstanciaDeAvaliacao() {
        String colaboradorToken = loginAsColaboradorA();

        auth(colaboradorToken)
                .when()
                .get("/api/avaliacoes/pendentes/{funcionarioId}", seed.colaboradorAId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-instancia-list.schema.json"))
                .body("codigo", hasItem(seed.instanciaPendenteId().intValue()));

        auth(colaboradorToken)
                .when()
                .get("/api/avaliacoes/instancias/{instanciaId}/responder", seed.instanciaPendenteId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-para-responder.schema.json"))
                .body("avaliacaoFuncionarioCodigo", equalTo(seed.instanciaPendenteId().intValue()))
                .body("perguntas.size()", greaterThan(0));

        Long respostaId = auth(colaboradorToken)
                .body(Map.of(
                        "funcionarioAvaliacaoCodigo", seed.instanciaPendenteId(),
                        "perguntaCodigo", seed.perguntaTextoId(),
                        "respostaTexto", "Primeira resposta do colaborador"
                ))
                .when()
                .post("/api/avaliacoes/respostas")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/resposta-colaborador.schema.json"))
                .body("respostaTexto", equalTo("Primeira resposta do colaborador"))
                .extract()
                .jsonPath()
                .getLong("codigo");

        auth(colaboradorToken)
                .body(Map.of(
                        "funcionarioAvaliacaoCodigo", seed.instanciaPendenteId(),
                        "perguntaCodigo", seed.perguntaTextoId(),
                        "respostaTexto", "Resposta atualizada do colaborador"
                ))
                .when()
                .post("/api/avaliacoes/respostas")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/resposta-colaborador.schema.json"))
                .body("codigo", equalTo(respostaId.intValue()))
                .body("respostaTexto", equalTo("Resposta atualizada do colaborador"));

        auth(colaboradorToken)
                .body(Map.of(
                        "funcionarioAvaliacaoCodigo", seed.instanciaPendenteId(),
                        "perguntaCodigo", seed.perguntaOpcaoId(),
                        "opcaoSelecionadaCodigo", seed.opcaoAvancadoId()
                ))
                .when()
                .post("/api/avaliacoes/respostas")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/resposta-colaborador.schema.json"))
                .body("opcaoSelecionadaCodigo", equalTo(seed.opcaoAvancadoId().intValue()));

        auth(colaboradorToken)
                .when()
                .put("/api/avaliacoes/instancias/{instanciaId}/finalizar", seed.instanciaPendenteId())
                .then()
                .statusCode(204);

        auth(colaboradorToken)
                .when()
                .put("/api/avaliacoes/instancias/{instanciaId}/finalizar", seed.instanciaPendenteId())
                .then()
                .statusCode(409);

        auth(loginAsAdminA())
                .when()
                .get("/api/avaliacoes/instancias/{instanciaId}/respostas", seed.instanciaPendenteId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/resposta-colaborador-list.schema.json"))
                .body("perguntaCodigo", hasItems(seed.perguntaTextoId().intValue(), seed.perguntaOpcaoId().intValue()));
    }

    @Test
    void deveBloquearRespostaDeOutroColaboradorEOpcaoInconsistente() {
        auth(loginAsColaboradorB())
                .body(Map.of(
                        "funcionarioAvaliacaoCodigo", seed.instanciaPendenteId(),
                        "perguntaCodigo", seed.perguntaTextoId(),
                        "respostaTexto", "Tentativa indevida"
                ))
                .when()
                .post("/api/avaliacoes/respostas")
                .then()
                .statusCode(403);

        auth(loginAsColaboradorA())
                .body(Map.of(
                        "funcionarioAvaliacaoCodigo", seed.instanciaPendenteId(),
                        "perguntaCodigo", seed.perguntaTextoId(),
                        "opcaoSelecionadaCodigo", seed.opcaoAvancadoId()
                ))
                .when()
                .post("/api/avaliacoes/respostas")
                .then()
                .statusCode(400);
    }

    @Test
    void deveExecutarFluxoDeRevisaoELerRespostasExistentes() {
        auth(loginAsGestorA())
                .when()
                .get("/api/avaliacoes/revisao/{codigoAvaliacaoFuncionario}", seed.instanciaRevisaoId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/revisao-detalhada-list.schema.json"))
                .body("size()", equalTo(2));

        auth(loginAsGestorA())
                .body(Map.of(
                        "comentarioSupervisao", "Boa entrega técnica.",
                        "comentarioParaColaborador", "Continue aprofundando o uso de Spring.",
                        "resultadoStatus", "APROVADO"
                ))
                .when()
                .put("/api/avaliacoes/instancias/{instanciaId}/revisar", seed.instanciaRevisaoId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/avaliacao-instancia.schema.json"))
                .body("codigo", equalTo(seed.instanciaRevisaoId().intValue()))
                .body("resultadoStatus", equalTo("APROVADO"))
                .body("comentarioSupervisao", equalTo("Boa entrega técnica."));

        auth(loginAsAdminA())
                .when()
                .get("/api/avaliacoes/instancias/{instanciaId}/respostas", seed.instanciaRevisaoId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/resposta-colaborador-list.schema.json"))
                .body("codigo", hasItems(901, 902));
    }

    @Test
    void deveRetornar404ParaAvaliacaoInexistente() {
        auth(loginAsAdminA())
                .when()
                .get("/api/avaliacoes/{id}", 999999)
                .then()
                .statusCode(404);
    }
}
