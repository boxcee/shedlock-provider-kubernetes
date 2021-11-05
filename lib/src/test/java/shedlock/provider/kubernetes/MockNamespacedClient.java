package shedlock.provider.kubernetes;

import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.APIGroup;
import io.fabric8.kubernetes.api.model.APIGroupList;
import io.fabric8.kubernetes.api.model.APIResourceList;
import io.fabric8.kubernetes.api.model.APIService;
import io.fabric8.kubernetes.api.model.APIServiceList;
import io.fabric8.kubernetes.api.model.Binding;
import io.fabric8.kubernetes.api.model.ComponentStatus;
import io.fabric8.kubernetes.api.model.ComponentStatusList;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LimitRange;
import io.fabric8.kubernetes.api.model.LimitRangeList;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.PersistentVolumeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerList;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaList;
import io.fabric8.kubernetes.api.model.RootPaths;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountList;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.authentication.TokenReview;
import io.fabric8.kubernetes.api.model.certificates.v1beta1.CertificateSigningRequest;
import io.fabric8.kubernetes.api.model.certificates.v1beta1.CertificateSigningRequestList;
import io.fabric8.kubernetes.api.model.coordination.v1.Lease;
import io.fabric8.kubernetes.api.model.coordination.v1.LeaseList;
import io.fabric8.kubernetes.api.model.node.v1beta1.RuntimeClass;
import io.fabric8.kubernetes.api.model.node.v1beta1.RuntimeClassList;
import io.fabric8.kubernetes.client.AdmissionRegistrationAPIGroupDSL;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.RequestConfig;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ApiextensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AuthorizationAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AutoscalingAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.BatchAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.CertificatesAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.Deletable;
import io.fabric8.kubernetes.client.dsl.DiscoveryAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.EditReplacePatchDeletable;
import io.fabric8.kubernetes.client.dsl.EventingAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.ExtensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.FilterNested;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.FilterWatchListMultiDeletable;
import io.fabric8.kubernetes.client.dsl.FlowControlAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.FunctionCallable;
import io.fabric8.kubernetes.client.dsl.Gettable;
import io.fabric8.kubernetes.client.dsl.InOutCreateable;
import io.fabric8.kubernetes.client.dsl.Informable;
import io.fabric8.kubernetes.client.dsl.KubernetesListMixedOperation;
import io.fabric8.kubernetes.client.dsl.MetricAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.dsl.NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.dsl.Namespaceable;
import io.fabric8.kubernetes.client.dsl.NetworkAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.PolicyAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.RbacAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.ReplaceDeletable;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.fabric8.kubernetes.client.dsl.SchedulingAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import io.fabric8.kubernetes.client.dsl.StorageAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.V1APIGroupDSL;
import io.fabric8.kubernetes.client.dsl.Waitable;
import io.fabric8.kubernetes.client.dsl.WatchAndWaitable;
import io.fabric8.kubernetes.client.dsl.WritableOperation;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElectorBuilder;
import io.fabric8.kubernetes.client.extended.run.RunOperations;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class MockNamespacedClient implements NamespacedKubernetesClient {

  private final Map<String, ConfigMap> storage = Collections.synchronizedMap(new HashMap<>());

  @Override
  public ApiextensionsAPIGroupDSL apiextensions() {
    return null;
  }

  @Override
  public NonNamespaceOperation<CertificateSigningRequest, CertificateSigningRequestList, Resource<CertificateSigningRequest>> certificateSigningRequests() {
    return null;
  }

  @Override
  public CertificatesAPIGroupDSL certificates() {
    return null;
  }

  @Override
  public <T extends CustomResource> MixedOperation<T, KubernetesResourceList<T>, Resource<T>> customResources(Class<T> resourceType) {
    return null;
  }

  @Override
  public <T extends HasMetadata, L extends KubernetesResourceList<T>> MixedOperation<T, L, Resource<T>> resources(Class<T> resourceType, Class<L> listClass) {
    return null;
  }

  @Override
  public <T extends CustomResource, L extends KubernetesResourceList<T>> MixedOperation<T, L, Resource<T>> customResources(Class<T> resourceType, Class<L> listClass) {
    return null;
  }

  @Override
  public <T extends HasMetadata, L extends KubernetesResourceList<T>> MixedOperation<T, L, Resource<T>> customResources(ResourceDefinitionContext context, Class<T> resourceType, Class<L> listClass) {
    return null;
  }

  @Override
  public MixedOperation<GenericKubernetesResource, GenericKubernetesResourceList, Resource<GenericKubernetesResource>> genericKubernetesResources(String apiVersion, String kind) {
    return null;
  }

  @Override
  public DiscoveryAPIGroupDSL discovery() {
    return null;
  }

  @Override
  public EventingAPIGroupDSL events() {
    return null;
  }

  @Override
  public ExtensionsAPIGroupDSL extensions() {
    return null;
  }

  @Override
  public FlowControlAPIGroupDSL flowControl() {
    return null;
  }

  @Override
  public VersionInfo getVersion() {
    return null;
  }

  @Override
  public VersionInfo getKubernetesVersion() {
    return null;
  }

  @Override
  public RawCustomResourceOperationsImpl customResource(CustomResourceDefinitionContext customResourceDefinition) {
    return null;
  }

  @Override
  public AdmissionRegistrationAPIGroupDSL admissionRegistration() {
    return null;
  }

  @Override
  public AppsAPIGroupDSL apps() {
    return null;
  }

  @Override
  public AutoscalingAPIGroupDSL autoscaling() {
    return null;
  }

  @Override
  public NetworkAPIGroupDSL network() {
    return null;
  }

  @Override
  public StorageAPIGroupDSL storage() {
    return null;
  }

  @Override
  public BatchAPIGroupDSL batch() {
    return null;
  }

  @Override
  public MetricAPIGroupDSL top() {
    return null;
  }

  @Override
  public PolicyAPIGroupDSL policy() {
    return null;
  }

  @Override
  public RbacAPIGroupDSL rbac() {
    return null;
  }

  @Override
  public SchedulingAPIGroupDSL scheduling() {
    return null;
  }

  @Override
  public NonNamespaceOperation<ComponentStatus, ComponentStatusList, Resource<ComponentStatus>> componentstatuses() {
    return null;
  }

  @Override
  public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> load(InputStream is) {
    return null;
  }

  @Override
  public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(String s) {
    return null;
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(KubernetesResourceList list) {
    return null;
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(HasMetadata... items) {
    return null;
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(Collection<HasMetadata> items) {
    return null;
  }

  @Override
  public <T extends HasMetadata> NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<T> resource(T is) {
    return null;
  }

  @Override
  public NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<HasMetadata> resource(String s) {
    return null;
  }

  @Override
  public MixedOperation<Binding, KubernetesResourceList<Binding>, Resource<Binding>> bindings() {
    return null;
  }

  @Override
  public MixedOperation<Endpoints, EndpointsList, Resource<Endpoints>> endpoints() {
    return null;
  }

  @Override
  public NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces() {
    return null;
  }

  @Override
  public NonNamespaceOperation<Node, NodeList, Resource<Node>> nodes() {
    return null;
  }

  @Override
  public NonNamespaceOperation<PersistentVolume, PersistentVolumeList, Resource<PersistentVolume>> persistentVolumes() {
    return null;
  }

  @Override
  public MixedOperation<PersistentVolumeClaim, PersistentVolumeClaimList, Resource<PersistentVolumeClaim>> persistentVolumeClaims() {
    return null;
  }

  @Override
  public MixedOperation<Pod, PodList, PodResource<Pod>> pods() {
    return null;
  }

  @Override
  public MixedOperation<ReplicationController, ReplicationControllerList, RollableScalableResource<ReplicationController>> replicationControllers() {
    return null;
  }

  @Override
  public MixedOperation<ResourceQuota, ResourceQuotaList, Resource<ResourceQuota>> resourceQuotas() {
    return null;
  }

  @Override
  public MixedOperation<Secret, SecretList, Resource<Secret>> secrets() {
    return null;
  }

  @Override
  public MixedOperation<Service, ServiceList, ServiceResource<Service>> services() {
    return null;
  }

  @Override
  public MixedOperation<ServiceAccount, ServiceAccountList, Resource<ServiceAccount>> serviceAccounts() {
    return null;
  }

  @Override
  public NonNamespaceOperation<APIService, APIServiceList, Resource<APIService>> apiServices() {
    return null;
  }

  @Override
  public KubernetesListMixedOperation lists() {
    return null;
  }

  @Override
  public MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> configMaps() {
    return new MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>>() {
      @Override
      public FilterWatchListMultiDeletable<ConfigMap, ConfigMapList> inAnyNamespace() {
        return null;
      }

      @Override
      public ConfigMap createOrReplace(ConfigMap... item) {
        return null;
      }

      @Override
      public WritableOperation<ConfigMap> dryRun(boolean isDryRun) {
        return null;
      }

      @Override
      public ConfigMap edit(UnaryOperator<ConfigMap> function) {
        return null;
      }

      @Override
      public ConfigMap edit(Visitor... visitors) {
        return null;
      }

      @Override
      public <V> ConfigMap edit(Class<V> visitorType, Visitor<V> visitor) {
        return null;
      }

      @Override
      public ConfigMap accept(Consumer<ConfigMap> function) {
        return null;
      }

      @Override
      public FilterNested<FilterWatchListDeletable<ConfigMap, ConfigMapList>> withNewFilter() {
        return null;
      }

      @Override
      public Deletable withGracePeriod(long gracePeriodSeconds) {
        return null;
      }

      @Override
      public EditReplacePatchDeletable<ConfigMap> withPropagationPolicy(DeletionPropagation propagationPolicy) {
        return null;
      }

      @Override
      public Boolean delete() {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withLabels(Map<String, String> labels) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withoutLabels(Map<String, String> labels) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withLabelIn(String key, String... values) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withLabelNotIn(String key, String... values) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withLabel(String key, String value) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withoutLabel(String key, String value) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withFields(Map<String, String> fields) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withField(String key, String value) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withoutFields(Map<String, String> fields) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withoutField(String key, String value) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withLabelSelector(LabelSelector selector) {
        return null;
      }

      @Override
      public FilterWatchListDeletable<ConfigMap, ConfigMapList> withInvolvedObject(ObjectReference objectReference) {
        return null;
      }

      @Override
      public ConfigMap create(ConfigMap... item) {
        return null;
      }

      @Override
      public ConfigMap create(ConfigMap item) {
        storage.put(item.getMetadata().getName(), item);
        return item;
      }

      @Override
      public Informable<ConfigMap> withIndexers(Map<String, Function<ConfigMap, List<String>>> indexers) {
        return null;
      }

      @Override
      public SharedIndexInformer<ConfigMap> inform(ResourceEventHandler<? super ConfigMap> handler, long resync) {
        return null;
      }

      @Override
      public SharedIndexInformer<ConfigMap> runnableInformer(long resync) {
        return null;
      }

      @Override
      public CompletableFuture<List<ConfigMap>> informOnCondition(Predicate<List<ConfigMap>> condition) {
        return null;
      }

      @Override
      public ConfigMapList list() {
        return null;
      }

      @Override
      public ConfigMapList list(Integer limitVal, String continueVal) {
        return null;
      }

      @Override
      public ConfigMapList list(ListOptions listOptions) {
        return null;
      }

      @Override
      public Resource<ConfigMap> load(InputStream is) {
        return null;
      }

      @Override
      public Resource<ConfigMap> load(URL url) {
        return null;
      }

      @Override
      public Resource<ConfigMap> load(File file) {
        return null;
      }

      @Override
      public Resource<ConfigMap> load(String path) {
        return null;
      }

      @Override
      public Boolean delete(ConfigMap... items) {
        return null;
      }

      @Override
      public Boolean delete(List<ConfigMap> items) {
        return null;
      }

      @Override
      public Resource<ConfigMap> withName(String name) {
        return new Resource<ConfigMap>() {
          @Override
          public Deletable withGracePeriod(long gracePeriodSeconds) {
            return null;
          }

          @Override
          public EditReplacePatchDeletable<ConfigMap> withPropagationPolicy(DeletionPropagation propagationPolicy) {
            return null;
          }

          @Override
          public EditReplacePatchDeletable<ConfigMap> cascading(boolean enabled) {
            return null;
          }

          @Override
          public ConfigMap createOrReplace(ConfigMap... item) {
            return null;
          }

          @Override
          public Boolean delete() {
            storage.remove(name);
            return true;
          }

          @Override
          public WritableOperation<ConfigMap> dryRun(boolean isDryRun) {
            return null;
          }

          @Override
          public ConfigMap edit(UnaryOperator<ConfigMap> function) {
            return null;
          }

          @Override
          public ConfigMap edit(Visitor... visitors) {
            return null;
          }

          @Override
          public <V> ConfigMap edit(Class<V> visitorType, Visitor<V> visitor) {
            return null;
          }

          @Override
          public ConfigMap accept(Consumer<ConfigMap> function) {
            return null;
          }

          @Override
          public Gettable<ConfigMap> fromServer() {
            return null;
          }

          @Override
          public ConfigMap get() {
            return storage.get(name);
          }

          @Override
          public ConfigMap create(ConfigMap... item) {
            return null;
          }

          @Override
          public ConfigMap create(ConfigMap item) {
            return null;
          }

          @Override
          public Informable<ConfigMap> withIndexers(Map<String, Function<ConfigMap, List<String>>> indexers) {
            return null;
          }

          @Override
          public SharedIndexInformer<ConfigMap> inform(ResourceEventHandler<? super ConfigMap> handler, long resync) {
            return null;
          }

          @Override
          public SharedIndexInformer<ConfigMap> runnableInformer(long resync) {
            return null;
          }

          @Override
          public CompletableFuture<List<ConfigMap>> informOnCondition(Predicate<List<ConfigMap>> condition) {
            return null;
          }

          @Override
          public ReplaceDeletable<ConfigMap> lockResourceVersion(String resourceVersion) {
            return null;
          }

          @Override
          public ConfigMap patch(PatchContext patchContext, ConfigMap item) {
            return null;
          }

          @Override
          public ConfigMap patch(PatchContext patchContext, String patch) {
            return null;
          }

          @Override
          public boolean isReady() {
            return false;
          }

          @Override
          public ConfigMap replace(ConfigMap item) {
            return null;
          }

          @Override
          public ConfigMap require() throws ResourceNotFoundException {
            return null;
          }

          @Override
          public ConfigMap editStatus(UnaryOperator<ConfigMap> function) {
            return null;
          }

          @Override
          public ConfigMap patchStatus(ConfigMap item) {
            return null;
          }

          @Override
          public ConfigMap replaceStatus(ConfigMap item) {
            return null;
          }

          @Override
          public ConfigMap updateStatus(ConfigMap item) {
            return null;
          }

          @Override
          public WatchAndWaitable<ConfigMap> withResourceVersion(String resourceVersion) {
            return null;
          }

          @Override
          public ConfigMap waitUntilReady(long amount, TimeUnit timeUnit) {
            return null;
          }

          @Override
          public ConfigMap waitUntilCondition(Predicate<ConfigMap> condition, long amount, TimeUnit timeUnit) {
            return null;
          }

          @Override
          public Waitable<ConfigMap, ConfigMap> withWaitRetryBackoff(long initialBackoff, TimeUnit backoffUnit, double backoffMultiplier) {
            return null;
          }

          @Override
          public Watch watch(Watcher<ConfigMap> watcher) {
            return null;
          }

          @Override
          public Watch watch(ListOptions options, Watcher<ConfigMap> watcher) {
            return null;
          }

          @Override
          public Watch watch(String resourceVersion, Watcher<ConfigMap> watcher) {
            return null;
          }
        };
      }

      @Override
      public NonNamespaceOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> inNamespace(String name) {
        return null;
      }

      @Override
      public ConfigMap patch(PatchContext patchContext, ConfigMap item) {
        return null;
      }

      @Override
      public ConfigMap patch(PatchContext patchContext, String patch) {
        return null;
      }

      @Override
      public ConfigMap replace(ConfigMap item) {
        return null;
      }

      @Override
      public ConfigMap editStatus(UnaryOperator<ConfigMap> function) {
        return null;
      }

      @Override
      public ConfigMap patchStatus(ConfigMap item) {
        return null;
      }

      @Override
      public ConfigMap replaceStatus(ConfigMap item) {
        return null;
      }

      @Override
      public ConfigMap updateStatus(ConfigMap item) {
        return null;
      }

      @Override
      public WatchAndWaitable<ConfigMap> withResourceVersion(String resourceVersion) {
        return null;
      }

      @Override
      public Watch watch(Watcher<ConfigMap> watcher) {
        return null;
      }

      @Override
      public Watch watch(ListOptions options, Watcher<ConfigMap> watcher) {
        return null;
      }

      @Override
      public Watch watch(String resourceVersion, Watcher<ConfigMap> watcher) {
        return null;
      }
    };
  }

  @Override
  public MixedOperation<LimitRange, LimitRangeList, Resource<LimitRange>> limitRanges() {
    return null;
  }

  @Override
  public AuthorizationAPIGroupDSL authorization() {
    return null;
  }

  @Override
  public InOutCreateable<TokenReview, TokenReview> tokenReviews() {
    return null;
  }

  @Override
  public SharedInformerFactory informers() {
    return null;
  }

  @Override
  public SharedInformerFactory informers(ExecutorService executorService) {
    return null;
  }

  @Override
  public <C extends Namespaceable<C> & KubernetesClient> LeaderElectorBuilder<C> leaderElector() {
    return null;
  }

  @Override
  public MixedOperation<Lease, LeaseList, Resource<Lease>> leases() {
    return null;
  }

  @Override
  public V1APIGroupDSL v1() {
    return null;
  }

  @Override
  public RunOperations run() {
    return null;
  }

  @Override
  public NonNamespaceOperation<RuntimeClass, RuntimeClassList, Resource<RuntimeClass>> runtimeClasses() {
    return null;
  }

  @Override
  public <C> Boolean isAdaptable(Class<C> type) {
    return null;
  }

  @Override
  public <C> C adapt(Class<C> type) {
    return null;
  }

  @Override
  public URL getMasterUrl() {
    return null;
  }

  @Override
  public String getApiVersion() {
    return null;
  }

  @Override
  public String getNamespace() {
    return null;
  }

  @Override
  public RootPaths rootPaths() {
    return null;
  }

  @Override
  public boolean supportsApiPath(String path) {
    return false;
  }

  @Override
  public void close() {

  }

  @Override
  public APIGroupList getApiGroups() {
    return null;
  }

  @Override
  public APIGroup getApiGroup(String name) {
    return null;
  }

  @Override
  public APIResourceList getApiResources(String groupVersion) {
    return null;
  }

  @Override
  public Config getConfiguration() {
    return null;
  }

  @Override
  public NamespacedKubernetesClient inAnyNamespace() {
    return null;
  }

  @Override
  public NamespacedKubernetesClient inNamespace(String name) {
    return null;
  }

  @Override
  public FunctionCallable<NamespacedKubernetesClient> withRequestConfig(RequestConfig requestConfig) {
    return null;
  }
}
