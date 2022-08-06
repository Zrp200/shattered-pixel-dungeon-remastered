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

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

// Helper methods for setting/using a default base path and file address mode
private lateinit var defaultFileType: Files.FileType
private lateinit var defaultPath: String

fun setDefaultFileProperties(type: Files.FileType, path: String) {
    defaultFileType = type
    defaultPath = path
}

fun getAsset(name: String): FileHandle = Gdx.files.internal(name)

fun getFileHandle(name: String): FileHandle = getFileHandle(defaultPath, name)

private fun getFileHandle(basePath: String, name: String): FileHandle = when (defaultFileType) {
    Files.FileType.Classpath -> Gdx.files.classpath(basePath + name)
    Files.FileType.Internal -> Gdx.files.internal(basePath + name)
    Files.FileType.External -> Gdx.files.external(basePath + name)
    Files.FileType.Absolute -> Gdx.files.absolute(basePath + name)
    Files.FileType.Local -> Gdx.files.local(basePath + name)
}

// Files

//returns length of a file in bytes, or 0 if file does not exist
fun fileLength(name: String): Long = with(getFileHandle(name)) { if (!exists()) 0 else length() }

fun deleteFile(name: String): Boolean = getFileHandle(name).delete()

//replaces a file with junk data, for as many bytes as given
//This is helpful as some cloud sync systems do not persist deleted, empty, or zeroed files
fun overwriteFile(name: String, length: Int) {
    getFileHandle(name).writeBytes(ByteArray(length) { 1 }, false)
}

// Directories
fun dirExists(name: String): Boolean = with(getFileHandle(name)) { exists() && isDirectory }

fun deleteDir(name: String): Boolean = with(getFileHandle(name)) { if (!isDirectory) false else deleteDirectory() }

fun filesInDir(name: String): List<String> = with(getFileHandle(name)) { if (isDirectory) list().map { name } else emptyList() }

// bundle reading
//only works for base path
@Throws(IOException::class)
fun bundleFromFile(fileName: String): Bundle = try {
    bundleFromStream(getFileHandle(fileName).read())
} catch (e: GdxRuntimeException) {
    throw IOException(e)
}

@Throws(IOException::class)
private fun bundleFromStream(input: InputStream) = Bundle.read(input).also { input.close() }

// bundle writing
//only works for base path
@Throws(IOException::class)
fun bundleToFile(fileName: String, bundle: Bundle) {
    try {
        with(getFileHandle(fileName)) {
            //write to a temp file, then move the files.
            // This helps prevent save corruption if writing is interrupted
            if (exists()) {
                getFileHandle("$fileName.tmp").let { temp ->
                    bundleToStream(temp.write(false), bundle)
                    delete()
                    temp.moveTo(this)
                }
            } else {
                bundleToStream(write(false), bundle)
            }
        }

    } catch (e: GdxRuntimeException) {
        //game classes expect an IO exception, so wrap the GDX exception in that
        throw IOException(e)
    }
}

@Throws(IOException::class)
private fun bundleToStream(output: OutputStream, bundle: Bundle) = Bundle.write(bundle, output).also { output.close() }

/**
 * Checks for any evidence of interrupted saving and recovers.
 * Replaces the base files with the temporary ones if the base ones are invalid or temporary are valid and newer.
 * @return whether temporary files were found
 */
@JvmOverloads
fun cleanTempFiles(dirName: String = ""): Boolean {
    var foundTemp = false
    for (file in getFileHandle(dirName).list()) {
        if (file.isDirectory) foundTemp = cleanTempFiles(dirName + file.name()) || foundTemp
        else {
            if (file.name().endsWith(".tmp")) {
                val original = getFileHandle("", file.path().replace(".tmp", ""))
                try {
                    bundleFromStream(file.read())
                    try {
                        bundleFromStream(original.read())
                        if (file.lastModified() > original.lastModified()) {
                            file.moveTo(original)
                        } else {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        file.moveTo(original)
                    }
                } catch (e: Exception) {
                    file.delete()
                }
                foundTemp = true
            }
        }
    }
    return foundTemp
}
