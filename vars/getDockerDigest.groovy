#!groovy

def call(String secret, Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    stage('Retrieve Docker Digest') {
        openshift.withCluster( config.openShiftUri, secret ) {
		    openshift.withProject( config.openShiftProject ) {
        	    def istagobj = openshift.selector( "istag/${config.imageName}:${config.imageTag}" ).object()
        	    return istagobj.image.metadata.name
            }
   	    }
    }
}
