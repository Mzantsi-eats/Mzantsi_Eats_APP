package com.mzantsi.table.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.mzantsi.table.data.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream


// Shows a picture from the app's assets folder (or a photo file on the phone).
// While it loads, or if there is no picture, it shows the placeholder instead (for example an emoji).
// fallbackPaths are tried in order when the first picture does not exist
// (for example: a culture photo, then a photo of one of that culture's dishes).
@Composable
fun AssetImage(
    path: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackPaths: List<String> = emptyList(),
    placeholder: @Composable BoxScope.() -> Unit = {}
) {
    val context = LocalContext.current
    val allPaths = listOf(path) + fallbackPaths
    val bitmap by produceState<ImageBitmap?>(
        initialValue = allPaths.firstNotNullOfOrNull { AssetImages.cached(it) },
        key1 = allPaths
    ) {
        if (value == null) value = withContext(Dispatchers.IO) {
            allPaths.firstNotNullOfOrNull { AssetImages.load(context, it) }
        }
    }
    Box(modifier) {
        val img = bitmap
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            placeholder()
        }
    }
}


// Turns a name into a file name, for example "Bunny Chow" becomes "bunny_chow".
fun slugOf(text: String): String =
    text.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')


// Finds the picture for a recipe: the user's photo, or assets/images/recipes/<recipe_name>.jpg
fun Recipe.imagePath(): String = when {
    imageRes.startsWith("file:") -> imageRes
    imageRes.isNotBlank() && !imageRes.startsWith("http") -> "recipes/" + slugOf(imageRes)
    else -> "recipes/" + slugOf(title)
}


// assets/images/cultures/<culture_name>.jpg
fun cultureImagePath(culture: String): String = "cultures/" + slugOf(culture)


const val LOGO_IMAGE_PATH = "branding/logo"


const val FEATURED_IMAGE_PATH = "branding/featured"

// Loads pictures and keeps up to 40 in memory, so the same picture isn't loaded twice.
// Pictures that don't exist are remembered too, so we don't keep looking for them.
private object AssetImages {
    private val cache = LruCache<String, ImageBitmap>(40)
    private val missing = HashSet<String>()
    private val extensions = listOf("jpg", "jpeg", "png", "webp")

    fun cached(path: String): ImageBitmap? = cache.get(path)

    fun load(context: Context, path: String, maxDimension: Int = 1024): ImageBitmap? {
        cache.get(path)?.let { return it }
        synchronized(missing) { if (path in missing) return null }

        val bitmap: Bitmap? = if (path.startsWith("file:")) {
            val file = File(path.removePrefix("file:"))
            if (file.exists()) decode(maxDimension) { file.inputStream() } else null
        } else {
            extensions.firstNotNullOfOrNull { ext ->
                decode(maxDimension) { context.assets.open("images/$path.$ext") }
            }
        }

        if (bitmap == null) {
            synchronized(missing) { missing.add(path) }
            return null
        }
        return bitmap.asImageBitmap().also { cache.put(path, it) }
    }

    // Big photos are made smaller while loading, so the app doesn't run out of memory.
    private fun decode(maxDimension: Int, open: () -> InputStream): Bitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open().use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        open().use { BitmapFactory.decodeStream(it, null, options) }
    } catch (e: IOException) {
        null
    }
}
