package com.renju_note.isoo.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.renju_note.isoo.data.BoardColorSetting
import io.realm.kotlin.types.RealmList

class BoardPreview(context : Context, attrs : AttributeSet) : View(context, attrs) {

    private var lineInterval = 0f
    private val paint = Paint()
    private val boardColorSetting = BoardColorSetting.getDefaultSetting()
    private val stones = ArrayList<MiniStone>()

    inner class MiniStone(private val x : Int, private val y : Int, private val stoneColor : Int) {
        fun draw(canvas : Canvas) {
            paint.style = Paint.Style.FILL
            paint.color = stoneColor
            canvas.drawCircle(getRealX(x), getRealY(y), lineInterval/2, paint)
        }
    }

    init {
        setBackgroundColor(Color.parseColor(boardColorSetting.boardColor))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        lineInterval = MeasureSpec.getSize(widthMeasureSpec).toFloat() / 15
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor(boardColorSetting.lineColor)
        paint.strokeWidth = (width / 351.3 + 0.5).toInt().toFloat()
        canvas!!.drawRect(lineInterval*0.5f, lineInterval*0.5f, lineInterval*14.5f, lineInterval*14.5f, paint)
        stones.forEach {
            it.draw(canvas)
        }
    }

    fun putStone(sequence : RealmList<String>) {
        stones.clear()
        for((count, coord) in sequence.withIndex()) {
            val point = coord.split("/")
            val x = point[0].toInt()
            val y = point[1].toInt()
            if(count % 2 == 0) {
                stones.add(MiniStone(x, y, Color.BLACK))
            } else {
                stones.add(MiniStone(x, y, Color.WHITE))
            }
        }
        invalidate()
    }

    private fun getRealX(x : Int) : Float {
        return lineInterval/2 + x*lineInterval
    }

    private fun getRealY(y : Int) : Float {
        return lineInterval/2 + y*lineInterval
    }

}