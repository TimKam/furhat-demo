package furhatos.app.travel_agent.nlu

import furhatos.app.travel_agent.flow.GlobalLanguage
import furhatos.nlu.TextGenerator
import furhatos.util.Language
import furhatos.nlu.*
import furhatos.nlu.common.*

open class OrderBusIntent : Intent(), TextGenerator {
    var destination : Place? = null
    var timeToLeave : Time? = null

    override fun getExamples(lang: Language): List<String> {
        if(GlobalLanguage == Language.ENGLISH_US) {
            return listOf(
                    "I would like to go to @destination at @timeToLeave",
                    "I would like to go to @destination",
                    "To @destination",
                    "At @timeToLeave"
            )
        }
        else
        {
            return listOf(
                    "Jag vill åka till @destination kl @timeToLeave",
                    "Jag skulle vilja åka till @destination",
                    "Till @destination"
            )
        }
    }

    override fun toText(lang : Language) : String {
        if(GlobalLanguage == Language.ENGLISH_US)
            return generate(lang, "[$destination] [at $timeToLeave]")
        else
            return generate(lang, "[$destination] [kl $timeToLeave]")
    }

    override fun toString(): String {
        return toText()
    }

//    override fun adjoin(record: GenericRecord<Any>?) {
//        super.adjoin(record)
//        if (topping != null){
//            topping?.list = topping?.list?.distinctBy { it.value }
//        }
//    }
}

class TellPlaceIntent : Intent() {
    var destination : Place? = null

    override fun getExamples(lang: Language): List<String> {
        if(GlobalLanguage == Language.ENGLISH_US)
            return listOf("Guitars", "to guitars", "I want to go to guitars")
        else
            return listOf("Guitars", "till guitars", "Jag vill åka till guitars")
    }
}

class RequestJokeIntent : Intent()  {
    override fun getExamples(lang: Language): List<String> {
        if(GlobalLanguage == Language.ENGLISH_US) {
            return listOf("Can you tell me a joke",
                    "tell me a joke")
        }
        else
        {
            return listOf("Kan du berätta en vits",
                    "berätta en vits")
        }
    }
}

class TellTimeIntent(var time : Time? = null) : Intent() {

    override fun getExamples(lang: Language): List<String> {
        if(GlobalLanguage == Language.ENGLISH_US)
            return listOf("@time", "at @time", "@time o clock")
        else
            return listOf("@time", "kl @time")
    }
}

class TellTimeNowIntent : Intent() {

    override fun getExamples(lang: Language): List<String> {
        if(GlobalLanguage == Language.ENGLISH_US)
            return listOf("Now")
        else
            return listOf("Nu", "Genast")
    }
}