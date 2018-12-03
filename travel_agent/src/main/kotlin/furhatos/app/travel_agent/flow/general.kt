package furhatos.app.travel_agent.flow

import furhatos.flow.kotlin.*
import furhatos.util.*

val Idle: State = state {

    init {
        furhat.setVoice(GlobalLanguage, Gender.MALE)
        println("changing language")
        if (users.count > 0) {
            furhat.attend(users.random)
            goto(Start)
        }
    }

    onEntry {
        furhat.attendNobody()
    }

    onUserEnter {
        furhat.attend(it)
        goto(Start)
    }
}

val Interaction: State = state {

    onUserLeave(instant = true) {
        if (users.count > 0) {
            if (it == users.current) {
                furhat.attend(users.other)
                goto(Start)
            } else {
                furhat.glance(it)
            }
        } else {
            goto(Idle)
        }
    }

    onUserEnter(instant = true) {
        furhat.glance(it)
    }

    onNoResponse {
        if (GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Sorry, I didn't hear you.")
        else
            furhat.say("Ursäkta, jag hörde inte.")
        reentry()
    }

    onResponse { // Catches everything else
        if (GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Sorry, I didn't understand that.")
        else
            furhat.say("Ursäkta, jag förstog inte.")
        reentry()
    }
}
