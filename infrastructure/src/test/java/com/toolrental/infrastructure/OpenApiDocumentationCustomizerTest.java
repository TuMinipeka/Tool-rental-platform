package com.toolrental.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.infrastructure.common.config.OpenApiDocumentationCustomizer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OpenApiDocumentationCustomizerTest {
  @Test
  void documentsJwtAndAddsRequestResponseExamplesWithoutChangingPublicSecurity() {
    var schema =
        new ObjectSchema()
            .addProperty("email", new StringSchema().format("email"))
            .addProperty("roles", new Schema<>().types(Set.of("array")).items(new StringSchema()))
            .addProperty("published", new Schema<>().types(Set.of("boolean")))
            .addProperty("amount", new Schema<>().types(Set.of("number")))
            .addProperty("password", new StringSchema());
    var media = new MediaType().schema(new Schema<>().$ref("#/components/schemas/LoginRequest"));
    var operation =
        new Operation()
            .security(List.of())
            .requestBody(
                new RequestBody().content(new Content().addMediaType("application/json", media)))
            .responses(
                new ApiResponses()
                    .addApiResponse(
                        "200",
                        new ApiResponse()
                            .content(
                                new Content()
                                    .addMediaType("*/*", new MediaType().schema(schema)))));
    var api =
        new OpenAPI()
            .info(new Info())
            .components(
                new Components()
                    .addSchemas("LoginRequest", schema)
                    .addSchemas(
                        "Money", new ObjectSchema().addProperty("currency", new ObjectSchema())))
            .paths(new Paths().addPathItem("/api/v1/auth/login", new PathItem().post(operation)));

    new OpenApiDocumentationCustomizer().customise(api);

    assertTrue(api.getInfo().getDescription().contains("Authorization: Bearer"));
    assertTrue(api.getInfo().getDescription().contains("X-XSRF-TOKEN"));
    assertNotNull(operation.getSummary());
    assertTrue(operation.getSecurity().isEmpty());
    var requestExample = (Map<?, ?>) media.getExample();
    assertEquals("customer@example.test", requestExample.get("email"));
    assertEquals(List.of("CUSTOMER"), requestExample.get("roles"));
    assertEquals(true, requestExample.get("published"));
    assertEquals(25000, requestExample.get("amount"));
    Schema<?> currency =
        (Schema<?>) api.getComponents().getSchemas().get("Money").getProperties().get("currency");
    assertEquals("COP", currency.getExample());
    assertNotNull(
        operation.getResponses().get("200").getContent().get("application/json").getExample());
    assertTrue(operation.getResponses().containsKey("401"));
    assertTrue(operation.getResponses().containsKey("429"));
  }

  @Test
  void preservesBinaryResponsesAndDocumentsCsrfAndProtectedRoutes() {
    var pdf = new MediaType().schema(new StringSchema().format("binary"));
    var document =
        new Operation()
            .responses(
                new ApiResponses()
                    .addApiResponse(
                        "200",
                        new ApiResponse()
                            .content(new Content().addMediaType("application/pdf", pdf))));
    var logout =
        new Operation().responses(new ApiResponses().addApiResponse("204", new ApiResponse()));
    var api =
        new OpenAPI()
            .info(new Info())
            .components(new Components())
            .paths(
                new Paths()
                    .addPathItem("/api/v1/invoices/{id}/document", new PathItem().get(document))
                    .addPathItem("/api/v1/auth/logout", new PathItem().post(logout)));

    new OpenApiDocumentationCustomizer().customise(api);

    assertNull(pdf.getExample());
    assertTrue(document.getResponses().containsKey("401"));
    assertTrue(document.getResponses().containsKey("403"));
    assertEquals("X-XSRF-TOKEN", logout.getParameters().getFirst().getName());
    assertNull(logout.getResponses().get("204").getContent());
  }
}
