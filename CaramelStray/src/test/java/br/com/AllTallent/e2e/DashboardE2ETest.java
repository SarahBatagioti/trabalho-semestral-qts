package br.com.AllTallent.e2e;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import br.com.AllTallent.e2e.support.BaseApiE2ETest;
import org.junit.jupiter.api.Test;

class DashboardE2ETest extends BaseApiE2ETest {

    @Test
    void deveRetornarDashboardGlobalParaAdmin() {
        auth(loginAsAdminA())
                .when()
                .get("/api/dashboard")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/dashboard-response.schema.json"))
                .body("totalColaboradores", equalTo(5))
                .body("avaliacoesConcluidasMes", greaterThan(0))
                .body("totalPendencias", greaterThan(0))
                .body("totalColaboradoresArea.size()", greaterThan(0));
    }

    @Test
    void deveAplicarFiltroExplicitoParaAdminEImplicitoParaGestor() {
        auth(loginAsAdminA())
                .queryParam("codigoArea", seed.areaAId())
                .when()
                .get("/api/dashboard")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/dashboard-response.schema.json"))
                .body("totalColaboradores", equalTo(3));

        auth(loginAsGestorA())
                .queryParam("codigoArea", seed.areaBId())
                .when()
                .get("/api/dashboard")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/dashboard-response.schema.json"))
                .body("totalColaboradores", equalTo(3));
    }

    @Test
    void deveBloquearDashboardSemToken() {
        json()
                .when()
                .get("/api/dashboard")
                .then()
                .statusCode(401);
    }
}
