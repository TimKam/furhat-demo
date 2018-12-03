package furhatos.app.travel_agent.flow

import furhatos.app.travel_agent.nlu.OrderBusIntent
import furhatos.flow.kotlin.NullSafeUserDataDelegate
import furhatos.records.User
import furhatos.util.Language

// Associate an order to a user
val User.order by NullSafeUserDataDelegate { OrderBusIntent() }

//var GlobalLanguage = Language.ENGLISH_US
var GlobalLanguage = Language.SWEDISH
