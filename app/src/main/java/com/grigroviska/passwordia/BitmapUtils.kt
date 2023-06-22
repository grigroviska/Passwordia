import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import com.grigroviska.passwordia.R

object BitmapUtils {

    fun generateInitialsBitmap(context: Context, initials: String, textSize: Float, imageSize: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.firstColor)
            this.textSize = textSize
            isAntiAlias = true
        }

        val centerX = imageSize / 2f
        val centerY = imageSize / 2f
        val radius = imageSize / 2f

        // Draw circular background
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Set paint properties for drawing initials
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER

        // Calculate text bounds
        val textBounds = Rect()
        paint.getTextBounds(initials, 0, initials.length, textBounds)

        // Calculate the baseline of the text
        val textBaseline = centerY - textBounds.centerY().toFloat()

        // Draw initials on the canvas
        canvas.drawText(initials, centerX, textBaseline, paint)

        return bitmap
    }
}
