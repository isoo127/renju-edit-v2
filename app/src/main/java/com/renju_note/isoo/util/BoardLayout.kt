package com.renju_note.isoo.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.renju_note.isoo.data.BoardColorSetting
import com.renju_note.isoo.data.BoardDisplaySetting
import java.io.Serializable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class BoardLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs), Serializable {

    private var lineInterval : Float = 0f
    private var density : Float = 0f
    private val paint = Paint()
    private val bounds = Rect()

    private var boardColorSetting = BoardColorSetting.getDefaultSetting()
    private var boardDisplaySetting = BoardDisplaySetting.getDefaultSetting()

    private val stones = HashMap<String, StoneView>()
    private val childs = ArrayList<Child>()
    private val drawingElements = ArrayList<Element>() // Line, Area, Arrow
    private val points = ArrayList<Point>()

    interface Element {
        fun draw(canvas : Canvas?)
    }

    inner class Line(private val startX : Int, private val startY : Int, private val endX : Int, private val endY : Int) : Element {
        private val lineColor = boardColorSetting.drawLineColor

        override fun draw(canvas : Canvas?) {
            paint.apply {
                color = Color.parseColor(lineColor)
                style = Paint.Style.STROKE
                strokeWidth = lineInterval / 9.4107f
                pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            }
            canvas?.drawLine(getRealX(startX), getRealY(startY), getRealX(endX), getRealY(endY), paint)
        }

        fun hasNoLength() : Boolean {
            return (startX == endX) && (startY == endY)
        }
    }

    inner class Area(private var left : Int, private var top : Int, private var right : Int, private var bottom : Int) : Element {
        private val areaColor = boardColorSetting.drawAreaColor

        override fun draw(canvas : Canvas?) {
            paint.apply {
                color = Color.parseColor(areaColor)
                style = Paint.Style.FILL
            }
            if(left > right) {
                val tmp = left
                left = right
                right = tmp
            }
            if(top > bottom) {
                val tmp = top
                top = bottom
                bottom = tmp
            }
            val margin = lineInterval / 2
            val rect = RectF(getRealX(left) - margin, getRealY(top) - margin, getRealX(right) + margin, getRealY(bottom) + margin)
            val path = Path()
            path.addRoundRect(rect, lineInterval / 1.6468f, lineInterval / 1.6468f, Path.Direction.CW)
            canvas?.drawPath(path, paint)
        }
    }

    inner class Arrow(private val startX : Int, private val startY : Int, private val endX : Int, private val endY : Int) : Element {
        private val arrowColor = boardColorSetting.drawArrowColor

        override fun draw(canvas: Canvas?) {
            val width = lineInterval / 2
            val startX = getRealX(this.startX)
            val startY = getRealY(this.startY)
            val endX = getRealX(this.endX)
            val endY = getRealY(this.endY)
            val angle = atan2((endX - startX), (endY - startY))
            val angle90 = angle - Math.toRadians(90.0)

            val p1 = Pair(startX + width/2*sin(angle90), startY + width/2*cos(angle90))
            val p2 = Pair(startX - width/2*sin(angle90), startY - width/2*cos(angle90))
            val p3 = Pair(endX - sin(angle)*width + sin(angle90)*width/2,
                endY - cos(angle)*width + cos(angle90)*width/2)
            val p4 = Pair(endX - sin(angle)*width + sin(angle90)*width,
                endY - cos(angle)*width + cos(angle90)*width)
            val p5 = Pair(endX - sin(angle)*width - sin(angle90)*width/2,
                endY - cos(angle)*width - cos(angle90)*width/2)
            val p6 = Pair(endX - sin(angle)*width - sin(angle90)*width,
                endY - cos(angle)*width - cos(angle90)*width)

            val path = Path()
            path.moveTo(p1.first.toFloat(), p1.second.toFloat())
            path.lineTo(p2.first.toFloat(), p2.second.toFloat())
            path.lineTo(p5.first.toFloat(), p5.second.toFloat())
            path.lineTo(p6.first.toFloat(), p6.second.toFloat())
            path.lineTo(endX, endY)
            path.lineTo(p4.first.toFloat(), p4.second.toFloat())
            path.lineTo(p3.first.toFloat(), p3.second.toFloat())
            path.lineTo(p1.first.toFloat(), p1.second.toFloat())
            path.close()

            paint.style = Paint.Style.FILL
            paint.pathEffect = null
            paint.color = Color.parseColor(arrowColor)
            canvas?.drawPath(path, paint)
        }

        fun hasNoLength() : Boolean {
            return (startX == endX) && (startY == endY)
        }
    }

    inner class Point(private val x : Int, private val y : Int, private val pointColor : String) : Element {
        override fun draw(canvas : Canvas?) {
            paint.apply {
                color = Color.parseColor(pointColor)
                style = Paint.Style.FILL
                pathEffect = null
            }
            canvas?.drawCircle(getRealX(x), getRealY(y), lineInterval / 6.5875f, paint)
        }
    }

    enum class StoneViewType { BLACK, WHITE }

    inner class StoneView(private var type : StoneViewType, private var index : Int, private val x : Int, private val y : Int) : Element {
        val id = "$x/$y"

        fun setStoneType(type : StoneViewType) {
            this.type = type
        }

        fun getStoneType() : StoneViewType = type

        fun setStoneIndex(idx : Int) {
            index = idx
        }

        fun getStoneIndex() : Int = index

        override fun draw(canvas: Canvas?) {
            val radius = lineInterval / 2 - 2
            paint.apply {
                pathEffect = null
                strokeWidth = (width / 351.3 + 0.5).toInt().toFloat()
                color = if(index == stones.size)
                    Color.parseColor(blendColors("#FFFFFF", boardColorSetting.lastStoneStrokeColor))
                else
                    Color.parseColor(blendColors("#FFFFFF", boardColorSetting.lineColor))
                style = Paint.Style.STROKE
            }
            canvas?.drawCircle(getRealX(x), getRealY(y), radius, paint)

            paint.style = Paint.Style.FILL
            if(type == StoneViewType.BLACK)
                paint.color = Color.BLACK
            else
                paint.color = Color.WHITE
            canvas?.drawCircle(getRealX(x), getRealY(y), radius - paint.strokeWidth / 2, paint)

            if(boardDisplaySetting.sequenceVisible && index - boardDisplaySetting.startPoint > 0) {
                paint.apply {
                    color = if (type == StoneViewType.BLACK)
                        Color.WHITE
                    else
                        Color.BLACK
                    textSize = lineInterval / 2.2f
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                val idxText = (index - boardDisplaySetting.startPoint).toString()
                paint.getTextBounds("333", 0, idxText.length, bounds)
                val centerX = bounds.exactCenterX()
                val centerY = bounds.exactCenterY()
                canvas?.drawText(idxText, getRealX(x) - centerX, getRealY(y) - centerY, paint)
            }
        }
    }

    inner class Child(private var text : String, private val x : Int, private val y : Int) : Element {
        override fun draw(canvas: Canvas?) {
            if(!boardDisplaySetting.nextNodeVisible) return
            paint.pathEffect = null
            paint.strokeWidth = (width / 351.3 + 0.5).toInt().toFloat()
            paint.style = Paint.Style.FILL
            if (text.isNotEmpty()) {
                paint.color = Color.parseColor(blendColors("#FFFFFF", boardColorSetting.boardColor))
                paint.getTextBounds(text, 0, text.length, bounds)
                if(bounds.width() > lineInterval) {
                    text = text.substring(0, text.length - 1)
                    paint.getTextBounds(text, 0, text.length, bounds)
                }
                val centerX = bounds.exactCenterX()
                val centerY = bounds.exactCenterY()
                canvas?.drawRect(
                    getRealX(x) - centerX,
                    getRealY(y) - centerY + lineInterval / 13.2f,
                    getRealX(x) + centerX,
                    getRealY(y) + centerY - lineInterval / 13.2f, paint
                )
                paint.color = Color.parseColor(blendColors("#FFFFFF", boardColorSetting.textColor))
                paint.textSize = lineInterval / 2.2f
                paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                canvas?.drawText(
                    text,
                    getRealX(x) - centerX,
                    getRealY(y) - centerY,
                    paint
                )
            } else {
                paint.color = Color.parseColor(blendColors("#FFFFFF", boardColorSetting.nodeColor))
                canvas?.drawCircle(getRealX(x), getRealY(y), lineInterval / 34 * 6, paint)
            }
        }
    }

    open class OnBoardTouchListener {
        open fun getCoordinates(x : Int, y : Int) {
            return
        }
    }

    private var onBoardTouchListener = OnBoardTouchListener()

    fun setOnBoardTouchListener(listener : OnBoardTouchListener) {
        onBoardTouchListener = listener
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        density = context.resources.displayMetrics.density
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBoardStatus(boardColorSetting, boardDisplaySetting)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        lineInterval = MeasureSpec.getSize(widthMeasureSpec).toFloat() / 16
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.isAntiAlias = true
        drawBoard(canvas)
        drawingElements.forEach { drawingElement ->
            if(drawingElement is Line) drawingElement.draw(canvas)
        }
        childs.forEach { child -> child.draw(canvas) }
        stones.keys.forEach { key -> stones[key]?.draw(canvas) }
        drawingElements.forEach { drawingElement ->
            if(drawingElement is Arrow) drawingElement.draw(canvas)
        }
        drawingElements.forEach { drawingElement ->
            if(drawingElement is Area) drawingElement.draw(canvas)
        }
        points.forEach { point -> point.draw(canvas) }
    }

    private fun drawBoard(canvas : Canvas?) {
        // board background
        paint.pathEffect = null
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor(boardColorSetting.boardColor)
        canvas!!.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // board lines
        paint.color = Color.parseColor(blendColors("#FFFFFF", boardColorSetting.lineColor))
        paint.strokeWidth = (width / 351.3 + 0.5).toInt().toFloat()
        val x = lineInterval
        for (i in 1..15) {
            canvas.drawLine(x * i + x / 2, x - x / 2, x * i + x / 2, x * 15 - x / 2, paint)
            canvas.drawLine(x + x / 2, x * i - x / 2, x * 15 + x / 2, x * i - x / 2, paint)
        }
        canvas.drawCircle(x * 8 + x / 2, x * 8 - x / 2, (width / 150.5 + 0.5).toInt().toFloat(), paint)
        canvas.drawCircle(x * 4 + x / 2, x * 12 - x / 2, (width / 150.5 + 0.5).toInt().toFloat(), paint)
        canvas.drawCircle(x * 12 + x / 2, x * 12 - x / 2, (width / 150.5 + 0.5).toInt().toFloat(), paint)
        canvas.drawCircle(x * 4 + x / 2, x * 4 - x / 2, (width / 150.5 + 0.5).toInt().toFloat(), paint)
        canvas.drawCircle(x * 12 + x / 2, x * 4 - x / 2, (width / 150.5 + 0.5).toInt().toFloat(), paint)

        canvas.drawLine(x / 2, x * 15, x / 2, x * 16, paint)
        canvas.drawLine(0f, x * 15 + x / 2, x, x * 15 + x / 2, paint)

        // board coordinate
        paint.textSize = lineInterval / 2.2f
        paint.typeface = Typeface.MONOSPACE
        for(i in 0..14) {
            val mText = (15 - i).toString()
            paint.getTextBounds("45", 0, mText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawText(mText, x / 2 - textWidth / 2, x * i + x / 2 + textHeight / 2, paint)
        }
        for(i in 0..14) {
            val mText = (i + 65).toChar().toString()
            paint.getTextBounds("A", 0, mText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawText(mText, x * (i + 1) + x / 2 - textWidth / 2, x * 15 + x / 2 + textHeight / 2, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Float
        val y: Float
        try {
            if (event.action == MotionEvent.ACTION_UP) {
                x = if ((event.x + lineInterval / 2) % lineInterval <= lineInterval / 2) event.x - (event.x + lineInterval / 2) % lineInterval else event.x - (event.x + lineInterval / 2) % lineInterval + lineInterval
                y = if ((event.y - lineInterval / 2) % lineInterval <= lineInterval / 2) event.y - (event.y - lineInterval / 2) % lineInterval else event.y - (event.y - lineInterval / 2) % lineInterval + lineInterval
                if (((x - lineInterval / 2) / lineInterval + 0.5).toInt() - 1 >= 0 && ((y + lineInterval / 2) / lineInterval + 0.5).toInt() - 1 < 15) { // if x,y is in board
                    val xc = ((x - lineInterval / 2) / lineInterval + 0.5).toInt() - 1
                    val yc = ((y + lineInterval / 2) / lineInterval + 0.5).toInt() - 1
                    onBoardTouchListener.getCoordinates(xc, yc)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    fun placeStones(addStones : ArrayList<StoneView>?, deleteStonesID : ArrayList<String>?, childs : ArrayList<Child>) {
        if (deleteStonesID != null) {
            for (deleteStoneID in deleteStonesID) {
                stones.remove(deleteStoneID)
            }
        }

        if (addStones != null) {
            for(addStone in addStones) {
                if(stones.contains(addStone.id)) {
                    stones[addStone.id]!!.setStoneIndex(addStone.getStoneIndex())
                    stones[addStone.id]!!.setStoneType(addStone.getStoneType())
                } else {
                    stones[addStone.id] = addStone
                }
            }
        }

        this.childs.clear()
        this.childs.addAll(childs)
        invalidate()
    }

    fun removeAllStones() {
        stones.clear()
        childs.clear()
        invalidate()
    }

    fun addLine(line : Line) {
        if(line.hasNoLength()) return
        drawingElements.add(line)
        invalidate()
    }

    fun addArea(area : Area) {
        drawingElements.add(area)
        invalidate()
    }

    fun addArrow(arrow : Arrow) {
        if(arrow.hasNoLength()) return
        drawingElements.add(arrow)
        invalidate()
    }

    fun addPoint(point : Point) {
        points.add(point)
        invalidate()
    }

    fun deleteLastDrawingElement() {
        points.clear()
        if(drawingElements.isNotEmpty())
            drawingElements.removeLast()
        invalidate()
    }

    fun deleteAllDrawingElements() {
        drawingElements.clear()
        points.clear()
        invalidate()
    }

    fun deleteAllPoints() {
        points.clear()
        invalidate()
    }

    fun getStoneID(x : Int, y : Int) : String = "$x/$y"

    private fun getRealX(x : Int) : Float = (x + 1) * lineInterval + lineInterval / 2

    private fun getRealY(y : Int) : Float = (y + 1) * lineInterval - lineInterval / 2

    fun updateBoardStatus(boardColorSetting : BoardColorSetting, boardDisplaySetting : BoardDisplaySetting) {
        this.boardColorSetting = boardColorSetting
        this.boardDisplaySetting = boardDisplaySetting
        invalidate()
    }

    private fun blendColors(baseColor: String, blendColor: String): String {
        val base = Color.parseColor(baseColor)
        val blend = Color.parseColor(blendColor)
        val alpha = Color.alpha(blend) / 255f
        val red = (1 - alpha) * Color.red(base) + alpha * Color.red(blend)
        val green = (1 - alpha) * Color.green(base) + alpha * Color.green(blend)
        val blue = (1 - alpha) * Color.blue(base) + alpha * Color.blue(blend)
        val alphaInt = (alpha * 255).toInt()
        val colorInt = Color.argb(alphaInt, red.toInt(), green.toInt(), blue.toInt())
        return String.format("#%06X", 0xFFFFFF and colorInt)
    }

}