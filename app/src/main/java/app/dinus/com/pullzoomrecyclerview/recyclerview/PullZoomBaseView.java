package app.dinus.com.pullzoomrecyclerview.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 *
 * @author dinus
 */
public abstract class PullZoomBaseView<T extends View> extends LinearLayout {
    protected static final long ZOOM_BACK_DURATION = 300L;

    private static final float FRICTION = 2.5f;

    public static final int ZOOM_HEADER = 0;
    public static final int ZOOM_FOOTER = 1;

    protected T mWrapperView;
    protected ViewGroup mHeaderContainer;
    protected View mZoomView;

    private float mInitTouchX;
    private float mInitTouchY;
    private float mLastTouchX;
    private float mLastTouchY;

    private boolean isZoomEnable;
    private boolean isZooming;
    private boolean isPullStart;

    protected int mModel;
    private int mTouchSlop;

    private OnPullZoomListener mOnPullZoomListener;

    public PullZoomBaseView(Context context) {
        this(context, null);
    }

    public PullZoomBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mModel = createDefaultPullZoomModel();

        isZoomEnable = true;
        isPullStart = false;
        isZooming = false;

        mWrapperView = createWrapperView(context);
        addView(mWrapperView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isZoomEnable){
            return false;
        }

        if (event.getEdgeFlags() != 0 && event.getAction() == MotionEvent.ACTION_DOWN){
            return false;
        }

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // do nothing
                // the init action has been done in the function onInterceptTouchEvent
                break;

            case MotionEvent.ACTION_MOVE:
                if (isPullStart){
                    isZooming = true;
                    mLastTouchY = event.getY();
                    mLastTouchX = event.getX();

                    float scrollValue = mModel == ZOOM_HEADER ?
                            Math.round(Math.min(mInitTouchY - mLastTouchY, 0) / FRICTION)
                            :Math.round(Math.max(mInitTouchY - mLastTouchY, 0) / FRICTION);
                    pullZoomEvent(scrollValue);

                    if (mOnPullZoomListener != null){
                        mOnPullZoomListener.onPullZooming(scrollValue);
                    }

                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isPullStart){
                    isPullStart = false;
                    if (isZooming){
                        isZooming = false;
                        smoothScrollToTop();

                        if (mOnPullZoomListener != null){
                            final float scrollValue = mModel == ZOOM_HEADER ?
                                    Math.round(Math.min(mInitTouchY - mLastTouchY, 0) / FRICTION)
                                    :Math.round(Math.max(mInitTouchY - mLastTouchY, 0) / FRICTION);

                            mOnPullZoomListener.onPullZoomEnd(scrollValue);
                        }
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (!isZoomEnable){
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE && isPullStart){
            return true;
        }


        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (isReadyZoom()){
                    mInitTouchX = mLastTouchX = event.getX();
                    mInitTouchY = mLastTouchY = event.getY();
                    isPullStart = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isReadyZoom()){
                    float mCurrentX = event.getX();
                    float mCurrentY = event.getY();

                    float xDistance = mCurrentX - mLastTouchX;
                    float yDistance = mCurrentY - mLastTouchY;

                    if (mModel == ZOOM_HEADER && yDistance > mTouchSlop && yDistance > Math.abs(xDistance)
                            || mModel == ZOOM_FOOTER && -yDistance > mTouchSlop && -yDistance > Math.abs(xDistance)){
                        mLastTouchY = mCurrentY;
                        mLastTouchX = mCurrentX;

                        if (mOnPullZoomListener != null){
                            mOnPullZoomListener.onPullStart();
                        }
                        isPullStart = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // do nothing
                // the reset action will be done in the function onTouchEvent
                break;
        }
        return isPullStart;
    }

    public void setModel(int mModel) {
        this.mModel = mModel;
    }

    public void setOnPullZoomListener(OnPullZoomListener mOnPullZoomListener) {
        this.mOnPullZoomListener = mOnPullZoomListener;
    }

    public void setIsZoomEnable(boolean isZoomEnable) {
        this.isZoomEnable = isZoomEnable;
    }

    public void setZoomView(View mZoomView) {
        this.mZoomView = mZoomView;
    }

    public void setHeaderContainer(ViewGroup mHeaderContainer) {
        this.mHeaderContainer = mHeaderContainer;
    }

    public boolean isZoomEnable() {
        return isZoomEnable;
    }

    public boolean isZooming() {
        return isZooming;
    }

    protected abstract T createWrapperView(Context context);

    protected abstract int createDefaultPullZoomModel();

    protected abstract boolean isReadyZoom();

    /**
     * @param scrollValue vertical distance scrolled in pixels
     *          if scrollValue < 0  ; scroll up
     *          if scrollValue > 0  ; scroll down
     */
    protected abstract void pullZoomEvent(float scrollValue);

    protected abstract void smoothScrollToTop();

    public interface OnPullZoomListener {
        void onPullZooming(float newScrollValue);

        void onPullStart();

        void onPullZoomEnd(float newScrollValue);
    }
}
