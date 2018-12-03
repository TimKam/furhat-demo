package furhatos.app.travel_agent

import com.eclipsesource.json.Json
import furhatos.app.travel_agent.flow.*
import furhatos.app.travel_agent.nlu.getSchedule
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import furhatos.nlu.common.Time
import java.awt.PageAttributes.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.Response
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import okhttp3.MediaType as OkMediaType


val client = OkHttpClient()

class Travel_agentSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}


fun main(args: Array<String>) {
    // "https://reseplanerare.fskab.se/umea/v2/rpajax.aspx?net=UMEA&lang=se&letters=address
    //getSchedule("Nydalasjön 2", "Umeå företagspark 12", "2018-12-03", "18:15")
    //getSchedule("Universum", "Vasaplan", "2018-12-03", "18:15")
    Skill.main(args)
}