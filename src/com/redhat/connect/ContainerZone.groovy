#!/usr/bin/groovy
/**
 * ContainerZone.groovy
 */
package com.redhat.connect


@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.2')

import groovy.json.*
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*

/**
 * TODO: Add exception handling
 */
class ContainerZone implements Serializable {

    private String projectId
    private String secret
    // TODO: is this variable now needed?
    private String imageName
    private String dockerImageDigest

    /**
     * Static variables below are to print ASCII characters
     * and colors to make the output easier to read.
     */
    private static final String CHECK = "\u2713"
    private static final String X = "\u274C"
    private static final String ANSI_RESET = "\u001B[0m"
    private static final String ANSI_BLACK = "\u001B[30m"
    private static final String ANSI_RED = "\u001B[31m"
    private static final String ANSI_GREEN = "\u001B[32m"

    /**
     * Constructor
     * @param imageName
     * @param projectId
     * @param secret
     */
    ContainerZone(projectId, secret, dockerImageDigest) {
        this.projectId = projectId
        this.secret = secret
        // this.imageName = imageName
        this.dockerImageDigest = dockerImageDigest
    }
    /**
    * Getter / Setter bug must use @
    * https://issues.jenkins-ci.org/browse/JENKINS-31484
    */
    def setProjectId(value) {
        this.@projectId = value
    }
    def setImageName(value) {
        this.@imageName = value
    }
    def setSecret(value) {
        this.@secret = value
    }
    def setDockerImageDigest(value) {
        this.@dockerImageDigest = value
    }
    String getProjectId() { return this.@projectId }
    String getSecret() { return  this.@secret }
    String getImageName() { return this.@imageName }
    String getDockerImageDigest() { return this.@dockerImageDigest }

    /**
     * POST to uri, retrieves the contents, parses to HashMap.
     *
     * TODO: Add check for any HTTP code !200
     * https://github.com/codetojoy/talk_maritimedevcon_groovy/blob/master/exampleB_REST_Client/v2_groovy/RESTClient.groovy
     * http://stackoverflow.com/questions/37864542/jenkins-pipeline-notserializableexception-groovy-json-internal-lazymap
     * https://issues.jenkins-ci.org/browse/JENKINS-37629
     * @param uri
     * @param json
     * @return HashMap
     */
    private static final HashMap getResponseMap(String uri, String jsonString) {

        CloseableHttpClient client = HttpClientBuilder.create().build()
        HttpPost httpPost = new HttpPost(uri)
        httpPost.addHeader("content-type", "application/json")
        httpPost.setEntity(new StringEntity(jsonString))

        CloseableHttpResponse response = client.execute(httpPost)

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
        String jsonResponse = bufferedReader.getText()
        // NOTE: The JsonSluperClassic must be used vs JsonSlurper since it returns a LazyMap which is not serializable.
        JsonSlurperClassic parser = new JsonSlurperClassic()
        HashMap resultMap = (HashMap) parser.parseText(jsonResponse)
        parser = null

        return resultMap
    }

    /**
     * The API is not functional
     * TODO: This method is not functional
     * @param timeout
     * @param retry
     * @return boolean
     */
    public boolean waitForScan(int timeout=10, int retry=30) {
        String uri = "https://stage-connect.redhat.com/api/container/scanResults"
        long timeoutMilliseconds = (long)(timeout * 60 * 1000)
        int retries = (timeout * 60) / retry
        String jsonString = """
        {
            "secret": "${this.secret}",
            "pid": "${this.projectId}",            
            "docker_image_digest": "${this.dockerImageDigest}"
        }
        """
        println(jsonString)

        for (int i = 0; i < retries; i++) {
            HashMap resultMap = getResponseMap(uri, jsonString)
            int size = resultMap.size()

            println("resultMap.size(): ${size}")

            /*
            if( !resultMap.isEmpty() ) {
                return true
            }
            */

            sleep((long)(timeoutMilliseconds/retries))
        }

        return false
    }

    /**
     * getScanResults iterates through assessment list creating easier to read output.
     *
     * http://stackoverflow.com/questions/36636017/jenkins-groovy-how-to-call-methods-from-noncps-method-without-ending-pipeline
     * @return String
     */
    public String getScanResults() {
        println("in getScanResults")
        String uri = "https://stage-connect.redhat.com/api/container/scanResults"
        String output = ""
        String jsonString = """
        { 
            "secret": "${this.secret}",
            "pid": "${this.projectId}",
            "docker_image_digest": "${this.dockerImageDigest}"
        }
        """
        println(jsonString)
        HashMap resultMap = this.getResponseMap(uri, jsonString)

        // TODO: Determine the right output and error if not successful

        if (!(boolean)resultMap["certifications"][0]["Successful"]) {
            def assessments = resultMap["certifications"][0]["assessment"]

            for(int i = 0; i < (int) assessments.size(); i++) {
                HashMap assessment = (HashMap)assessments[i]

                /* Clean up of the assessment name
                 * Remove the underscore, "exists" and capitalize
                 */
                String name = assessment.name.replaceAll('_', ' ').minus(" exists").capitalize()

                if ((boolean)assessment["value"]) {
                    output += "${this.ANSI_GREEN} ${this.CHECK} ${name} ${this.ANSI_RESET}\n"
                } else {
                    output += "${this.ANSI_RED} ${this.X} ${name} ${this.ANSI_RESET}\n"
                }
            }
        }
        println(output)
        return output
    }


    /**
     * main is only used for local testing outside of Jenkins
     * will probably be removed at some point.
     */
    /*
    static void main(String[] args) {
        def secret = ""
        def projectid = "p1966151495b64a79545ff0637c5839b01d1d8d717e"

        ContainerZone cz = new ContainerZone("foo",projectid, secret)
        cz.setDockerImageDigest("sha256:a372ddd5d52d5e28c05bd718388f71778c8ec8b935c3dbbe10c9f3747283eff6")
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(cz)
        println(cz.getScanResults())

    }
    */

}
