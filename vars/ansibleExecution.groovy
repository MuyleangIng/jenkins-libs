import org.begoingto.Ansible

def call(Map params) {
    def username = 'default'
    def password = 'default'

    withCredentials([usernamePassword(credentialsId: params.credentialsId, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
        steps.echo "registry Name: ${USERNAME}"
        username = USERNAME
        password = PASSWORD
    }

    return new Ansible(steps,this, username, password)
}