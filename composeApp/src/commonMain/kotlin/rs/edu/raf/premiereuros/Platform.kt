package rs.edu.raf.premiereuros

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform