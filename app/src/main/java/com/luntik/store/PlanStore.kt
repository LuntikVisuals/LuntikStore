    var pauseLeft: Long get() = p.getLong("pauseLeft", 0); set(v) = p.edit().putLong("pauseLeft", v).apply()
    var familyPaid: Boolean get() = p.getBoolean("familyPaid", false); set(v) = p.edit().putBoolean("familyPaid", v).apply()
