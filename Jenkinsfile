@Library('ContainerZone')
import com.redhat.connect.*

node {
  def cz = null
  def image = null
  def digest = ""
  stage('checkout') {
    checkout scm 
  }
  
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'container-zone', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
    docker.withRegistry('https://registry.rhc4tp.openshift.com', 'container-zone') {
      stage('Build Docker Image') {
        image = docker.build("${env.USERNAME}/usejenkins:latest") 
      }
      stage('Push Image') {
        image.push()
      }
      stage('Digest and Container Zone') {
        openshift.withCluster( "insecure://api.rhc4tp.openshift.com", "${env.PASSWORD}" ) {
          openshift.withProject( "${env.USERNAME}" ) {
            def istagobj = openshift.selector( "istag/usejenkins:latest" ).object()
            cz = new com.redhat.connect.ContainerZone(env.USERNAME, env.PASSWORD, istagobj.image.metadata.name)
          }
        }
      }
      stage('Wait for Scan') {
         cz.waitForScan()
      }
      stage('Scan Results') {
        def scanResults = cz.getScanResults()
        wrap([$class: 'AnsiColorBuildWrapper']) {
          print(scanResults.output)
        }
        if( !(scanResults.success) ) {
          error("Certification Scan Failed")
        }
      }
    }
  }
}
