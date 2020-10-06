package io.fabric8.demo.tekton;

import io.fabric8.tekton.client.DefaultTektonClient;
import io.fabric8.tekton.client.TektonClient;

public class JKubeOpenShiftPipelineDeployer {
    private static final String NAMESPACE = "default";

    public static void main(String[] args) {
        try (TektonClient tkn = new DefaultTektonClient()) {
            // Create Eclipse JKube OpenShift Task
            tkn.v1beta1().tasks().inNamespace(NAMESPACE).createOrReplaceWithNew()
                    .withNewMetadata().withName("mvn-openshift").endMetadata()
                    .withNewSpec()
                        .addNewStep()
                            .withName("eclipse-jkube-deploy")
                            .withImage("gcr.io/cloud-builders/mvn")
                            .withWorkingDir("/workspace/source")
                            .withCommand("/usr/bin/mvn", "package", "org.eclipse.jkube:openshift-maven-plugin:build", "org.eclipse.jkube:openshift-maven-plugin:resource", "org.eclipse.jkube:openshift-maven-plugin:deploy")
                        .endStep()
                        .withNewResources()
                            .addNewInput()
                            .withName("source")
                            .withType("git")
                            .endInput()
                        .endResources()
                    .endSpec()
                    .done();

            // Create Eclipse JKube OpenShift Pipeline
            tkn.v1beta1().pipelines().inNamespace(NAMESPACE).createOrReplaceWithNew()
                    .withNewMetadata().withName("jkube-openshift-deploy-pipeline").endMetadata()
                    .withNewSpec()
                        .addNewResource()
                            .withName("app-git")
                            .withType("git")
                        .endResource()
                        .addNewTask()
                            .withName("build")
                            .withNewTaskRef().withName("mvn-openshift").endTaskRef()
                            .withNewResources()
                                .addNewInput()
                                .withName("source")
                                .withResource("app-git")
                                .endInput()
                            .endResources()
                        .endTask()
                    .endSpec()
                    .done();

        }
    }
}