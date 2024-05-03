# OAuth MultiTenancy & RBAC

This repo has implementation of 3 things.
### AuthN
Implementation for general auth flows, like sign-in, sign-up, password-reset(using email), forgot-password(using email). 

### OAuth
Implementation for creating client-id and secrets so that a tenant can exposed APIs as per permissions granted

### MultiTenancy And Multi-Level rbac
This is example of multi-tenant app with following features:
1. A tenant can set roles/permissions for its users
2. A tenant can onboard another tenant(agencies) and this onboarded sub-tenant can also add roles/permissions to its users
3. An user can be part of multiple tenants
4. An user can switch to assigned tenants to perform authorised action or access authorised APIs for that tenant.
5. There's a provision of super tenant who has access to everything.


## Local Setup

### Pre-requisites

* Java 21
* Docker Compose
* Gradle

## Installation (For mac)

* https://docs.docker.com/desktop/install/mac-install/
* `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"` (if Homebrew is not
  installed).
* `brew install openjdk@17`
* https://gradle.org/install/
* Choose an IDE of your choice

## Running it locally

1. Start the database: In root directory run `docker-compose up`
2. If you are running it from IntelliJ, there's a big green play button on the menu bar. or run `gradle bootRun`

## Running tests

* `gradle test`
* `gradle build jacocoTestReport` // for test coverage
* find test coverage file in `build/reports/jacoco/test/html/index.html` file

# Swagger:

http://localhost:8080/swagger-ui/index.html