package com.androidview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.math.max

/**
 *  author : 破天荒
 *  date : 4/20/21
 *  description :多功能Item 按钮
 */
@Suppress("DEPRECATION")
class CommonItemView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // 网络图片URL地址
    private var imageUrl: String? = null

    // 是否显示圆形图片 主要针对网络图片
    private var civIsCircle: Boolean = false

    // 网络图片生成Drawable成功
    private var createDrawableSuccess: Boolean = false

    // 除数
    private val divisor = 2f

    // 中间文字不可用宽度【两边图标占用的宽度+间距】
    private var unAvailableWidth = 0F

    // 中间文字最小高度 包含上下间距和文字间的间距
    private var wordsHeight = 0F

    // 顶部文字高度
    private var topWordsHeight = 0F

    // 底部文字高度
    private var bottomWordsHeight = 0F

    // 背景颜色
    private var civBackgroundColor = Color.TRANSPARENT

    // 按下是的背景颜色
    private var civPressedColor = Color.parseColor("#D8DAE5")

    // 是否处于按下状态
    private var pressDowning = false

    // 圆角
    private var civCorner = 0F

    // 左边图标
    private var startImageDrawable: Drawable? = null

    private var startDrawableWidth: Float = 0.dp
    private var startDrawableHeight: Float = 0.dp
    private var drawablePaddingText: Float = 0.dp

    // 上下两排文字属性
    private var topText: String? = null

    private var textPaddingText: Float = 0.dp
    private var topTextSize: Float = 14.dp
    private var bottomText: String? = null
    private var bottomTextSize: Float = 0F
    private var topTextBold: Boolean = false
    private var bottomTextBold: Boolean = false

    // 内边距
    private var startSpace: Float = 0.dp
    private var endSpace: Float = 0.dp
    private var topSpace: Float = 0.dp
    private var bottomSpace: Float = 0.dp
    private var horizontalSpace: Float = 0.dp
    private var verticalSpace: Float = 0.dp

    // 右边图标
    private var endImageDrawable: Drawable? = null

    private var endDrawableWidth: Float = 0.dp
    private var endDrawableHeight: Float = 0.dp
    private var textPaddingDrawable: Float = 0.dp
    private var topTextColor: Int = Color.BLACK

    private var bottomTextColor: Int = Color.BLACK
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
    }

    private var viewWidth = 0
    private var viewHeight = 0
    private val rect = Rect()
    private val fontMetrics = Paint.FontMetrics()
    private var bounds: RectF? = null
    private val xFerMode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CommonItemView)
        topText = array.getString(R.styleable.CommonItemView_civTopText)
        imageUrl = array.getString(R.styleable.CommonItemView_civImageUrl)
        topTextSize = array.getDimension(R.styleable.CommonItemView_civTopTextSize, topTextSize)
        bottomText = array.getString(R.styleable.CommonItemView_civBottomText)
        bottomTextSize = array.getDimension(R.styleable.CommonItemView_civBottomTextSize, bottomTextSize)
        topTextBold = array.getBoolean(R.styleable.CommonItemView_civTopTextBold, topTextBold)
        bottomTextBold = array.getBoolean(R.styleable.CommonItemView_civBottomTextBold, bottomTextBold)
        civIsCircle = array.getBoolean(R.styleable.CommonItemView_civIsCircle, civIsCircle)
        textPaddingText = array.getDimension(R.styleable.CommonItemView_civTextPaddingText, textPaddingText)

        startImageDrawable = array.getDrawable(R.styleable.CommonItemView_civStartDrawable)
        startDrawableWidth = array.getDimension(R.styleable.CommonItemView_civStartDrawableWidth, startDrawableWidth)
        startDrawableHeight = array.getDimension(R.styleable.CommonItemView_civStartDrawableHeight, startDrawableHeight)
        drawablePaddingText =
            array.getDimension(R.styleable.CommonItemView_civDrawablePaddingText, drawablePaddingText)
        startSpace = array.getDimension(R.styleable.CommonItemView_civSpaceStart, startSpace)
        endSpace = array.getDimension(R.styleable.CommonItemView_civSpaceEnd, endSpace)
        topSpace = array.getDimension(R.styleable.CommonItemView_civSpaceTop, topSpace)
        bottomSpace = array.getDimension(R.styleable.CommonItemView_civSpaceBottom, bottomSpace)
        horizontalSpace = array.getDimension(R.styleable.CommonItemView_civSpaceHorizontal, horizontalSpace)
        verticalSpace = array.getDimension(R.styleable.CommonItemView_civSpaceVertical, verticalSpace)
        endImageDrawable = array.getDrawable(R.styleable.CommonItemView_civEndDrawable)
        endDrawableWidth = array.getDimension(R.styleable.CommonItemView_civEndDrawableWidth, endDrawableWidth)
        endDrawableHeight = array.getDimension(R.styleable.CommonItemView_civEndDrawableHeight, endDrawableHeight)
        textPaddingDrawable = array.getDimension(R.styleable.CommonItemView_civTextPaddingDrawable, textPaddingDrawable)
        topTextColor = array.getColor(R.styleable.CommonItemView_civTopTextColor, topTextColor)
        bottomTextColor = array.getColor(R.styleable.CommonItemView_civBottomTextColor, bottomTextColor)
        civBackgroundColor = array.getColor(R.styleable.CommonItemView_civBackgroundColor, civBackgroundColor)
        civPressedColor = array.getColor(R.styleable.CommonItemView_civPressedColor, civPressedColor)
        civCorner = array.getDimension(R.styleable.CommonItemView_civCorner, civCorner)

        if (horizontalSpace > 0) {
            startSpace = horizontalSpace
            endSpace = horizontalSpace
        }
        if (verticalSpace > 0) {
            topSpace = verticalSpace
            bottomSpace = verticalSpace
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (civPressedColor != Color.TRANSPARENT) {
                isClickable = true
                val drawable = GradientDrawable().apply {
                    setColor(civBackgroundColor)
                    cornerRadius = civCorner
                }
                background = RippleDrawable(ColorStateList.valueOf(civPressedColor), drawable, null)
                civBackgroundColor = Color.TRANSPARENT
            }
        }
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = resolveSize(getViewWidth(), widthMeasureSpec)
        viewHeight = resolveSize(getViewHeight(), heightMeasureSpec)
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawWithImage(canvas)
    }

    /**
     * 绘制操作
     */
    private fun drawWithImage(canvas: Canvas) {
        // 绘制点击效果
        if (civPressedColor != Color.TRANSPARENT && pressDowning) {
            paint.style = Paint.Style.FILL
            paint.color = civPressedColor
            canvas.drawRoundRect(
                0f, 0f, viewWidth.toFloat(),
                viewHeight.toFloat(), civCorner, civCorner, paint
            )
        }

        // 绘制矩形背景
        if (civBackgroundColor != Color.TRANSPARENT && !pressDowning) {
            paint.style = Paint.Style.FILL
            paint.color = civBackgroundColor
            canvas.drawRoundRect(
                0f, 0f, viewWidth.toFloat(),
                viewHeight.toFloat(), civCorner, civCorner, paint
            )
        }
        // 绘制左边图标
        val leftSize =
            if ((startImageDrawable != null || imageUrl != null) && startDrawableWidth * startDrawableHeight > 0) {
                val topValue = (viewHeight - topSpace - bottomSpace - startDrawableHeight) / divisor
                if (civIsCircle && imageUrl != null) {
                    if (createDrawableSuccess) {
                        drawOvalBitmap(canvas, topValue)
                    } else {
                        createDrawable()
                    }
                } else {
                    canvas.drawBitmap(getBitmapFormDrawable(), startSpace, topValue, paint)
                }
                startSpace + startDrawableWidth + drawablePaddingText
            } else {
                startSpace
            }
        val availableWidth = viewWidth - unAvailableWidth
        // 绘制上边边文字
        topText?.let {
            paint.color = topTextColor
            paint.textSize = topTextSize
            paint.style = Paint.Style.FILL
            paint.typeface = if (topTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.getFontMetrics(fontMetrics)
            val realWords =
                TextUtils.ellipsize(it, TextPaint(paint), availableWidth, TextUtils.TruncateAt.END).toString()
            val center = (viewHeight - wordsHeight) / divisor + topWordsHeight / divisor -
                (fontMetrics.descent + fontMetrics.ascent) / divisor
            /** 如果只有一个文字，则文字居中显示【圆角按钮】 **/
            if (startImageDrawable == null && imageUrl.isNullOrEmpty() && endImageDrawable == null && bottomText == null) {
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(realWords, viewWidth / divisor, center, paint)
            } else {
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(realWords, leftSize, center, paint)
            }
        }
        // 绘制下边边文字
        bottomText?.let {
            paint.color = bottomTextColor
            paint.textSize = bottomTextSize
            paint.style = Paint.Style.FILL
            paint.typeface = if (bottomTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.getFontMetrics(fontMetrics)
            val realWords =
                TextUtils.ellipsize(it, TextPaint(paint), availableWidth, TextUtils.TruncateAt.END).toString()
            paint.getTextBounds(realWords, 0, realWords.length, rect)
            val center = (viewHeight - wordsHeight) / divisor + topWordsHeight + textPaddingText +
                bottomWordsHeight / divisor - (fontMetrics.descent + fontMetrics.ascent) / divisor
            canvas.drawText(realWords, leftSize, center, paint)
        }

        // 绘制右边图标
        if (endImageDrawable != null && endDrawableWidth * endDrawableHeight > 0) {
            canvas.drawBitmap(
                getBitmapFormDrawable(1), viewWidth - endDrawableWidth - endSpace,
                (viewHeight - endDrawableHeight) / divisor, paint
            )
        }
    }

    /**
     * 绘制圆形图标
     */
    private fun drawOvalBitmap(canvas: Canvas, topValue: Float) {
        bounds =
            RectF(startSpace, topValue, startSpace + startDrawableWidth, topValue + startDrawableHeight)
        val count = canvas.saveLayer(bounds, null)
        canvas.drawOval(
            startSpace, topValue, startSpace + startDrawableWidth,
            topValue + startDrawableHeight, paint
        )
        paint.xfermode = xFerMode
        canvas.drawBitmap(getBitmapFormDrawable(), startSpace, topValue, paint)
        paint.xfermode = null
        canvas.restoreToCount(count)
    }

    /**
     * 根据ImageUrl生成Drawable
     * %
     */
    private fun createDrawable() {
        Glide.with(context).asDrawable()
            .load("$imageUrl")
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    createDrawableSuccess = true
                    setCivStartDrawable(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    errorDrawable?.let {
                        createDrawableSuccess = true
                        setCivStartDrawable(it)
                    }
                }

            })
    }


    /**
     * view 需要正常显示需要的最小宽(包括已经设置好的间距)
     */
    private fun getViewWidth(): Int {
        val startImageWidth: Float = if (startImageDrawable == null && imageUrl.isNullOrEmpty()) {
            0F
        } else {
            startDrawableWidth + drawablePaddingText
        }
        val endImageWidth: Float = if (endImageDrawable == null) 0F else endDrawableWidth + textPaddingDrawable
        unAvailableWidth = startImageWidth + endImageWidth + startSpace + endSpace
        /** 居中文字宽 -- 顶部 **/
        var wordsWidth = 0F
        topText?.let {
            paint.textSize = topTextSize
            paint.typeface = if (topTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            wordsWidth = paint.measureText(it)
        }
        /** 居中文字宽 -- 底部 **/
        bottomText?.let {
            paint.textSize = bottomTextSize
            paint.typeface = if (bottomTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (wordsWidth < paint.measureText(it)) {
                wordsWidth = paint.measureText(it)
            }
        }
        val totalWidth = (startImageWidth + wordsWidth + endImageWidth + startSpace + endSpace)
        viewWidth = totalWidth.toInt()
        /** 类型转换之后如果丢失进度需要加1，防止文字显示不完整 **/
        if (viewWidth < totalWidth) viewWidth += 1
        return viewWidth
    }

    /**
     * view 需要正常显示需要的最小高(包括已经设置好的间距)
     */
    private fun getViewHeight(): Int {
        val startImageHeight: Int = if (startImageDrawable == null && imageUrl.isNullOrEmpty()) {
            0
        } else {
            startDrawableHeight.toInt()
        }
        val endImageHeight: Int = if (endImageDrawable == null) 0 else endDrawableHeight.toInt()
        viewHeight = max(startImageHeight, endImageHeight)
        /** 居中文字高 -- 顶部 **/
        topText?.let {
            paint.textSize = topTextSize
            paint.typeface = if (topTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.getTextBounds(it, 0, it.length, rect)
            topWordsHeight = rect.height().toFloat()
            wordsHeight = topWordsHeight
        }
        /** 居中文字高 -- 底部 **/
        bottomText?.let {
            paint.textSize = bottomTextSize
            paint.typeface = if (bottomTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.getTextBounds(it, 0, it.length, rect)
            bottomWordsHeight = rect.height().toFloat()
            wordsHeight += bottomWordsHeight
        }
        /** 顶部和底部文字都不为空的时候 文字间距属性才会生效 **/
        if (!topText.isNullOrEmpty() && !bottomText.isNullOrEmpty()) {
            wordsHeight += textPaddingText
        }
        if (viewHeight < wordsHeight) viewHeight = wordsHeight.toInt()
        viewHeight += (topSpace + bottomSpace).toInt()
        return viewHeight
    }

    /**
     * 根据drawable id 获取Bitmap对象
     * @param location 0代表是左边图标 1是右边图标
     */
    private fun getBitmapFormDrawable(location: Int = 0): Bitmap {
        val bitmap = Bitmap.createBitmap(
            if (location == 0) startDrawableWidth.toInt() else endDrawableWidth.toInt(),
            if (location == 0) startDrawableHeight.toInt() else endDrawableHeight.toInt(), Bitmap.Config.ARGB_8888
        )
        if (location == 0) {
            val canvas = Canvas(bitmap)
            startImageDrawable?.setBounds(0, 0, startDrawableWidth.toInt(), startDrawableHeight.toInt())
            // 设置绘画的边界，此处表示完整绘制
            startImageDrawable?.draw(canvas)
        } else {
            val canvas = Canvas(bitmap)
            endImageDrawable?.setBounds(0, 0, endDrawableWidth.toInt(), endDrawableHeight.toInt())
            // 设置绘画的边界，此处表示完整绘制
            endImageDrawable?.draw(canvas)
        }
        return bitmap
    }

    /**
     * 设置左边图标的drawable
     */
    fun setCivStartDrawable(resource: Drawable) {
        startImageDrawable = resource
        invalidate()
    }

    /**
     * 设置左边图标的drawable
     */
    fun setCivEndDrawable(resource: Drawable) {
        endImageDrawable = resource
        invalidate()
    }

    /**
     * 设置网络图片
     */
    fun setCivImageUrl(civImageUrl: String?) {
        imageUrl = civImageUrl
        createDrawableSuccess = false
        invalidate()
    }

    /**
     * 设置顶部文字内容
     */
    fun setCivTopText(civTopText: String?) {
        topText = if (civTopText.isNullOrEmpty()) {
            " "
        } else {
            civTopText
        }
        requestLayout()
    }

    /**
     * 设置顶部文字颜色
     */
    fun setCivTopTextColor(civTopTextColor: Int) {
        topTextColor = civTopTextColor
        invalidate()
    }

    /**
     * 设置底部文字内容
     */
    fun setCivBottomText(civBottomText: String?) {
        bottomText = if (civBottomText.isNullOrEmpty()) {
            " "
        } else {
            civBottomText
        }
        requestLayout()
    }

    /**
     * 设置背景颜色
     */
    fun setCivBackgroundColor(civBackgroundColor: Int) {
        this.civBackgroundColor = civBackgroundColor
        invalidate()
    }

    /**
     * 设置按下时的颜色
     */
    fun setCivPressedColor(civPressedColor: Int) {
        this.civPressedColor = civPressedColor
        invalidate()
    }
}