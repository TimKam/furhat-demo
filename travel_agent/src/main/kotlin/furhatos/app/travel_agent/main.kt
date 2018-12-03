package furhatos.app.travel_agent

import com.eclipsesource.json.Json
import furhatos.app.travel_agent.flow.*
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

fun getSchedule(startPlace: String, destination: String, startDate: String, startTime: String) {
    val client = OkHttpClient()

    val getStartRequest = Request.Builder()
            .url("https://reseplanerare.fskab.se/umea/v2/rpajax.aspx?net=UMEA&lang=se&letters=$startPlace")
            .get()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Postman-Token", "c692ec64-2f35-4304-b2cc-a9153fe247f2")
            .build()

    val getStartResponseBody = client.newCall(getStartRequest).execute().body()?.string()
    val startPlaceName = getStartResponseBody!!
            .substring(0,getStartResponseBody.indexOf("|"))
    val startPlaceId = getStartResponseBody!!
            .substring(getStartResponseBody.indexOf("|")+1)
            .substring(0, 5)
    println("startPlaceId:")
    println(startPlaceName)
    println(startPlaceId)

    val getDestinationRequest = Request.Builder()
            .url("https://reseplanerare.fskab.se/umea/v2/rpajax.aspx?net=UMEA&lang=se&letters=$destination")
            .get()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Postman-Token", "c692ec64-2f35-4304-b2cc-a9153fe247f2")
            .build()

    val getDestinationResponseBody = client.newCall(getDestinationRequest).execute().body()?.string()
    val destinationName = getDestinationResponseBody!!
            .substring(0,getDestinationResponseBody!!.indexOf("|"))
    var destinationId = getDestinationResponseBody!!
            .substring(getDestinationResponseBody.indexOf("|")+1)
            .substring(0, 5)

    println("destinationId:")
    println(destinationName)
    println(destinationId)


    // Example: D6stermalmsgatan, Vasaplan, 2018-12-01, 21%3A16, (second time: 00%3A10)
    val mediaType = OkMediaType.parse("raw")
    val body = RequestBody.create(mediaType, "selPointFrKey=$startPlaceId&selPointToKey=$destinationId&inpPointFr=$startPlaceName&inpPointFr_ajax=$startPlaceName%7C$startPlaceId%7C0&inpPointTo=$destinationName&inpPointTo_ajax=$destinationName%7C$destinationId%7C0&inpPointInterm_ajax=&selRegionFr=741&optTypeFr=0&optTypeTo=0&inpPointInterm=&selDirection=0&inpTime=$startTime&inpDate=$startDate&optReturn=0&selDirection2=0&inpTime2=$startTime&inpDate2=$startDate&trafficmask=1,2&selChangeTime=0&selWalkSpeed=0&selPriority=0&cmdAction=search&EU_Spirit=False&TNSource=UMEA&SupportsScript=True&Language=se&VerNo=&Source=querypage_adv&MapParams=")
    val request = Request.Builder()
            .url("https://reseplanerare.fskab.se/umea/v2/resultspage.aspx")
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
                println(line)
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

fun main(args: Array<String>) {
    // "https://reseplanerare.fskab.se/umea/v2/rpajax.aspx?net=UMEA&lang=se&letters=address
    //getSchedule("Universum", "Vasaplan", "2018-12-03", "16:15")
    Skill.main(args)
}