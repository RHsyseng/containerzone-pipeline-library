#!groovy

import com.redhat.connect.ContainerZone

def call(String dockerCfg, String dockerDigest) {
    def containerZone = new com.redhat.connect.ContainerZone(dockerCfg)
    containerZone.setDockerImageDigest(dockerDigest)

    stage('Scanning') {
      containerZone.waitForScan(20, 30)
    }
    stage('Scan Results') {
      def scanResults = containerZone.getScanResults()
      wrap([$class: 'AnsiColorBuildWrapper']) {
          print(scanResults.output)
      }
      if( !(scanResults.success) ) {
          error("Certification Scan Failed")
      }
    }
}
