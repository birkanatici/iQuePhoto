package net.iquesoft.iquephoto.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import net.iquesoft.iquephoto.R;
import net.iquesoft.iquephoto.model.Sticker;
import net.iquesoft.iquephoto.model.Text;

import java.util.ArrayList;
import java.util.List;

public class ImageEditorView extends ImageView {

    private float mPrevDistance;
    private boolean isScaling;


    // START - For text and sticker
    private float MIN_SCALE = 0.5f;
    private float MAX_SCALE = 0.5f;

    private Bitmap mDeleteHandleBitmap;
    private Bitmap mResizeHandleBitmap;
    private Bitmap mFrontHandleBitmap;

    private Paint mFramePaint;
    // END - For text and sticker.

    private int mCheckedTextId = -1;
    private int mCheckedStickerId = -1;

    private int mPreMoveX;
    private int mPreMoveY;

    private Context mContext;

    private Bitmap mSourceBitmap;
    private Bitmap mOverlayBitmap;
    private Bitmap mFrameBitmap;

    private Paint mOverlayPaint;

    private List<Text> mTextsList;
    private List<EditorSticker> mStickersList;

    private Drawable mSourceDrawable;

    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private float mScale = 1.0f;
    private float mAngle = 0.0f;
    private float mImgWidth = 0.0f;
    private float mImgHeight = 0.0f;

    private float mBrightnessValue = 0;
    private float mWarmthValue = 0;

    private ColorMatrix mAdjustColorMatrix;
    private ColorMatrixColorFilter mAdjustColorMatrixColorFilter;

    private Paint mPaintBitmap;
    private Paint mFilterPaint;

    private ColorMatrix mFilterColorMatrix;
    private boolean mHasFilter;

    private boolean mIsInitialized = false;
    private Matrix mMatrix = null;
    private Paint mPaintTranslucent;


    private RectF mImageRect;
    private PointF mCenter = new PointF();

    private float mLastX, mLastY;

    private TouchArea mTouchArea = TouchArea.OUT_OF_BOUNDS;

    private int mTouchPadding = 0;

    private boolean mIsEnabled = true;

    public ImageEditorView(Context context) {
        this(context, null);
    }

    public ImageEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        float density = getDensity();

        mContext = context;

        mStickersList = new ArrayList<>();
        mTextsList = new ArrayList<>();

        mPaintTranslucent = new Paint();
        mPaintBitmap = new Paint();
        mPaintBitmap.setFilterBitmap(true);

        mFilterPaint = new Paint();
        mOverlayPaint = new Paint();

        mAdjustColorMatrix = new ColorMatrix();

        mMatrix = new Matrix();

        mScale = 1.0f;

        initStickerView();
    }

    private void initStickerView() {
        mDeleteHandleBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_handle_delete)).getBitmap();
        mResizeHandleBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_handle_resize)).getBitmap();
        mFrontHandleBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_handle_edit)).getBitmap();

        mFramePaint = new Paint();
        mFramePaint.setColor(Color.WHITE);
        mFramePaint.setAntiAlias(true);
        mFramePaint.setDither(true);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(7.5f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();

        if (mIsInitialized) {
            setMatrix();

            canvas.drawBitmap(mSourceBitmap, mMatrix, mPaintBitmap);
        }

        if (mOverlayBitmap != null)
            canvas.drawBitmap(mOverlayBitmap, mMatrix, mOverlayPaint);

        if (mFrameBitmap != null)
            canvas.drawBitmap(mFrameBitmap, mMatrix, mPaintBitmap);

        if (mStickersList.size() > 0) {
            drawStickers(canvas);
        }

        if (mTextsList.size() > 0) {
            drawTexts(canvas);
        }

        canvas.restore();
        //canvas.drawBitmap(mSourceBitmap, mMatrix, mFilterPaint);

        //canvas.drawBitmap(mSourceBitmap, mMatrix, getAdjustPaint());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(viewWidth, viewHeight);

        mViewWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        mViewHeight = viewHeight - getPaddingTop() - getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getDrawable() != null) setupLayout(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchCount = event.getPointerCount();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /*if (isInButton(event, mDeleteHandleRect)) {
                    mStickersList.clear();
                    invalidate();
                }*/
                if (touchCount >= 2) {
                    float distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                    mPrevDistance = distance;
                    isScaling = true;
                } else {
                    mPreMoveX = (int) event.getX();
                    mPreMoveY = (int) event.getY();

                    //findCheckedText(mPreMoveX, mPreMoveY);
                    findCheckedSticker(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchCount >= 2 && isScaling) {
                    float dist = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                    float scale = (dist - mPrevDistance) / dispDistance();
                    mPrevDistance = dist;
                    scale += 1;
                    scale = scale * scale;
                    if (mCheckedStickerId != -1)
                        mTextsList.get(mCheckedTextId).setSize(mTextsList.get(mCheckedTextId).getSize() * scale);

                } else if (!isScaling) {
                    int distanceX = mPreMoveX - (int) event.getX();
                    int distanceY = mPreMoveY - (int) event.getY();

                    mPreMoveX = (int) event.getX();
                    mPreMoveY = (int) event.getY();
                    if (mCheckedTextId != -1) {

                        Text text = mTextsList.get(mCheckedTextId);
                        text.setX(text.getX() - Math.round(distanceX));
                        text.setY(text.getY() - Math.round(distanceY));
                    }
                    float x = event.getX(0);
                    float y = event.getY(0);

                    if (mCheckedStickerId != -1) {
                        mStickersList.get(mCheckedStickerId).getMatrix().postTranslate(distanceX, distanceY);

                        mLastX = x;
                        mLastY = y;
                        invalidate();
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        invalidate();
        return true;

    }

    private float distance(float x0, float x1, float y0, float y1) {

        float x = x0 - x1;
        float y = y0 - y1;
        return (float) Math.sqrt(x * x + y * y);
    }

    private float dispDistance() {

        return (float) Math.sqrt(getWidth() * getWidth() + getHeight() * getHeight());
    }

    public void addSticker(Sticker sticker) {
        sticker.setBitmap(((BitmapDrawable) mContext.getResources().getDrawable(sticker.getImage())).getBitmap());
        EditorSticker editorSticker = new EditorSticker(sticker);

        mStickersList.add(editorSticker);
        invalidate();
    }

    private void drawStickers(Canvas canvas) {
        for (EditorSticker editorSticker : mStickersList) {
            editorSticker.drawSticker(canvas,
                    mDeleteHandleBitmap,
                    mResizeHandleBitmap,
                    mFrontHandleBitmap,
                    mFramePaint);
        }
    }

    private boolean isInButton(MotionEvent event, Rect rect) {
        int left = rect.left;
        int right = rect.right;
        int top = rect.top;
        int bottom = rect.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    private boolean isInBitmap(MotionEvent event, EditorSticker editorSticker) {
        float[] arrayOfFloat1 = new float[9];
        editorSticker.getMatrix().getValues(arrayOfFloat1);

        float f1 = 0.0F * arrayOfFloat1[0] + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f2 = 0.0F * arrayOfFloat1[3] + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];

        float f3 = arrayOfFloat1[0] * editorSticker.getBitmap().getWidth() + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f4 = arrayOfFloat1[3] * editorSticker.getBitmap().getWidth() + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];

        float f5 = 0.0F * arrayOfFloat1[0] + arrayOfFloat1[1] * editorSticker.getBitmap().getHeight() + arrayOfFloat1[2];
        float f6 = 0.0F * arrayOfFloat1[3] + arrayOfFloat1[4] * editorSticker.getBitmap().getHeight() + arrayOfFloat1[5];

        float f7 = arrayOfFloat1[0] * mStickersList.get(0).getBitmap().getWidth() + arrayOfFloat1[1] * editorSticker.getBitmap().getHeight() + arrayOfFloat1[2];
        float f8 = arrayOfFloat1[3] * mStickersList.get(0).getBitmap().getWidth() + arrayOfFloat1[4] * editorSticker.getBitmap().getHeight() + arrayOfFloat1[5];

        float[] arrayOfFloat2 = new float[4];
        float[] arrayOfFloat3 = new float[4];

        arrayOfFloat2[0] = f1;
        arrayOfFloat2[1] = f3;
        arrayOfFloat2[2] = f7;
        arrayOfFloat2[3] = f5;

        arrayOfFloat3[0] = f2;
        arrayOfFloat3[1] = f4;
        arrayOfFloat3[2] = f8;
        arrayOfFloat3[3] = f6;
        return pointInRect(arrayOfFloat2, arrayOfFloat3, event.getX(0), event.getY(0));
    }

    private boolean pointInRect(float[] xRange, float[] yRange, float x, float y) {

        double a1 = Math.hypot(xRange[0] - xRange[1], yRange[0] - yRange[1]);
        double a2 = Math.hypot(xRange[1] - xRange[2], yRange[1] - yRange[2]);
        double a3 = Math.hypot(xRange[3] - xRange[2], yRange[3] - yRange[2]);
        double a4 = Math.hypot(xRange[0] - xRange[3], yRange[0] - yRange[3]);

        double b1 = Math.hypot(x - xRange[0], y - yRange[0]);
        double b2 = Math.hypot(x - xRange[1], y - yRange[1]);
        double b3 = Math.hypot(x - xRange[2], y - yRange[2]);
        double b4 = Math.hypot(x - xRange[3], y - yRange[3]);

        double u1 = (a1 + b1 + b2) / 2;
        double u2 = (a2 + b2 + b3) / 2;
        double u3 = (a3 + b3 + b4) / 2;
        double u4 = (a4 + b4 + b1) / 2;


        double s = a1 * a2;

        double ss = Math.sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2))
                + Math.sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3))
                + Math.sqrt(u3 * (u3 - a3) * (u3 - b3) * (u3 - b4))
                + Math.sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1));
        return Math.abs(s - ss) < 0.5;


    }

    public void addText(Text text) {
        if (text.getSize() == 0) {
            text.setSize(Text.DEFAULT_SIZE);
        }
        if (text.getColor() == 0) {
            text.setColor(Text.DEFAULT_COLOR);
        }
        if (text.getTypeface() == null) {
            text.setTypeface(Typeface.DEFAULT);
        }

        text.setX(100);
        text.setY(100);

        mTextsList.add(text);
        invalidate();
    }

    private void drawTexts(Canvas canvas) {
        for (Text text : mTextsList) {
            canvas.drawText(text.getText(), text.getX(), text.getY(), text.getPaint());
            Paint paint = new Paint();
            paint.setColor(Color.DKGRAY);
            canvas.drawRect(text.getRect(), paint);
            Log.i("Draw text", text.toString());
        }
    }

    private Paint getAdjustPaint() {
        Paint paint = new Paint();

        mAdjustColorMatrix.setConcat(getWarmthColorMatrix(mWarmthValue), getBrightnessColorMatrix(mBrightnessValue));

        paint.setColorFilter(new ColorMatrixColorFilter(mAdjustColorMatrix));

        return paint;
    }

    public void setFilter(@Nullable ColorMatrix colorMatrix) {
        mFilterColorMatrix = colorMatrix;
        if (colorMatrix != null) {
            mHasFilter = true;
            //mFilterPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            mSourceDrawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            mSourceBitmap = ((BitmapDrawable) mSourceDrawable).getBitmap();
        } else {
            mHasFilter = false;
        }
        invalidate();
    }

    public void setFrame(@Nullable Drawable drawable) {
        if (drawable != null) {
            mFrameBitmap = Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(),
                    mSourceBitmap.getWidth(), mSourceBitmap.getHeight(), false);
        } else {
            mFrameBitmap = null;
        }
    }

    public void setOverlay(@Nullable Drawable drawable) {
        if (drawable != null) {
            mOverlayBitmap = Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(),
                    mSourceBitmap.getWidth(), mSourceBitmap.getHeight(), false);
            mOverlayPaint.setAlpha(125);
        } else {
            mOverlayBitmap = null;
        }

        invalidate();
    }

    public void setOverlayOpacity(int value) {
        int alpha = (int) Math.round(value * 1.5);
        mOverlayPaint.setAlpha(alpha);
        invalidate();
    }

    public void setFilterIntensity(int intensity) {
        int filterIntensity = (int) (intensity * 2.55);
        mFilterPaint.setAlpha(filterIntensity);
        invalidate();
    }

    public void setBrightnessValue(float brightnessValue) {
        mBrightnessValue = brightnessValue;
        invalidate();
    }

    public void setWarmthValue(float warmthValue) {
        mWarmthValue = warmthValue;
        invalidate();
    }

    private ColorMatrix getWarmthColorMatrix(float value) {
        float temp = value / 220;

        return new ColorMatrix(new float[]
                {
                        1, 0, 0, temp, 0,
                        0, 1, 0, temp / 2, 0,
                        0, 0, 1, temp / 4, 0,
                        0, 0, 0, 1, 0
                });
    }

    private ColorMatrix getBrightnessColorMatrix(float value) {
        return new ColorMatrix(new float[]{1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0});
    }

    private void findCheckedText(int x, int y) {
        for (int i = mTextsList.size() - 1; i >= 0; i--) {
            if (mTextsList.get(i).getRect().contains(x, y)) {
                mCheckedTextId = i;
                Log.i("checked text", String.valueOf(mCheckedTextId));
                return;
            }
        }
        mCheckedTextId = -1;
    }

    private void findCheckedSticker(MotionEvent event) {
        for (int i = mStickersList.size() - 1; i >= 0; i--) {
            EditorSticker editorSticker = mStickersList.get(i);
            if (isInBitmap(event, editorSticker)) {
                mCheckedStickerId = i;
                return;
            }
        }
        mCheckedStickerId = -1;
    }

    private void deleteText(Text text) {
        if (mTextsList != null && mTextsList.contains(text)) {
            mTextsList.remove(text);
            invalidate();
            requestLayout();
            String string = String.format(getResources().getString(R.string.text_deleted), text.getText());
            Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
        }
    }


    private void setMatrix() {
        mMatrix.reset();
        mMatrix.setTranslate(mCenter.x - mImgWidth * 0.5f, mCenter.y - mImgHeight * 0.5f);
        mMatrix.postScale(mScale, mScale, mCenter.x, mCenter.y);
        mMatrix.postRotate(mAngle, mCenter.x, mCenter.y);
    }

    private void setupLayout(int viewW, int viewH) {
        if (viewW == 0 || viewH == 0) return;
        setCenter(new PointF(getPaddingLeft() + viewW * 0.5f, getPaddingTop() + viewH * 0.5f));
        setScale(calcScale(viewW, viewH, mAngle));
        setMatrix();
        mImageRect = calcImageRect(new RectF(0f, 0f, mImgWidth, mImgHeight), mMatrix);
        mIsInitialized = true;
        invalidate();
    }

    private float calcScale(int viewW, int viewH, float angle) {
        mImgWidth = getDrawable().getIntrinsicWidth();
        mImgHeight = getDrawable().getIntrinsicHeight();
        if (mImgWidth <= 0) mImgWidth = viewW;
        if (mImgHeight <= 0) mImgHeight = viewH;
        float viewRatio = (float) viewW / (float) viewH;
        float imgRatio = getRotatedWidth(angle) / getRotatedHeight(angle);
        float scale = 1.0f;
        if (imgRatio >= viewRatio) {
            scale = viewW / getRotatedWidth(angle);
        } else if (imgRatio < viewRatio) {
            scale = viewH / getRotatedHeight(angle);
        }
        return scale;
    }

    private RectF calcImageRect(RectF rect, Matrix matrix) {
        RectF applied = new RectF();
        matrix.mapRect(applied, rect);
        return applied;
    }

    private void onDown(MotionEvent e) {
        invalidate();
        mLastX = e.getX();
        mLastY = e.getY();
        //checkTouchArea(e.getX(), e.getY());
    }

    private void onMove(MotionEvent e) {
        float diffX = e.getX() - mLastX;
        float diffY = e.getY() - mLastY;
        switch (mTouchArea) {
            case CENTER:
                //moveFrame(diffX, diffY);
                break;
            case LEFT_TOP:
                //moveHandleLT(diffX, diffY);
                break;
            case RIGHT_TOP:
                //moveHandleRT(diffX, diffY);
                break;
            case LEFT_BOTTOM:
                // moveHandleLB(diffX, diffY);
                break;
            case RIGHT_BOTTOM:
                //moveHandleRB(diffX, diffY);
                break;
            case OUT_OF_BOUNDS:
                break;
        }
        invalidate();
        mLastX = e.getX();
        mLastY = e.getY();
    }

    private boolean isInsideHorizontal(float x) {
        return mImageRect.left <= x && mImageRect.right >= x;
    }

    private boolean isInsideVertical(float y) {
        return mImageRect.top <= y && mImageRect.bottom >= y;
    }

    private float getDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    private Bitmap getBitmap() {
        Bitmap bm = null;
        Drawable d = getDrawable();
        if (d != null && d instanceof BitmapDrawable) bm = ((BitmapDrawable) d).getBitmap();
        return bm;
    }

    private float getRotatedWidth(float angle) {
        return getRotatedWidth(angle, mImgWidth, mImgHeight);
    }

    private float getRotatedWidth(float angle, float width, float height) {
        return angle % 180 == 0 ? width : height;
    }

    private float getRotatedHeight(float angle) {
        return getRotatedHeight(angle, mImgWidth, mImgHeight);
    }

    private float getRotatedHeight(float angle, float width, float height) {
        return angle % 180 == 0 ? height : width;
    }

    public Bitmap getImageBitmap() {
        return getBitmap();
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        mSourceBitmap = bitmap;
        mSourceDrawable = new BitmapDrawable(bitmap);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        mIsInitialized = false;
        super.setImageDrawable(drawable);

        //mSourceBitmap = ((BitmapDrawable) drawable).getBitmap();

        updateLayout();
    }

    @Override
    public void setImageURI(Uri uri) {
        mIsInitialized = false;
        super.setImageURI(uri);
        updateLayout();
    }

    private void updateLayout() {
        Drawable d = getDrawable();
        if (d != null) {
            setupLayout(mViewWidth, mViewHeight);
        }
    }

    private void setScale(float scale) {
        mScale = scale;
    }

    private void setCenter(PointF center) {
        mCenter = center;
    }


    private enum TouchArea {
        OUT_OF_BOUNDS, CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }

    public class MakeImage extends AsyncTask<Void, Void, Bitmap> {
        private Canvas mCanvas;
        private Bitmap mBitmap;

        @Override
        protected Bitmap doInBackground(Void... params) {

            mBitmap = mSourceBitmap.copy(mSourceBitmap.getConfig(), false);
            mCanvas = new Canvas(mBitmap);

            if (mOverlayBitmap != null)
                mCanvas.drawBitmap(mOverlayBitmap, mMatrix, mOverlayPaint);

            if (mFrameBitmap != null)
                mCanvas.drawBitmap(mFrameBitmap, mMatrix, mPaintBitmap);

            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }
}
