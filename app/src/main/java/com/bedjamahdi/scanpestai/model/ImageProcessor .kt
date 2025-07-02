package com.bedjamahdi.scanpestai.model

import android.graphics.*
import android.util.Log

object ImageProcessor {

    fun drawBoundingBoxesOnBitmap(bitmap: Bitmap, boundingBoxes: List<BoundingBox>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Scale factor based on image size
        val scaleFactor = (bitmap.width + bitmap.height) / 2000f  // Adjust this divisor as needed

        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f * scaleFactor  // Scale stroke width
        }

        val textPaint = Paint().apply {
            color = Color.RED
            textSize = 40f * scaleFactor  // Scale text size
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        boundingBoxes.forEachIndexed { index, box ->
            val left = box.x1 * bitmap.width
            val top = box.y1 * bitmap.height
            val right = box.x2 * bitmap.width
            val bottom = box.y2 * bitmap.height

            val labelText = "${index+1} ${box.clsName} ${(box.cnf * 100).toInt()}%"

            Log.d("ImageProcessor", "Drawing -> $labelText at [$left, $top, $right, $bottom]")

            canvas.drawRect(left, top, right, bottom, boxPaint)
            canvas.drawText(labelText, left, if (top - 10 > textPaint.textSize) top - 10 else top + textPaint.textSize, textPaint)
        }

        return mutableBitmap
    }


        fun drawBoundingBoxesOnBitmap1(bitmap: Bitmap, boundingBoxes: List<com.bedjamahdi.scanpestai.RoomDatabase.BoundingBox>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Scale factor based on image size
        val scaleFactor = (bitmap.width + bitmap.height) / 2000f  // Adjust this divisor as needed

        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f * scaleFactor  // Scale stroke width
        }

        val textPaint = Paint().apply {
            color = Color.RED
            textSize = 40f * scaleFactor  // Scale text size
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        boundingBoxes.forEachIndexed { index, box ->
            val left = box.x1 * bitmap.width
            val top = box.y1 * bitmap.height
            val right = box.x2 * bitmap.width
            val bottom = box.y2 * bitmap.height

            val labelText = "${index+1} ${box.clsName} ${(box.cnf * 100).toInt()}%"

            Log.d("ImageProcessor", "Drawing -> $labelText at [$left, $top, $right, $bottom]")

            canvas.drawRect(left, top, right, bottom, boxPaint)
            canvas.drawText(labelText, left, if (top - 10 > textPaint.textSize) top - 10 else top + textPaint.textSize, textPaint)
        }

        return mutableBitmap
    }

}
