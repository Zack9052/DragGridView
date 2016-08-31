package com.example.a19459.mydraggridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 19459 on 2016/8/28.
 */

public class DragGridView extends GridView {
    /**
     * 标记当前是否处于拖动状态，默认为false
     */
    private boolean isDrag=false;

    /**
     * windowManager全局变量，用于拖动时显示item的镜像
     */
    private WindowManager windowManager;

    /**
     * 拖动时的item镜像
     */
    private ImageView virtualImage;

    /**
     * item镜像的参数
     */
    private WindowManager.LayoutParams windowParams;


    /**
     * 记录item中position相对于屏幕和相对于gridView
     *
     * 的x和y差值
     */
    private int winViewDx;

    private int winViewDy;

    /**
     * 手指点击位置距离该item左上角
     *
     * 的x和y差值
     */
    private int fingerDx;

    private int fingerDy;

    /**
     * 用来暂时记录长按下时当时的x和y坐标值
     */
    private int tmpX;

    private int tmpY;

    /**
     * 要拖动item的起始位置
     */
    private int dragOriginPosition;

    /**
     * 拖动时item当前位置
     */
    private int dragCurrentPosition=-1;

    /**
     * 动画是否在执行
     */
    private boolean isMove=false;

    /**
     * 上一次拖动item后的位置（和currentPosition做一个对比）
     */
    private int lastDragPosition=-1;

    public DragGridView(Context context) {
        super(context);
        init();
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs,defStyleAttr);
        init();
    }

    /** 在ScrollView内，所以要进行计算高度 */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    /**
     * 初始化windowManager
     */
    private void init() {
        windowManager=(WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                tmpX= (int) ev.getX();
                tmpY= (int) ev.getY();
                setLongItemClick(ev);//在按下时绑定长按监听
                break;

            case MotionEvent.ACTION_MOVE:
                int x= (int) ev.getRawX();
                int y= (int) ev.getRawY();
                /**
                 * 当处于拖动状态时，要实时更新镜像x，y的值，
                 *
                 * 记得加上手指点击位置和该item左上角的差值
                 *
                 */
                if(isDrag){
                    windowParams.x=x+fingerDx;
                    windowParams.y=y+fingerDy;
                    windowManager.updateViewLayout(virtualImage,windowParams);
                }
                /**
                 * 当前处于拖动状态并且所有的动画都已经结束时
                 */
                if(!isMove&&isDrag){
                    /**
                     * 获取当前镜像所属的item位置
                     */
                    dragCurrentPosition=pointToPosition((int)ev.getX(),(int)ev.getY());
                    /**
                     * 这里要注意两点
                     *
                     * 第一是当前position和上一个记录的position不能是一个，因为可能你手指只是轻轻移动一下，
                     *
                     * 这并不能再次开始动画
                     *
                     * 第二是 当前的position是有效的，-1指的是无效的位置（比如说你手指移到了item缝隙或
                     * 者gridView外面）
                     */
                    if(dragCurrentPosition!=lastDragPosition&&dragCurrentPosition!=-1){
                        /**
                         * 动画是一个接着一个的，这里分开两张情况
                         *
                         * 一种是拖动到后面，另一种是拖动到前面
                         */
                        getChildAt(dragCurrentPosition).setVisibility(INVISIBLE);
                        if(dragCurrentPosition>dragOriginPosition){
                            for(int i=dragOriginPosition+1;i<=dragCurrentPosition;i++){
                                startAnimation(getChildAt(i),i,i-1,i==dragCurrentPosition);
                            }
                        }else{

                            for(int i=dragOriginPosition-1;i>=dragCurrentPosition;i--){
                                startAnimation(getChildAt(i),i,i+1,i==dragCurrentPosition);
                            }
                        }
                        /**
                         * 更新最后拖动位置
                         */
                        lastDragPosition=dragCurrentPosition;
                    }

                }

                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 手指抬起拖动状态为false
                 */
                isDrag=false;
                /**
                 * 有镜像时将其移除
                 */
                if(virtualImage!=null)
                    try {
                        windowManager.removeView(virtualImage);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                if(isMove){
                    getChildAt(dragCurrentPosition-getFirstVisiblePosition()).setVisibility(VISIBLE);

                }else{
                    getChildAt(dragOriginPosition-getFirstVisiblePosition()).setVisibility(VISIBLE);
                }


                lastDragPosition=-1;
                requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 开始一个个执行item移动动画
     * @param view 传进来的item view
     * @param startPosition view开始移动的位置
     * @param endPosition view最后到达的位置
     * @param isLast 当前view是不是最后这次动画的最后一个
     */
    private void startAnimation(final View view, int startPosition, final int endPosition, final boolean isLast){
        /**
         * 获取view的宽和高
         */
        int height=view.getHeight();
        int width=view.getWidth();
        int columNum=getNumColumns();
        /**
         * 计算item所在行数和列数
         */
        int startRow=startPosition/columNum;
        int startColum=startPosition%columNum;
        int endRow=endPosition/columNum;
        int endColum=endPosition%columNum;

        /**
         * 计算x和y方向的偏移量
         */
        int xValue=(endColum-startColum)*(getHorizontalSpacing()+width);
        int yValue=(endRow-startRow)*(getVerticalSpacing()+height);

        /**
         * 开始设置动画
         */
        TranslateAnimation animation=new TranslateAnimation(0,xValue,0,yValue);
        animation.setDuration(300L);
        view.startAnimation(animation);
        /**
         * 设置监听
         */
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                /**
                 * 动画开始，move设置为true
                 */
                isMove=true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /**
                 * 最后一次item移动结束后
                 *
                 *
                 */
                if(isLast){
                    getChildAt(dragOriginPosition).setVisibility(VISIBLE);//最开始隐藏的item显示出来
                    ((MyNewAdapter)getAdapter()).moveItem(dragOriginPosition,dragCurrentPosition);//开始刷新item的新位置
                    dragOriginPosition=dragCurrentPosition;//刷新拖动的初始position
                    isMove=false;//move状态设置为false
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    /**
     * 设置长按点击事件
     * @param ev 当前MotionEvent
     */
    private void setLongItemClick(final MotionEvent ev){
          winViewDx= (int) (ev.getRawX()-ev.getX());//这个差值表示任一点相对于gridView和屏幕的x差值
          winViewDy= (int) (ev.getRawY()-ev.getY());//这个差值表示任一点相对于gridView和屏幕的y差值
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                /**
                 * 开始进入拖动状态
                 */
                isDrag=true;
                dragOriginPosition=i;

                //将当前item隐藏
                getChildAt(i).setVisibility(INVISIBLE);
                //获取当前view 的镜像
                Bitmap virtualBm=getBitmap(i);
                View view1=getChildAt(i);
                /**
                 * 计算View中的point在屏幕上和gridView上的差值
                 */
                fingerDx= (view1.getLeft()-tmpX);
                fingerDy= (view1.getTop()-tmpY);

                //显示镜像view
                virtualImage=showVirtualView(virtualBm,view1.getX()+winViewDx,view1.getY()+winViewDy);
                requestDisallowInterceptTouchEvent(true);
                return true;
            }
        });
    }

    /**
     * 将镜像bitmap显示在屏幕上，返回显示的imageView
     * @param virtualView 镜像bitmap
     * @param x 显示在屏幕上的x值
     * @param y 显示在屏幕上的y值
     * @return 返回imageVIew
     */
    private ImageView showVirtualView(Bitmap virtualView, float x, float y) {
        windowParams=new WindowManager.LayoutParams();
        windowParams.gravity= Gravity.START|Gravity.TOP;
        windowParams.x= (int) x;
        windowParams.y= (int) y;

        windowParams.alpha=0.5f;
        windowParams.width= (int) (virtualView.getWidth()*1.2f);
        windowParams.height= (int) (virtualView.getHeight()*1.2f);
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
        ImageView imageView=new ImageView(getContext());
        imageView.setImageBitmap(virtualView);
        windowManager.addView(imageView,windowParams);
        return imageView;

    }

    /**
     * 通过item position位置获得bitmap
     * @param i gridView的position
     * @return
     */
    private Bitmap getBitmap(int i) {
        ViewGroup viewGroup=(ViewGroup)getChildAt(i-getFirstVisiblePosition());
        viewGroup.destroyDrawingCache();
        viewGroup.setDrawingCacheEnabled(true);
        return Bitmap.createBitmap(viewGroup.getDrawingCache());
    }


}
