package org.eclipse.edc.vault.hashicorp.auth.kubernetes;

import org.eclipse.edc.hashicorp.vault.auth.spi.HashicorpVaultAuthRegistry;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ExecutorInstrumentation;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesAuthClient;
import org.eclipse.edc.vault.hashicorp.auth.kubernetes.client.HashicorpVaultKubernetesSettings;

@Extension(value = HashicorpVaultKubernetesAuthExtension.NAME)
public class HashicorpVaultKubernetesAuthExtension implements ServiceExtension {
    public static final String NAME = "Hashicorp Vault Kubernetes Auth";

    @Inject
    private EdcHttpClient httpClient;

    @Inject
    private ExecutorInstrumentation executorInstrumentation;

    @Inject
    private HashicorpVaultAuthRegistry registry;

    @Configuration
    private HashicorpVaultKubernetesSettings config;

    private HashicorpVaultKubernetesAuthClient client;
    private HashicorpVaultKubernetesTokenRenewTask tokenRenewalTask;
    private Monitor monitor;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor().withPrefix(NAME);
        registry.register("kubernetes", new HashicorpVaultAuthKubernetesImpl(
                config.getVaultK8sAuthRole(),
                client,
                ,
                ,
                monitor));
        tokenRenewalTask = new HashicorpVaultKubernetesTokenRenewTask(
                NAME,
                executorInstrumentation,
                hashicorpVaultTokenAuthClient(),
                config.renewBuffer(),
                monitor);
    }

    @Override
    public void start() {
        if (config.renewToken()) {
            tokenRenewalTask.start();
        }
    }

    @Override
    public void shutdown() {
        if (tokenRenewalTask.isRunning()) {
            tokenRenewalTask.stop();
        }
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
