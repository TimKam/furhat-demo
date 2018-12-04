package furhatos.app.travel_agent.nlu

import com.eclipsesource.json.Json
import furhatos.app.travel_agent.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import java.awt.PageAttributes.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import okhttp3.MediaType as OkMediaType

val client = OkHttpClient()

class Travel_agentSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}


enum class BusAnswer(val index: Int) {
    SHORT(0),
    LONG(1)
}

fun getSchedule(startPlace: String, destination: String, startDate: String, startTime: String): Array<String> {
    var startTime1 = ""
    var startPoint1 = ""
    var endTime1 = ""
    var duration1 = ""
    var line1 = ""
    var direction1 = ""
    var numberOfChanges1 = ""
    var changeStops1 = ""
    var lineChanges1 = ""
    var directionChanges1 = ""
    var walkingTime1 = 0

    var startTime2 = ""
    var startPoint2 = ""
    var endTime2 = ""
    var duration2 = ""
    var line2 = ""
    var direction2 = ""
    var numberOfChanges2 = ""
    var changeStops2 = ""
    var lineChanges2 = ""
    var directionChanges2 = ""
    var walkingTime2 = 0

    var duration3 = ""

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
    println("startPlace:")
    println(getStartResponseBody)
    println(startPlaceName)
    println(startPlaceId)

    startPoint1 = startPlaceName
    startPoint2 = startPlaceName

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
    if(destinationId.substring(4,5) == "|") destinationId = destinationId.substring(0, 4)

    // destinationId = destinationId
    println("destination:")
    println(getDestinationResponseBody)
    println(destinationName)
    println(destinationId)

    val startPlaceType = getStartResponseBody[getStartResponseBody.indexOf("#") - 1]
    val destinationType = getDestinationResponseBody[getDestinationResponseBody.indexOf("#") - 1]


    // Example: D6stermalmsgatan, Vasaplan, 2018-12-01, 21%3A16, (second time: 00%3A10)
    val mediaType = OkMediaType.parse("raw")
    val body = RequestBody.create(mediaType, "selPointFrKey=$startPlaceId&selPointToKey=$destinationId&inpPointFr=$startPlaceName&inpPointFr_ajax=$startPlaceName%7C$startPlaceId%7C$startPlaceType&inpPointTo=$destinationName&inpPointTo_ajax=$destinationName%7C$destinationId%7C$destinationType&inpPointInterm_ajax=&selRegionFr=741&optTypeFr=0&optTypeTo=0&inpPointInterm=&selDirection=0&inpTime=$startTime&inpDate=$startDate&optReturn=0&selDirection2=0&inpTime2=$startTime&inpDate2=$startDate&trafficmask=1,2&selChangeTime=0&selWalkSpeed=0&selPriority=0&cmdAction=search&EU_Spirit=False&TNSource=UMEA&SupportsScript=True&Language=se&VerNo=&Source=querypage_adv&MapParams=")
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
    var isWalking = false
    var gotWalkingStart = false
    var gotFirstWalkingTime1 = false
    var gotFirstWalkingTime2 = false
    var firstWalkingTime1 = 0
    var firstWalkingTime2 = 0
    var walkingStart = SimpleDateFormat("HH:mm")
    var needsFirstStation = false
    var needsFirstStationTime = false
    if(startPlaceType == '1') {
        needsFirstStation = true
        needsFirstStationTime = true
    }
    responseLines?.forEachIndexed {
        index, line ->
        // println(line) //
        if(line.contains("rp-date-header")) {
            val dateLine = line
                    .substring(line.indexOf(">")+1) // 7598
                    .substring(0, 10)
            schedule.add(dateLine)
        } else if(line.contains("queryStation") && needsFirstStation) {
            val startPoint1Temp = line.substring(line.indexOf("queryStation")+14)
            startPoint1 = startPoint1Temp.substring(0, startPoint1Temp.indexOf("|"))
            startPoint2 = startPoint1Temp.substring(0, startPoint1Temp.indexOf("|"))
            needsFirstStation = false

        }else if(line.contains("summary-timeframe")) {
            val timesLine = responseLines[index + 1]
            //println("Timesline")
            //println(timesLine)
            var startTime = ""
            var endTime = ""
            if(timesLine.contains(";")) {
                startTime = timesLine
                        .substring(timesLine.indexOf(";")+1)
                        .substring(0, 5)
                endTime = timesLine
                        .substring(timesLine.indexOf("&raquo;")+8)
                        .substring(0, 5)
            } else {
                startTime = timesLine
                        .substring(timesLine.indexOf("ca ")+3)
                        .substring(0, 5)
                endTime = timesLine
                        .substring(timesLine.indexOf("&raquo;")+12)
                        .substring(0, 5)
            }

            schedule.add(startTime)
            schedule.add(endTime)
            if (startTime1.equals("")) {
                startTime1 = startTime
            } else if (startTime2.equals("")) {
                startTime2 = startTime
            }
        } else if(line.contains("summary-heading")) {
            if(line.contains("Restid")) {
                val travelTimeLine = line
                        .substring(line.indexOf(">Restid")+8)
                        .substring(0, 5)
                schedule.add(travelTimeLine)
                if(duration1.equals("")) {
                    duration1 = travelTimeLine
                } else if (duration2.equals("")) {
                    duration2 = travelTimeLine
                } else if (duration3.equals("")){
                    duration3 = travelTimeLine
                }
            }
            else {
                val switchesLine = line
                        .substring(line.indexOf("heading'>")+9)
                        .substring(0, 1)
                schedule.add(switchesLine)
                if(duration2.equals("")) {
                    numberOfChanges1 = switchesLine
                } else if(duration3.equals("")) {
                    numberOfChanges2 = switchesLine
                }
            }

        } else if(line.contains("MqueryLine")) {
            val lineLine = responseLines[index + 2].substring(0, 2)
            var line = ""
            if(lineLine.substring(1,2) == "<") {
                line = lineLine.substring(0, 1)
            } else {
                line = lineLine.substring(0, 2)
            }
            if(duration2.equals("") && line1.equals("")) {
                line1 = line
            } else if (line2.equals("")) {
                line2 = line
            }
        } else if(line.contains(">Riktning:")) {
            val directionLine = responseLines[index]
                    .substring(line.indexOf(">Riktning:</span> ")+18)
            if(duration2.equals("") && direction1.equals("")) {
                direction1 = directionLine
            } else if (direction2.equals("")) {
                direction2 = directionLine
            }
        } else if(line.contains("details-content total-info")) {
            val vehicleLine = responseLines[index + 1]
            if(vehicleLine.contains("Gång")) {
                isWalking = true
            }

        } else if(line.contains(">ca&nbsp")) {
            val walkingTimeLine = line
                    .substring(line.indexOf(">ca&nbsp")+9)
                    .substring(0, 5)
            val formatter = SimpleDateFormat("HH:mm")
            val mFormatter = SimpleDateFormat("mm")
            if(!gotWalkingStart && isWalking) {
                walkingStart.applyPattern(walkingTimeLine)
                gotWalkingStart = true
            } else if (duration2.equals("") && isWalking) {
                val walkingStop = SimpleDateFormat("HH:mm")
                walkingStop.applyPattern(walkingTimeLine)
                val start = formatter.parse(walkingStart.toLocalizedPattern())
                val end = formatter.parse(walkingStop.toLocalizedPattern())
                val walkingTimeDateJoda = Minutes.minutesBetween(DateTime(start), DateTime(end))
                val walkingTimeDate = walkingTimeDateJoda.getMinutes() % 60
                println(walkingTimeDate)
                walkingTime1 += walkingTimeDate
                if(!gotFirstWalkingTime1) {
                    gotFirstWalkingTime1 = true
                    firstWalkingTime1 += walkingTimeDate
                    val startTime1Format = SimpleDateFormat("HH:mm")
                    startTime1Format.applyPattern(startTime1)
                    val startTime1Temp = formatter.parse(startTime1Format.toLocalizedPattern())
                    startTime1 = formatter.format(startTime1Temp.time.toLong().plus(walkingTimeDateJoda.getMinutes() * 60 * 1000))
                    println("startTime1:")
                    println(startTime1)
                }
                isWalking = false
                gotWalkingStart = false
                if(startPlaceType == '1') {
                    needsFirstStation = true
                }
                println("Walking time:")
                println(walkingTime1)
            } else if (duration3.equals("") && isWalking) {
                val walkingStop = SimpleDateFormat("HH:mm")
                walkingStop.applyPattern(walkingTimeLine)
                val start = formatter.parse(walkingStart.toLocalizedPattern())
                val end = formatter.parse(walkingStop.toLocalizedPattern())
                val walkingTimeDateJoda = Minutes.minutesBetween(DateTime(start), DateTime(end))
                val walkingTimeDate = walkingTimeDateJoda.getMinutes() % 60
                println(walkingTimeDate)
                walkingTime2 += walkingTimeDate
                if(!gotFirstWalkingTime2) {
                    gotFirstWalkingTime2 = true
                    firstWalkingTime2 += walkingTimeDate
                    val startTime2Format = SimpleDateFormat("HH:mm")
                    startTime2Format.applyPattern(startTime2)
                    val startTime2Temp = formatter.parse(startTime2Format.toLocalizedPattern())
                    startTime2 = formatter.format(startTime2Temp.time.toLong().plus(walkingTimeDateJoda.getMinutes() * 60 * 1000))
                    println("startTime2:")
                    println(startTime2)
                }
                isWalking = false
                gotWalkingStart = false
                println("Walking time 2:")
                println(walkingTime2)
            }

        }
    }
    println("Schedule:")
    println(schedule)
    var walkSnippet1 = ""
    if (walkingTime1 > 0) {
        if(walkingTime1 == 1)
            walkSnippet1 = "Du måste gå till fots i $walkingTime1 minut."
        else
            walkSnippet1 = "Du måste gå till fots i $walkingTime1 minuter."
    }
    var walkSnippet2 = ""
    if (walkingTime2 > 0) {
        if(walkingTime2 == 1)
            walkSnippet2 = "Du måste gå till fots i $walkingTime2 minut."
        else
            walkSnippet2 = "Du måste gå till fots i $walkingTime2 minuter."
    }
    var changeSnippet1 = ""
    if (numberOfChanges1 !== "" && numberOfChanges1.compareTo("0")!=0) {
        if (numberOfChanges1 == "1") {
            changeSnippet1 = " Du måste byta buss $numberOfChanges1 gång. "
        } else {
            changeSnippet1 = " Du måste byta buss $numberOfChanges1 gånger. "
        }
    }
    var changeSnippet2 = ""
    if (numberOfChanges2 !== "" && numberOfChanges2.compareTo("0")!=0) {
        if (numberOfChanges2 == "1") {
            changeSnippet2 = " Du måste byter buss $numberOfChanges2 gang. "
        } else {
            changeSnippet2 = " Du måste byter buss $numberOfChanges2 ganger. "
        }
    }

    //Remove hours from duration
    duration1 = duration1.substring(duration1.length-2)
    duration2 = duration2.substring(duration2.length-2)

    val finalShortResponse = "Den första bussen du kan ta går klockan $startTime1 från $startPoint1."
    val finalLongResponse = "Bussen går klockan $startTime1 från $startPoint1. Ta linje nummer $line1 i riktning $direction1. Resan tar $duration1 minuter.$changeSnippet1$walkSnippet1" +
            " Ännu en buss går klockan $startTime2 från $startPoint2. Det är linje nummer $line2 i riktning $direction2 och resan tar $duration2 minuter.$changeSnippet2$walkSnippet2"

    println(arrayOf(finalShortResponse, finalLongResponse)[0])
    return arrayOf(finalShortResponse, finalLongResponse)
}