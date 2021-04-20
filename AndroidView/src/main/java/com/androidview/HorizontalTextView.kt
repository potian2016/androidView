package com.androidview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

/**
 *  author : 破天荒
 *  date : 4/19/21
 *  description :垂直View 2个文本一行显示
 */
@Suppress("DEPRECATION")
class HorizontalTextView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var startText: String? = null
    private var startTextSize: Float = 0f
    private var startTextColor: Int = Color.parseColor("#000000")
    private var startTextBold: Boolean = false

    /** 左边文字默认宽 0代表自适应 **/
    private var startTextWidth: Float = 0f

    /** 左边文字最小宽 **/
    private var startWordsWidth = 0F

    /** 左右两边文字间距 **/
    private var textPadding: Float = 0f

    private var endText: String? = null
    private var endTextSize: Float = 0f
    private var endTextColor: Int = Color.parseColor("#000000")
    private var endTextBold: Boolean = false

    /** 右边文字对齐方式  1左对齐  2右对齐【默认】 **/
    private var htvEndTextGravity: Int = 2

    /** 左右文字对齐方式  1居中对齐  2顶部对齐【默认】 **/
    private var htvTextGravity: Int = 1

    /** 右边文字默认宽 0代表自适应 **/
    private var endTextWidth: Float = 0f

    /** 右边文字最小宽 **/
    private var endWordsWidth = 0F

    /** 默认单行显示 **/
    private var endTextSingle: Boolean = true

    /** 文字自动换行 **/
    private lateinit var staticLayout: StaticLayout

    private val rect = Rect()
    private val fontMetrics = Paint.FontMetrics()
    private var startPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
    }
    private var endPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
    }

    // view的最小宽
    private var viewWidth = 0

    // view的最小高
    private var viewHeight = 0


    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTextView)
        startText = array.getString(R.styleable.HorizontalTextView_htvStartText)
        startTextSize = array.getDimension(R.styleable.HorizontalTextView_htvStartTextSize, startTextSize)
        startTextWidth = array.getDimension(R.styleable.HorizontalTextView_htvStartTextWidth, startTextWidth)
        startTextColor = array.getColor(R.styleable.HorizontalTextView_htvStartTextColor, startTextColor)
        startTextBold = array.getBoolean(R.styleable.HorizontalTextView_htvStartTextBold, startTextBold)
        textPadding = array.getDimension(R.styleable.HorizontalTextView_htvTextPaddingText, textPadding)
        endText = array.getString(R.styleable.HorizontalTextView_htvEndText)
        endTextSize = array.getDimension(R.styleable.HorizontalTextView_htvEndTextSize, endTextSize)
        endTextColor = array.getColor(R.styleable.HorizontalTextView_htvEndTextColor, endTextColor)
        endTextWidth = array.getDimension(R.styleable.HorizontalTextView_htvEndTextWidth, endTextWidth)
        endTextBold = array.getBoolean(R.styleable.HorizontalTextView_htvEndTextBold, endTextBold)
        endTextSingle = array.getBoolean(R.styleable.HorizontalTextView_htvEndTextSingle, endTextSingle)
        htvEndTextGravity = array.getInt(R.styleable.HorizontalTextView_htvEndTextGravity, htvEndTextGravity)
        htvTextGravity = array.getInt(R.styleable.HorizontalTextView_htvTextGravity, htvTextGravity)
        array.recycle()
        startText?.let {
            startPaint.textSize = startTextSize
            startPaint.color = startTextColor
            startPaint.typeface = if (startTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        endText?.let {
            endPaint.textSize = endTextSize
            endPaint.color = endTextColor
            endPaint.typeface = if (endTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewWidth = resolveSize(getViewWidth(), widthMeasureSpec)
        if (endTextSingle) {
            viewHeight = resolveSize(getViewHeight(), heightMeasureSpec)
        } else {
            endText?.let {
                endPaint.getTextBounds(it, 0, it.length, rect)
                endPaint.getFontMetrics(fontMetrics)
                val wordsHeight = rect.height()
                endWordsWidth = viewWidth - startWordsWidth
                staticLayout = StaticLayout(
                    it, endPaint,
                    endWordsWidth.toInt(), Layout.Alignment.ALIGN_NORMAL, 1F, 0F, true
                )
                viewHeight = max(wordsHeight, staticLayout.height)
            }
        }
        setMeasuredDimension(viewWidth, viewHeight)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        drawLeftText(canvas)
        drawRightText(canvas)
    }

    /**
     * 绘制左边文字
     */
    private fun drawLeftText(canvas: Canvas) {
        startText?.let {
            startPaint.getFontMetrics(fontMetrics)
            startPaint.getTextBounds(it, 0, it.length, rect)
            val centerHeight = viewHeight / 2f - (fontMetrics.descent + fontMetrics.ascent) / 2f
            if (endText.isNullOrEmpty()) {// 只有左侧文字直接整体左侧居中显示
                canvas.drawText(it, 0f, centerHeight, startPaint)
            } else {
                if (this::staticLayout.isInitialized) {
                    if (htvTextGravity == 1 && staticLayout.lineCount > 1) {
                        canvas.drawText(it, 0f, centerHeight, startPaint)
                    } else if (htvTextGravity == 2 && staticLayout.lineCount > 1) {
                        startPaint.getTextBounds(it, 0, it.length, rect)
                        val itemHeight = staticLayout.height / staticLayout.lineCount
                        canvas.drawText(it, 0f, rect.height() + (itemHeight - rect.height()) / 2f, startPaint)
                    } else {
                        canvas.drawText(it, 0f, centerHeight, startPaint)
                    }
                } else {// 单行显示
                    canvas.drawText(it, 0f, centerHeight, startPaint)
                }
            }
        }
    }

    /**
     * 绘制右边边文字
     */
    private fun drawRightText(canvas: Canvas) {
        endText?.let {
            endPaint.getFontMetrics(fontMetrics)
            endPaint.getTextBounds(it, 0, it.length, rect)
            val availableWidth = viewWidth - startWordsWidth - textPadding
            /** 单行显示 **/
            if (endTextSingle) {
                val realWords =
                    TextUtils.ellipsize(it, TextPaint(endPaint), availableWidth, TextUtils.TruncateAt.END).toString()
                if (htvEndTextGravity == 1) {// 左对齐
                    canvas.drawText(
                        realWords,
                        startWordsWidth + textPadding,
                        viewHeight / 2f - (fontMetrics.descent + fontMetrics.ascent) / 2f,
                        endPaint
                    )
                } else if (htvEndTextGravity == 2) {// 右对齐
                    canvas.drawText(
                        realWords,
                        viewWidth - endPaint.measureText(realWords),
                        viewHeight / 2f - (fontMetrics.descent + fontMetrics.ascent) / 2f,
                        endPaint
                    )
                }
            } else {
                if (htvEndTextGravity == 1) {// 左对齐
                    canvas.translate(startWordsWidth + textPadding, 0F)
                    staticLayout = StaticLayout(
                        it, endPaint, availableWidth.toInt(),
                        Layout.Alignment.ALIGN_NORMAL, 1F, 0F, true
                    )
                    staticLayout.draw(canvas)
                } else if (htvEndTextGravity == 2) {// 右对齐
                    // 左边有文字需要绘制，需要移动canvas之后再绘制防止文字重叠
                    canvas.translate(startWordsWidth + textPadding, 0F)
                    staticLayout = StaticLayout(
                        it, endPaint, availableWidth.toInt(),
                        Layout.Alignment.ALIGN_OPPOSITE, 1F, 0F, true
                    )
                    staticLayout.draw(canvas)
                }
            }
        }
    }

    /**
     * view 正常显示需要的最小宽(包括已经设置好的间距)
     */
    private fun getViewWidth(): Int {
        startText?.let {
            /**  如果设置了固定宽就不需要再测量 **/
            startWordsWidth = if (startTextWidth > 0) {
                startTextWidth
            } else {
                startPaint.measureText(it)
            }
        }
        endText?.let {
            /**  按照单行完全显示测量后面根据显示方式矫正 **/
            endWordsWidth = endPaint.measureText(it)
        }
        viewWidth = (startWordsWidth + endWordsWidth + textPadding).toInt()
        return viewWidth
    }

    /**
     * view 正常显示需要的最小高
     */
    private fun getViewHeight(): Int {
        var wordsHeight = 0
        /** 左边文字高 **/
        startText?.let {
            startPaint.getTextBounds(it, 0, it.length, rect)
            wordsHeight = rect.height()
        }
        /** 右边文字高 **/
        endText?.let {
            /** 单行显示需要加省略号 **/
            endPaint.getTextBounds(it, 0, it.length, rect)
            if (wordsHeight < rect.height()) {
                wordsHeight = rect.height()
            }
        }
        viewHeight = wordsHeight
        return viewHeight
    }

    /**
     * 设置左边文字内容
     */
    fun setHtvStartText(leftText: String?) {
        startText = leftText
        requestLayout()
    }

    /**
     * 设置右边文字内容
     */
    fun setHtvEndText(rightText: String?) {
        endText = rightText
        requestLayout()
    }
}