package rs.edu.raf.premiereuros.data.remote

import de.jensklingenberg.ktorfit.Ktorfit

expect fun createPremiereApiClient(ktorfit: Ktorfit): PremiereApi
expect fun createShowtimeApiClient(ktorfit: Ktorfit): ShowtimeApi
