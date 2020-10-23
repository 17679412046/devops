package org.devops

//构建类型

def Build(buildType,buildCmd){
    def buildTools = ["mvn":"M3","ant":"ANT","gradle":"GRADLE","npm":"NPM"]
    
    buildHome = tool buildTools[buildType]

    sh "cd ${RelativeTargetDirectory} && ${buildHome}/bin/${buildType} ${buildCmd}"
}
/*
//jar/war包重命名
def renamePackage(targetPath,packageType){
    
    //获取jar/war包名字
     def jarName = sh returnStdout: true, script: "cd ${targetPath};ls *.${packageType}"
     env.jarname = jarName - "\n"
     
     //生成新jar/war包名字
     def add_tag= sh(script: "date +'%Y%m%d%H%M%S'", returnStdout: true).trim()
     jaroldName = jarname.split("\\.jar")[0].toString()
     env.newJarName = "${jaroldName}-${add_tag}.${packageType}"
     println("${jarName}")
     println("${newJarName}")

     //重命名
     sh " cd ${targetPath} && mv ${jarname} ${newJarName} "
}
*/
