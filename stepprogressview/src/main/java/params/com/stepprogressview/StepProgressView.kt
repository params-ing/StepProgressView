package params.com.stepprogressview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.reflect.KProperty


class StepProgressView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    public var totalProgress: Int by OnValidateProp(184)

    public var markers: MutableList<Int> by OnValidateProp(mutableListOf(10, 80, 112, 136, 152))


    public var currentProgress: Int by OnValidateProp(60)


    public var markerWidth: Float by OnValidateProp(3F.pxValue())


    public var rectRadius: Float by OnValidateProp(5F.pxValue())


    public var textMargin: Float by OnValidateProp(10F.pxValue())


    public var progressBarHeight: Float by OnLayoutProp(15F.pxValue())


    public var textSizeMarkers: Float by OnLayoutProp(12F.pxValue(TypedValue.COMPLEX_UNIT_SP)) {
        paintText.textSize = textSizeMarkers

    }

    public var markerColor: Int by OnValidateProp(Color.WHITE) {
        paintMarkers.color = markerColor
    }

    public var progressColor: Int by OnValidateProp(Color.GREEN) {
        paintProgress.color = progressColor
    }

    public var progressBackgroundColor: Int by OnValidateProp(Color.GRAY) {
        paintBackground.color = progressBackgroundColor
    }

    public var textColorMarker: Int by OnValidateProp(Color.BLACK) {
        paintText.color = textColorMarker
    }

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = progressBackgroundColor
    }

    private val paintMarkers = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = markerColor
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = progressColor
    }

    val paintText = TextPaint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = textColorMarker
        it.style = Paint.Style.FILL
        it.textSize = textSizeMarkers
        it.textAlign = Paint.Align.CENTER
        it.typeface = Typeface.DEFAULT
    }


    private val rBar = RectF()

    //used for drawing one-side curved rectangle
    private val rectRoundPath = Path()

    //used fo drawing complete view
    private val drawingPath = Path()

    private val arcRect = RectF()

    private var textHeight: Int = 0

    private var textVerticalCenter: Float = 0F

    private var propsInitialisedOnce = false

    private val minWidthProgressBar = 300F.pxValue()

    init {

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StepProgressView, 0, 0)

        try {

            currentProgress = a.getInt(R.styleable.StepProgressView_currentProgress, currentProgress)
            totalProgress = a.getInt(R.styleable.StepProgressView_totalProgress, totalProgress)

            progressBarHeight = a.getDimension(R.styleable.StepProgressView_progressBarHeight,
                    progressBarHeight)
            textMargin = a.getDimension(R.styleable.StepProgressView_textMargin, textMargin)
            markerWidth = a.getDimension(R.styleable.StepProgressView_markerWidth, markerWidth)
            textSizeMarkers = a.getDimension(R.styleable.StepProgressView_textSize, textSizeMarkers)

            progressBackgroundColor = a.getColor(R.styleable.StepProgressView_progressBackgroundColor,
                    progressBackgroundColor)
            markerColor = a.getColor(R.styleable.StepProgressView_markerColor, markerColor)
            progressColor = a.getColor(R.styleable.StepProgressView_progressColor, progressColor)
            textColorMarker = a.getColor(R.styleable.StepProgressView_textColor, textColorMarker)


            val markerString = a.getString(R.styleable.StepProgressView_markers)
            if (!markerString.isNullOrBlank()) {
                this.markers.clear()

                val input = markerString.split(",")

                try {
                    input.map { it -> this.markers.add(it.toInt()) }
                } catch (e: Exception) {
                    throw  IllegalArgumentException("Invalid input markers! Should be comma separated digits");
                }
            }


        } finally {
            a.recycle()
        }

        propsInitialisedOnce = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun getSuggestedMinimumWidth(): Int {
        return (minWidthProgressBar).toInt()
    }

    override fun getSuggestedMinimumHeight(): Int {
        textHeight = getTextHeight()
        return (progressBarHeight + textHeight + textMargin).toInt()
    }

    private fun getTextHeight(): Int {

        val rect = Rect()
        val text = "8"
        paintText.getTextBounds(text, 0, text.length, rect)
        return rect.height()

    }


    override fun onLayout(changed: Boolean, leftP: Int, topP: Int, rightP: Int, bottomP: Int) {
        super.onLayout(changed, leftP, topP, rightP, bottomP)

        rBar.apply {
            left = 0F
            top = 0F
            right = rightP.toFloat() - leftP.toFloat()
            bottom = progressBarHeight
        }


        textVerticalCenter = (progressBarHeight + textMargin) + textHeight


    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawingPath.reset()

        if (currentProgress in 1 until totalProgress) {

            val progressX = (currentProgress / totalProgress.toFloat()) * (rBar.right - rBar.left)


            if (progressX > rectRadius) { //if progress exceeds beyond left corner avoid redrawing


                //progressX-1 is used so that there is no gap between progressRect drawing and
                // backgroundRect Drawing
                drawingPath.addPath(drawRoundedRightRect((progressX - 1), rBar.top, rBar.right,
                        rBar.bottom, rectRadius, paintBackground, canvas))


                val progressRight: Float
                val drawProgressInRightCorner = progressX > (rBar.right - rectRadius)

                if (drawProgressInRightCorner) {
                    progressRight = rBar.right - rectRadius
                } else {
                    progressRight = progressX

                }

                drawingPath.addPath(drawRoundedLeftRect(rBar.left, rBar.top, progressRight,
                        rBar.bottom, rectRadius, paintProgress, canvas))

                canvas.save()
                canvas.clipPath(drawingPath)

                if (drawProgressInRightCorner) {

                    canvas.drawRect((rBar.right - rectRadius), rBar.top, progressX, rBar.bottom, paintProgress)

                }


            } else {

                drawCompleteProgressBar(canvas, paintBackground)

                canvas.drawRect(rBar.left, rBar.top, progressX, rBar.bottom, paintProgress)


            }


        } else {

            //incase there is no progress only draw background progress bar
            val paint = if (currentProgress > 0) paintProgress else paintBackground

            drawCompleteProgressBar(canvas, paint)

        }



        for (i in markers) {
            if (i in 1..totalProgress) {
                val left: Float = (i / totalProgress.toFloat()) * (rBar.right - rBar.left)

                canvas.drawRect(left - markerWidth / 2, rBar.top, left + markerWidth / 2
                        , rBar.bottom, paintMarkers)



                canvas.drawText(i.toString(), left, textVerticalCenter, paintText)
            }

        }

        canvas.restore()
    }

    private fun drawCompleteProgressBar(canvas: Canvas, paint: Paint) {
        drawingPath.addRoundRect(rBar, rectRadius, rectRadius, Path.Direction.CW)

        canvas.drawPath(drawingPath, paint)
        canvas.save()
        canvas.clipPath(drawingPath)
    }



    private fun drawRoundedLeftRect(leftP: Float, topP: Float, rightP: Float, bottomP: Float,
                                    cornerRadius: Float, paint: Paint, canvas: Canvas): Path {

        rectRoundPath.reset()
        arcRect.run {
            left = leftP
            top = topP
            right = leftP + (2 * cornerRadius)
            bottom = bottomP
        }


        rectRoundPath.addArc(arcRect, 90F, 180F)

        rectRoundPath.addRect(leftP + cornerRadius, topP, rightP, bottomP, Path.Direction.CW)

        canvas.drawPath(rectRoundPath, paint)

        return rectRoundPath
    }


    private fun drawRoundedRightRect(leftP: Float, topP: Float, rightP: Float, bottomP: Float,
                                     cornerRadius: Float, paint: Paint, canvas: Canvas): Path {

        arcRect.run {
            left = rightP - (2 * cornerRadius)
            top = topP
            right = rightP
            bottom = bottomP
        }



        rectRoundPath.reset()
        if (rightP - leftP > cornerRadius) {
            rectRoundPath.addRect(leftP, topP, rightP - cornerRadius, bottomP, Path.Direction.CW)

        }
        rectRoundPath.addArc(arcRect, -90F, 180F)
        canvas.drawPath(rectRoundPath, paint)

        return rectRoundPath

    }


    private fun Float.pxValue(unit: Int = TypedValue.COMPLEX_UNIT_DIP): Float {
        return TypedValue.applyDimension(unit, this, resources.displayMetrics)
    }

    /**
     * Delegate property used to requestLayout on value set after executing a custom function
     */
    inner class OnLayoutProp<T>(private var field: T, private inline var func: () -> Unit = {}) {
        operator fun setValue(thisRef: Any?, p: KProperty<*>, v: T) {
            field = v
            func()
            if (propsInitialisedOnce) {
                requestLayout()

            }

        }

        operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
            return field
        }

    }

    /**
     * Delegate Property used to invalidate on value set after executing a custom function
     */
    inner class OnValidateProp<T>(private var field: T, private inline var func: () -> Unit = {}) {
        operator fun setValue(thisRef: Any?, p: KProperty<*>, v: T) {
            field = v
            func()
            if (propsInitialisedOnce) {
                invalidate()

            }

        }

        operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
            return field
        }

    }

}