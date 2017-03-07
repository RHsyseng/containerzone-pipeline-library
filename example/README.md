




This is required for the ImageStreamImport

```
oc secrets new-dockercfg container-zone --docker-email='your@email.com' --docker-password="${REGISTRY_KEY}" --docker-username='${PROJECT_ID}' --docker-server='registry'

oc secrets add serviceaccount/default secrets/container-zone --for=pull,mount
```


### Example Pipeline

This pipeline is a work in progress.  It current works but could be simplified.

```
@Library('ContainerZone')
import com.redhat.connect.*


def projectid = ""
def dockerImageDigest = ""
def dockerCfg = ""
def cz = null
def uri = "https://stage-connect.redhat.com/api/container/scanResults"


def imageName = "demo"
def imageTag = "latest"
def externalRegistryImageName = "${imageName}-ex-reg"

node {
  
    stage('get secret') {
      openshift.withCluster() {
        def secret = openshift.selector('secret/container-zone').object()
        dockerCfg = secret.data.'.dockercfg'
      }  
    }
    stage('build') {
        openshiftBuild(buildConfig: "${externalRegistryImageName}", showBuildLogs: 'true')
    }
    stage('create imagestreamtag') {
        cz = new com.redhat.connect.ContainerZone(dockerCfg)
        cz.setImageName(imageName)
        cz.setImageTag(imageTag)
        
        
        openshift.withCluster() {
          
            def imageStreamImport = cz.getImageStreamImport("${externalRegistryImageName}", true)
            def createImportImage = openshift.create( imageStreamImport )
            
            def istagobj = openshift.selector("istag/${externalRegistryImageName}:${imageTag}").object()
            cz.setDockerImageDigest(istagobj.image.metadata.name)
        }
    }
    stage('waitforscan') {
        cz.setUri(uri)
        cz.waitForScan(20, 30)
    }
    stage('scanresults') {
        def output = cz.getScanResults()
        wrap([$class: 'AnsiColorBuildWrapper']) {
          print(output)
        }  
    }
}
```
