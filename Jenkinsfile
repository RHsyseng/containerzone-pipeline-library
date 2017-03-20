#!groovy

@Library('ContainerZone')
import com.redhat.connect.*

node {
    stage('checkout') {
        checkout scm
    }

    dockerBuildPush {
        credentialsId = "container-zone"
        contextDir = "examples/docker"
        imageName = "czone"
        imageTag = "latest"
    }

    def dockerDigest = getDockerDigest {
        openShiftUri = "insecure://api.rhc4tp.openshift.com"
        imageName = "czone"
        imageTag = "latest"
    }

    containerZoneScan([credentialsId: 'container-zone', dockerDigest: dockerDigest])
}
