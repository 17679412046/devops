package org.devops

//封装HTTP
def HttpReq(reqType,reqUrl,reqBody,sonarServer,sonarUser){
    //def sonarServer = "http://192.168.208.251:30090/api"
   
    result = httpRequest authentication: "${sonarUser}", 
            consoleLogResponseBody: true, 
            contentType: 'APPLICATION_JSON', 
            httpMode: "${reqType}", 
            ignoreSslErrors: true, 
            //quiet: true, 
            requestBody: "${reqBody}", 
            url: "${sonarServer}/api/${reqUrl}"
    
    return result
}

//获取Sonar质量阈状态
def GetProjectStatus(projectName,sonarServer,sonarUser){
    apiUrl = "project_branches/list?project=${projectName}"
    response = HttpReq("GET",apiUrl,'',sonarServer,sonarUser)
    
    response = readJSON text: """${response.content}"""
    result = response["branches"][0]["status"]["qualityGateStatus"]
    
    return result
}

//搜索Sonar项目是否存在
def SearchProject(projectName,sonarServer,sonarUser){
    apiUrl = "projects/search?projects=${projectName}"
    response = HttpReq("GET",apiUrl,'',sonarServer,sonarUser)
    
    response = readJSON text: """${response.content}"""
    result = response["paging"]["total"]
    if (result.toString() == "0" ){
        println("${projectName}项目不存在")
        return "false"
    } else {
        println("${projectName} 项目存在")
        return "true"
    }
}

//创建sonra项目
def CreateProject(projectName,sonarServer,sonarUser){
    
    result = SearchProject(projectName,sonarServer,sonarUser)
    println(result)
    if (result == "false" ){
        println("准备创建项目--->${projectName}")
        apiUrl = "projects/create?name=${projectName}&project=${projectName}"
        response = HttpReq("POST",apiUrl,'',sonarServer,sonarUser)
    }
}

//配置项目质量规则
def ConfigQualityProfiles(projectName,lang,qpname,sonarServer,sonarUser){
    apiUrl = "qualityprofiles/add_project?language=${lang}&project=${projectName}&qualityProfile=${qpname}"
    response = HttpReq("POST",apiUrl,'',sonarServer,sonarUser)
}


//获取质量阈ID
def GetQualtyGateId(gateName,sonarServer,sonarUser){
    apiUrl= "qualitygates/show?name=${gateName}"
    response = HttpReq("GET",apiUrl,'',sonarServer,sonarUser)
    response = readJSON text: """${response.content}"""
    result = response["id"]
    
    return result
}

//配置项目质量阈

def ConfigQualityGates(projectName,gateName,sonarServer,sonarUser){
    gateId = GetQualtyGateId(gateName,sonarServer,sonarUser)
    apiUrl = "qualitygates/select?gateId=${gateId}&projectKey=${projectName}"
    response = HttpReq("POST",apiUrl,'',sonarServer,sonarUser)
    println(response)println(response)
}

//sonarqube扫描
def  SonarScan(sonarType,projectName,projectDesc,projectPath){
    
     // 定义服务器列表
     def servers = ["test":"sonarqube-test","prod":"sonarqube-prod"]
     withSonarQubeEnv("${servers[sonarType]}"){
    
     def sonarDate = sh returnStdout: true, script: 'date +%Y%m%d%H%M%S'
     sonarDate = sonarDate - "\n"
     
     sh """
       cd ${RelativeTargetDirectory}; \
       /usr/local/sonar-scanner/bin/sonar-scanner  -Dsonar.projectKey=${projectName} \
       -Dsonar.projectName="${projectName}"  \
       -Dsonar.projectVersion=${sonarDate} \
       -Dsonar.ws.timeout=30 \
       -Dsonar.projectDescription=${projectDesc}  \
       -Dsonar.links.homepage=http://www.baidu.com \
       -Dsonar.sources=${projectPath} \
       -Dsonar.sourceEncoding=UTF-8 \
       -Dsonar.java.binaries=target/classes \
       -Dsonar.java.test.binaries=target/test-classes \
       -Dsonar.java.surefire.report=target/surefire-reports
       
     """
}
        /* def qg = waitForQualityGate()
        if (qg.status != 'OK') {
            error "Pipeline aborted due to quality gate failure: ${qg.status}" 
    } */
}

//获取扫描结果
def sonarqResult(){
    sleep 20
    result = GetProjectStatus(projectName,sonarServer,sonarUser)
    if (result.toString()== "ERROR" ){
        error "代码质量阈错误，请及时修复！！！"
    }else {
         println(result)
    }
}
