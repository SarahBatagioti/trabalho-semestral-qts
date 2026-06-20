package br.com.AllTallent.e2e;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import br.com.AllTallent.e2e.support.BaseApiE2ETest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CadastrosBaseE2ETest extends BaseApiE2ETest {

    @Test
    void deveCriarEListarAreasEPerfisAutenticado() {
        String token = loginAsAdminA();

        Integer areaId = auth(token)
                .body(Map.of("nome", "Qualidade", "descricao", "Área de qualidade"))
                .when()
                .post("/api/area")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/area.schema.json"))
                .body("nome", equalTo("Qualidade"))
                .extract()
                .jsonPath()
                .getInt("codigo");

        auth(token)
                .when()
                .get("/api/area")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/area-list.schema.json"))
                .body("nome", hasItem("Qualidade"))
                .body("find { it.codigo == %s }.descricao".formatted(areaId), equalTo("Área de qualidade"));

        Integer perfilId = auth(token)
                .body(Map.of("nome", "Especialista", "descricao", "Perfil especialista"))
                .when()
                .post("/api/perfil")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/perfil.schema.json"))
                .body("nome", equalTo("Especialista"))
                .extract()
                .jsonPath()
                .getInt("codigo");

        auth(token)
                .when()
                .get("/api/perfil")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/perfil-list.schema.json"))
                .body("nome", hasItem("Especialista"))
                .body("find { it.codigo == %s }.descricao".formatted(perfilId), equalTo("Perfil especialista"));
    }

    @Test
    void deveBloquearAreaEPerfilSemAutenticacao() {
        json()
                .when()
                .get("/api/area")
                .then()
                .statusCode(401);

        json()
                .when()
                .get("/api/perfil")
                .then()
                .statusCode(401);
    }

    @Test
    void deveExecutarCicloDeVidaDeCompetenciaComValidacoes() {
        String token = loginAsAdminA();

        auth(token)
                .when()
                .get("/api/competencia")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/competencia-dto-list.schema.json"))
                .body("nome", hasItems("Java", "Spring"));

        Integer competenciaId = auth(token)
                .body(Map.of("nome", "Kotlin", "categoria", "hard-skill"))
                .when()
                .post("/api/competencia")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/competencia-entity.schema.json"))
                .body("nome", equalTo("Kotlin"))
                .extract()
                .jsonPath()
                .getInt("codigo");

        auth(token)
                .when()
                .get("/api/competencia/{id}", competenciaId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/competencia-entity.schema.json"))
                .body("codigo", equalTo(competenciaId));

        auth(token)
                .body(Map.of("nome", "Kotlin Avançado", "categoria", "hard-skill"))
                .when()
                .put("/api/competencia/{id}", competenciaId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/competencia-entity.schema.json"))
                .body("nome", equalTo("Kotlin Avançado"));

        auth(token)
                .body(Map.of("nome", "Java"))
                .when()
                .post("/api/competencia")
                .then()
                .statusCode(400);

        auth(token)
                .when()
                .delete("/api/competencia/{id}", competenciaId)
                .then()
                .statusCode(204);

        auth(token)
                .when()
                .get("/api/competencia/{id}", competenciaId)
                .then()
                .statusCode(404);

        auth(token)
                .when()
                .delete("/api/competencia/{id}", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    void deveBloquearCompetenciaSemAutenticacao() {
        json()
                .when()
                .get("/api/competencia")
                .then()
                .statusCode(401);
    }

    @Test
    void deveExecutarFluxosDePerguntaComSucessoEErrosEsperados() {
        String adminToken = loginAsAdminA();

        Long perguntaId = auth(adminToken)
                .body(Map.of(
                        "pergunta", "Como foi sua evolução técnica?",
                        "competenciaCodigo", seed.competenciaJavaId(),
                        "tipoPergunta", "TEXTO",
                        "opcoes", List.of()
                ))
                .when()
                .post("/api/perguntas")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/pergunta-response.schema.json"))
                .body("competenciaCodigo", equalTo(seed.competenciaJavaId()))
                .extract()
                .jsonPath()
                .getLong("codigo");

        auth(adminToken)
                .when()
                .get("/api/perguntas")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/pergunta-response-list.schema.json"))
                .body("codigo.size()", greaterThan(0))
                .body("pergunta", hasItem("Como foi sua evolução técnica?"));

        auth(adminToken)
                .when()
                .get("/api/perguntas/{id}", perguntaId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/pergunta-response.schema.json"))
                .body("codigo", equalTo(perguntaId.intValue()));

        auth(adminToken)
                .body(Map.of(
                        "pergunta", "Pergunta inválida",
                        "competenciaCodigo", 999999,
                        "tipoPergunta", "TEXTO",
                        "opcoes", List.of()
                ))
                .when()
                .post("/api/perguntas")
                .then()
                .statusCode(400);

        auth(loginAsColaboradorA())
                .body(Map.of(
                        "pergunta", "Não deveria criar",
                        "competenciaCodigo", seed.competenciaJavaId(),
                        "tipoPergunta", "TEXTO",
                        "opcoes", List.of()
                ))
                .when()
                .post("/api/perguntas")
                .then()
                .statusCode(403);

        auth(adminToken)
                .when()
                .delete("/api/perguntas/{id}", perguntaId)
                .then()
                .statusCode(204);

        auth(adminToken)
                .when()
                .get("/api/perguntas/{id}", perguntaId)
                .then()
                .statusCode(404);

        auth(adminToken)
                .when()
                .delete("/api/perguntas/{id}", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    void deveBloquearPerguntasSemAutenticacao() {
        json()
                .when()
                .get("/api/perguntas")
                .then()
                .statusCode(401);
    }
}
