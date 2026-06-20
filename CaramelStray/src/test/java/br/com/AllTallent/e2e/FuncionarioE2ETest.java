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

class FuncionarioE2ETest extends BaseApiE2ETest {

    @Test
    void deveListarFuncionariosEFiltrarPorTexto() {
        auth(loginAsAdminA())
                .when()
                .get("/api/funcionario")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-list.schema.json"))
                .body("codigo.size()", greaterThan(0))
                .body("codigo", hasItems(seed.adminAId(), seed.colaboradorAId(), seed.colaboradorBId()));

        auth(loginAsAdminA())
                .queryParam("texto", "Spring")
                .when()
                .get("/api/funcionario")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-list.schema.json"))
                .body("codigo", hasItems(seed.colaboradorAId(), seed.colaboradorBId()));
    }

    @Test
    void deveBuscarProprioFuncionarioERetornar404QuandoNaoExiste() {
        auth(loginAsColaboradorA())
                .when()
                .get("/api/funcionario/{id}", seed.colaboradorAId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-response.schema.json"))
                .body("codigo", equalTo(seed.colaboradorAId()))
                .body("email", equalTo(seed.colaboradorAEmail()));

        auth(loginAsAdminA())
                .when()
                .get("/api/funcionario/{id}", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    void deveCriarELimparFuncionarioComoAdmin() {
        String token = loginAsAdminA();

        Integer funcionarioId = auth(token)
                .body(Map.of(
                        "nomeCompleto", "Novo QA",
                        "email", "novo.qa@alltallent.test",
                        "telefone", "11999990000",
                        "senhaHash", "senha123",
                        "areaId", seed.areaAId(),
                        "perfilId", seed.colaboradorPerfilId(),
                        "gestorId", seed.gestorAId(),
                        "tituloProfissional", "QA",
                        "localizacao", "São Paulo",
                        "resumo", "Criado via E2E"
                ))
                .when()
                .post("/api/funcionario")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-response.schema.json"))
                .body("nomeCompleto", equalTo("Novo QA"))
                .extract()
                .jsonPath()
                .getInt("codigo");

        auth(token)
                .when()
                .get("/api/funcionario/{id}", funcionarioId)
                .then()
                .statusCode(200)
                .body("codigo", equalTo(funcionarioId));

        auth(token)
                .when()
                .delete("/api/funcionario/{id}", funcionarioId)
                .then()
                .statusCode(204);

        auth(token)
                .when()
                .get("/api/funcionario/{id}", funcionarioId)
                .then()
                .statusCode(404);
    }

    @Test
    void deveAtualizarProprioPerfilCertificadosECompetencias() {
        String token = loginAsColaboradorA();

        auth(token)
                .body(Map.of(
                        "localizacao", "Santos",
                        "resumo", "Resumo atualizado",
                        "tituloProfissional", "Senior Backend Developer"
                ))
                .when()
                .put("/api/funcionario/{id}", seed.colaboradorAId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-response.schema.json"))
                .body("localizacao", equalTo("Santos"))
                .body("tituloProfissional", equalTo("Senior Backend Developer"));

        Integer certificadoId = auth(token)
                .body(Map.of("nome", "CKA"))
                .when()
                .post("/api/funcionario/{id}/certificados", seed.colaboradorAId())
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/certificado-response.schema.json"))
                .body("nome", equalTo("CKA"))
                .extract()
                .jsonPath()
                .getInt("codigo");

        auth(token)
                .when()
                .get("/api/funcionario/{id}/perfil", seed.colaboradorAId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-perfil.schema.json"))
                .body("certificados.nome", hasItems("AWS Practitioner", "CKA"));

        auth(token)
                .body(Map.of("codigosCompetencia", List.of(seed.competenciaJavaId(), seed.competenciaComunicacaoId())))
                .when()
                .put("/api/funcionario/{id}/competencias", seed.colaboradorAId())
                .then()
                .statusCode(204);

        auth(token)
                .when()
                .get("/api/funcionario/{id}/competencias", seed.colaboradorAId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-competencias.schema.json"))
                .body("competencias.nome", hasItems("Java", "Comunicação"));

        auth(token)
                .when()
                .delete("/api/funcionario/certificados/{certificadoId}", certificadoId)
                .then()
                .statusCode(204);

        auth(token)
                .when()
                .get("/api/funcionario/{id}/perfil", seed.colaboradorAId())
                .then()
                .statusCode(200)
                .body("certificados.nome", not(hasItem("CKA")));
    }

    @Test
    void deveTratarCompetenciasInexistentesEOuAcessoForaDaArea() {
        auth(loginAsColaboradorA())
                .body(Map.of("codigosCompetencia", List.of(999999)))
                .when()
                .put("/api/funcionario/{id}/competencias", seed.colaboradorAId())
                .then()
                .statusCode(404);

        auth(loginAsGestorB())
                .body(Map.of("codigosCompetencia", List.of(seed.competenciaJavaId())))
                .when()
                .put("/api/funcionario/{id}/competencias", seed.colaboradorAId())
                .then()
                .statusCode(403);
    }

    @Test
    void deveGerenciarExperienciasERespeitarAutorizacao() {
        String token = loginAsColaboradorA();

        auth(token)
                .when()
                .get("/api/funcionario/{id}/experiencias", seed.colaboradorAId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-experiencias.schema.json"))
                .body("experiencias.cargo", hasItem("Desenvolvedora Java"));

        Integer experienciaId = auth(token)
                .body(Map.of(
                        "cargo", "Tech Lead",
                        "empresa", "OpenAI",
                        "dataInicio", LocalDate.now().minusYears(1).toString(),
                        "dataFim", LocalDate.now().toString(),
                        "descricao", "Atuação em arquitetura"
                ))
                .when()
                .post("/api/funcionario/{id}/experiencias", seed.colaboradorAId())
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/experiencia-response.schema.json"))
                .body("cargo", equalTo("Tech Lead"))
                .extract()
                .jsonPath()
                .getInt("codigo");

        auth(token)
                .body(Map.of(
                        "cargo", "Principal Engineer",
                        "empresa", "OpenAI",
                        "dataInicio", LocalDate.now().minusYears(1).toString(),
                        "dataFim", LocalDate.now().toString(),
                        "descricao", "Atuação estratégica"
                ))
                .when()
                .put("/api/funcionario/experiencias/{experienciaId}", experienciaId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/experiencia-response.schema.json"))
                .body("cargo", equalTo("Principal Engineer"));

        auth(loginAsColaboradorB())
                .body(Map.of(
                        "cargo", "Não autorizado",
                        "empresa", "Outra",
                        "dataInicio", LocalDate.now().minusMonths(3).toString(),
                        "dataFim", LocalDate.now().toString(),
                        "descricao", "Tentativa indevida"
                ))
                .when()
                .put("/api/funcionario/experiencias/{experienciaId}", seed.experienciaId())
                .then()
                .statusCode(403);
    }

    @Test
    void deveBloquearFuncionarioSemToken() {
        json()
                .when()
                .get("/api/funcionario")
                .then()
                .statusCode(401);
    }
}
