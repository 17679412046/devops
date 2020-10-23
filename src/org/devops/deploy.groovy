package org.devops 


//saltstack
def SaltDeploy(hosts,func){
    sh " salt \"${hosts}\" ${func} "
}


//ansible

def AnsibleDeploy(devloyYml){
    sh " ansible-playbook ${RelativeTargetDirectory}/ansible-playbook/${devloyYml}"
    
}
