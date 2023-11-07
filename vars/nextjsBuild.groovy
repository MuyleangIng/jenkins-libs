import org.begoingto.NodeJsDeployer

def call(Map params){
    def nodeVersion = params.nodeVersion ?: 'nodejs-18.12.1'
    def nodeDeployer = new NodeJsDeployer(steps, this, nodeVersion)
    nodeDeployer.deploy(params.source, params.branch, params.projectType)
}