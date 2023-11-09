package org.begoingto

class Notification {
    def steps
    def script

    Notification(steps, script) {
        this.steps = steps
        this.script = script
    }

    def sendTelegram(String message) {
        steps.echo "-------- Start Send Telegram Message --------"
        steps.sh """
        curl -s -X POST https://api.telegram.org/bot${script.env.BOT_ID}:${script.env.BOT_TOKEN}/sendMessage -d chat_id=${script.env.CHAT_ID} -d text="${message}"
        """
        steps.echo "-------- End Send Telegram Message --------"
    }

    def sendMail(Map params) {
        steps.echo "-------- Start Send Mail Message --------"
        script.mail(bcc: '', 
            body: "${params.message}", 
            cc: '', 
            from: script.env.MAIL_FROM, 
            replyTo: '', 
            subject: "${params.subject}", 
            to: "${params.to}"
        )
        steps.echo "-------- End Send Mail Message --------"
    }
}