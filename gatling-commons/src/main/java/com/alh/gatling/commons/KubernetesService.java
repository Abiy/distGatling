package com.alh.gatling.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentCondition;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;


@Singleton
public class KubernetesService {

    private static final Logger log = LoggerFactory.getLogger(KubernetesService.class);
    public String namespace;
    private ApiClient apiClient;
    private AppsV1Api appsApi;
    private int replicasCount;


    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public KubernetesService(String namespace) {
        // create Kubernetes client
        try {
            this.apiClient = Config.defaultClient();
            Configuration.setDefaultApiClient(apiClient);
            apiClient.setDebugging(true);
        } catch (IOException e) {
            log.warn("Can't generate client for kubernetes: {}", e);
        }

        this.appsApi = new AppsV1Api(apiClient);
        this.namespace = namespace;
        this.replicasCount = 1;
    }

    /**
     * Generate name for a deployment based on job id
     */
    private static String deploymentNameFor(String jobId) {
        return String.format("gatling-worker.%s", jobId);
    }

    /**
     * Get information from the yaml file for building a deployment
     *
     * @return deployment object
     */
    private V1Deployment readDeploymentFile() {
        V1Deployment deployment;
        try {
            deployment = mapper
                .readerFor(V1Deployment.class)
                .readValue(Objects.requireNonNull(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("worker-pod.yaml")));
        } catch (IOException e) {
            log.warn("Can't read the yaml file: {}", e);
            return null;
        }
        return deployment;
    }

    /**
     * Create a deployment
     *
     * @return deployment name
     */
    public String createDeploy(String jobId) {
        V1Deployment deployment = readDeploymentFile();

        deployment.getSpec().setReplicas(this.replicasCount);
        deployment.getMetadata().setName(deploymentNameFor(jobId));
        deployment.getMetadata().setNamespace(this.namespace);

        V1Deployment newDeployment;
        try {
            newDeployment = appsApi.createNamespacedDeployment(this.namespace, deployment, null, null, null);
        } catch (ApiException e) {
            log.warn("Can't create deployment for workId={}", jobId);
            return null;
        }

        return newDeployment.getMetadata().getName();

    }


    /**
     * Create a watch for a deployment
     *
     * @param name name of the deployment to watch after
     * @return a watch for the deployemnt
     */
    private Watch<V1Deployment> createDeploymentWatch(String name) throws ApiException {
        Call call = appsApi.listNamespacedDeploymentCall(
            this.namespace, null, null, null, null,
            null, null, null, 360,
            true, null, null);
        Type watchType = new TypeToken<Watch.Response<V1Deployment>>() {
        }.getType();

        return Watch.createWatch(apiClient, call, watchType);
    }

    /**
     * Wait for replicas to be ready
     *
     * @param deployment       deployment object
     * @param expectedReplicas number of replicas
     * @param expectedName     expected name of the deployment
     * @return true if all replicas are ready, false otherwise
     */
    private boolean replicasReady(V1Deployment deployment, Integer expectedReplicas, String expectedName) {
        String deploymentName = deployment.getMetadata().getName();
        Integer readyReplicas = deployment.getStatus().getReadyReplicas();
        log.info("Deployment {} - {}/{} replicas are ready", deploymentName,
                 readyReplicas, expectedReplicas);
        if (!expectedName.equals(deploymentName)) {
            log.info("Deployment {} - watch event has different deployment name {}", expectedName, deploymentName);
            return false;
        }
        List<V1DeploymentCondition> conditionList = deployment.getStatus().getConditions();
        if (conditionList == null) {
            log.warn("Deployment {} - watch event has null conditions null", deploymentName);
            return false;
        }
        boolean readyReplicasCondition = readyReplicas != null && readyReplicas.equals(expectedReplicas);
        boolean availableCondition = conditionList.stream()
            .anyMatch(condition -> "Available".equals(condition.getType()) && "True".equals(condition.getStatus()));
        if (availableCondition && readyReplicasCondition) {
            log.info("Deployment {} - {}/{} has all replicas ready", deploymentName,
                     readyReplicas, expectedReplicas);
            return true;
        }
        return false;
    }

    /**
     * Wait after a deployment
     *
     * @param name             deployment name
     * @param expectedReplicas number of replicas
     */
    public void waitUntilDeploymentIsReady(String name, int expectedReplicas) {
        try (Watch<V1Deployment> watch = createDeploymentWatch(name)) {
            for (Watch.Response<V1Deployment> item : watch) {
                V1Deployment deployment = item.object;
                if (deployment != null && deployment.getMetadata() != null && deployment.getStatus() != null) {
                    if (replicasReady(deployment, expectedReplicas, name)) {
                        break;
                    }
                } else {
                    log.warn("Null watch event for deployment {}, {}", name, deployment);
                }
            }
        } catch (ApiException | IOException e) {
            log.warn("Failed to watch after deployment {}", name);
        }
    }

    /**
     * Delete deployment
     *
     * @param jobId job for which the deployment have to be deleted
     * @return status of the operation
     */
    public V1Status deleteDeployment(String jobId) {
        try {
            String deploymentName = deploymentNameFor(jobId);
            return appsApi
                .deleteNamespacedDeployment(deploymentName, this.namespace, new V1DeleteOptions(), "true", null, null,
                                            null,
                                            null);
        } catch (ApiException e) {
            log.warn("Failed to call AppsV1Api#createNamespacedDeployment");
            return null;
        }
    }
}


