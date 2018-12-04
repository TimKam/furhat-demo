package furhatos.app.travel_agent.flow

import furhatos.flow.kotlin.*
import furhatos.util.*

val Idle: State = state {

    init {
        furhat.setVoice(GlobalLanguage, Gender.MALE)
        println("changing language")
        if (users.count > 0) {
            //Clear
            var order = users.current.order
            furhat.attend(users.random)
            order.timeToLeave = null
            order.destination = null
            order.start = null
            order.busTripResponses = null
            order.busFound = false
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
            furhat.say("Ursäkta, jag förstod inte.")
        reentry()
    }
}
