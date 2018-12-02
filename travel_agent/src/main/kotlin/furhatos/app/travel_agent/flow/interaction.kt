package furhatos.app.travel_agent.flow

import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.travel_agent.nlu.*

val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("Hi there. Do you like robots?")
    }

    onResponse<Yes>{
        furhat.say("I like humans.")
    }

    onResponse<No>{
        furhat.say("That's sad.")
    }
}
