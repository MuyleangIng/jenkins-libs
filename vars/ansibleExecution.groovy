import org.begoingto.Ansible

def call() {
    def ansible = new Ansible(steps,this)
    
    ansible.testScript()
    def exist = ansible.exitst()
    echo "Ansible is available: ${exist}"
}