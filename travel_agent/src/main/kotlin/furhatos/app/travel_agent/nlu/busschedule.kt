package furhatos.app.travel_agent.nlu

import furhatos.app.travel_agent.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Request
import java.time.LocalTime
import java.time.temporal.ChronoUnit.MINUTES
import okhttp3.MediaType as OkMediaType


class Travel_agentSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}


abstract class Segment(val startTime : LocalTime, val endTime : LocalTime, val startPoint : String, val endPoint : String)
{
    override fun toString(): String
    {
        return "<Segment: start at $startTime, end at $endTime, from $startPoint to $endPoint>"
    }

    abstract fun getWalkingTime() : Long

    abstract fun getLongDescription() : String
}


class WalkingSegment(startTime: LocalTime, endTime: LocalTime, startPoint: String, endPoint: String) : Segment(startTime, endTime, startPoint, endPoint)
{
    override fun toString(): String
    {
        return "<WalkingSegment: start at $startTime, end at $endTime, from $startPoint to $endPoint>"
    }

    override fun getWalkingTime() : Long
    {
        return MINUTES.between(this.startTime, this.endTime)
    }

    override fun getLongDescription(): String
    {
        return "Gå från ${this.startPoint} till ${this.endPoint}."
    }
}


class BusSegment(startTime: LocalTime, endTime: LocalTime, startPoint: String, endPoint: String, val line : String, val direction : String) : Segment(startTime, endTime, startPoint, endPoint)
{
    override fun toString(): String
    {
        return "<BusSegment: start at $startTime, end at $endTime, from $startPoint to $endPoint, in direction $direction>"
    }

    override fun getWalkingTime(): Long
    {
        return 0
    }

    override fun getLongDescription(): String
    {
        return "Ta linje nummer ${this.line} från ${this.startPoint} i riktning ${this.direction} och gå av vid ${this.endPoint}. "
    }

    fun getDuration() : Long
    {
        return MINUTES.between(this.startTime, this.endTime)
    }
}


class Trip(val startTime : LocalTime, val endTime : LocalTime, val duration : Int, val changes : Int, val segments : ArrayList<Segment> = ArrayList())
{
    override fun toString(): String {
        var rep = "<Trip: start at $startTime, end at $endTime, duration of $duration minutes, with $changes changes>"
        segments.forEach { segment -> rep += "\n" + segment.toString() }
        return rep
    }

    fun addSegment(segment : Segment)
    {
        this.segments.add(segment)
    }

    fun getWalkingTime() : Long
    {
        var walkingTime : Long = 0
        segments.forEach { segment -> walkingTime += segment.getWalkingTime() }
        return walkingTime
    }

    fun getStartPoint() : String
    {
        return segments.first().startPoint
    }

    fun getEndPoint() : String
    {
        return segments.last().endPoint
    }

    fun getShortDescription() : String
    {
        return "Bussen går klockan ${this.startTime} från ${this.getStartPoint()}"
    }

    fun getLongDescription() : String
    {
        // general information on the bus
        var description = "Bussen går klockan ${this.startTime} från ${this.getStartPoint()} och resan tar "
        if (this.duration == 1)
            description += "en minut. "
        else
            description += "${this.duration} minuter."

        // walking and changing snippets
        description += when
        {
            this.changes > 0 && this.getWalkingTime() > 0 ->
                when
                {
                    this.changes == 1 && this.getWalkingTime() == 1L ->
                        "Du måste byta buss en gång och gå till fots i en minut. "

                    this.changes == 1 ->
                        "Du måste byta buss en gång och gå till fots i ${this.getWalkingTime()} minuter. "

                    this.getWalkingTime() == 1L ->
                        "Du måste byta buss ${this.changes} gånger och gå till fots i en minut. "

                    else ->
                        "Du måste byta buss ${this.changes} gånger och gå till fots i ${this.getWalkingTime()} minuter. "
                }

            this.changes > 0 ->
                if (this.changes == 1)
                    "Du måste byta buss en gång. "
                else
                    "Du måste byta buss ${this.changes} gånger. "

            this.getWalkingTime() > 0 ->
                if (this.getWalkingTime() == 1L)
                    "Du måste gå till fots i en minut. "
                else
                    "Du måste gå till fots i ${this.getWalkingTime()} minuter. "

            else -> // no details to add, no walking and no changing
                ""
        }

        if (this.segments.size == 1)
        {
            description += this.segments[0].getLongDescription()
        }
        else
        {
            description += "Först " + this.segments[0].getLongDescription()
            var i = 1
            while (i < segments.size)
                description += "Sen ${segments[i++].getLongDescription()}"
        }

        return description
    }
}


enum class BusAnswer(val index: Int) {
    SHORT(0),
    LONG(1)
}

enum class ParseSection
{
    NONE, SUMMARY, DETAILS
}

fun getSchedule(startPlace: String, destination: String, startDate: String, startTime: String): Array<String> {
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
    //println("destination:")
    //println(getDestinationResponseBody)
    //println(destinationName)
    //println(destinationId)

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
    //println("Response:")
    //println(response.message())
    //println(response.body()!!.contentType())
    val responseLines = resBody?.lines() ?: emptyList()
    var trips = arrayListOf<Trip>()
    var isWalking = false

    // collect info for creating trip objects
    var tripStart    : LocalTime = LocalTime.now()
    var tripEnd      : LocalTime = LocalTime.now()
    var tripDuration : Int       = 0
    var tripChanges  : Int       = 0
    var currentTrip  : Trip?     = null

    var segmentLine       : String?    = null
    var segmentStartPoint : String?    = null
    var segmentEndPoint   : String?    = null
    var segmentStartTime  : LocalTime? = null
    var segmentEndTime    : LocalTime? = null
    var segmentDirection  : String?    = null

    // to keep track what kind of section we are parsing
    var section : ParseSection = ParseSection.NONE
    var line : String = ""
    var index = 0
    while (index < responseLines.size)
    {
        line = responseLines[index]
        when (section)
        {
            ParseSection.NONE ->
            {
                // if we find a summary heading, we enter the summary parsing mode and keep the index where it is
                if (line.contains("summary-heading"))
                {
                    section = ParseSection.SUMMARY
                    index -= 1 // hack
                }
            }
            ParseSection.SUMMARY ->
            {
                if (line.contains("Restid")) {
                    val pattern = "(\\d\\d:\\d\\d)".toRegex()
                    val times = pattern.findAll(line)

                    val startTime = times.elementAt(0).value
                    val endTime = times.elementAt(1).value

                    tripStart = LocalTime.parse(startTime)
                    tripEnd = LocalTime.parse(endTime)

                    val travelTimeLine = line.substring(line.indexOf(">Restid") + 8).substring(0, 5)

                    val pattern2 = "(\\d\\d):(\\d\\d)".toRegex()
                    val matches = pattern2.findAll(travelTimeLine)
                    val hours = Integer.parseUnsignedInt(matches.elementAt(0)?.groups[1]?.value)
                    val minutes = Integer.parseUnsignedInt(matches.elementAt(0)?.groups[2]?.value)

                    tripDuration = 60 * hours + minutes
                }
                else if (line.contains("Byt"))
                {
                    val switchesLine = line.substring(line.indexOf("heading'>")+9).substring(0, 1)
                    tripChanges = Integer.parseUnsignedInt(switchesLine)
                    currentTrip = Trip(tripStart, tripEnd, tripDuration, tripChanges)
                    trips.add(currentTrip)

                    // after the number of changes, we can go to parsing the details of the trip, i.e., the individual segments
                    section = ParseSection.DETAILS
                }
            }
            ParseSection.DETAILS ->
            {
                if (line.contains("MqueryLine"))
                {
                    val lineLine = responseLines[index + 2].substring(0, 2)
                    segmentLine = if (lineLine.substring(1,2) == "<") lineLine.substring(0, 1) else lineLine.substring(0, 2)
                }
                else if (line.contains(">Riktning:"))
                {
                    val directionLine = responseLines[index].substring(line.indexOf(">Riktning:</span> ")+18)
                    segmentDirection = directionLine
                }
                else if (line.contains("details-content total-info"))
                {
                    val vehicleLine = responseLines[index + 2]
                    isWalking = vehicleLine.contains("Gång")
                }

                // start and end point of the segment
                else if (line.contains("table-item col-1") && !line.contains("details-heading"))
                {
                    val pattern = ">([^<>]+)<".toRegex()
                    val match   = pattern.find(line)!!
                    val place   = match.value.removePrefix(">").removeSuffix("<")
                    if (segmentStartPoint == null)
                        segmentStartPoint = place
                    else
                        segmentEndPoint = place
                }

                // start and end time of the segment
                else if (line.contains("table-item col-4") && !line.contains("details-heading"))
                {
                    val pattern = "(\\d\\d:\\d\\d)".toRegex()
                    val time    = pattern.find(line)!!
                    if (segmentStartTime == null)
                        segmentStartTime = LocalTime.parse(time.value)
                    else
                        segmentEndTime = LocalTime.parse(time.value)
                }
                else if (line.contains("summary"))
                {
                    // once we find the string summary, we're entering the summary of a new trip
                    section = ParseSection.SUMMARY
                }

                if (segmentStartPoint != null && segmentEndPoint != null && segmentStartTime != null && segmentEndTime != null && (isWalking || (segmentLine != null && segmentDirection != null)))
                {
                    val segment = if (isWalking) WalkingSegment(segmentStartTime, segmentEndTime, segmentStartPoint, segmentEndPoint)
                                  else           BusSegment(segmentStartTime, segmentEndTime, segmentStartPoint, segmentEndPoint, segmentLine ?: "", segmentDirection ?: "")
                    currentTrip?.addSegment(segment)
                    segmentLine       = null
                    segmentStartPoint = null
                    segmentEndPoint   = null
                    segmentStartTime  = null
                    segmentEndTime    = null
                    segmentDirection  = null
                    isWalking         = false
                }
            }
        }
        index += 1
    }

    // select the first trip that happens *after* the desired time of departure...
    // tabussen is so nice as to give us trips prior to the departure time
    // BUT we could report trips prior to the departure time if the departure time is some time in the future (?)
    val shouldStart = LocalTime.parse(startTime)
    var tripIndex = 0
    while (tripIndex < trips.size && trips[tripIndex].startTime < shouldStart)
        tripIndex++

    if (tripIndex < trips.size)
    {
        val trip = trips[tripIndex]
        return arrayOf(trip.getShortDescription(), trip.getLongDescription())
    }
    else
        return emptyArray()
}