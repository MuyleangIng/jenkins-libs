package org.begoingto

class Notification {
    def steps
    def script

    Notification(steps, script) {
        this.steps = steps
        this.script = script
    }

    def sendTelegram(Map params) {
        steps.echo "-------- Start Send Telegram Message --------"
        steps.sh """
        curl -s -X POST https://api.telegram.org/bot${params.botId}:${params.botToken}/sendMessage -d chat_id=${params.chatId} -d text="${params.message}"
        """
        steps.echo "-------- End Send Telegram Message --------"
    }

    def sendMail(Map params) {
        steps.echo "-------- Start Send Mail Message --------"
        script.mail(bcc: '', 
            body: "${params.message}", 
            cc: '', 
            from: "${params.from}", 
            replyTo: '', 
            subject: "${params.subject}", 
            to: "${params.to}"
        )
        steps.echo "-------- End Send Mail Message --------"
    }
}