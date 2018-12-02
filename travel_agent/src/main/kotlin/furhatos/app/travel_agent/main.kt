package furhatos.app.travel_agent

import furhatos.app.travel_agent.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*
import java.awt.PageAttributes.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType as OkMediaType



class Travel_agentSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}

fun main(args: Array<String>) {
    val client = OkHttpClient()

    val mediaType = OkMediaType.parse("multipart/form-data")
    val body = RequestBody.create(mediaType, "inpPointFr_ajax=Ume%25E5%2B%25D6stermalmsgatan%257C25340%257C0&inpPointTo_ajax=Ume%25E5%2BVasaplan%257C25332%257C0&inpPointInterm_ajax=&selRegionFr=741&inpPointFr=&optTypeFr=0&inpPointTo=&optTypeTo=0&inpPointInterm=&selDirection=0&inpTime=21%253A16&inpDate=2018-12-01&optReturn=0&selDirection2=0&inpTime2=00%253A10&inpDate2=2018-12-02&trafficmask=2&selChangeTime=0&selWalkSpeed=0&selPriority=0&cmdAction=search&EU_Spirit=False&TNSource=UMEA&SupportsScript=True&Language=se&VerNo=&Source=querypage_adv&MapParams=")
    val request = Request.Builder()
            .url("https://reseplanerare.fskab.se/umea/v2/querypage_adv.aspx")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            //.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Postman-Token", "d48e3e9c-2151-44c6-8f7b-2779277b955a")
            .build()

    val response = client.newCall(request).execute()
    // val resBody = response.body()?.string()
    System.out.println("Response:")
    System.out.println(response.message())
    System.out.println(response.body())
    System.out.println(response.body()!!.contentType())
    System.out.println(response.body()?.string())
}
