# View绘制流程及原理
Android提供了一些多功能且强大的组建模式去创建你自己UI，基于布局类：**View**和**ViewGroup**。在一开始Android平台就已经提供了各式各样的View和ViewGroup的子类，包括Button，TextView，EditText，ListView，CheckBox，RadioButton，Gallery，Spinner，以及一些特殊作用的AutoCompleteTextView，ImageSwitcher，和 TextSwitcher。并且有一些布局类提供，像 LinearLayout, FrameLayout, RelativeLayout等。

但现有的组建或布局都无法满足你的需求，则可以建立自己的View子类。如果只需要进行小的修改对于现有的组建或布局，只需要继承该组建或布局，并且（override）重写里面的方法。

## View的基础概念
View是Android中所有控件的基类，无论是Buttom或者LinearLayout的共同基类都是View。View是一种界面层的控件的一种抽象，它代表了一个控件。除了View，还有ViewGroup，ViewGroup内部包含了许多个控件，即一组View。ViewGroup也继承了View，这意味着View可以是单个控件也可以为多个控件组成，通过这个关系就形成了**View树**的结构。

## ViewRoot和Decorview的概念
#### Decorview
Decorview作为**顶级View**，一般情况下它的内容会包含一个竖直方向的LinearLayout，上面是标题栏（titlebar），下面是内容栏（content）。
![Android 层次结构](https://github.com/pd411/CustomView/blob/master/View%E7%BB%98%E5%88%B6%E6%B5%81%E7%A8%8B%E5%8F%8A%E5%8E%9F%E7%90%86-1.jpg "Android 层次结构")
最开始在Activity中通过setContentView所设置的布局文件其实就是被加入到内容栏中，查看setContentView的代码：
```
    public void setContentView(@LayoutRes int layoutResID) {
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }
```
从上面代码中可以观察到，在该方法中先获取到Window对象将布局资源传递给它，之后查看PhoneWindow中的setContentView的内部实现：
```
    public void setContentView(int layoutResID) {
        if (mContentParent == null) {
            installDecor();
        } else {
            mContentParent.removeAllViews();
        }
        mLayoutInflater.inflate(layoutResID, mContentParent);
    }
```
在Window的setContentView先调用installDecor初始化了DecorView，在解析传入的布局资源设置为mContentParent的子布局，同时在installDecor中调用generateDecor创建DecorView对象。其实DecorView是一个**FrameLayout**。通过`ViewGroup content = (ViewGroup)findViewById(android.R.id.content)`得到content；通过`content.getChildAt(0)`获得设置的View。

#### ViewRoot
ViewRoot对应于ViewRootImpl类，用于连接WindowManager和DecoreView的**连接器**。View的三大流程（*measure*：测量View的宽和高；*layout*：确定View的父容器中的放置位置；*draw*：用于将View进行绘制）是通过ViewRoot来完成的。在ActivityThread中，当Activity对象被创建完毕后，会将DecorView添加到Window中，同时会创建ViewRootImpl对象，并将ViewRootImpl对象和DecorView建立关联：
```
root = new ViewRootImpl(view.getContent(),display);
root.setView(view,wparams,panelParentView)
```
View的绘制流程是从ViewRoot的performTraversals方法开始，经过measure、layout和draw将View绘制出来。performTraversals的流程图如下：

![performTraversals流程图](https://github.com/pd411/CustomView/blob/master/View%E7%BB%98%E5%88%B6%E6%B5%81%E7%A8%8B%E5%8F%8A%E5%8E%9F%E7%90%86-2.png "performTraversals流程图")

如上图所示，performTraversals会依次调用performMeasure、performLayout和performDraw三个方法，这三个方法分别完成View的measure、layou和draw三大流程通过调用onMeasure、onLayout和onDraw。（之后有详细说明）


## View的绘制基本流程
1. 继承View类或者是View的子类。
2. 重写一些方法从超类中，这些超类的方法都是以'on'开头，例如，onDraw(), onMeasure(), 和onKeyDown()等。与继承Activity重写生命周期的'on'方法相似。
3. 在XML中写入自己的拓展的子类。

下面用一个自定义的圆形View来进行演示:

定义自定义的attributes，新创建一个XML在res/values/attrs.xml，
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="CircleView">
        <attr name="showText" format="boolean" />
        <attr name="innerColor" format="color" />
    </declare-styleable>
</resources>
```
showText与innerColor为attribute的名字，format为类型。

创建自定义的View子类`public class CircleView extends View`，重写构造函数：
```
public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        innerColor = ta.getColor(R.styleable.CircleView_innerColor, innerColor);
        ta.recycle();

        areaPaint = new Paint();
		// 设置锯齿
        areaPaint.setAntiAlias(true);
		// 设置笔刷类型
        areaPaint.setStrokeCap(Paint.Cap.ROUND);
		// 设置颜色
        areaPaint.setColor(innerColor);
    }
```
通过**context.obtainStyledAttributes()**方法获取到设置的attributes，返回类型为TypedArray。通过TypedArray里面的方法去设置相应的属性（第一个参数为attribute name，第二个参数为default）。**设置完过后一定要调用recycle()。**
> 官方对**TypedArray**的介绍：
Container for an array of values that were retrieved with **Resources.Theme#obtainStyledAttributes(AttributeSet, int[], int, int)** or **Resources#obtainAttributes**. Be sure to call **recycle()** when done with them. The indices used to retrieve values from this structure correspond to the positions of the attributes given to **obtainStyledAttributes**.
通过查看**obtainStyledAttributes**的源码
```
@NonNull
TypedArray obtainStyledAttributes(@NonNull Resources.Theme wrapper,
		AttributeSet set,
		@StyleableRes int[] attrs,
		@AttrRes int defStyleAttr,
		@StyleRes int defStyleRes) {
	synchronized (mKey) {
		final int len = attrs.length;
		final TypedArray array = TypedArray.obtain(wrapper.getResources(), len);
			......
	}
}
```
跳转到TypedArray.obtain()方法：
```
static TypedArray obtain(Resources res, int len) {
	TypedArray attrs = res.mTypedArrayPool.acquire();
	if (attrs == null) {
		attrs = new TypedArray(res);
	}
		......
}
```
通过上面代码观察到mTypedArrayPool以及下面的创建实例的部分，意味着是使用一个池+单例模式去保证不会OOM，不会在频繁的去创建TypedArray。

重写onMeasure()方法：
```
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int result = Math.min(widthSize, heightSize);

        // 设置图片的为正方形
        setMeasuredDimension(result, result);
    }
```

重写onDraw()方法：
```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 设置半径
        int radius = getWidth() / 2;
        // 画圆
        canvas.drawCircle(radius, radius, radius, areaPaint);
    }
```

添加至XML中
```
<com.app.custom_view.CircleView
        customer:innerColor="@color/colorPrimaryDark"
        android:layout_height="wrap_content"
        android:layout_width="wrap_content" />
```


## View的三个方法

#### onMeasure
该方法是用于测量view的尺寸，去决定被测量的宽和高。当override该方法时一定要去调用**setMeasuredDimension(int, int)**去保存设置的宽和高。

**View.MeasureSpec**
MeasureSpec代表一个32位int值，高2位代表SpecMode（测量模式），低30位代表SpecSize（测量规格）。通过MeasureSpec.getSize和MeasureSpec.getMode方法去获得宽和高相对应的size和mode，一共有三种SpecMode：
- UNISPECIFIED：父视图不会限制子视图的尺寸大小，例如ListView和ScrollView，常量值为0 (0x00000000)；
- EXACTLY：子视图有确切的尺寸大小，例如match_parent和300dp，常量值为1073741824 (0x40000000)；
- AT_MOST：子视图尺寸大小不能超过某个值，例如wrap_parent，常量值为-2147483648 (0x80000000)。

**MeasureSpec和LayoutParams的对应关系**
在系统测量的时候，系统会讲LayoutParams在芙蓉起的约束下转换为对应的MeasureSpec，在根据这个MeasureSpec来确定View测量后的宽高。LayoutParams需要和父容器一起才能决定View的MeasureSpec从而进一步决定View的宽高。对于**DecorView**，其MeasureSpec由芙蓉起的MeasureSpec来共同确定；对于**普通View**，其MeasureSpec由父容器的MeasureSpec和自身的LayoutParams来共同决定，MeasureSpec一旦确定后，onMeasure中就可以确定View的测量宽高。

对于DecorView的MeasureSpec的产生过程就很明确，根据它的LayoutParams中的宽高的参数来划分。
- LayoutParams.MATCH_PARENT：EXACTLY，大小就是窗口的大小；
- LayoutParams.WRAP_CONTENT：AT_MOST，大小不定，但不能超过窗口大小；
- 固定大小（dp）：EXACTLY，大小为LayoutParams中指定的大小。

对于普通View来说，这里是指我们布局中的View，View的measure过程由ViewGroup传递而来，子元素的MeasureSpec的创建与父容器的MeasureSpec和子元素本身的LayoutParams有关，此外还和View的margin及padding有关。下表是普通MeasureSpec的创建规矩：

|  childLayoutParams \ parentSpecMode | EXACTLY  | AT_MOST  | UNSPECIFIED  |
| :------------: | :------------: | :------------: | :------------: |
| dp/px  | EXACTLY (childSize)  | EXACTLY (childSize)  | EXACTLY (childSize)  |
| match_parent  | EXACTLY (childSize)  | AT_MOST (parentSize)  | UNSPECIFIED (0)  |
| wrap_content  | AT_MOST (parentSize)  | AT_MOST (parentSize)  | UNSPECIFIED (0)  |

当View采用固定宽高的时候，不管父容器的MeasureSpec是什么，View的MeasureSpec都是EXACTLY模式并且其大小遵循LayoutParams中的大小。当View的宽高是match_parent时，如果父容器的模式是EXACTLY模式，那么View也是EXACTLY模式并且其大小是父容器的剩余空间；如果父容易是AT_MOST模式，那么View也是AT_MOST并且其大小不会超过父容器的剩余空间。当View的宽高是wrap_content时，不管父容器的模式是EXACTLY还是AT_MOST，View的模式总是AT_MOST并且大小不能超过父容器的剩余空间。

注：在demo里面，我使用了一个ViewPager嵌套RecyclerView的布局，但如果不设置固定ViewPager的大小（如设置match_parent或者wrap_content），则RecyclerView的width和height都0并不显示。

#### onLayout
该方法用于指定子视图相对于父视图的位置。Layout的作用是ViewGroup用来确定子元素的位置，当ViewGroup的位置被确定后，它在onLayout中会遍历所有的子元素并调用其layout方法，在layout方法中onLayout方法又会被调用。

layout方法的大致流程如下：1. 通过setFrame方法来设定View的四个顶点的位置，那么View在父容器中的位置也就确定了；2. 调用onLayout方法，这个方法的用途是父容器确定子元素的位置，和onMeasure方法类似，onLayout的具体实现同样和具体的布局有关，所有View和ViewGroup均没有真正实现onLayout方法。

首先查看layout的源码
```
public final void layout(int l, int t, int r, int b) {
        boolean changed = setFrame(l, t, r, b);
        if (changed || (mPrivateFlags & LAYOUT_REQUIRED) == LAYOUT_REQUIRED) {
            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.ON_LAYOUT);
            }
            onLayout(changed, l, t, r, b);
            mPrivateFlags &= ~LAYOUT_REQUIRED;
        }
        mPrivateFlags &= ~FORCE_LAYOUT;
    }
```
在最开始调用了setFrame方法
```
protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = false;
        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            changed = true;
 			// ...
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
			// ...
        }
        return changed;
    }
```
在setFrame里面将left、top、right和bottom这四个值保存下来。也就是说在layout中的setFrame方法中会将子View相对于父View的left、top、right的四个值保存下来，通过这四个值可以确定子视图在父视图中的位置。

在ViewGroup中的onLayout是一个抽象方法，如果继承ViewGroup类则必须实现该方法。

**mLeft, mTop, mRight, mBottom讲解：**
- mLeft: View.getLeft()，子视图的左边到父视图的左边的距离；
- mTop：View.getTop()，子视图的顶部到父视图的顶部的距离；
- mRight: View.getRight()，子视图的右边到父视图的右边的距离；
- mBottom: View.getBottom()，子视图的底部到父视图的底部的距离。

#### onDraw
View的绘制过程遵循如下几步：
1. 绘制背景 background.draw(canvas)
2. 绘制自己 (onDraw)
3. 绘制 children (dispatchDraw)
4. 绘制装饰 (onDrawScrollBars)

```
public void draw(Canvas canvas) {
    if (ViewDebug.TRACE_HIERARCHY) {
        ViewDebug.trace(this, ViewDebug.HierarchyTraceType.DRAW);
    }
    final int privateFlags = mPrivateFlags;
    final boolean dirtyOpaque = (privateFlags & DIRTY_MASK) == DIRTY_OPAQUE &&
            (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
    mPrivateFlags = (privateFlags & ~DIRTY_MASK) | DRAWN;
    // Step 1, draw the background, if needed
    int saveCount;
    if (!dirtyOpaque) {
        final Drawable background = mBGDrawable;
        if (background != null) {
            final int scrollX = mScrollX;
            final int scrollY = mScrollY;
            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0,  mRight - mLeft, mBottom - mTop);
                mBackgroundSizeChanged = false;
            }
            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                background.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
    }
    final int viewFlags = mViewFlags;
    boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
    boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
    if (!verticalEdges && !horizontalEdges) {
        // Step 2, draw the content
        if (!dirtyOpaque) onDraw(canvas);
        // Step 3, draw the children
        dispatchDraw(canvas);
        // Step 4, draw decorations (scrollbars)
        onDrawScrollBars(canvas);
        // we're done...
        return;
    }
}
```

第一步是对视图的背景进行绘制，首先会得到一个mBGDrawable对象，然后在根据layout过程确定视图位置来设置背景的绘制区域，之后在调用Drawable的draw方法来进行背景绘制。

第二步是对视图的内容进行绘制，这里调用了onDraw方法，但onDraw是一个空方法，需要子类去进行实现。

第三步是对当前视图的所有子视图进行绘制，View绘制过程的传递是通过dispatchDraw来实现的，dispatchDraw会遍历调用所有子元素的draw方法，如此draw事件就一层层地传递了下去。View有一个特殊的方法setWillNotDraw，表示如果一个View不需要绘制任何内容，那么设置这个标记为true后，系统会进行相应的优化。默认情况下，View没有启用这个优化标记位，但ViewGroup会默认启动这个优化标记位，但是ViewGroup会默认启用这个优化标记位。这个标记位对实际开发的意义是：当我们自定义控件继承于ViewGroup并且本身不具备绘制功能时，就可以开启这个标记位从而便于系统进行后续的优化。当然，当明确知道一个ViewGroup需要通过onDraw来绘制内容是，我们需要显式的关闭WILL_NOT_DRAW这个标记位。

第四步是对视图的滚动条进行绘制。

