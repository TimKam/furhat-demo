package furhatos.app.travel_agent.nlu

import furhatos.nlu.TextGenerator
import furhatos.util.Language
import furhatos.nlu.*
import furhatos.nlu.common.*
import furhatos.nlu.common.Date
import furhatos.nlu.common.Number
import java.time.LocalDate
import java.time.LocalTime

open class OrderBusIntent : Intent(), TextGenerator {
    var start        : String? = null
    var destination  : String? = null

    // default travel date and time
    var travelDate   : LocalDate = LocalDate.now()
    var timeToLeave  : LocalTime = LocalTime.now()

    // we remember whether we have updated anything so that we can generate meaningful speech, we don't need to mention
    // the date of time of travel if the user didn't change it, then we implicitly assume today and now
    var updatedTime  : Boolean = false
    var updatedDate  : Boolean = false

    var timeChecked  : Boolean = false

    fun setStartPoint(start : String)
    {
        this.start = start
    }

    fun setTime(timeToLeave : LocalTime)
    {
        this.timeToLeave = timeToLeave
        this.updatedTime = true
        this.setTimeChecked()
    }

    fun setDate(travelDate : LocalDate)
    {
        this.travelDate  = travelDate
        this.updatedDate = true
        this.setTimeChecked()
    }

    fun setTimeChecked()
    {
        this.timeChecked = true
    }

    fun setTimeUnChecked()
    {
        this.timeChecked = false
    }

    fun setDest(dest : String)
    {
        this.destination = dest
    }

    var busTripResponses : Array<String>? = null
    var busFound : Boolean = false

    fun initBusOrder()
    {
        start            = null
        timeToLeave      = LocalTime.now()
        travelDate       = LocalDate.now()
        destination      = null
        busTripResponses = null
        busFound         = false
        setTimeUnChecked()
    }

    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Jag vill åka till @destination klockan @timeToLeave",
                    "Jag vill åka till @destination",
                    "Jag skulle vilja åka till @destination",
                    "Till @destination")
            Language.GERMAN  -> listOf("Ich will um @timeToLeave nach @destination fahren"
                    , "Ich will um @timeToLeave zu @destination fahren"
                    , "Ich will nach @destination fahren"
                    , "Ich will zu @destination fahren"
                    , "Zu @destination"
                    , "Nach @destination"
            )
            else             -> listOf("I would like to go to @destination at @timeToLeave",
                    "I would like to go to @destination",
                    "To @destination",
                    "At @timeToLeave")
        }
    }

    override fun toText(lang : Language) : String {
        return when (lang)
        {
            Language.SWEDISH -> generate(lang, "[$destination] [klockan $timeToLeave]")
            Language.GERMAN  -> generate(lang, "[$destination] [um $timeToLeave]")
            else             -> generate(lang, "[$destination] [at $timeToLeave]")
        }
    }

    override fun toString(): String {
        return toText()
    }
}


class TellPlaceIntent : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Guitars", "till guitars", "Jag vill åka till guitars", "till @destination", "@destination")
            Language.GERMAN  -> listOf("Guitars", "zu Guitars", "Ich will zu Guitars fahren", "zu @destination", "nach @destination", "@destination")
            else             -> listOf("Guitars", "to guitars", "I want to go to guitars", "to @destination", "@destination")
        }
    }
}


class RequestJokeIntent : Intent()  {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Kan du berätta en vits", "berätta en vits")
            Language.GERMAN  -> listOf("Kannst du einen Witz erzählen?")
            else             -> listOf("Can you tell me a joke", "tell me a joke")
        }
    }
}


class TellDateIntent(var date : Date? = null) : Intent()
{
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("idag", "imorgon", "@date")
            Language.GERMAN  -> listOf("Heute", "Morgen", "@date")
            else             -> listOf("Today", "Tomorrow", "@date")
        }
    }
}


class TellDateAndTimeIntent(var date : Date? = null, var time : Time? = null) : Intent()
{
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Den @date, klockan @time")
            Language.GERMAN  -> listOf("Am @date um @time")
            else             -> listOf("On the @date at @time")
        }
    }
}


class TellTimeIntent(var time : Time? = null) : Intent()
{
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("@time", "klockan @time")
            Language.GERMAN  -> listOf("@time", "um @time", "um @time Uhr")
            else             -> listOf("@time", "at @time", "@time o clock")
        }
    }
}


class TellAfterHowLongIntent(var inHours : Number? = null, var inMinutes : Number? = null) : Intent()
{
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Om @inHours timme", "Om @inHours timmar", "Om @inMinutes minuter", "Om @inHours timme och @inMinutes minuter")
            Language.GERMAN  -> listOf("In @inHours Stunde", "In @inHours Stunden", "In @inMinutes Minuten", "In @inHours Stunden und @inMinutes Minuten")
            else             -> listOf("In @inHours hour", "In @inHours hours", "In @inMinutes minutes", "In @inHours hours and @inMinutes minutes")
        }
    }
}


class TellTimeNowIntent : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Nu", "Genast")
            Language.GERMAN  -> listOf("Jetzt")
            else             -> listOf("Now", "Immediately")
        }
    }
}


class SvaraJaIntent : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Ja", "Självklart", "Jajamensan","Jajamen", "Schp")
            Language.GERMAN  -> listOf("Ja")
            else             -> listOf("Yes")
        }
    }
}


class SvaraNejIntent : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Nej", "Inte alls", "Nix")
            Language.GERMAN  -> listOf("Nein")
            else             -> listOf("No")
        }
    }
}

class ChangeDestinationIntent : Intent()  {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Destination", "Destinationen", "Min destination", "Vart jag vill åka", "slutdestination")
            Language.GERMAN  -> listOf("Ziel")
            else             -> listOf("Destination", "My destination", "End station")
        }
    }
}

class ChangeTimeToLeaveIntent : Intent()  {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Avresetid", "tid", "tiden", "starttid")
            Language.GERMAN  -> listOf("Abfahrtszeit")
            else             -> listOf("My travel time", "time to travel", "time")
        }
    }
}

class ChangeStartingPlaceIntent : Intent()  {
    override fun getExamples(lang: Language): List<String> {
        return when (lang)
        {
            Language.SWEDISH -> listOf("Startplats", "startstation", "avrese station")
            Language.GERMAN  -> listOf("Ausgangspunkt")
            else             -> listOf("Going from", "Departure station")
        }
    }
}