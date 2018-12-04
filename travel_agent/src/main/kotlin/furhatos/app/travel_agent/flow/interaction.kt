package furhatos.app.travel_agent.flow


import furhatos.flow.kotlin.*
import furhatos.app.travel_agent.nlu.*

import furhatos.gestures.Gestures
import furhatos.nlu.common.No
import furhatos.nlu.common.Number
import furhatos.nlu.common.Time
import furhatos.nlu.common.Yes
import furhatos.util.Language
import java.time.LocalTime

val General: State = state(Interaction) {
    onResponse<RequestJokeIntent> {
        if(GlobalLanguage == Language.ENGLISH_US) {
            furhat.say("Did you here about the man who ran in front of the bus? He got tired.")
        }
        else
        {
            furhat.say("Vad gör en sjuk bussförare för att bli frisk?")
            furhat.gesture(Gestures.BrowRaise)
            delay(1000)
            furhat.say("Tar en buss-kur")
            furhat.gesture(Gestures.BigSmile)

        }
        reentry()
    }
}

// Start of interaction
val Start = state(parent = General) {
    onEntry {
        furhat.gesture(Gestures.Smile)
        println("starting")

        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("Hi there. Do you want me to find a bus trip for you?")
        else
            furhat.ask("Hej, vill du att jag letar upp en buss till dig?")

    }
    onResponse<Yes>{
        furhat.gesture(Gestures.BigSmile)
        goto(CheckOrder)
    }

    onResponse<SvaraJaIntent>{
        furhat.gesture(Gestures.BigSmile)
        goto(CheckOrder)
        //goto(GetBusTrips)
    }


    onResponse<No>{
        furhat.gesture(Gestures.ExpressSad)
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Okay, have a nice day.")
        else
            furhat.say("Ok, ha en bra dag.")
    }

    onResponse<SvaraNejIntent>{
        furhat.gesture(Gestures.ExpressSad)
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Okay, have a nice day.")
        else
            furhat.say("Ok, ha en bra dag.")
    }


    onResponse<OrderBusIntent> {
        users.current.order.adjoin(it.intent)
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Ok, I can tell you what bus to take ${it.intent}")
        else
            furhat.say("Ok, Jag kan säga vilken bus ni ska ta ${it.intent}")

        goto(CheckOrder)
    }
}

// Form-filling state that checks any missing slots and if so, goes to specific slot-filling states.
val CheckOrder = state {
    onEntry {
        val order = users.current.order
        when {
            order.start       == null -> goto(RequestStart)
            order.destination == null -> goto(RequestDestination)
            order.timeToLeave == null -> goto(RequestTime)
            else -> {
                if(GlobalLanguage == Language.ENGLISH_US)
                    furhat.say("Alright, so you want to go from ${order.start} to ${order.destination} at ${order.timeToLeave}")
                else
                    furhat.say("Ok, så ni vill åka från ${order.start} till ${order.destination} klockan ${order.timeToLeave}")
                goto(ConfirmOrder)
            }
        }
    }
}



/*
    State for handling changes to an existing order
 */
val OrderHandling: State = state(parent = General) {

    // Handler that re-uses our pizza intent but has a more intelligent response handling depending on what new information we get
    onResponse<OrderBusIntent> {
        val order = users.current.order

        // Message to be constructed based on what data points we get from the user
        var message = "Okay"

        // Adding or changing delivery option and time
        if (it.intent.destination != null || it.intent.timeToLeave != null) {

            /* We are constructing a specific message depending on if we
            get a delivery place and/or time and if this slot already had a value
             */
            when {
                it.intent.destination != null && it.intent.timeToLeave != null -> { // We get both a delivery place and time
                    if(GlobalLanguage == Language.ENGLISH_US) {
                        message += ", going to ${it.intent.destination} ${it.intent.timeToLeave} "
                        if (order.destination != null || order.timeToLeave != null) message += "instead " // Add an "instead" if we are overwriting any of the slots
                    }
                    else
                    {
                        message += ", åker till ${it.intent.destination} ${it.intent.timeToLeave} "
                        if (order.destination != null || order.timeToLeave != null) message += "istället " // Add an "instead" if we are overwriting any of the slots
                    }

                }
                it.intent.destination != null -> { // We get only a delivery place
                    if(GlobalLanguage == Language.ENGLISH_US) {
                        message += ", going to ${it.intent.destination} "
                        if (order.destination != null) message += "instead " // Add an "instead" if we are overwriting the slot
                    }
                    else
                    {
                        message += ", åker till ${it.intent.destination} "
                        if (order.destination != null) message += "istället " // Add an "instead" if we are overwriting the slot
                    }
                }
                it.intent.timeToLeave != null -> { // We get only a delivery time
                    if(GlobalLanguage == Language.ENGLISH_US) {
                        message += ", leaving at ${it.intent.timeToLeave} "
                        if (order.timeToLeave != null) message += "instead " // Add an "instead" if we are overwriting the slot
                    }
                    else
                    {
                        message += ", åker  ${it.intent.timeToLeave} "
                        if (order.timeToLeave != null) message += "istället " // Add an "instead" if we are overwriting the slot
                    }
                }
            }
        }

        // Deliver our message
        furhat.say(message)

        // Finally we join the existing order with the new one
        order.adjoin(it.intent)
        goto(ConfirmOrder)

        //reentry()
    }
}

// Confirming order
val ConfirmOrder : State = state(parent = OrderHandling) {
    onEntry {
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("Does that sound good?")
        else
            furhat.ask("Låter det bra?")
    }

    onResponse<Yes> {
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Great")
        else
            furhat.say("Toppen")
        goto(GetBusTrips)
        //goto(EndOrder)
    }

    onResponse<SvaraJaIntent>{
        furhat.gesture(Gestures.BigSmile)
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Great")
        else
            furhat.say("Toppen")
        goto(GetBusTrips)
    }

    onResponse<No> {
        goto(ChangeOrder)
    }

    onResponse<SvaraNejIntent> {
        goto(ChangeOrder)
    }
}



// Changing order
val ChangeOrder = state(parent = OrderHandling) {
    onEntry {
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("Anything that you like to change?")
        else
            furhat.ask("Något ni vill ändra?")
    }

    onReentry {
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("What would you like to change?")
        else
            furhat.ask("Vad vill ni ändra?")
            //furhat.ask("Ni vill åka till ${users.current.order}. Något som ni vill ändra?")
    }

    onResponse<Yes> {
        reentry()
    }

    onResponse<SvaraJaIntent> {
        reentry()
    }

    onResponse<No> {
        goto(EndOrder)
    }

    onResponse<SvaraNejIntent> {
        goto(EndOrder)
    }
    onResponse<ChangeDestinationIntent> {
        goto(RequestDestination)
    }
    onResponse<ChangeStartingPlaceIntent> {
        goto(RequestStart)
    }
    onResponse<ChangeTimeToLeaveIntent> {
        goto(RequestTime)
    }

}

val GetBusTrips = state {
    onEntry {
        var order = users.current.order
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("I am now searching busses for you")
        else
            furhat.say("Nu söker jag bussar")
        order.busTripResponses = getSchedule("Universum", "Vasaplan", "2018-12-04", "16:15")
        goto(BusTripInformation)
    }
}

val BusTripInformation = state {
    onEntry {
        var order = users.current.order
        if(order.busTripResponses == null)
        {
            furhat.gesture(Gestures.ExpressSad)
            if(GlobalLanguage == Language.ENGLISH_US)
                furhat.say("I could not find any matching bustrips for you")
            else
                furhat.say("Jag hittade inga bussar åt dig")
            goto(SearchAgain)
        }
        else {
            order.busFound = true
            if (GlobalLanguage == Language.ENGLISH_US) {
                furhat.say("I found the following bustrip")
            }
            else {
                furhat.say("Jag hittade följande buss")
            }

            furhat.say(order.busTripResponses!![BusAnswer.SHORT.index])

            if (GlobalLanguage == Language.ENGLISH_US) {
                furhat.ask("Do you want to know more about that trip?")
            }
            else {
                furhat.ask("Vill ni veta mer om den bussturen?")
            }
        }
    }

    onResponse<Yes> {
        var order = users.current.order
        furhat.say(order.busTripResponses!![BusAnswer.LONG.index])
        delay(500)
        goto(SearchAgain)
    }

    onResponse<SvaraJaIntent> {
        var order = users.current.order
        furhat.say(order.busTripResponses!![BusAnswer.LONG.index])
        delay(500)
        goto(SearchAgain)
    }

    onResponse<No> {
        goto(SearchAgain)
    }

    onResponse<SvaraNejIntent> {
        goto(SearchAgain)
    }
}

// Changing order
val SearchAgain = state(parent = OrderHandling) {
    onEntry {
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("Do you want to make a new search?")
        else
            furhat.ask("Vill ni göra en ny sökning?")
    }

    onResponse<Yes> {
        val order = users.current.order
        order.initBusOrder()

        goto(CheckOrder)
    }

    onResponse<SvaraJaIntent> {
        val order = users.current.order
        order.initBusOrder()

        goto(CheckOrder)
    }

    onResponse<No> {
        goto(EndOrder)
    }

    onResponse<SvaraNejIntent> {
        goto(EndOrder)
    }
}

// Order completed
val AbortOrder = state {
    onEntry {
        furhat.gesture(Gestures.ExpressSad)
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Sad that I could not help you")
        else
            furhat.say("Tråkigt att jag inte kunde hjälpa er")
        val order = users.current.order
        order.initBusOrder()
        goto(Idle)
    }
}

// Order completed
val EndOrder = state {
    onEntry {
        var order = users.current.order
        if(order.busFound) {
            furhat.gesture(Gestures.BigSmile)
            if (GlobalLanguage == Language.ENGLISH_US)
                furhat.say("Have a nice trip")
            else
                furhat.say("Ha en trevlig resa")
            val order = users.current.order
            furhat.gesture(Gestures.Blink)
        }
        else
        {
            furhat.gesture(Gestures.ExpressSad)
            if(GlobalLanguage == Language.ENGLISH_US)
                furhat.say("Sad that I could not help you")
            else
                furhat.say("Tråkigt att jag inte kunde hjälpa er")
        }
        order.initBusOrder()
        goto(Idle)
    }
}


// Request delivery time
val RequestTime : State = state(parent = OrderHandling) {
    onEntry() {
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("At what time do you want to go?")
        else
            furhat.ask("Vilken tid vill ni åka?")
    }
/*
    onResponse<Number> {
        var hour = it.intent.value
        if (hour != null) {
            var now = LocalTime.now()

            if (now.hour > hour)
                hour += 12

            raise(it, TellTimeIntent(Time(LocalTime.of(hour, 0))))
        }
        else {
            propagate()
        }
    }
*/
    onResponse<Number> {
        val hour = it.intent.value
        val time = LocalTime.of(hour, 0)

        /*
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Okay, at ${time}")
        else
            furhat.say("Ok, klockan ${time}")
        */

        users.current.order.timeToLeave = time
        goto(CheckOrder)
    }

    onResponse<TellTimeNowIntent>{
        val timeNow = LocalTime.now()
        /*
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Okay, at ${timeNow}")
        else
            furhat.say("Ok, ${timeNow}")
        */

        users.current.order.timeToLeave = timeNow
        goto(CheckOrder)
    }


    onResponse<TellTimeIntent> {
        /*
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.say("Okay, at ${it.intent.time}")
        else
            furhat.say("Ok, ${it.intent.time}")
        */

        users.current.order.timeToLeave = it.intent.time?.asLocalTime()
        goto(CheckOrder)
    }
}

// Request start
val RequestStart : State = state(parent = OrderHandling) {
    onEntry() {
        if(GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("From where would you like to travel?")
        else
            furhat.ask("Vart vill du åka från?")
    }

    onResponse {
        var start = it.speech.text
        furhat.say("Okay, ${start}")
        users.current.order.start = start
        goto(CheckOrder)
    }
}

// Request destination
val RequestDestination : State = state(parent = OrderHandling) {
    onEntry() {
        if (GlobalLanguage == Language.ENGLISH_US)
            furhat.ask("Where do you want to go?")
        else
            furhat.ask("Vart vill du åka?")
    }

    onResponse {
        var destination = it.speech.text
        var destSplit: MutableList<String> = mutableListOf<String>()
        if (GlobalLanguage == Language.ENGLISH_US) {
            destSplit.addAll(destination.split("to".toRegex()))
            destination = destSplit.last()
            furhat.say("Okay, ${destination}")
        }
        else {
            destSplit.addAll(destination.split("till".toRegex()))
            destination = destSplit.last()
            furhat.say("Ok, ${destination}")
        }

        users.current.order.destination = destination
        goto(CheckOrder)
    }
}