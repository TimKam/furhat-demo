package furhatos.app.travel_agent.flow

import furhatos.flow.kotlin.*
import furhatos.app.travel_agent.nlu.*

import furhatos.gestures.Gestures
import furhatos.nlu.common.No
import furhatos.nlu.common.Number
import furhatos.nlu.common.Yes
import furhatos.util.Language
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Hej, vill du att jag letar upp en buss till dig?"
            Language.GERMAN  -> "Hallo, möchtest du, dass ich dir eine Busverbindung raussuche?"
            else             -> "Hi there. Do you want me to find a bus trip for you?"
        })

    }
    onResponse<Yes>{
        furhat.gesture(Gestures.BigSmile)
        goto(CheckOrder)
    }

    onResponse<SvaraJaIntent>{
        furhat.gesture(Gestures.BigSmile)
        goto(CheckOrder)
    }

    onResponse<No>{
        furhat.gesture(Gestures.ExpressSad)
        furhat.say(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Ok, ha en bra dag."
            Language.GERMAN  -> "Ok, dann wünsche ich noch einen schönen Tag. Bis bald."
            else             -> "Okay, have a nice day."
        })
    }

    onResponse<SvaraNejIntent>{
        furhat.gesture(Gestures.ExpressSad)
        furhat.say(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Ok, ha en bra dag."
            Language.GERMAN  -> "Ok, dann wünsche ich noch einen schönen Tag. Bis bald."
            else             -> "Okay, have a nice day."
        })
    }

    // when do we end up here?
    onResponse<OrderBusIntent> {
        println("OrderBusIntent")
        users.current.order.adjoin(it.intent)
        furhat.say(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Ok, Jag kan säga vilken bus ni ska ta ${it.intent}"
            Language.GERMAN  -> "Ok, ich kann dir sagen, welchen Bus du nehmen kannst ${it.intent}"
            else             -> "Ok, I can tell you what bus to take ${it.intent}"
        })
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
            !order.timeChecked        -> goto(ConfirmTime)
            else                      ->
            {
                when (GlobalLanguage)
                {
                    Language.SWEDISH ->
                    {
                        var message = "Så ni vill åka från ${order.start} till ${order.destination}"

                        if (order.updatedDate) {
                            if(order.travelDate.dayOfMonth <= 2)
                                message += " den ${order.travelDate.dayOfMonth}:a ${order.travelDate.month}"
                            else
                                message += " den ${order.travelDate.dayOfMonth}:e ${order.travelDate.month}"
                        }

                        if (order.updatedTime) {
                            message += " klockan ${order.timeToLeave.hour}"
                            if (order.timeToLeave.minute > 0)
                                message += ":${order.timeToLeave.minute}"
                            message += "."
                        }
                        print(message)
                        furhat.say(message)
                    }
                    else ->
                    {
                        furhat.say("So you want to go from ${order.start} to ${order.destination} at ${order.timeToLeave}")
                    }
                }
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
    }
}


val ConfirmTime : State = state(parent = OrderHandling)
{
    onEntry {
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Vill du åka nu direkt?"
            Language.GERMAN  -> "Möchtest du so bald wie möglich fahren?"
            else             -> "Would you like to travel as soon as possible?"
        })
    }

    onResponse<SvaraJaIntent> {
        users.current.order.setTimeChecked()
        goto(CheckOrder)
    }

    onResponse<SvaraNejIntent> {
        goto(RequestTime)
    }

    onResponse<TellTimeNowIntent> {
        users.current.order.setTime(LocalTime.now())
        goto(CheckOrder)
    }

    onResponse<TellTimeIntent> {
        users.current.order.setTime(it.intent.time?.asLocalTime() ?: LocalTime.now())
        goto(CheckOrder)
    }

    onResponse<TellAfterHowLongIntent> {
        val now       = LocalTime.now()
        val timePoint = LocalTime.of( now.hour   + (it.intent.inHours?.value ?: 0)
                                    , now.minute + (it.intent.inMinutes?.value ?: 0)
                                    )
        users.current.order.setTime(timePoint)
        goto(CheckOrder)
    }

    onResponse<TellDateIntent> {
        users.current.order.setDate(it.intent.date?.asLocalDate() ?: LocalDate.now())
        goto(CheckOrder)
    }
}

// Confirming order
val ConfirmOrder : State = state(parent = OrderHandling) {
    onEntry {
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Låter det bra?"
            Language.GERMAN  -> "Richtig?"
            else             -> "Does that sound good?"
        })
    }

    onResponse<Yes> {
        furhat.say(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Toppen"
            Language.GERMAN  -> "Gut"
            else             -> "Great"
        })
        goto(GetBusTrips)
    }

    onResponse<SvaraJaIntent>{
        furhat.gesture(Gestures.BigSmile)
        furhat.say(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Toppen"
            Language.GERMAN  -> "Gut"
            else             -> "Great"
        })
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
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Något ni vill ändra?"
            Language.GERMAN  -> "Möchtest du Etwas ändern?"
            else             -> "Anything that you like to change?"
        })
    }

    onReentry {
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Vad vill ni ändra?"
            Language.GERMAN  -> "Was möchtest du ändern?"
            else             -> "What would you like to change?"
        }, timeout = 3000)
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

val GetBusTrips = state(parent = OrderHandling) {
    onEntry {
        furhat.say(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Nu söker jag bussar"
            Language.GERMAN  -> "Ok, mal sehen."
            else             -> "I am now searching busses for you"
        })
        furhat.gesture(Gestures.Thoughtful)

        val order = users.current.order
        order.busTripResponses = getSchedule( order.start ?: "Universum"
                                            , order.destination ?: "Vasaplan"
                                            , order.travelDate.format(DateTimeFormatter.ISO_DATE).toString()
                                            , order.timeToLeave.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
                                            )
        goto(BusTripInformation)
    }
}

val BusTripInformation = state(parent = General) {
    onEntry {
        var order = users.current.order
        if (order.busTripResponses == null)
        {
            furhat.gesture(Gestures.ExpressSad)
            furhat.say(when (GlobalLanguage)
            {
                Language.SWEDISH -> "Jag hittade inga bussar åt dig"
                Language.GERMAN  -> "Ich konnte leider keinen passenden Bus finden"
                else             -> "I could not find any matching bustrips for you"
            })
            goto(SearchAgain)
        }
        else {
            order.busFound = true
            furhat.say(when (GlobalLanguage)
            {
                Language.SWEDISH -> "Jag hittade följande buss"
                Language.GERMAN  -> "Ich habe folgenden Bus gefunden"
                else             -> "I found the following bustrip"
            })

            // ok this is only in Swedish for now
            furhat.say(order.busTripResponses!![BusAnswer.SHORT.index])

            furhat.ask(when (GlobalLanguage)
            {
                Language.SWEDISH -> "Vill ni veta mer om den bussturen?"
                Language.GERMAN  -> "Möchtest du mehr über diese Route wissen?"
                else             -> "Do you want to know more about that trip?"
            }, timeout = 6000) // wait a bit longer for a user reply
        }
    }

    // Don't repeat everything when the users doesn't reply, just ask again
    onNoResponse {
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Ursäkta jag hörde inte, vill ni veta mer om den bussturen?"
            Language.GERMAN  -> "Entschudligung, das habe ich nicht gehört. Möchtest du mehr über diese Route wissen?"
            else             -> "Sorry, I didn't hear you. Do you want to know more about that trip?"
        }, timeout = 5000) // wait a bit longer for a user reply
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
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Vill ni göra en ny sökning?"
            Language.GERMAN  -> "Möchtest du noch einen Trip suchen?"
            else             -> "Do you want to make a new search?"
        })
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
        furhat.say(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Tråkigt att jag inte kunna hjälpa er"
            Language.GERMAN  -> "Hm, schade, dass ich nicht helfen konnte."
            else             -> "Sad that I could not help you"
        })

        val order = users.current.order
        order.initBusOrder()
        goto(Idle)
    }
}

// Order completed
val EndOrder = state {
    onEntry {
        val order = users.current.order
        if (order.busFound) {
            furhat.gesture(Gestures.BigSmile)
            furhat.say(when (GlobalLanguage)
            {
                Language.SWEDISH -> "Ha en trevlig resa"
                Language.GERMAN  -> "Gute Reise!"
                else             -> "Have a nice trip"
            })
            furhat.gesture(Gestures.Blink)
        }
        order.initBusOrder()
        goto(Idle)
    }
}


// Request delivery time
val RequestTime : State = state(parent = OrderHandling) {
    onEntry {
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "När vill ni åka?"
            Language.GERMAN  -> "Wann möchtest du fahren?"
            else             -> "When do you want to go?"
        })
    }

    onResponse<Number> {
        val hour = it.intent.value
        users.current.order.setTime(LocalTime.of(hour, 0))
        goto(CheckOrder)
    }

    onResponse<TellTimeNowIntent>{
        users.current.order.setTime(LocalTime.now())
        goto(CheckOrder)
    }

    onResponse<TellTimeIntent> {
        users.current.order.setTime(it.intent.time?.asLocalTime() ?: LocalTime.now())
        goto(CheckOrder)
    }

    onResponse<TellDateAndTimeIntent> {
        users.current.order.setDate(it.intent.date?.asLocalDate() ?: LocalDate.now())
        users.current.order.setTime(it.intent.time?.asLocalTime() ?: LocalTime.now())
        goto(CheckOrder)
    }

    onResponse<TellAfterHowLongIntent> {
        val now       = LocalTime.now()
        val timePoint = LocalTime.of( now.hour   + (it.intent.inHours?.value ?: 0)
                                    , now.minute + (it.intent.inMinutes?.value ?: 0)
                                    )
        users.current.order.setTime(timePoint)
        goto(CheckOrder)
    }
}


// Request start
val RequestStart : State = state(parent = OrderHandling) {
    onEntry {
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Vart vill du åka från?"
            Language.GERMAN  -> "Von wo möchtest du fahren?"
            else             -> "From where would you like to travel?"
        })
    }

    onResponse {
        var start = it.speech.text
        var startSplit: MutableList<String> = mutableListOf<String>()
        if (GlobalLanguage == Language.ENGLISH_US) {
            startSplit.addAll(start.split("from".toRegex()))
            start = startSplit.last()
            furhat.say("Okay, ${start}")
        }
        else
        {
            startSplit.addAll(start.split("från".toRegex()))
            start = startSplit.last()
            furhat.say("Ok, ${start}")
        }

        users.current.order.setStartPoint(start)
        goto(CheckOrder)
    }
}

// Request destination
val RequestDestination : State = state(parent = OrderHandling) {
    onEntry {
        furhat.ask(when (GlobalLanguage)
        {
            Language.SWEDISH -> "Vart vill du åka?"
            Language.GERMAN  -> "Wohin möchtest du fahren?"
            else             -> "Where do you want to go?"
        })
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

        users.current.order.setDest(destination)
        goto(CheckOrder)
    }
}