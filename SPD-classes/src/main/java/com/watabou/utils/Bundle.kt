/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.utils

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.lang.Enum.valueOf
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Wrapper for [JSONObject]. Use to store saves and configurations.
 *
 * WARNING: NOT ALL METHODS IN ORG.JSON ARE PRESENT ON ANDROID/IOS!
 * Many methods which work on desktop will cause the game to crash on Android and iOS
 *
 * This is because the Android runtime includes its own version of org.json which does not
 * implement all methods. MobiVM uses the Android runtime and so this applies to iOS as well.
 *
 * org.json is very fast (~2x faster than libgdx JSON), which is why the game uses it despite
 * this dependency conflict.
 *
 * See https://developer.android.com/reference/org/json/package-summary for details on
 * what methods exist in all versions of org.json. This class is also commented in places
 * Where Android/iOS force the use of unusual methods.
 */
class Bundle private constructor(
    private val data: JSONObject
) {

    constructor() : this(JSONObject())

    override fun toString() = data.toString()

    operator fun contains(key: String) = !data.isNull(key)

    fun remove(key: String): Boolean = data.remove(key) != null

    // region get...

    operator fun get(key: String): Bundlable? = getBundle(key)?.get()

    // JSONObject.keyset() doesn't exist on Android/iOS
    fun getKeys(): List<String> = data.keys().asSequence().toList()

    fun getBoolean(key: String): Boolean = data.optBoolean(key)

    fun getInt(key: String): Int = data.optInt(key)

    fun getLong(key: String): Long = data.optLong(key)

    fun getFloat(key: String): Float = data.optDouble(key, 0.0).toFloat()

    fun getString(key: String): String = data.optString(key)

    fun getClass(key: String): Class<*>? =
        Reflection.forName(
            getString(key)
                .replace("class ", "")
                .takeUnless(""::equals)
                ?.let { cls -> aliases[cls] ?: cls }
        )

    fun getBundle(key: String): Bundle? = data.optJSONObject(key)?.let(::Bundle)

    private fun get(): Bundlable? {
        return (Reflection.newInstance(Reflection.forName(getString(CLASS_NAME).let { cls ->
            aliases[cls] ?: cls
        }) ?: return null) as Bundlable).also {
            it.restoreFromBundle(this)
        }
    }

    fun <E : Enum<E>> getEnum(key: String, enumClass: Class<E>): E =
        valueOf(enumClass, data.getString(key))

    fun getIntArray(key: String) = getArray(key, ::IntArray, JSONArray::getInt)

    fun getLongArray(key: String) = getArray(key, ::LongArray, JSONArray::getLong)

    fun getFloatArray(key: String) = getArray(key, ::FloatArray) {
        optDouble(it, 0.0).toFloat()
    }

    fun getBooleanArray(key: String) = getArray(key, ::BooleanArray, JSONArray::getBoolean)

    fun getStringArray(key: String) = getArray(key, ::Array, JSONArray::getString)

    fun getClassArray(key: String): Array<Class<*>> = getArray(key, ::Array) { index ->
        Reflection.forName(
            getString(index).replace("class ", "")
                .let { cls -> aliases[cls] ?: cls }
        )
    }

    @JvmOverloads
    fun getBundleArray(key: String = DEFAULT_KEY) = getArray(key, ::Array) {
        Bundle(getJSONObject(it))
    }

    fun getCollection(key: String): Collection<Bundlable> = getArray(
        key,
        // this exploits the fact that filterNotNull returns a collection
        initArray = { size, init -> Array(size, init).filterNotNull() },
        getValue = { Bundle(getJSONObject(it)).get() }
    )

    /**
     * Gets an array from [data] corresponding to [key].
     * @param initArray The constructor for the array. Determines the return type of the function
     * @param getValue The method to be used to extract each value from the retrieved [JSONArray].
     */
    private inline fun <R, RArray> getArray(
        key: String,
        initArray: (size: Int, ((index: Int) -> R)) -> RArray,
        crossinline getValue: JSONArray.(index: Int) -> R,
    ) = data.getJSONArray(key).run {
        initArray(length()) { getValue(it) }
    }

    // endregion

    // region put...

    @JvmName("put")
    operator fun set(key: String, value: Boolean?) {
        data.put(key, value)
    }

    @JvmName("put")
    operator fun set(key: String, value: Int?) {
        data.put(key, value)
    }

    @JvmName("put")
    operator fun set(key: String, value: Long?) {
        data.put(key, value)
    }

    @JvmName("put")
    operator fun set(key: String, value: Float?) {
        data.put(key, value?.toDouble())
    }

    @JvmName("put")
    operator fun set(key: String, value: String?) {
        data.put(key, value)
    }

    @JvmName("put")
    operator fun set(key: String, value: Class<*>?) {
        data.put(key, value)
    }

    @JvmName("put")
    operator fun set(key: String, bundle: Bundle?) {
        data.put(key, bundle?.data)
    }

    @JvmName("put")
    operator fun set(key: String, obj: Bundlable?) {
        data.put(key, storeObject(obj))
    }

    @JvmName("put")
    operator fun set(key: String, value: Enum<*>?) {
        data.put(key, value?.name)
    }

    @JvmName("put")
    operator fun set(key: String, array: IntArray?) {
        put(key, array, IntArray::forEachIndexed, JSONArray::put)
    }

    @JvmName("put")
    operator fun set(key: String, array: LongArray?) {
        put(key, array, LongArray::forEachIndexed, JSONArray::put)
    }

    @JvmName("put")
    operator fun set(key: String, array: FloatArray?) {
        put(key, array, FloatArray::forEachIndexed) { index, float ->
            put(index, float.toDouble())
        }
    }

    @JvmName("put")
    operator fun set(key: String, array: BooleanArray?) {
        put(key, array, BooleanArray::forEachIndexed, JSONArray::put)
    }

    @JvmName("put")
    operator fun set(key: String, array: Array<String>?) {
        put(key, array, JSONArray::put)
    }

    @JvmName("put")
    operator fun set(key: String, array: Array<Class<*>>?) {
        put(key, array) { i, c -> put(i, c.name) }
    }

    @JvmName("put")
    operator fun set(key: String, collection: Collection<Bundlable?>?) {
        data.put(key, collection?.let { col ->
            JSONArray(col.mapNotNull { storeObject(it) })
        } ?: JSONArray())
    }

    /**
     * Wrapper method for the various set methods that handle arrays.
     * Because primitive arrays have different forEachIndexed signatures, the [forEachIndexed] param takes it so it can be used anyway.
     * @param key the key to store the array to
     * @param array the array to store
     * @param forEachIndexed the primitive array method corresponding to [Array.forEachIndexed]
     * @param putValue the corresponding put method from [JSONArray]
     */
    private inline fun <T, TArray> put(
        key: String, array: TArray?,
        forEachIndexed: TArray.(action: (index: Int, value: T) -> Unit) -> Unit,
        crossinline putValue: JSONArray.(Int, T) -> Unit
    ) {
        data.put(key, JSONArray().apply {
            array?.forEachIndexed { index, value -> putValue(index, value) }
        })
    }

    /**
     * A shortcut method variant of [put] for [Array] objects using [Array.forEachIndexed]
     * @param putValue the corresponding put method from [JSONArray]
     **/
    private inline fun <T> put(
        key: String, array: Array<T>?,
        crossinline putValue: JSONArray.(Int, T) -> Unit,
    ) = put(key, array, Array<T>::forEachIndexed, putValue)

    // endregion

    /**
     * Writes contents of the bundle into a stream.
     * @param stream stream to write into
     * @param compressed whether the data should be compressed
     */
    fun toStream(stream: OutputStream, compressed: Boolean = COMPRESSION) {
        val writer =
            if (compressed) BufferedWriter(
                OutputStreamWriter(GZIPOutputStream(stream, GZIP_BUFFER))
            )
            else BufferedWriter(OutputStreamWriter(stream))
        writer.write(data.toString()) // JSONObject.write doesn't exist on Android/iOS
        writer.close()
        stream.close()
    }

    companion object {

        private const val CLASS_NAME = "__className"
        private const val DEFAULT_KEY = "key"

        private val aliases = HashMap<String, String>()

        // Turn this off for save data debugging.
        private const val COMPRESSION = true
        private const val GZIP_BUFFER = 1024 * 4 // 4Kb

        /**
         * Produces a bundle from the stream.
         * @param stream stream to read data from
         * @return resulting bundle
         */
        @JvmStatic
        fun read(stream: InputStream): Bundle {

            // JSONTokenizer only has a string-based constructor on Android/iOS.
            var json = JSONTokener(buildString {
                BufferedReader(InputStreamReader(stream.checkCompression()))
                    .forEachLine { append("$it\n") }
            }).nextValue()

            // If the data is an array, put it in a fresh object with the default key.
            if (json is JSONArray) json = JSONObject().put(DEFAULT_KEY, json)

            return Bundle(json as JSONObject)
        }

        private fun InputStream.checkCompression(): InputStream {

            if (!markSupported()) return BufferedInputStream(this, 2).checkCompression()

            // Determine if it's a regular or compressed file.
            mark(2)
            val header = ByteArray(2).also(::read)
            reset()

            // GZIP header is 0x1f8b.
            return if (header[0] == 0x1f.toByte() && header[1] == 0x8b.toByte())
                GZIPInputStream(this, GZIP_BUFFER)
            else
                this
        }

        private fun storeObject(obj: Bundlable?): JSONObject? = obj?.javaClass
            // Skip none-static inner classes as they can't be instantiated through bundle restoring.
            // Classes which make use of none-static inner classes must manage instantiation manually.
            ?.takeUnless { Reflection.isMemberClass(it) && !Reflection.isStatic(it) }
            ?.let { cl ->
                Bundle().also {
                    it[CLASS_NAME] = cl.name
                    obj.storeInBundle(it)
                }.data
            }

        /**
         * Adds an alias to the class.
         * This will essentially convert bundled [aliased][alias] object into the specified [class][cl] the first time the bundle is loaded.
         * Aliases can be used to remove items from the game without breaking the existing save files.
         * @param alias old class name as it was saved into the bundle
         * @param cl class to convert found aliased objects into
         */
        @JvmStatic
        fun addAlias(alias: String, cl: Class<*>) {
            aliases[alias] = cl.name
        }

        /**
         * Adds class aliases.
         * This will essentially convert bundled aliased objects into the specified classes the first time the bundle is loaded.
         * Aliases can be used to remove items from the game without breaking the existing save files.
         * @param aliases map of the aliases and their respected classes
         */
        fun addAliases(aliases: Map<String, Class<*>>) {
            aliases.forEach {
                addAlias(it.key, it.value)
            }
        }
    }
}