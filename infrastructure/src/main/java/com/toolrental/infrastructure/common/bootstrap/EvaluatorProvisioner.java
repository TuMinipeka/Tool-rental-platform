package com.toolrental.infrastructure.common.bootstrap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Offline operator tool for isolated academic accounts; never exposed over HTTP. */
public final class EvaluatorProvisioner {
  private EvaluatorProvisioner() {}

  public static void main(String[] args) {
    try {
      provision();
      System.out.println("Evaluator provisioned; previous sessions and reset tokens revoked.");
    } catch (Exception failure) {
      System.err.println("Evaluator provisioning failed; check private operator configuration.");
      System.exit(1);
    }
  }

  private static void provision() throws Exception {
    var database = required("SHOWCASE_DATABASE_NAME");
    var url = required("DB_URL");
    var email = required("EVALUATOR_EMAIL").trim().toLowerCase(java.util.Locale.ROOT);
    var password = required("EVALUATOR_PASSWORD");
    var role = required("EVALUATOR_ROLE");
    if (!database.matches("[a-z][a-z0-9_]*showcase[a-z0-9_]*")
        || !url.endsWith("/" + database)
        || !email.matches("evaluator\\.[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
        || password.length() < 12
        || password.length() > 72
        || !Set.of("CUSTOMER", "SUPPLIER", "ADMIN").contains(role)) {
      throw new IllegalArgumentException("Invalid isolated evaluator configuration");
    }
    var hash = new BCryptPasswordEncoder(12).encode(password);
    try (var connection =
        DriverManager.getConnection(url, required("DB_USERNAME"), required("DB_PASSWORD"))) {
      connection.setAutoCommit(false);
      UUID id = UUID.randomUUID();
      try (var existing =
          connection.prepareStatement(
              "SELECT user_id FROM users WHERE lower(email)=? FOR UPDATE")) {
        existing.setString(1, email);
        try (var rows = existing.executeQuery()) {
          if (rows.next()) id = rows.getObject(1, UUID.class);
        }
      }
      update(
          connection,
          "INSERT INTO users(user_id,email,display_name,password_hash,account_status,registered_at) VALUES (?,?,'Academic Evaluator',?,'ACTIVE',now()) ON CONFLICT(user_id) DO UPDATE SET password_hash=EXCLUDED.password_hash,account_status='ACTIVE'",
          id,
          email,
          hash);
      update(
          connection,
          "INSERT INTO user_roles(user_id,role_id) SELECT ?,role_id FROM roles WHERE code=? ON CONFLICT DO NOTHING",
          id,
          role);
      if (role.equals("CUSTOMER")) {
        update(
            connection,
            "INSERT INTO customers(user_id,person_type,document_type,document_number,billing_name) VALUES (?,'PERSON','CC',?,'Academic Evaluator') ON CONFLICT(user_id) DO NOTHING",
            id,
            "EVAL-" + id);
      } else if (role.equals("SUPPLIER")) {
        update(
            connection,
            "INSERT INTO suppliers(user_id,document_type,document_number,business_name) VALUES (?,'NIT',?,'Academic Evaluator') ON CONFLICT(user_id) DO NOTHING",
            id,
            "EVAL-" + id);
      }
      update(
          connection,
          "UPDATE user_sessions SET session_status='REVOKED',revoked_at=now() WHERE user_id=? AND session_status IN ('ACTIVE','ROTATED')",
          id);
      update(
          connection,
          "UPDATE password_reset_tokens SET revoked_at=now() WHERE user_id=? AND used_at IS NULL AND revoked_at IS NULL",
          id);
      connection.commit();
    }
  }

  private static void update(Connection connection, String sql, Object... values) throws Exception {
    try (var statement = connection.prepareStatement(sql)) {
      for (int index = 0; index < values.length; index++)
        statement.setObject(index + 1, values[index]);
      statement.executeUpdate();
    }
  }

  private static String required(String name) {
    var value = System.getenv(name);
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("Missing operator setting");
    return value;
  }
}
