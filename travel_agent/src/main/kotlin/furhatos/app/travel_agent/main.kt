package furhatos.app.travel_agent

import com.sun.xml.internal.fastinfoset.util.StringArray
import furhatos.app.travel_agent.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType as OkMediaType
import java.time.format.DateTimeFormatter
import org.joda.time.Minutes
import org.joda.time.Hours
import org.joda.time.DateTime
import java.text.SimpleDateFormat


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