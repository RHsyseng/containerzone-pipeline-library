#!groovy

package com.redhat.connect

import groovy.json.*

def getDockerCfgPassword(String dockerCfg) {
    
    JsonSlurperClassic parser = new JsonSlurperClassic()
    HashMap dockerCfgMap = (HashMap)parser.parseText(new String(dockerCfg.decodeBase64()))
    parser = null

    Set keys = dockerCfgMap.keySet()
    Integer size = (Integer) keys.size()

    if(size != 1) {
        throw new Exception("dockerCfgMap keySet should only be a size of one (1) and is ${size}")
    }

    return dockerCfgMap[keys[0]].password
}
