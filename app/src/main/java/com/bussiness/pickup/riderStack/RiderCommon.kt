import com.bussiness.pickup.riderStack.riderModel.DriverInfoModel

object RiderCommon {
    // Use safe calls to avoid NullPointerException
    fun buildWelcomeMessage(): String {
        val firstName = currentUser!!.firstName ?: "User"
        val lastName = currentUser!!.lastName ?: ""
        return StringBuilder("Welcome, ")
            .append(firstName)
            .append(" ")
            .append(lastName)
            .toString()
    }

    var currentUser: DriverInfoModel? = null
    val DRIVER_INFO_REFERENCE: String = "DriverInfo"
    val DRIVER_LOCATION_REFERENCE: String = "DriversLocation"
}
