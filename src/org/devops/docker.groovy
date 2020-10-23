package org.devops

//封装docker构建镜像上传方法

def dockerBuild(dockerRegistryUrl,projectName,branchName){
    def add_tag= sh(script: "date +'%Y%m%d%H%M%S'", returnStdout: true).trim()
    
    //构建镜像
    sh "docker build  . -t ${dockerRegistryUrl}/${projectName}:${branchName}_${add_tag}"
    
    //上传镜像
    sh "docker push ${dockerRegistryUrl}/${projectName}:${branchName}_${add_tag}"

}

//jar/war包重命名
def renamePackage(targetPath,packageType){
    
    //获取jar/war包名字
     def jarName = sh returnStdout: true, script: "cd ${targetPath};ls *.${packageType}"
     env.jarname = jarName - "\n"
     
     //生成新jar/war包名字
     ef add_tag= sh(script: "date +'%Y%m%d%H%M%S'", returnStdout: true).trim()
     jaroldName = jarname.split("\\.jar")[0].toString()
     env.newJarName = "${jaroldName}-${add_tag}.${packageType}"
     println("${jarName}")
     println("${newJarName}")

     //重命名
     sh " cd ${targetPath} && mv ${jarname} ${newJarName} "
}
