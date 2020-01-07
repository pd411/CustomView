# View事件体系

## 触摸滑动事件
#### 1. MotionEvent
MotionEvent表示在手指触摸屏幕后所产生的一系列事件，分别有以下几种：

| 事件  | 触发场景  | 单次事件中触发的次数  |
| :------------: | :------------: | :------------: |
| ACTION_DOWN  | 手指刚接触屏幕  | 1次  |
| ACTION_MOVE  |  手指在屏幕上移动 | 0次或多次  |
| ACTION_UP  | 手指在屏幕上松开的一瞬间  |  0次或1次 |
| ACTION_CANCEL  | 手指在滑动超出控件边界  |  0次或1次 |

正常情况下，一次手指触摸屏幕的行为会触发一系列点击事件，考虑如下几种情况：
- 点击屏幕后离开松开，事件为DOWN -> UP
- 点击屏幕滑动一会再松开，事件为DOWN -> MOVE -> ... -> MOVE -> UP

MotionEvent 对象提供了两组方法，可以得到点击事件发生的x和y坐标：
- getX：View的左边到触摸点的距离
- getY：View的顶部到触摸点的距离
- getRawX：手机屏幕左边到触摸点的距离
- getRawY：手机屏幕顶部到触摸点的距离

#### 2. TouchSlop
TouchSlop是系统所能识别的被认为是**滑动的最小距离**，被系统认为滑动和点击事件的临界点。是一个滑动距离值的**常量**，和设备有关，如果滑动距离太短，系统则不认为它是滑动。设备不同，则该值可能是不同的。通过`ViewConfiguration.get(getContext()).getScaledTouchSlop()`获取该常量。在frameworks/base/core/res/res/values/config.xml文件中，可以找到该常量的定义：
`<dimen name="config_viewConfigurationTouchSlop">8dp</dimen>`

#### 3. VelocityTracker
VelocityTracker为滑动速度追踪器，用于监听手指在滑动过程中的速度，包括水平和竖直方向的速度。
首先在View的onTouchEvent方法中获取VelocityTracker对象，再将事件添加到VelocityTracker中：
```
VelocityTracker velocityTracker = VelocityTracker.obtain();
velocityTracker.addMovement(event);
```
之后要获取当前的滑动速度时，采用如下方式来获取当前的滑动速度：
```
velocityTracker.computeCurrentVelocity(1000);
int x = (int) velocityTracker.getXVelocity();
int y = (int) velocityTracker.getYVelocity();
```
首先必须设置computeCurrentVelocity是单位时间内的速度，在这里传入的参数为ms，1000则为1s。这里的速度是指一段时间内手指所滑过的像素数，注意速度可以为负数，如果手指从右向左滑动时，水平方向速度即为负数。

最后当不使用它的时候，需要调用clear方法来重置并回首内存：
```
velocityTracker.clear();
velocityTracker.recycle();
```

#### 4. GestureDetector
GestureDetector用于手势检测，用于辅助检测各种手势或者事件，补充MotionEvent类。当一些特殊的事件发生时，onGestureListener会回调给用户。该类的使用为：
1. 给View创建一个GestureDetector实例；
2. 在onTouchEvent方法中确保你调用了onTouchEvent，该方法定义在当事件发生时，回调被执行；
3. 如果监听onContextClick，必须调用onGenericMotionEvent在View.onGenericMotionEvent里面。

使用的过程如下：
1. 需要创建一个GestureDetector对象并实现OnGestureListener接口，根据需要还可以实现OnDoubleTapListener从而能够监听双击行为：
```
GestureDector mGestureDetector = new GestureDetector(this);
// 解决长按屏幕后无法拖动
mGestureDetector.setIsLongpressEnabled(false);
```
2. 在目标View的onTouchEvent方法中，添加如下实现：
```
boolean consume = mGestureDetector.onTouchEvent(event);
return consume;
```

一些接口说明：

| 方法名  | 描述  | 所属接口  |
| :------------: | :------------: | :------------: |
| onDown  | 手指轻轻触摸屏幕的一瞬间  | onGestureListener  |
| onShowPress  | 手指轻轻触摸屏幕 （注意和onDown()的区别，他强调的是没有松开活着拖动的状态）  | onGestureListener  |
| onSingleTapUp  | 手指轻轻触摸屏幕后松开，伴随着1个MotionEvent.ACTION_UP而触发，这是单击行为  | onGestureListener  |
| onScroll  | 手指按下屏幕并拖动，由1个ACTION_DOWN，多个ACTION_MOVE触发，这是拖动行为  | onGestureListener  |
|  onLongPress | 用户长久地按这屏幕不放，即长按  | onGestureListener  |
| onFling  | 用户按下触摸屏、快速滑动后松开，由1个ACTION_DOWN、多个ACTION_MOVE和1个ACTION_UP触发，这是快速滑动行为  | onGestureListener  |
| onDoubleTap  | 双击，由两个单击组成，不能喝onSingleTapUp并存  | onDoubleTapListener  |

## View的滑动
#### 1. ScrollTo
scrollTo是将view移动到**指定**的位置，也就是基于所传递参数的**绝对滑动**。以下是源码的部分：
```
    /**
     * Set the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the x position to scroll to
     * @param y the y position to scroll to
     */
    public void scrollTo(int x, int y) {
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            invalidateParentCaches();
            onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }
```

在上面的代码中，mScrollX的值总是等于View左边缘和View content左边元在水平方向的距离，而mScrollY的值总是等于View上边缘和View content上边缘在竖直方向的距离。

![mScrollX和mScrollY的变换规律](https://upload-images.jianshu.io/upload_images/5318674-7703a89c73abd483.png?imageMogr2/auto-orient/strip|imageView2/2/w/719/format/webp "mScrollX和mScrollY的变换规律")

#### 2. ScrollBy
scrollBy是在现有的基础上移动view，是以**偏移量**为目的，也就是基于所传递参数的**相对滑动**。
```
    /**
     * Move the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the amount of pixels to scroll by horizontally
     * @param y the amount of pixels to scroll by vertically
     */
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + x, mScrollY + y);
    }
```

从上面代码观察，scrollBy其实也是调用scrollTo的内容，但传入参数为当前的mScrollX/Y + x/y，即表明相对的位移变化。

> 注：scrollTo和scrollBy都是对于content位置的滑动，不会改变View布局的实际位置。

#### 3. Scroller
Scroller是一个封装滑动的类，你能用该类去实现一些滑动的动画，比如一些滑动手势的响应。Scroller追踪滑动的偏移量，但是不会自动的将位置应用于视图。需要开发者自己去获取或者调用新的坐标，并且有一定速度是滑动动画看起来平滑。

下面是一个例子：
```
 private Scroller mScroller = new Scroller(context);
 ...
 public void zoomIn() {
     // Revert any animation currently in progress
     mScroller.forceFinished(true);
     // Start scrolling by providing a starting point and
     // the distance to travel
     mScroller.startScroll(0, 0, 100, 0);
     // Invalidate to request a redraw
     invalidate();
 }

@Override
public void computeScroll() {
	 if (mScroller.computeScrollOffset()) {
		 // Get current x and y positions
		 int currX = mScroller.getCurrX();
		 int currY = mScroller.getCurrY();
		...
	 }
}
```

从上面的代码可以观察到一开始调用startScroll方法，下面是startScroll的源码部分：
```
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDeltaX = dx;
        mDeltaY = dy;
        mDurationReciprocal = 1.0f / (float) mDuration;
    }
```

传入的参数：startX和startY表示**滑动的起点**；dx和dy表示的是要**滑动的距离**；duration表示的是**滑动时间**。通过上面的代码可以看出startScroll只是代表了变量的值，而无法进行滑动操作。而滑动操作是在于接下来的**invalidate**方法。在invalidate方法里面是调用invalidateInternal对View进行重绘，在View的draw方法中又去调用computeScroll方法，computeScroll方法在View中是一个空方法，但上面的代码已经实现了computeScroll方法，最后实现滑动。过程如下：
- 当View重绘后会在draw方法中调用computeScroll，而computeScroll又会去向Scroller获取当前的scrollX和scrollY；
- 通过scrollTo方法实现滑动；
- 调用postInvalidate方法来进行第二次重绘，这一次重绘的过程和第一次重绘一样，还是会导致computeScroll方法被调用；
- 继续向Scroller获取当前的scrollX/Y，并通过scrollTo方法滑动到新的位置，循环此过程知道滑动结束。

下面是调用computeScrollOffset方法去计算偏移量:
```
    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }

        int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    
        if (timePassed < mDuration) {
            switch (mMode) {
            case SCROLL_MODE:
                final float x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal);
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
                break;
            case FLING_MODE:
                final float t = (float) timePassed / mDuration;
                final int index = (int) (NB_SAMPLES * t);
                float distanceCoef = 1.f;
                float velocityCoef = 0.f;
                if (index < NB_SAMPLES) {
                    final float t_inf = (float) index / NB_SAMPLES;
                    final float t_sup = (float) (index + 1) / NB_SAMPLES;
                    final float d_inf = SPLINE_POSITION[index];
                    final float d_sup = SPLINE_POSITION[index + 1];
                    velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                    distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                }

                mCurrVelocity = velocityCoef * mDistance / mDuration * 1000.0f;
                
                mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
                // Pin to mMinX <= mCurrX <= mMaxX
                mCurrX = Math.min(mCurrX, mMaxX);
                mCurrX = Math.max(mCurrX, mMinX);
                
                mCurrY = mStartY + Math.round(distanceCoef * (mFinalY - mStartY));
                // Pin to mMinY <= mCurrY <= mMaxY
                mCurrY = Math.min(mCurrY, mMaxY);
                mCurrY = Math.max(mCurrY, mMinY);

                if (mCurrX == mFinalX && mCurrY == mFinalY) {
                    mFinished = true;
                }

                break;
            }
        }
        else {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        return true;
    }
```

通过上面的代码，如果动画完成了则返回true，反之返回false。这部分代码根据duration来计算出scrollX和scrollY的值，也就是通过时间流逝的百分比来计算出scrollX和scrollY改变的百分比并计算当前值。

## View的事件分发机制
事件分发也就是对MotionEvent事件的分发过程，即当一个MotionEvent产生了之后，系统需要把这个事件传递给一个具体的View，而这个传递的过程就是分发过程。事件分发机制主要有三个方法来处理：

#### public boolean dispatchTouchEvent(MotionEvent ev)
该方法是用来进行事件的分发。如果事件能够传递给当前的View则一定会被调用，返回结果表示是否消费事件，消费则调用当前View的onTouchEvent，否则传递事件。
View的返回结果：
- true：该组件已经对该事件进行处理，不用继续调用其余组件的分发事件，即停止分发；
- false：该组件不能对该事件进行处理，需要按照规则继续分发事件。

Activity的分发处理：
- 如果经过子View层层传递或处理后，该事件被消费了（返回true），则Activity也返回true；
- 如果经过子View层层传递或处理后，该事件没被消费（返回false），则Activity的dispatchTouchEvent不会返回true，而是调用onTouchEvent去处理；
- 如果onTouchEvent消费了事件，那依然能返回true，表示Activity已经消费了事件；
- 如果onTouchEvent没有消费该事件，那就返回false，表示Activity没有消费事件，需要继续处理。

ViewGroup的分发处理：
- 如果onInterceptTouchEvent 返回了false，则交给子View去调用dispatchTouchEvent方法；
- 如果onInterceptTouchEvent 返回了true，则直接交给该ViewGroup对象的onTouchEvent方法进行处理。

View的分发处理：
- 如果onTouch方法返回true，则dispatchTouchEvent也会返回true，表示消费该事件；
- 如果onTouch方法返回false，则调用自身的onTouchEvent来处理。

> 给View设置的onTouchListener，其优先级比onTouchEvent要高，而OnClickListener的优先级最低。


#### public boolean onInterceptTouchEvent(MotionEvent ev)
该方法是用来判断是否拦截某个事件，不往下传递，如果当前View拦截了某个事件，那么在同一个时间序列当中，此方法不会被再次调用，返回结果表示是否拦截当前事件，该方法比较特殊只存在于ViewGroup中，该方法在**dispatchTouchEvent**中调用。

- 该方法默认返回false的，即不拦截事件；
- 如果不希望事件继续往子View传播，可以覆写该方法，返回true，阻止向下传播。

#### public boolean onTouchEvent(MotionEvent ev)
该方法用来处理点击事件，返回结果表示是否消费当前事件，如果不消费，则在同一个事件序列中，当前View无法再次接收到事件，该方法在**dispatchTouchEvent**调用。

- 该方法返回true表示已经处理事件；
- 该方法返回false表示没有对事件进行处理需要继续传递事件。

下面是对事件分发以及拦截的图解：

![事件分发流程图](https://images2017.cnblogs.com/blog/896629/201710/896629-20171007002430630-1646060778.png "事件分发流程图")

通过上图观察到最开始先调用Activity的dispatchTouchEvent方法，如果返回值为true或者false则表示Activity消费了该事件，返回值为super则传递给下一层ViewGroup的dispatchTouchEvent方法；如果ViewGroup的分发方法放回true，则表示ViewGroup消费了当前事件，如果为false则不再向下传播，调用Activity的onTouchEvent方法，如果为super则调用该ViewGroup的onInterceptTouchEvent方法；如果该ViewGroup的onInterceptTouchEvent方法返回false或者super则传递给下一层View的dispatchTouchEvent方法，如果返回true则进行拦截，调用ViewGroup的onTouchEvent方法；若之后传递给子View的dispatchTouchEvent方法，如果该方法返回true则表示对View消费了该事件，如果返回super则交给onTouchEvent处理，最后从最底部的View向上传播会Activity的onTouchEvent方法。

> 在demo中需要使用到垂直滑动ScrollView嵌套上下两个ViewPager，上半部分是一个水平滑动的ViewPager，下半部分同样是一个水平滑动的ViewPager但嵌套多个垂直滑动的RecyclerView。处理滑动冲突的方法：
1. 调用下半部分ViewPager的getTop方法，获取到下方的ViewPager到屏幕顶部的距离；
2. 将该距离设置为限制，如果滑动距离小于该值则最外层ScrollView拦截上下滑动事件，不拦截左右滑动；
3. 如果滑动距离大于或者等于该值，则将滑动事件交给下半部分的ViewPager进行处理。
