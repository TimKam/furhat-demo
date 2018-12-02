package furhatos.app.travel_agent

import com.eclipsesource.json.Json
import furhatos.app.travel_agent.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import java.awt.PageAttributes.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType as OkMediaType



class Travel_agentSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}

fun main(args: Array<String>) {
    val client = OkHttpClient()

    val mediaType = OkMediaType.parse("raw")
    val body = RequestBody.create(mediaType, "inpPointFr_ajax=Ume%E5+%D6stermalmsgatan%7C25340%7C0&inpPointTo_ajax=Ume%E5+Vasaplan%7C25332%7C0&inpPointInterm_ajax=&selRegionFr=741&inpPointFr=&optTypeFr=0&inpPointTo=&optTypeTo=0&inpPointInterm=&selDirection=0&inpTime=21%3A16&inpDate=2018-12-01&optReturn=0&selDirection2=0&inpTime2=00%3A10&inpDate2=2018-12-02&trafficmask=2&selChangeTime=0&selWalkSpeed=0&selPriority=0&cmdAction=search&EU_Spirit=False&TNSource=UMEA&SupportsScript=True&Language=se&VerNo=&Source=querypage_adv&MapParams=")
    val request = Request.Builder()
            .url("https://reseplanerare.fskab.se/umea/v2/querypage_adv.aspx")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Cache-Control", "no-cache")
            .build()

    val response = client.newCall(request).execute()
    val resBody = response.body()?.string()
    println("Response:")
    println(response.message())
    println(response.body()!!.contentType())
    val responseLines = resBody?.lines()
    val schedule = arrayListOf<String>()
    responseLines?.forEachIndexed {
        index, line ->
            println(line)
            if(line.contains("rp-date-header")) {
                val dateLine = line
                        .substring(line.indexOf(">")+1)
                        .substring(0, 10)
                schedule.add(dateLine)
            } else if (line.contains("summary-timeframe")) {
                val timesLine = responseLines[index + 1]
                val startTime = timesLine
                        .substring(timesLine.indexOf(";")+1)
                        .substring(0, 5)
                val endTime = timesLine
                        .substring(timesLine.indexOf("&raquo;")+8)
                        .substring(0, 5)
                schedule.add(startTime)
                schedule.add(endTime)
            } else if(line.contains("summary-heading")) {
                if(line.contains("Restid")) {
                    val travelTimeLine = line
                            .substring(line.indexOf(">Restid")+8)
                            .substring(0, 5)
                    schedule.add(travelTimeLine)
                }
                else {
                    val switchesLine = line
                            .substring(line.indexOf("heading'>")+9)
                            .substring(0, 1)
                    schedule.add(switchesLine)
                }

            }
    }
    println("Schedule:")
    println(schedule)
}
