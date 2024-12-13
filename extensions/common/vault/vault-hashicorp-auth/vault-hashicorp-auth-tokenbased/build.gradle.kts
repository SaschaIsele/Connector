/*
 *  Copyright (c) 2024 Cofinity-X GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Cofinity-X GmbH - Initial API and Implementation
 *
 */
plugins {
    `java-library`
}

dependencies {
    api(project(":spi:common:core-spi"))
    api(project(":spi:common:http-spi"))
    api(project(":spi:common:hashicorp-vault-auth-spi"))

    implementation(project(":core:common:lib:util-lib"))
    implementation(project(":extensions:common:vault:vault-hashicorp"))

    testImplementation(project(":core:common:connector-core"))
    testImplementation(testFixtures(project(":core:common:lib:http-lib")))
    testImplementation(project(":core:common:junit"))
    testImplementation(libs.jakarta.json.api)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.vault)
    implementation(libs.bouncyCastle.bcpkixJdk18on)

}