package org.devops

//获取POM中的坐标
def GetGav(buildPath,targetPath){
    //buildPath--构建路径
    //targetPath--生成jar/war包路径
    
    //上传制品
    sh "cd ${RelativeTargetDirectory}/${buildPath} && ls"
    def pom = readMavenPom file: "${RelativeTargetDirectory}/${buildPath}/pom.xml"
    env.pomVersion = "${pom.version}"
    env.pomArtifact = "${pom.artifactId}"
    env.pomPackaging = "${pom.packaging}"
    env.pomGroupId = "${pom.groupId}"
    
    //需要根据实际情况使用shell匹配，这里默认编译只生成一个jar或者war包
    if ("${pomPackaging}" == "jar"){
        def jarName = sh returnStdout: true, script: "cd ${RelativeTargetDirectory}/${targetPath};ls *.jar"
        env.jarName = jarName - "\n"
    } else {
        def jarName = sh returnStdout: true, script: "cd ${RelativeTargetDirectory}/${targetPath};ls *.war"
        env.jarName = jarName - "\n"
    }
    //打印输出验证
    println("${pomGroupId}-${pomArtifact}-${pomVersion}-${pomPackaging}")
    
    //返回一个列表
    return ["${pomGroupId}","${pomArtifact}","${pomVersion}","${pomPackaging}"]
}



//使用maven原生命令上传制品到nexus制品仓库
def MavenUpload(buildPath,targetPath,nexusRegistry){
    //nexusRegistry--nexus仓库地址,例如：http://nexus.k8s.com
    //nexusRepositoriesName--nexus存储库名，例如：maven-hosts
    
    //执行方法
    GetGav(buildPath,targetPath)
    
    //在jenkins里配置的maven工具名称
    def mvnHome = tool "M3"
    sh  """ 
        cd ${targetPath}/
        ${mvnHome}/bin/mvn deploy:deploy-file -Dmaven.test.skip=true  \
                                -Dfile=${jarName} -DgroupId=${pomGroupId} \
                                -DartifactId=${pomArtifact} -Dversion=${pomVersion}  \
                                -Dpackaging=${pomPackaging} -DrepositoryId=maven-hosts \
                                -Durl=${nexusRegistry}/repository/${nexusRepositoriesName}/
        """
}


//使用nexus插件上传制品到nexus制品仓库，后期需要维护插件升级
def nexusUpload(buildPath,targetPath,nexusUser,nexusRegistry,nexusRepositoriesName){
    //nexusRegistry--nexus仓库地址,例如：http://nexus.k8s.com
    //nexusRepositoriesName--nexus存储库名，例如：maven-hosts
    
    //执行方法
    GetGav(buildPath,targetPath)
    
    //nexus插件上传方法
    nexusArtifactUploader artifacts: [[artifactId: "${pomArtifact}", 
                                    classifier: '', 
                                    file: "${RelativeTargetDirectory}/${targetPath}/${jarName}", 
                                    type: "${pomPackaging}"]], 
                                    credentialsId: "${nexusUser}", 
                                    groupId: "${pomGroupId}", 
                                    nexusUrl: "${nexusRegistry}", 
                                    nexusVersion: 'nexus3', 
                                    protocol: 'http', 
                                    repository: "${nexusRepositoriesName}", 
                                    version: "${pomVersion}"
}

//制品发布
//需要安装Maven Artifact ChoiceListProvider (Nexus)插件


//制品晋级
def ArtifactUpdate(updateType,artifactUrl,nexusUser,nexusRegistry,nexusRepositoriesName,NewNexusRepositoriesName){

    //晋级策略
    if ("${updateType}" == "snapshot -> release"){
        println("snapshot -> release")

        //下载原始制品
        sh "  rm -fr ${RelativeTargetDirectory}/updates && mkdir ${RelativeTargetDirectory}/updates && cd ${RelativeTargetDirectory}/updates; pwd; wget ${artifactUrl}; pwd "

        //获取artifactID 
        
        artifactUrl = artifactUrl -  "http://${nexusRegistry}/repository/${nexusRepositoriesName}/"
        //com/mycompany/app/  my-app/  1.0-SNAPSHOT/  my-app-1.0-20201016.112218-24.jar
        artifactUrl = artifactUrl.split("/").toList()
        //[com/mycompany/app,my-app,1.0-SNAPSHOT,my-app-1.0-20201016.112218-24.jar]
        
        println(artifactUrl.size())
        env.jarName = artifactUrl[-1] 
        // my-app-1.0-20201016.112218-24.jar
        env.pomVersion = artifactUrl[-2].replace("SNAPSHOT","RELEASE")
        // 1.0-RELEASE
        
        env.pomArtifact = artifactUrl[-3]
        // my-app
        pomPackaging = artifactUrl[-1]
        // my-app-1.0-20201016.112218-24.jar
        pomPackaging = pomPackaging.split("\\.").toList()[-1]
        // jar
        env.pomPackaging = pomPackaging[-1]
        // jar
        env.pomGroupId = artifactUrl[0..-4].join(".")
        println("${pomGroupId}##${pomArtifact}##${pomVersion}##${pomPackaging}")
        env.newJarName = "${pomArtifact}-${pomVersion}.${pomPackaging}"
        // my-app-1.0-RELEASE.jar
        
        //更改名称
        sh " cd ${RelativeTargetDirectory}/updates; mv ${jarName} ${newJarName}; pwd; ls "
        
        //上传制品
        //env.repoName = "maven-release"
        //env.filePath = "updates/${newJarName}"
        //env.nexusRepositoriesName = "maven-release"
        //env.targetPath = "updates/${newJarName}"
        //NewNexusRepositoriesName = "maven-release"
        env.targetPath = "updates"

        //这里由于pomVersion变量因素 不能调用制品上传方法。也是一个缺陷问题，后期看看怎么完善
        println("${pomArtifact}")
        println("${RelativeTargetDirectory}")
        println("${targetPath}")
        println("${newJarName}")
        println("${nexusRegistry}")
        println("${NewnexusRepositoriesName}")
        println("${pomVersion}")
        println("${pomGroupId}")
        println("${nexusUser}")
        println("${pomPackaging}")
    }
    
    //nexus插件上传方法
    nexusArtifactUploader artifacts: [[artifactId: "${pomArtifact}", 
                                    classifier: '', 
                                    file: "${RelativeTargetDirectory}/${targetPath}/${newJarName}", 
                                    type: "${pomPackaging}"]], 
                                    credentialsId: "${nexusUser}", 
                                    groupId: "${pomGroupId}", 
                                    nexusUrl: "${nexusRegistry}", 
                                    nexusVersion: 'nexus3', 
                                    protocol: 'http', 
                                    repository: "${NewNexusRepositoriesName}", 
                                    version: "${pomVersion}"

}

