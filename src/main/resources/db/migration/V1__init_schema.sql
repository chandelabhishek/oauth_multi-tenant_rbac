CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "role" (
  "id" uuid PRIMARY KEY,
  "name" text,
  "description" text,
  "status" text,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text
);

CREATE TABLE IF NOT EXISTS "tenant" (
  "id" uuid PRIMARY KEY,
  "name" text,
  "email" text UNIQUE,
  "phone_number" text,
  "country_code" text,
  "address" text,
  "state" text,
  "city" text,
  "country" text,
  "postal_code" text,
  "parent_id" uuid,
  "type" text,
  "config" jsonb,
  "status" text,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text
);

CREATE TABLE IF NOT EXISTS "user" (
  "id" uuid PRIMARY KEY,
  "first_name" text,
  "last_name" text,
  "email" text UNIQUE,
  "phone_number" text,
  "country_code" text,
  "password" text,
  "last_password_changed_at" timestamp,
  "config" jsonb,
  "status" text,
  "secure_code" text,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text
);

CREATE TABLE IF NOT EXISTS "user_tenant" (
  "id" uuid PRIMARY KEY,
  "tenant_id" uuid,
  "user_id" uuid,
  "role_id" uuid,
  "status" text,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text,
  CONSTRAINT "FK_user_tenant.tenant_id"
    FOREIGN KEY ("tenant_id")
      REFERENCES "tenant"("id"),
  CONSTRAINT "FK_user_tenant.user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "user"("id"),
  CONSTRAINT "FK_user_tenant.role_id"
    FOREIGN KEY ("role_id")
      REFERENCES "role"("id")
);

CREATE TABLE IF NOT EXISTS "permission" (
  "id" uuid PRIMARY KEY,
  "name" text,
  "description" text,
  "status" text,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text
);

CREATE TABLE IF NOT EXISTS "role_permission" (
  "id" uuid PRIMARY KEY,
  "role_id" uuid,
  "permission_id" uuid,
  "tenant_id" uuid,
  "status" text,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text,
  CONSTRAINT "FK_role_permission.role_id"
    FOREIGN KEY ("role_id")
      REFERENCES "role"("id"),
  CONSTRAINT "FK_role_permission.permission_id"
    FOREIGN KEY ("permission_id")
      REFERENCES "permission"("id"),
  CONSTRAINT "FK_role_permission.tenant_id"
    FOREIGN KEY ("tenant_id")
      REFERENCES "tenant"("id")
);

CREATE TABLE IF NOT EXISTS "token" (
  "id" uuid PRIMARY KEY,
  "token" text,
  "token_type" text,
  "revoked" boolean,
  "expired" boolean,
  "user_id" uuid,
  "tenant_id" uuid,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text,
  CONSTRAINT "FK_token.user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "user"("id"),
  CONSTRAINT "FK_credentials.tenant_id"
    FOREIGN KEY ("tenant_id")
      REFERENCES "tenant"("id")
);

CREATE TABLE IF NOT EXISTS "oauth2_registered_client" (
  "id" text PRIMARY KEY,
  "tenant_id" uuid,
  "client_id" text unique,
  "client_secret" text,
  "client_secret_expires_at" timestamp,
  "client_name" text,
  "client_authentication_methods" text,
  "authorization_grant_types" text,
  "scopes" text,
  "client_settings" text,
  "token_settings" text,
  "redirect_uris" text,
  "post_logout_redirect_uris" text,
  "revoked" boolean,
  "created_at" timestamp,
  "updated_at" timestamp,
  "deleted_at" timestamp,
  "created_by" text,
  "updated_by" text,
  "client_id_issued_at" timestamp,
  CONSTRAINT "FK_registered_client.tenant_id"
    FOREIGN KEY ("tenant_id")
      REFERENCES "tenant"("id")
);

create table if not exists oauth2_authorization
(
    id                            varchar(100) NOT NULL,
    registered_client_id          varchar(100) NOT NULL,
    principal_name                varchar(200) NOT NULL,
    authorization_grant_type      varchar(100) NOT NULL,
    authorized_scopes             varchar(1000) DEFAULT NULL,
    attributes                    text          DEFAULT NULL,
    state                         varchar(500)  DEFAULT NULL,
    authorization_code_value      text          DEFAULT NULL,
    authorization_code_issued_at  timestamp     DEFAULT NULL,
    authorization_code_expires_at timestamp     DEFAULT NULL,
    authorization_code_metadata   text          DEFAULT NULL,
    access_token_value            text          DEFAULT NULL,
    access_token_issued_at        timestamp     DEFAULT NULL,
    access_token_expires_at       timestamp     DEFAULT NULL,
    access_token_metadata         text          DEFAULT NULL,
    access_token_type             varchar(100)  DEFAULT NULL,
    access_token_scopes           varchar(1000) DEFAULT NULL,
    oidc_id_token_value           text          DEFAULT NULL,
    oidc_id_token_issued_at       timestamp     DEFAULT NULL,
    oidc_id_token_expires_at      timestamp     DEFAULT NULL,
    oidc_id_token_metadata        text          DEFAULT NULL,
    refresh_token_value           text          DEFAULT NULL,
    refresh_token_issued_at       timestamp     DEFAULT NULL,
    refresh_token_expires_at      timestamp     DEFAULT NULL,
    refresh_token_metadata        text          DEFAULT NULL,
    user_code_value               text          DEFAULT NULL,
    user_code_issued_at           timestamp     DEFAULT NULL,
    user_code_expires_at          timestamp     DEFAULT NULL,
    user_code_metadata            text          DEFAULT NULL,
    device_code_value             text          DEFAULT NULL,
    device_code_issued_at         timestamp     DEFAULT NULL,
    device_code_expires_at        timestamp     DEFAULT NULL,
    device_code_metadata          text          DEFAULT NULL,
    PRIMARY KEY (id)
);
