package org.eclipse.edc.vault.hashicorp.auth.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuthRegistry;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesAuthClient;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesSettings;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.util.FileUtil;

import java.time.Clock;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Extension(value = HashicorpVaultKubernetesAuthExtension.NAME)
public class HashicorpVaultKubernetesAuthExtension implements ServiceExtension {
    public static final String NAME = "Hashicorp Vault Kubernetes Auth";

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private HashicorpVaultAuthRegistry registry;

    @Configuration
    private HashicorpVaultKubernetesSettings config;

    private HashicorpVaultKubernetesAuthClient client;
    private Monitor monitor;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor().withPrefix(NAME);
        registry.register("kubernetes", new HashicorpVaultKubernetesAuthImpl(
                hashicorpVaultTokenAuthClient(),
                config,
                monitor,
                Clock.systemUTC(),
                new FileUtil()));
    }

    private HashicorpVaultKubernetesAuthClient hashicorpVaultTokenAuthClient() {
        if (client == null) {
            var mapper = new ObjectMapper();
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

            client = new HashicorpVaultKubernetesAuthClient(
                    config.getVaultUrl(),
                    httpClient,
                    mapper);
        }
        return client;
    }

}
