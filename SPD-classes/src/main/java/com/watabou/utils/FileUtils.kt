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

private lateinit var defaultFileType: Files.FileType
private lateinit var defaultPath: String

/**
 * Sets up [defaultFileType] and [defaultPath] for the platform.
 * Must be done strictly before any other file manipulations.
 * These settings specify the location of the directory where saves and configs of the player are stored.
 * @param type default file type
 * @param path default file path
 */
fun setDefaultFileProperties(type: Files.FileType, path: String) {
    defaultFileType = type
    defaultPath = path
}

// region FileHandles

/**
 * Obtains [FileHandle] of an internal file (asset).
 * @param path internal path to the asset in question
 * @return [FileHandle] of the asset
 */
fun getAsset(path: String): FileHandle = Gdx.files.internal(path)

private fun getFileHandle(path: String): FileHandle = getFileHandle(defaultPath, path)

private fun getFileHandle(basePath: String, path: String): FileHandle = when (defaultFileType) {
    Files.FileType.Classpath -> Gdx.files.classpath(basePath + path)
    Files.FileType.Internal -> Gdx.files.internal(basePath + path)
    Files.FileType.External -> Gdx.files.external(basePath + path)
    Files.FileType.Absolute -> Gdx.files.absolute(basePath + path)
    Files.FileType.Local -> Gdx.files.local(basePath + path)
}

// endregion

// region Files

/**
 * Obtains length of the file.
 * @param path path to the file in question
 * @returns length of the file in bytes, or 0 if the file doesn't exist
 */
fun fileLength(path: String): Long = with(getFileHandle(path)) { if (!exists()) 0 else length() }

/**
 * Deletes the file.
 * @param path path to the file in question
 * @return whether the file was found and deleted
 */
fun deleteFile(path: String): Boolean = getFileHandle(path).delete()

/**
 * Replaces a file with junk data, for as many bytes as given.
 * @param path path to the file in question
 * @param length length of the junk data in bytes
 */
//This is helpful as some cloud sync systems do not persist deleted, empty, or zeroed files
fun overwriteFile(path: String, length: Int) {
    getFileHandle(path).writeBytes(ByteArray(length) { 1 }, false)
}

// endregion

// region Directories
/**
 * Checks if the directory exists.
 * @param path path to the directory in question
 * @return whether the directory at the specified path exists
 */
fun dirExists(path: String): Boolean = with(getFileHandle(path)) { exists() && isDirectory }

/**
 * Deletes the directory.
 * @param path path to the directory in question
 * @return whether the directory was found and deleted
 */
fun deleteDir(path: String): Boolean = with(getFileHandle(path)) { if (!isDirectory) false else deleteDirectory() }

/**
 * Lists files in the directory.
 * @param path path to the directory in question
 */
fun filesInDir(path: String): List<String> = with(getFileHandle(path)) { if (isDirectory) list().map { path } else emptyList() }

// endregion

// region Bundles

/**
 * Writes content of the file into a bundle.
 * @return resulting bundle
 */
@Throws(IOException::class)
fun String.toBundle(): Bundle? = try { getFileHandle(this).read().toBundle() } catch (e: GdxRuntimeException) { throw IOException(e) }

private fun InputStream.toBundle() = Bundle.read(this).also { this.close() }

/**
 * Writes content of the bundle into a file.
 * @param path path of the file to write into
 */
@Throws(IOException::class)
fun Bundle.toFile(path: String) {
    try {
        getFileHandle(path).let { file ->
            // Write to a temp file, then move the files.
            // This helps to prevent save corruption if writing is interrupted.
            if (file.exists()) {
                getFileHandle("$path.tmp").let { temp ->
                    toStream(temp.write(false))
                    file.delete()
                    temp.moveTo(file)
                }
            } else {
                toStream(file.write(false))
            }
        }
    } catch (e: GdxRuntimeException) {
        throw IOException(e)
    }
}

/**
 * Checks for any evidence of interrupted saving and recovers.
 * Replaces the base files with the temporary ones if the base ones are invalid or temporary are valid and newer.
 * @param path path to the directory in question
 * @return whether temporary files were found
 */
@JvmOverloads
fun cleanTempFiles(path: String = ""): Boolean {
    var foundTemp = false
    for (file in getFileHandle(path).list()) {
        if (file.isDirectory) foundTemp = cleanTempFiles(path + file.name()) || foundTemp
        else {
            if (file.name().endsWith(".tmp")) {
                val original = getFileHandle("", file.path().replace(".tmp", ""))
                try {
                    file.read().toBundle()
                    try {
                        original.read().toBundle()
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

// endregion
