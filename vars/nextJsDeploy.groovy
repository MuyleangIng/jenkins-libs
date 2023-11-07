import org.begoingto.NextJsDeployer

def call(Map params){
    def nodeVersion = params.nodeJSVersion ?: 'nodejs-18.12.1'
    def nextJsDeployer = new NextJsDeployer(steps, this, nodeVersion)
    nextJsDeployer.deploy(params.sourceUrl, params.branch)
}