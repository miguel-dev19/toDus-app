package cu.todus.app.data.remote

fun randomHexId(len: Int = 16): String = (1..len).map { "abcdef0123456789".random() }.joinToString("")
fun extractAttr(xml: String, attr: String): String? = Regex("""$attr='([^']+)'""").find(xml)?.groupValues?.get(1)
