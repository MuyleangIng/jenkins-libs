import org.begoingto.Notification
import org.begoingto.Ansible

def call(Map params){
    try {
        withCredentials([usernamePassword(credentialsId: params.registryCredentialsId, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
            def ansible = new Ansible(steps,this, USERNAME, PASSWORD)

            ansible.ansiblePlaybook(
                hostIp: env.HOST_IP,
                hostname: env.HOST_NAME,
                hostuser: env.HOST_USER,
                registryName: "${params.registryName}",
                imageName: "${params.imageName}",
                tag: "${params.imageTag}",
                portExpose: "${params.exposePort}",
                portOut: "${params.targetPort}"
            )

            echo "--------------------Setup Domain Name-------------------------"
            ansible.setupDomainName(domainName: "${params.domainName}", targetPort: "${params.targetPort}")
            echo "--------------------End Setup Domain Name-------------------------"
        }

    }catch (Exception e) {
        def notify = new Notification(steps, this)
        notify.sendTelegram("Deploy failed⛔(<:>) Error: ${e.getMessage()}")
        echo "Build failed⛔(<:>) Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
    }
}