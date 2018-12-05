package furhatos.app.travel_agent

import furhatos.app.travel_agent.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import okhttp3.MediaType as OkMediaType



class Travel_agentSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}


fun main(args: Array<String>) {
    // "https://reseplanerare.fskab.se/umea/v2/rpajax.aspx?net=UMEA&lang=se&letters=address
    //getSchedule("Nydalasjön 2", "Umeå företagspark 12", "2018-12-03", "18:15")
    //getSchedule("Airport", "Hoppets Gränd 30A", "2018-12-05", "18:00")
    Skill.main(args)
}