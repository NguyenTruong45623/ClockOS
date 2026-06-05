package truong.nv.clockos.data.models

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String?): List<Int>? {
        return if (data.isNullOrEmpty()) {
            emptyList()
        } else {
            data.split(",").mapNotNull { it.toIntOrNull() }
        }
    }
}
