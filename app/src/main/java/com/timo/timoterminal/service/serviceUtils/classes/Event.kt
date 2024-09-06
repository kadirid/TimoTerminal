package com.timo.timoterminal.service.serviceUtils.classes

import android.os.Parcel
import android.os.Parcelable
import com.timo.timoterminal.utils.Utils
import org.json.JSONObject
import java.util.Calendar
import java.util.Date

class Event(
    private var login_firma: String? = null,
    private var Id: String? = null,
    private var Name: String? = null,
    var StartDate: Date? = null,
    var EndDate: Date? = null,
    var StartDateOriginal: Date? = null,
    var EndDateOriginal: Date? = null,
    private var ResourceId: String? = null,
    private var Resizable: Boolean = false,
    private var Draggable: Boolean = false,
    private var clickable: Boolean = false,
    private var taskId: Int = 0,
    private var projectId: Int = 0,
    private var style: String? = null,
    private var bgColor: String? = null,
    private var type: Int = 0,
    private var hours: String? = null,
    private var eventText: String? = null,
    private var tooltipText: String? = null,
    private var eventType: Int = 0,
    private var ticketNo: String? = null,

    //Kapa Felder
    private var kapaEdit: Boolean = false,
    var capaColor: String? = null,
    private var capaDescription: String? = null,

    //Prüfung ob Element gelöscht werden kann
    private var deleteElement: Boolean = false,

    //Termin Felder
    private var terminId: String? = null,
    private var appointmentBegend: String? = null,

    //Ticket Felder
    private var ticketId: Int = 0,
    private var ticketEdit: Boolean = false,

    //Antrag Felder
    private var antragId: Int = 0,
    private var antragGenehmigen: Boolean = false,

    //Urlaub Felder
    private var urlaubId: Int = 0,

    //Arbeitszeit Felder
    var arbeitszeitId: Int = 0,

    //Checklisten Felder
    private var checklistId: Int = 0,
    private var checklistDone: Boolean = false,
    private var checklistOpen: Boolean = false,

    //Opportunity Felder
    private var opportunityId: Int = 0,
    private var opportunityCustomerConvert: Boolean = false,

    var buchungType: Int = 0,

    var considerAsWorkingTime: Boolean = false,
    private var projectDesc: String? = null,

    private var projectName: String? = null,
    private var taskName: String? = null,

    private var customerName: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this() {
        login_firma = parcel.readString()
        Id = parcel.readString()
        Name = parcel.readString()
        StartDate = Date(parcel.readLong())
        EndDate = Date(parcel.readLong())
        StartDateOriginal = Date(parcel.readLong())
        EndDateOriginal = Date(parcel.readLong())
        ResourceId = parcel.readString()
        Resizable = parcel.readByte() != 0.toByte()
        Draggable = parcel.readByte() != 0.toByte()
        clickable = parcel.readByte() != 0.toByte()
        taskId = parcel.readInt()
        projectId = parcel.readInt()
        style = parcel.readString()
        bgColor = parcel.readString()
        type = parcel.readInt()
        hours = parcel.readString()
        eventText = parcel.readString()
        tooltipText = parcel.readString()
        eventType = parcel.readInt()
        ticketNo = parcel.readString()
        kapaEdit = parcel.readByte() != 0.toByte()
        capaColor = parcel.readString()
        capaDescription = parcel.readString()
        deleteElement = parcel.readByte() != 0.toByte()
        terminId = parcel.readString()
        appointmentBegend = parcel.readString()
        ticketId = parcel.readInt()
        ticketEdit = parcel.readByte() != 0.toByte()
        antragId = parcel.readInt()
        antragGenehmigen = parcel.readByte() != 0.toByte()
        urlaubId = parcel.readInt()
        arbeitszeitId = parcel.readInt()
        checklistId = parcel.readInt()
        checklistDone = parcel.readByte() != 0.toByte()
        checklistOpen = parcel.readByte() != 0.toByte()
        opportunityId = parcel.readInt()
        opportunityCustomerConvert = parcel.readByte() != 0.toByte()
        buchungType = parcel.readInt()
        considerAsWorkingTime = parcel.readByte() != 0.toByte()
        projectDesc = parcel.readString()
        projectName = parcel.readString()
        taskName = parcel.readString()
        customerName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(login_firma)
        parcel.writeString(Id)
        parcel.writeString(Name)
        parcel.writeSerializable(StartDate)
        parcel.writeSerializable(EndDate)
        parcel.writeSerializable(StartDateOriginal)
        parcel.writeSerializable(EndDateOriginal)
        parcel.writeString(ResourceId)
        parcel.writeByte(if (Resizable) 1 else 0)
        parcel.writeByte(if (Draggable) 1 else 0)
        parcel.writeByte(if (clickable) 1 else 0)
        parcel.writeInt(taskId)
        parcel.writeInt(projectId)
        parcel.writeString(style)
        parcel.writeString(bgColor)
        parcel.writeInt(type)
        parcel.writeString(hours)
        parcel.writeString(eventText)
        parcel.writeString(tooltipText)
        parcel.writeInt(eventType)
        parcel.writeString(ticketNo)
        parcel.writeByte(if (kapaEdit) 1 else 0)
        parcel.writeString(capaColor)
        parcel.writeString(capaDescription)
        parcel.writeByte(if (deleteElement) 1 else 0)
        parcel.writeString(terminId)
        parcel.writeString(appointmentBegend)
        parcel.writeInt(ticketId)
        parcel.writeByte(if (ticketEdit) 1 else 0)
        parcel.writeInt(antragId)
        parcel.writeByte(if (antragGenehmigen) 1 else 0)
        parcel.writeInt(urlaubId)
        parcel.writeInt(arbeitszeitId)
        parcel.writeInt(checklistId)
        parcel.writeByte(if (checklistDone) 1 else 0)
        parcel.writeByte(if (checklistOpen) 1 else 0)
        parcel.writeInt(opportunityId)
        parcel.writeByte(if (opportunityCustomerConvert) 1 else 0)
        parcel.writeInt(buchungType)
        parcel.writeByte(if (considerAsWorkingTime) 1 else 0)
        parcel.writeString(projectDesc)
        parcel.writeString(projectName)
        parcel.writeString(taskName)
        parcel.writeString(customerName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }

        fun convertJsonToObject(json: JSONObject): Event {
            return Event(
                json.optString("login_firma"),
                json.optString("Id"),
                json.optString("Name"),
                Date(json.getLong("StartDate")),
                Date(json.getLong("EndDate")),
                Date(json.getLong("StartDateOriginal")),
                Date(json.getLong("EndDateOriginal")),
                json.optString("ResourceId"),
                json.getBoolean("Resizable"),
                json.getBoolean("Draggable"),
                json.getBoolean("clickable"),
                json.getInt("taskId"),
                json.getInt("projectId"),
                json.optString("style"),
                json.optString("bgColor"),
                json.getInt("type"),
                json.optString("hours"),
                json.optString("eventText"),
                json.optString("tooltipText"),
                json.getInt("eventType"),
                json.optString("ticketNo"),
                json.getBoolean("kapaEdit"),
                json.optString("capaColor"),
                json.optString("capaDescription"),
                json.getBoolean("deleteElement"),
                json.optString("terminId"),
                json.optString("appointmentBegend"),
                json.getInt("ticketId"),
                json.getBoolean("ticketEdit"),
                json.getInt("antragId"),
                json.getBoolean("antragGenehmigen"),
                json.getInt("urlaubId"),
                json.getInt("arbeitszeitId"),
                json.getInt("checklistId"),
                json.getBoolean("checklistDone"),
                json.getBoolean("checklistOpen"),
                json.getInt("opportunityId"),
                json.getBoolean("opportunityCustomerConvert"),
                json.getInt("buchungType"),
                json.getBoolean("considerAsWorkingTime"),
                json.optString("projectDesc"),
                json.optString("projectName"),
                json.optString("taskName"),
                json.optString("customerName")
            )
        }

        /**
         * This method helps us to split Events, which are happening between 2 or more days
         * For example a Event which starts at 12 on 01.01 and ends at 8 am on 02.01 is split in
         * two events : 12 - 23:59 on 01.01 and 00:00 - 08:00 on 02.01
         * @param e : Event
         */
        fun splitEventToDaily(e: Event) : ArrayList<Event> {
            val outputEventArr = ArrayList<Event>()
            val datesBetweenStartAndEnd = Utils.daysBetween(e.StartDate!!, e.EndDate!!)
            var i = 0
            while (i <= datesBetweenStartAndEnd) {
                val event = e.clone()
                when (i) {
                    0 -> {
                        val specifiedDate = Calendar.getInstance().apply {
                            time = event.StartDate!!
                        }
                        specifiedDate.set(Calendar.HOUR_OF_DAY, 23)
                        specifiedDate.set(Calendar.MINUTE, 59)
                        specifiedDate.set(Calendar.SECOND, 59)
                        event.EndDate = specifiedDate.time
                        outputEventArr.add(event)
                    }
                    datesBetweenStartAndEnd -> {
                        val specifiedDate = Calendar.getInstance().apply {
                            time = event.EndDate!!
                        }
                        specifiedDate.set(Calendar.HOUR_OF_DAY, 0)
                        specifiedDate.set(Calendar.MINUTE, 0)
                        specifiedDate.set(Calendar.SECOND, 0)
                        event.StartDate = specifiedDate.time
                        outputEventArr.add(event)
                    }
                    else -> {
                        val specifiedStartDate = Calendar.getInstance().apply {
                            time = event.StartDate!!
                        }
                        // add days
                        specifiedStartDate.add(Calendar.DAY_OF_MONTH, 1)
                        specifiedStartDate.set(Calendar.HOUR_OF_DAY, 0)
                        specifiedStartDate.set(Calendar.MINUTE, 0)
                        specifiedStartDate.set(Calendar.SECOND, 0)
                        event.StartDate = specifiedStartDate.time
                        val specifiedEndDate = Calendar.getInstance().apply {
                            time = event.StartDate!!
                        }
                        specifiedEndDate.add(Calendar.DAY_OF_MONTH, 1)
                        specifiedEndDate.set(Calendar.HOUR_OF_DAY, 23)
                        specifiedEndDate.set(Calendar.MINUTE, 59)
                        specifiedEndDate.set(Calendar.SECOND, 59)
                        event.EndDate = specifiedEndDate.time
                        outputEventArr.add(event)
                    }
                }
                i++
            }
            return outputEventArr
        }
    }
    fun clone(): Event {
        return Event(
            this.login_firma,
            this.Id,
            this.Name,
            this.StartDate,
            this.EndDate,
            this.StartDateOriginal,
            this.EndDateOriginal,
            this.ResourceId,
            this.Resizable,
            this.Draggable,
            this.clickable,
            this.taskId,
            this.projectId,
            this.style,
            this.bgColor,
            this.type,
            this.hours,
            this.eventText,
            this.tooltipText,
            this.eventType,
            this.ticketNo,
            this.kapaEdit,
            this.capaColor,
            this.capaDescription,
            this.deleteElement,
            this.terminId,
            this.appointmentBegend,
            this.ticketId,
            this.ticketEdit,
            this.antragId,
            this.antragGenehmigen,
            this.urlaubId,
            this.arbeitszeitId,
            this.checklistId,
            this.checklistDone,
            this.checklistOpen,
            this.opportunityId,
            this.opportunityCustomerConvert,
            this.buchungType,
            this.considerAsWorkingTime,
            this.projectDesc,
            this.projectName,
            this.taskName,
            this.customerName
        )
    }
}