package rs.edu.raf.premiereuros.data.remote

import de.jensklingenberg.ktorfit.Ktorfit

actual fun createPremiereApiClient(ktorfit: Ktorfit): PremiereApi {
    return ktorfit.createPremiereApi()
}

actual fun createShowtimeApiClient(ktorfit: Ktorfit): ShowtimeApi {
    return ktorfit.createShowtimeApi()
}
