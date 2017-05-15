package cn.xiaoniaojun.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Package: cn.xiaoniaojun.myapplication
 * Created by xiaoniaojun on 2017/5/15.
 */

public class CarouselView extends View {


    private final static int COLOR_SELECTED = Color.rgb(251, 217, 70);
    private final static int COLOR_UNSELECTED = Color.rgb(51, 51, 53);
    private final static float DENSITY = Resources.getSystem().getDisplayMetrics().density;
    private final static int INDICATOR_INTERVAL = (int) (20 * DENSITY);

    private int mDrawIndicatorLeft;
    private Paint mSelectedIndicatorPaint;
    private Paint mUnSelectedIndicatorPaint;

    private static int sNextAvailableTag = 0x0;

    private List<Bitmap> mCarouselImages = null;
    private int mCurrentPosition;
    private int mImageCount;

    private OnCarouselViewClickListener mListener;

    // distinguish each carouselView for on click callback.
    private int mTag;

    private boolean _isCarouselImageSet = false;
    // interval that indicates how long to display next image.
    private int mInterval;

    private Bitmap _displayBitmap;
    private boolean _isClickEvent;

    private int mOffsetX;
    private int mOffsetY;
    private int mLastX;
    private int mLastY;
    private Rect mViewPort;
    private Rect mOriginViewPort;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public CarouselView(Context context) {
        this(context, null, 0);
    }

    public CarouselView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarouselView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {

        // indicates next available, auto increased, support mask operation.
        mTag = sNextAvailableTag;
        sNextAvailableTag = sNextAvailableTag << 1;

        if (context instanceof OnCarouselViewClickListener) {
            mListener = (OnCarouselViewClickListener) context;
        }

        mScroller = new Scroller(context);
        mVelocityTracker = VelocityTracker.obtain();
    }

    /*
    * Call this method to setup images that should be shown within carousel view.
    * There will also start a timer that display next image after a specify interval.
    * */
    public void setCarouselImages(List<Bitmap> carouseImages) {
        if (carouseImages != null) {
            mCarouselImages = carouseImages;
            notifyCarouselImagesSet();
        }
    }

    /**
     * Set how long it should shows next image.
     * must be used before {@link CarouselView#setCarouselImages(List)}.
     * @param interval with millisecond
     */
    public void setInterval(int interval) {
        mInterval = interval;
    }

    private void notifyCarouselImagesSet() {
        mCurrentPosition = 0;
        mViewPort = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
        mViewPort.offsetTo(getMeasuredWidth(), 0);
        mOffsetX = getMeasuredWidth();
        mOriginViewPort = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());

        // stitching bitmaps into one bitmap
        _displayBitmap = stitchingBitmaps(mCarouselImages, getMeasuredWidth(), getMeasuredHeight());
        // release list and bitmap resources
        mImageCount = mCarouselImages.size();
        mCarouselImages.clear();
        mCarouselImages = null;

        /*
        * Prepare indicate point drawn kit
        */
        if ((mImageCount & 1) != 0) {
            mDrawIndicatorLeft = getMeasuredWidth() / 2 - ((mImageCount / 2) * INDICATOR_INTERVAL);
        } else {
            mDrawIndicatorLeft = (int) (getMeasuredWidth() / 2 - (((float)(mImageCount / 2)) - 0.5) * INDICATOR_INTERVAL);
        }
        mSelectedIndicatorPaint = new Paint();
        mSelectedIndicatorPaint.setColor(COLOR_SELECTED);
        mSelectedIndicatorPaint.setStyle(Paint.Style.FILL);

        mUnSelectedIndicatorPaint = new Paint();
        mUnSelectedIndicatorPaint.setColor(COLOR_UNSELECTED);
        mUnSelectedIndicatorPaint.setStyle(Paint.Style.FILL);

        _isCarouselImageSet = true;
        invalidate();


        int interval = Math.max(mInterval, 5000);
        Observable<Long> timer = Observable.interval(interval, TimeUnit.MILLISECONDS);
        timer
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        // TODO:Display next image
                        mCurrentPosition = (++mCurrentPosition < mImageCount)? mCurrentPosition : 0;
                        performInfinityScrollToSuitablePosition(mCurrentPosition);
                    }
                });

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!_isCarouselImageSet) {
            return false;
        }

        mVelocityTracker.addMovement(event);

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _isClickEvent = true;
                break;
            case MotionEvent.ACTION_MOVE:
                _isClickEvent = false;
                int dx = x - mLastX;
                int dy = y - mLastY;
                mOffsetX -= dx;
                mViewPort.offsetTo(mOffsetX, 0);
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (_isClickEvent) {// <--- on click
                    if (mListener != null) {
                        mListener.onCarouselViewClick(mTag, mCurrentPosition);
                    }
                } else { // <--- scroll image to suitable position smoothly.
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float xVelocity = mVelocityTracker.getXVelocity();
                    Log.v("速度", String.valueOf(xVelocity));
                    // flying && fits user intent
                    if ((Math.abs(xVelocity) >= 50) || (!getCurrentRollbackRange().contains(mViewPort))) {
                        // be care xVelocity > 0 means the user expects to see the picture on the left;
                        mCurrentPosition = xVelocity > 0 ? mCurrentPosition - 1 : mCurrentPosition + 1;
                    }
                    performInfinityScrollToSuitablePosition(mCurrentPosition);
                    mVelocityTracker.clear();
                }

                break;
            default:
                break;
        }



        mLastX = x;
        mLastY = y;
        return true;
    }

    private void performInfinityScrollToSuitablePosition(int position) {

        // perform infinite loop, process index is -1 or last image + 1
        if (position == -1) {
            mViewPort.left += getMeasuredWidth() * mImageCount;
            mViewPort.right +=  getMeasuredWidth() * mImageCount;
            mOffsetX = mViewPort.left;
            position = mImageCount - 1;
        } else if (position == mImageCount) {
            mViewPort.left -= getMeasuredWidth() * mImageCount;
            mViewPort.right -=  getMeasuredWidth() * mImageCount;
            mOffsetX = mViewPort.left;
            position = 0;
        }
        mCurrentPosition = position;
        int currentPositionOffset = getMeasuredWidth() * (1 + position);
        int dx = mOffsetX - currentPositionOffset;
        smoothScrollBy(-dx, 0);
        mOffsetX = currentPositionOffset;
    }


    private Rect getCurrentRollbackRange() {
        // index 0 range rect;
        Rect rect = new Rect((int) (getMeasuredWidth() / 2 + 0.5),
                0, (int) (getMeasuredWidth() * 2.5 + 0.5), getMeasuredHeight());

        if (mCurrentPosition > 0) {
            rect.offsetTo(getMeasuredWidth() * mCurrentPosition, 0);
        }
        return rect;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (_isCarouselImageSet) {
            canvas.drawBitmap(_displayBitmap, mViewPort, mOriginViewPort, null);

            final int r = (int) (2 * DENSITY);
            Paint paint;
            float cx = mDrawIndicatorLeft + getScrollX();
            float cy = (float) (getMeasuredHeight() * 0.9);
            for (int i = 0; i < mImageCount; i++) {
                if (i == mCurrentPosition) {
                    paint = mSelectedIndicatorPaint;
                } else {
                    paint = mUnSelectedIndicatorPaint;
                }
                canvas.drawCircle(cx, cy, r, paint);
                cx += INDICATOR_INTERVAL;
            }
        }
    }

    /**
     * Stitching bitmaps side by side horizontally.
     * bitmaps must have same width and height.
     *
     * @param width      single image's width
     * @param height     single image's height
     * @param bitmapList bitmaps that should be deal with.
     * @return processed bitmap
     */
    private Bitmap stitchingBitmaps(List<Bitmap> bitmapList, int width, int height) {
        int size = bitmapList.size();
        int finalWidth = width * (size + 2);
        int finalHeight = height;

        Bitmap processedBitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(processedBitmap);

        int drawLeft = 0;

        Bitmap scaledBitmap;
        scaledBitmap = resizeBitmap(bitmapList.get(size - 1), width, height);
        canvas.drawBitmap(scaledBitmap, drawLeft, 0f, null);
        drawLeft += width;

        for (Bitmap bitmap : bitmapList) {
            scaledBitmap = resizeBitmap(bitmap, width, height);
            canvas.drawBitmap(scaledBitmap, drawLeft, 0f, null);
            drawLeft += width;
        }

        scaledBitmap = resizeBitmap(bitmapList.get(0), width, height);
        canvas.drawBitmap(scaledBitmap, drawLeft, 0f, null);

        return processedBitmap;
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int expectWidth, int expectHeight) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleX = ((float) expectWidth) / width;
        float scaleY = ((float) expectHeight) / height;
        matrix.postScale(scaleX, scaleY);
        return Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, matrix, true);
    }


    public void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mOffsetX, 0, dx, dy, 500);
        invalidate();
    }


    @Override
    public void computeScroll() {
        // this will be triggered when we call mScroller.startScroll()
        if (mScroller.computeScrollOffset()) {
            mViewPort.offsetTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }

    }


    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }

    public interface OnCarouselViewClickListener {
        public void onCarouselViewClick(int tag, int position);
    }


}


