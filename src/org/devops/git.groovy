package org.devops

//克隆代码
def gitClone(branchName,gitlabUser,srcUrl){
    //取出克隆子目录
    def RelativeTargetDirectory = srcUrl.split("/")[-1].toString()

	RelativeTargetDirectory = RelativeTargetDirectory.split("\\.")[0].toString()
	env.RelativeTargetDirectory = "${RelativeTargetDirectory}"

            
            checkout([$class: 'GitSCM',
           	branches: [[name: branchName]],
           	doGenerateSubmoduleConfigurations: false,
			extensions: [[$class: 'RelativeTargetDirectory', 
            relativeTargetDir: "${RelativeTargetDirectory}"]], 
            submoduleCfg: [], 
           	userRemoteConfigs: [[credentialsId: gitlabUser, 
			url: srcUrl]]])
}
