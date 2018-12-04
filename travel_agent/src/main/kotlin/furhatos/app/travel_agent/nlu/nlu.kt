package furhatos.app.travel_agent.nlu

import furhatos.nlu.EnumEntity
import furhatos.util.Language

class Place : EnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("vasaplan", "guitars")
    }

    // Method overridden to produce a spoken utterance of the place
    override fun toText(lang: Language): String {
        return generate(lang,"$value");
    }
}