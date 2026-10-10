    var familyPaid: Boolean get() = p.getBoolean("familyPaid", false); set(v) = p.edit().putBoolean("familyPaid", v).apply()
    var familySlots: Int get() = p.getInt("slots", 1); set(v) = p.edit().putInt("slots", v).apply()
    fun log(line: String) { p.edit().putString("log", (listOf(line) + p.getString("log", "")!!.lines().filter { it.isNotBlank() }).take(20).joinToString("\n")).apply() }
    fun journal() = p.getString("log", "")!!.lines().filter { it.isNotBlank() }
    fun auto(appId: String) = p.getBoolean("auto_$appId", false)
    fun setAuto(appId: String, v: Boolean) = p.edit().putBoolean("auto_$appId", v).apply()
