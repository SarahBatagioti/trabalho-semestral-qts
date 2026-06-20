package br.com.AllTallent.e2e;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import br.com.AllTallent.e2e.support.BaseApiE2ETest;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthE2ETest extends BaseApiE2ETest {

    @Test
    void deveRealizarLoginComCredenciaisValidas() {
        json()
                .body(Map.of("email", seed.adminAEmail(), "password", seed.defaultPassword()))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login-response.schema.json"))
                .body("userId", equalTo(seed.adminAId()))
                .body("nomeCompleto", equalTo("Ana Diretora"))
                .body("token", notNullValue());
    }

    @Test
    void deveFalharNoLoginComSenhaInvalida() {
        json()
                .body(Map.of("email", seed.adminAEmail(), "password", "senha-invalida"))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body(containsString("Credenciais inválidas"));
    }

    @Test
    void deveRegistrarNovoColaboradorEPermitirLoginPosterior() {
        Map<String, Object> payload = Map.ofEntries(
                Map.entry("nomeCompleto", "Novo Colaborador"),
                Map.entry("email", "novo.colaborador@alltallent.test"),
                Map.entry("senha", "senha123"),
                Map.entry("telefone", "11988887777"),
                Map.entry("idCracha", "CR-2000"),
                Map.entry("dataAdmissao", LocalDate.now().toString()),
                Map.entry("resumo", "Novo colaborador de testes"),
                Map.entry("codigoArea", seed.areaAId()),
                Map.entry("codigoPerfil", seed.colaboradorPerfilId()),
                Map.entry("cpf", "99999999999"),
                Map.entry("localizacao", "São Paulo"),
                Map.entry("tituloProfissional", "QA Engineer"),
                Map.entry("codigoGestor", seed.gestorAId())
        );

        json()
                .body(payload)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body(containsString("cadastrado com sucesso"));

        json()
                .body(Map.of("email", "novo.colaborador@alltallent.test", "password", "senha123"))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login-response.schema.json"))
                .body("nomeCompleto", equalTo("Novo Colaborador"));
    }

    @Test
    void deveRejeitarRegistroComEmailDuplicadoOuRelacionamentoInvalido() {
        Map<String, Object> payloadBase = Map.ofEntries(
                Map.entry("nomeCompleto", "Duplicado"),
                Map.entry("email", seed.colaboradorAEmail()),
                Map.entry("senha", "senha123"),
                Map.entry("telefone", "11988887777"),
                Map.entry("idCracha", "CR-3000"),
                Map.entry("dataAdmissao", LocalDate.now().toString()),
                Map.entry("resumo", "Teste"),
                Map.entry("codigoArea", seed.areaAId()),
                Map.entry("codigoPerfil", seed.colaboradorPerfilId()),
                Map.entry("cpf", "88888888888"),
                Map.entry("localizacao", "São Paulo"),
                Map.entry("tituloProfissional", "Analyst")
        );

        json()
                .body(payloadBase)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body(containsString("Email"));

        json()
                .body(Map.ofEntries(
                        Map.entry("nomeCompleto", "Área Inválida"),
                        Map.entry("email", "area.invalida@alltallent.test"),
                        Map.entry("senha", "senha123"),
                        Map.entry("telefone", "11988887777"),
                        Map.entry("idCracha", "CR-3001"),
                        Map.entry("dataAdmissao", LocalDate.now().toString()),
                        Map.entry("resumo", "Teste"),
                        Map.entry("codigoArea", 999999),
                        Map.entry("codigoPerfil", seed.colaboradorPerfilId()),
                        Map.entry("cpf", "77777777777"),
                        Map.entry("localizacao", "São Paulo"),
                        Map.entry("tituloProfissional", "Analyst")
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body(containsString("Área"));
    }

    @Test
    void deveRetornarPerfilDoUsuarioAutenticadoERejeitarTokensAusentesOuInvalidos() {
        auth(loginAsColaboradorA())
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/funcionario-response.schema.json"))
                .body("codigo", equalTo(seed.colaboradorAId()))
                .body("email", equalTo(seed.colaboradorAEmail()));

        json()
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(401);

        auth("token-invalido")
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(401);
    }
}
