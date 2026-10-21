package com.toolrental.infrastructure.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public final class OpenApiDocumentationCustomizer implements OpenApiCustomizer {
  private static final String SAMPLE_ID = "11111111-1111-4111-8111-111111111111";

  @Override
  public void customise(OpenAPI api) {
    if (api.getComponents().getSchemas() != null) {
      var money = api.getComponents().getSchemas().get("Money");
      if (money != null)
        money.addProperty("currency", new StringSchema().pattern("^[A-Z]{3}$").example("COP"));
    }
    api.getInfo()
        .setDescription(
            """
        Colombian tool rental API. Examples are illustrative: replace identifiers with values returned by your environment.

        ## Authentication and JWT
        1. POST /api/v1/auth/login with email and password. Copy accessToken from the JSON response.
        2. Click Authorize and paste only the access token. Swagger sends `Authorization: Bearer <token>`.
        3. Access tokens last 15 minutes and reference a revocable server-side session. Roles and ownership are checked by the server.
        4. Login also sets the HttpOnly trp_refresh cookie under /api. It is not an access token and must not be copied into Bearer authorization.
        5. GET /api/v1/auth/csrf initializes the CSRF cookie and returns a token. Send that token in X-XSRF-TOKEN for refresh, logout and logout-all, preserving cookies.
        6. POST /api/v1/auth/refresh rotates the refresh cookie and returns a new access token. Logout revokes the session immediately.
        7. 401 means missing, invalid or revoked credentials; 403 means insufficient role, ownership or CSRF validation failure.

        ## Business workflow
        Supplier: register tool and physical equipment units. Customer: check availability, quote and create a PENDING_PAYMENT reservation.
        Create an idempotent checkout, submit an APPROVED or DECLINED fictional decision, then query the persisted result.
        An approval confirms the reservation and issues one academic receipt. The supplier schedules/completes delivery, starts the rental, registers returns and closes it.
        Payments are academic simulations, with no external charge. Issued receipts have no fiscal validity.
        """);
    api.getPaths()
        .forEach(
            (path, item) ->
                item.readOperationsMap()
                    .forEach(
                        (method, operation) -> {
                          if (operation.getSummary() == null)
                            operation.setSummary(
                                method.name() + " " + path.substring("/api/v1/".length()));
                          operation.setDescription(description(path, method.name()));
                          if (operation.getRequestBody() != null)
                            examples(api, operation.getRequestBody().getContent());
                          operation
                              .getResponses()
                              .values()
                              .forEach(response -> examples(api, response.getContent()));
                          if (operation.getParameters() != null)
                            operation
                                .getParameters()
                                .forEach(
                                    parameter -> {
                                      if (parameter.getExample() == null
                                          && parameter.getSchema() != null)
                                        parameter.setExample(
                                            example(
                                                api,
                                                parameter.getSchema(),
                                                parameter.getName(),
                                                0));
                                    });
                          if (path.endsWith("/auth/refresh")
                              || path.endsWith("/auth/logout")
                              || path.endsWith("/auth/logout-all")) {
                            operation.addParametersItem(
                                new Parameter()
                                    .in("header")
                                    .name("X-XSRF-TOKEN")
                                    .required(true)
                                    .description(
                                        "Token returned by GET /api/v1/auth/csrf; preserve its cookie.")
                                    .schema(new Schema<>().type("string")));
                            error(
                                operation,
                                403,
                                "CSRF_VALIDATION_FAILED",
                                "CSRF cookie and header must match");
                          }
                          boolean publicOperation =
                              path.contains("/public/")
                                  || Set.of(
                                          "/api/v1/auth/login",
                                          "/api/v1/auth/register",
                                          "/api/v1/auth/supplier-registration",
                                          "/api/v1/auth/csrf")
                                      .contains(path);
                          if (!publicOperation) {
                            error(
                                operation,
                                401,
                                "AUTHENTICATION_REQUIRED",
                                "Authentication is required or the session is invalid");
                            error(
                                operation,
                                403,
                                "ACCESS_DENIED",
                                "Role or ownership does not permit this operation");
                          }
                          if (operation.getRequestBody() != null
                              || (operation.getParameters() != null
                                  && !operation.getParameters().isEmpty()))
                            error(
                                operation,
                                400,
                                "INVALID_REQUEST",
                                "Request body or parameter is invalid");
                          if (path.contains("{id}"))
                            error(
                                operation,
                                404,
                                "RESOURCE_NOT_FOUND",
                                "The requested resource does not exist or is not visible");
                          if (Set.of("POST", "PUT", "PATCH", "DELETE").contains(method.name())
                              && !path.startsWith("/api/v1/auth/"))
                            error(
                                operation,
                                409,
                                "BUSINESS_CONFLICT",
                                "The requested operation conflicts with the current business state");
                          if (path.equals("/api/v1/auth/login")) {
                            error(
                                operation,
                                401,
                                "AUTHENTICATION_REQUIRED",
                                "Invalid email or password");
                            error(
                                operation,
                                429,
                                "RATE_LIMIT_EXCEEDED",
                                "Too many authentication attempts");
                          }
                        }));
  }

  private static String description(String path, String method) {
    String scope =
        path.contains("/public/")
            ? "Public read-only catalog and feature information."
            : path.startsWith("/api/v1/auth/")
                ? "Authentication/session operation. Preserve cookies for session renewal and logout."
                : "Requires an active Bearer JWT. The backend validates role, resource ownership and business state.";
    String detail =
        path.startsWith("/api/v1/tools") || path.startsWith("/api/v1/equipment-units")
            ? " Suppliers manage their own tools and physical units; administrator access remains subject to the endpoint contract."
            : path.startsWith("/api/v1/payments")
                ? " Customer-owned academic checkout; amounts come from the server. Reuse Idempotency-Key for checkout retries. URL return parameters never confirm payment."
                : path.startsWith("/api/v1/reservations")
                    ? " Reservations use inclusive calendar dates and atomically validate availability. Follow allowedActions and the current reservation state."
                    : path.startsWith("/api/v1/reports")
                        ? " Supplier reports are restricted to their resources; administrators can query the global operation."
                        : path.contains("/unit-deliveries")
                                || path.contains("/unit-returns")
                                || path.contains("/rentals")
                            ? " Supplier-owned operational workflow: confirmed reservation, delivery, rental, return, then completion."
                            : " See the request and response schemas for fields and validation constraints.";
    if ("DELETE".equals(method) && path.equals("/api/v1/tools/{id}"))
      detail += " DELETE withdraws publication; it preserves the tool and its history.";
    return scope + detail;
  }

  private static void examples(OpenAPI api, Content content) {
    if (content == null) return;
    MediaType wildcard = content.get("*/*");
    if (wildcard != null
        && wildcard.getSchema() != null
        && !"binary".equals(wildcard.getSchema().getFormat())) {
      content.remove("*/*");
      content.put("application/json", wildcard);
    }
    content.forEach(
        (type, media) -> {
          if (type.contains("json") && media.getSchema() != null && media.getExample() == null)
            media.setExample(example(api, media.getSchema(), "value", 0));
        });
  }

  private static Object example(OpenAPI api, Schema<?> schema, String name, int depth) {
    if (schema == null || depth > 16) return null;
    if (schema.getExample() != null) return schema.getExample();
    if (schema.get$ref() != null)
      return example(
          api,
          api.getComponents()
              .getSchemas()
              .get(schema.get$ref().substring(schema.get$ref().lastIndexOf('/') + 1)),
          name,
          depth + 1);
    if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
      if (schema.getEnum().contains("PENDING_PAYMENT")) return "PENDING_PAYMENT";
      return schema.getEnum().getFirst();
    }
    if (schema.getProperties() != null) {
      Map<String, Object> value = new LinkedHashMap<>();
      schema
          .getProperties()
          .forEach((key, property) -> value.put(key, example(api, property, key, depth + 1)));
      return value;
    }
    if (isType(schema, "array") || schema.getItems() != null) {
      Object item = example(api, schema.getItems(), name, depth + 1);
      return item == null ? new ArrayList<>() : List.of(item);
    }
    if (isType(schema, "boolean")) return true;
    if (isType(schema, "integer"))
      return name.contains("Year") ? 2030 : name.contains("Month") ? 12 : 1;
    if (isType(schema, "number")) return 25000;
    if ("uuid".equals(schema.getFormat())) return SAMPLE_ID;
    if ("date".equals(schema.getFormat())) return "2030-01-15";
    if ("date-time".equals(schema.getFormat())) return "2030-01-15T14:00:00Z";
    if ("uri".equals(schema.getFormat()) || name.equals("imageUrls"))
      return "https://example.com/tool.jpg";
    return switch (name) {
      case "email" -> "customer@example.test";
      case "password", "currentPassword", "newPassword" -> "ExamplePassword2026!";
      case "accessToken" -> "<access-token-returned-by-login>";
      case "tokenType" -> "Bearer";
      case "roles" -> "CUSTOMER";
      case "currency" -> "COP";
      case "name" -> "Professional cordless drill";
      case "serviceCity", "city", "deliveryCity" -> "Bucaramanga";
      case "region", "deliveryRegion" -> "Santander";
      case "country", "deliveryCountryCode" -> "CO";
      case "condition", "currentCondition", "inboundCondition", "outboundCondition" -> "GOOD";
      case "paymentMethod" -> "CREDIT_CARD";
      case "cardNetwork" -> "VISA";
      case "cardLastFour" -> "1111";
      case "Idempotency-Key", "inventoryCode" -> "example-unique-key-001";
      default -> "Example " + name;
    };
  }

  private static boolean isType(Schema<?> schema, String type) {
    return type.equals(schema.getType())
        || (schema.getTypes() != null && schema.getTypes().contains(type));
  }

  private static void error(Operation operation, int status, String code, String detail) {
    String key = Integer.toString(status);
    if (operation.getResponses().containsKey(key)) return;
    operation
        .getResponses()
        .addApiResponse(
            key,
            new ApiResponse()
                .description(detail)
                .content(
                    new Content()
                        .addMediaType(
                            "application/problem+json",
                            new MediaType()
                                .schema(new Schema<>().type("object"))
                                .example(
                                    Map.of(
                                        "type",
                                        "about:blank",
                                        "title",
                                        detail,
                                        "status",
                                        status,
                                        "detail",
                                        detail,
                                        "instance",
                                        "/api/v1/resource",
                                        "code",
                                        code,
                                        "traceId",
                                        "example-trace-id")))));
  }
}
