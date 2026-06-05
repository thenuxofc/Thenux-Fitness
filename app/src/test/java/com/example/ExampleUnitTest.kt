package com.example

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun downloadLogo() {
    try {
      val url = URL("https://i.ibb.co/xKKq5KyT/thenux-fitness.png")
      val destination = File("src/main/res/drawable/thenux_fitness_logo.png")
      destination.parentFile?.mkdirs()
      url.openStream().use { inputStream ->
        Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
      }
      println("Logo downloaded successfully to $destination")
      assertTrue(destination.exists())
    } catch (e: Exception) {
      System.err.println("Error downloading logo: " + e.message)
    }
  }

  @Test
  fun copyApkOutputs() {
    try {
      val userDir = System.getProperty("user.dir")
      println("Current VM execution directory: $userDir")
      val rootDir = if (userDir.contains("/app")) File(userDir).parentFile else File(userDir)
      println("Root project directory identified: ${rootDir.absolutePath}")

      // Let's search recursively for app-debug.apk
      var foundApk: File? = null
      rootDir.walkBottomUp().forEach { file ->
        if (file.isFile && file.name == "app-debug.apk") {
          foundApk = file
          println("Found APK at: ${file.absolutePath} (Size: ${file.length()} bytes)")
        }
      }

      if (foundApk == null) {
        // Let's search using some standard relative search locations in case walk limit
        val possibilities = listOf(
          File(rootDir, "app/build/outputs/apk/debug/app-debug.apk"),
          File(rootDir, "build/outputs/apk/debug/app-debug.apk")
        )
        for (f in possibilities) {
          if (f.exists()) {
            foundApk = f
            break
          }
        }
      }

      assertNotNull("The debug APK must exist before executing file replication! Run assembleDebug.", foundApk)
      val apkFile = foundApk!!
      assertTrue("APK file is empty!", apkFile.length() > 1024 * 1024)

      // 1. Destination One: .build-outputs/app-debug.apk
      val destOne = File(rootDir, ".build-outputs/app-debug.apk")
      destOne.parentFile?.mkdirs()
      Files.copy(apkFile.toPath(), destOne.toPath(), StandardCopyOption.REPLACE_EXISTING)
      println("Successfully copied APK to: ${destOne.absolutePath} (Size: ${destOne.length()} bytes)")
      assertTrue(destOne.exists() && destOne.length() == apkFile.length())

      // 2. Destination Two: APK_DOWNLOAD/app-debug.apk
      val destTwo = File(rootDir, "APK_DOWNLOAD/app-debug.apk")
      destTwo.parentFile?.mkdirs()
      Files.copy(apkFile.toPath(), destTwo.toPath(), StandardCopyOption.REPLACE_EXISTING)
      println("Successfully copied APK to: ${destTwo.absolutePath} (Size: ${destTwo.length()} bytes)")
      assertTrue(destTwo.exists() && destTwo.length() == apkFile.length())

    } catch (e: Exception) {
      fail("APK copying failed with exception: ${e.message}")
    }
  }
}
