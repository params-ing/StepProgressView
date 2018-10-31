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


    public var rectRadius: Float by OnValidateProp(10F.pxValue())


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


    private val rectBar = RectF()

    private var rectBarProgress = RectF()

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

        rectBar.apply {
            left = 0F
            top = 0F
            right = rightP.toFloat() - leftP.toFloat()
            bottom = progressBarHeight
        }



        textVerticalCenter = (progressBarHeight + textMargin) + textHeight


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        rectBar.left = 0F

        if (currentProgress > 0) {

            val progressX: Float = (currentProgress / totalProgress.toFloat()) * (rectBar.right - rectBar.left)

            rectBarProgress.left = rectBar.left
            rectBarProgress.top = rectBar.top
            rectBarProgress.right = progressX
            rectBarProgress.bottom = rectBar.bottom

            //to avoid redrawing
            canvas.drawRoundedLeftRect(rectRadius, rectBarProgress, paintProgress)

            rectBar.left = progressX


            //to avoid redrawing
            canvas.drawRoundedRightRect(rectRadius, rectBar, paintBackground)


        } else {

            //incase there is no progress only draw background progress bar

            canvas.drawRoundRect(rectBarProgress, rectRadius, rectRadius, paintProgress)

        }


        rectBar.left = 0F

        for (i in markers) {
            val left: Float = (i / totalProgress.toFloat()) * (rectBar.right - rectBar.left)

            canvas.drawRect(left - markerWidth / 2, rectBar.top, left + markerWidth / 2
                    , rectBar.bottom, paintMarkers)



            canvas.drawText(i.toString(), left, textVerticalCenter, paintText)

        }

    }


    private fun Canvas.drawRoundedLeftRect(cornerRadius: Float, rect: RectF, paint: Paint) {

        arcRect.run {
            left = rect.left
            top = rect.top
            right = rect.left + (2 * cornerRadius)
            bottom = rect.bottom
        }

        drawArc(arcRect, 90F, 360F, true, paint)

        drawRect(rect.left + cornerRadius, rect.top, rect.right, rect.bottom, paint)


    }

    private fun Canvas.drawRoundedRightRect(cornerRadius: Float, rect: RectF, paint: Paint) {

        arcRect.run {
            left = rect.right - (2 * cornerRadius)
            top = rect.top
            right = rect.right
            bottom = rect.bottom
        }

        drawArc(arcRect, 360F, 450F, true, paint)

        drawRect(rect.left, rect.top, rect.right - cornerRadius, rect.bottom, paint)

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