/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.settings

import scalismo.ui.settings.SettingsFile.Codec

import scala.util._

/**
 * High-level abstraction for storing user preferences / settings.
 *
 * Settings are identified by keys, and can have one or more values.
 * Values can be of any type, as long as a codec for that type is defined below,
 * or in a type class provided by the user.
 *
 * Applications can define their own keys;
 * to avoid name clashes, keys should have package-like names. "common" is reserved
 * for the keys defined in the [[GlobalSettings]] source file.
 *
 */
class PersistentSettings(val settingsFile: SettingsFile) {

  /**
   * Returns a single-valued setting, wrapped in an Option. Returns None if the key is not found, or an error occured.
   *
   * If multiple values with this key are present, the first is returned.
   *
   * @param key settings key
   * @tparam A type of the setting's value.
   * @return the first value with the appropriate key, or None on error/not found.
   */
  def get[A: Codec](key: String): Option[A] = {
    val result = getList(key)
    result.flatMap(_.headOption)
  }

  /**
   * Returns a multi-valued setting, wrapped in an Option.
   *
   * Returns an empty list if the key is not found, None if an error occurred.
   *
   * @param key settings key
   * @tparam A type of the setting's value
   * @return all values for the key, or None on error.
   */
  def getList[A: Codec](key: String): Option[List[A]] = {

    Try(doGet(key)) match {
      case Failure(_) => None
      case Success(r) => Some(r.map(s => implicitly[Codec[A]].fromString(s)))
    }
  }

  /**
   * Sets a single-valued setting.
   *
   * All previous settings for the respective key are discarded and replaced by the new value.
   *
   * @param key   settings key
   * @param value settings value
   * @tparam A type of the setting's value.
   * @return Failure on error, Success otherwise
   */
  def set[A: Codec](key: String, value: A): Try[Unit] = {
    setList(key, List(value))
  }

  /**
   * Sets a multi-valued setting.
   *
   * All previous settings for the respective key are discarded and replaced by the new values.
   *
   * @param key    settings key
   * @param values settings values
   * @tparam A type of the setting's values.
   * @return Failure on error, Success otherwise
   */
  def setList[A: Codec](key: String, values: List[A]): Try[Unit] = {
    Try(doSet(key, values.map(v => implicitly[Codec[A]].toString(v)))) match {
      case Success(_)    => Success(())
      case Failure(oops) => Failure(oops)
    }
  }

  private def doGet(key: String): List[String] = {
    val sf: SettingsFile = settingsFile
    sf.getValues(key)
  }

  private def doSet(key: String, vals: List[String]): Unit = {
    val sf: SettingsFile = settingsFile
    sf.setValues(key, vals)
  }
}
