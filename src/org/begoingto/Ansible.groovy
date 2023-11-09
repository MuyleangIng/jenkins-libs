package org.begoingto

class Ansible {
    def steps
    def script

    Ansible(steps,script) {
        this.steps = steps
        this.script = script
    }


    def exitst() {
        
        try {
            // Try to execute `ansible --version`
            def output = steps.sh(script: "ansible --version", returnStdout: true).trim()
            steps.echo "Ansible is available: \n${output}"
            return true
        } catch (Exception e) {
            // If an error occurs, it means Ansible is not installed or not in PATH
            steps.echo "Ansible is not available: \n${e.message}"
            return false
        }
    }


    def testScript() {
        def s_domain = steps.libraryResource('ansible/setup.sh')
        
        steps.writeFile(file: 'set_domain.sh', text: s_domain)
        steps.sh 'chmod +x set_domain.sh'
        steps.sh './set_domain.sh'
    }


    def ansiblePlaybook(String hostIp,
                        String hostname,
                        String hostuser,
                        String registry_name,
                        String imageName, 
                        String tag, 
                        String credentialsId
        ){
        steps.withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
            // def image = getImage(registry_name,imageName,tag)

            // write ansible hosts
            writeAnsibleHosts(hostIp, hostname, hostuser)

            // execute ansible playbook
            anislbeShExecute(host,registry_name,imageName,tag)
            
        }

    }


    private ansibleShExecute(String registry_name, String imageName,String tag) {
        def playbook = steps.libraryResource('ansible/playbook.yml')
        steps.writeFile(file: 'playbook.yml', text: playbook)

        steps.sh """
        ansible-playbook -i hosts.ini playbook.yml -e 'image_name=${imageName}' \ 
        -e 'image_tag=${tag}' \
        -e 'registry_username=${USERNAME}' \
        -e 'registry_password=${PASSWORD}' \
        -e 'registry_url=${registry_name}' \
        -e 'container_name=${imageName}' \
        -e 'port_expose=3000' \
        -e 'port_out=3100'
        """
    }

    private ansiblePluginExecute(String registry_name,
                        String imageName, 
                        String tag
        ) {
        steps.ansiblePlaybook(becomeUser: null, 
                inventory: '/mnt/d/istad/_devops/ansible/deployment/inventories/hosts.ini', 
                playbook: '/mnt/d/istad/_devops/ansible/deployment/playbooks/container/setup.yml', 
                sudoUser: null, 
                vaultCredentialsId: 'container-setup',
                extraVars: [
                    "registry_username": "${USERNAME}",
                    "registry_password": "${PASSWORD}",
                    "container_name": "${imageName}",
                    "registry_url": "${registry_name}",
                    "image_name": "${imageName}",
                    "port_expose": "${port_expose}"
                ])
    }

    
    private String getImage(String registry_name,String imageName, String tag) {
        return "${registry_name}/${imageName}:${tag}"
    }

    private writeAnsibleHosts(String hostIp, String hostname, String hostuser) {
        def hostsContent = """
        [all]
        ${hostname} anislbe_host=${hostIp}
        [all:vars]
        ansible_user=${hostuser}
        ansible_host_key_checking=False
        """
        steps.writeFile(file: 'hosts.ini', text: hostsContent)
    }
}