package shedlock.provider.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.support.annotation.NonNull;

import java.time.LocalDateTime;

public class KubernetesLock implements SimpleLock {

  private final NamespacedKubernetesClient client;
  private final LockConfiguration lockConfiguration;
  private final LocalDateTime lockedAt;

  public KubernetesLock(@NonNull NamespacedKubernetesClient client, @NonNull LockConfiguration lockConfiguration, @NonNull LocalDateTime lockedAt) {
    this.client = client;
    this.lockConfiguration = lockConfiguration;
    this.lockedAt = lockedAt;
  }

  @Override
  public void unlock() {
    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(lockedAt.plus(lockConfiguration.getLockAtLeastFor()))) {
      String configMapName = KubernetesLockProvider.getConfigMapName(lockConfiguration);
      try {
        client.configMaps().withName(configMapName).delete();
      } catch (KubernetesClientException kce) {
        // Possibly deleted by another process, when lockAtLeastFor and lockAtMostFor expired.
      }
    }
  }
}
