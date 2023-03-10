### 准备工作

首先导包

```kotlin
implementation 'androidx.navigation:navigation-fragment:2.3.5'
implementation 'androidx.navigation:navigation-ui-ktx:2.3.5'

implementation("com.squareup.okhttp3:okhttp:4.9.0")
implementation("com.squareup.okio:okio:2.2.2")
implementation 'com.google.code.gson:gson:2.8.9'
implementation 'com.github.bumptech.glide:glide:4.13.2'
annotationProcessor 'com.github.bumptech.glide:compiler:4.13.2'
api 'com.readystatesoftware.systembartint:systembartint:1.0.3'
implementation("com.tencent:mmkv:1.2.13")

implementation 'io.github.scwang90:refresh-layout-kernel:2.0.5'      //核心必须依赖
implementation 'io.github.scwang90:refresh-header-classics:2.0.5'    //经典刷新头
implementation 'io.github.scwang90:refresh-footer-classics:2.0.5'    //经典加载
```

在gradle.properties添加如下一行

```xml
android.enableJetifier=true
```

然后同步一下

创建一个navigation用来管理我们的fragment

![image-20221217130016926](.\README.assets\image-20221217130016926.png)

添加network_security_config允许我们的网络请求

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

![image-20221217134007356](.\README.assets\image-20221217134007356.png)

创建一个BaseFragment作为fragment的基类，代码如下

```kotlin
abstract class BaseFragment<T : ViewBinding> : Fragment() {

    protected lateinit var mBinding: T
    protected lateinit var mainModel: MainModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainModel = ViewModelProvider(requireActivity()).get(MainModel::class.java)
        mBinding = providedViewBinding(inflater, container)
        initData()
        initEvent()
        return mBinding.root
    }

    abstract fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    abstract fun initData()

    abstract fun initEvent()

}
```

再创建MainModel继承ViewModel用来管理我们的数据

```kotlin
class MainModel : ViewModel() {
}
```

### 获取网络数据

创建一个HomeFragment继承BaseFragment作为我们的首页

```kotlin
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initData() {
    }

    override fun initEvent() {
    }

}
```

在nav_graph.xml中添加该布局

![image-20221217130745638](.\README.assets\image-20221217130745638.png)

在编写activity_main.xml 的代码，引入该navigation

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">


    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragment_container"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:defaultNavHost="true"
        app:layout_constraintTop_toTopOf="parent"
        app:navGraph="@navigation/nav_graph" />

</RelativeLayout>
```

MainActivity可以暂时关掉，开始编写HomeFragment中的逻辑

首先我们要先获取壁纸的分类，调用的接口如下

http://service.picasso.adesk.com/v1/vertical/category?adult=true&first=1

可以自己调用以下看看是否可用，如下

![image-20221217131319740](.\README.assets\image-20221217131319740.png)

复制这串json转成实体类

![image-20221217131856244](.\README.assets\image-20221217131856244.png)

用到的插件是这个

![image-20221217132125875](.\README.assets\image-20221217132125875.png)

转换成功如下所示

![image-20221217132234953](.\README.assets\image-20221217132234953.png)

接着我们在代码中调用

在HomeFragment中编写如下代码，用来获取我们网络请求的数据

```kotlin
private val list = mutableListOf<CategoryX>()

override fun initData() {
    val url = "$BASE_URL?adult=true&first=1"
    val request: Request = Request.Builder()
        .url(url)
        .method("GET", null)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.d(TAG, "onFailure: ")
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onResponse(call: Call, response: Response) {
            if (response.code == 200) {
                val string = response.body?.string()
                val result = Gson().fromJson(string, Category::class.java)
                list.clear()
                if (result?.res?.category != null) {
                    result.res.category.forEach {
                        list.add(it)
                    }
                }
                Log.d(TAG, "onResponse: $list")
            }
        }
    })
}
```

```kotlin
const val BASE_URL = "http://service.picasso.adesk.com/v1/vertical/category"
```

接着运行一下，结果如图证明请求返回的数据保存成功，如果失败接口没问题的话八成是权限的问题

![image-20221217134215022](.\README.assets\image-20221217134215022.png)

### 分类不同类型的壁纸

其次我们需要一个左右滚动的viewpager2用来存放不同分类的壁纸的fragment

![image-20221217134520943](.\README.assets\image-20221217134520943.png)

然后创建一个CategoryFragment用来显示不同分类的壁纸

```kotlin
class CategoryFragment(private val id: String) : BaseFragment<FragmentCategoryBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCategoryBinding {
        return FragmentCategoryBinding.inflate(inflater, container, false)
    }

    override fun initData() {
    }

    override fun initEvent() {
    }

}
```

这里有个id的成员变量是作为调用分类接口的参数，接口如下，

http://service.picasso.adesk.com/v1/vertical/category/4e4d610cdf714d2966000003/vertical?limit=30&skip=180&adult=false&first=1&order=new

limit：返回的数据条数

skip：跳过的个数

adult：这个我试过没用的，可惜

现在我们写一个adapter把HomeFragment的数据传过来

```kotlin
class CategoryAdapter(fragmentActivity: FragmentActivity, private var list: MutableList<String>) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return CategoryFragment(list[position])
    }
}
```

接着在HomeFragment中，给viewpager设置该适配器，创建一个ids做为id列表，请求返回数据时更新该列表

```kotlin
mBinding.apply {
    categoryAdapter = CategoryAdapter(requireActivity(), ids)
    viewPager.adapter = categoryAdapter
}
```

```kotlin
private val ids = mutableListOf<String>()
private lateinit var categoryAdapter: CategoryAdapter
```

```kotlin
@SuppressLint("NotifyDataSetChanged")
override fun onResponse(call: Call, response: Response) {
    if (response.code == 200) {
        val string = response.body?.string()
        val result = Gson().fromJson(string, Category::class.java)
        list.clear()
        ids.clear()
        if (result?.res?.category != null) {
            result.res.category.forEach {
                list.add(it)
                ids.add(it.id)
            }
            Handler(Looper.getMainLooper()).post {
                categoryAdapter.notifyDataSetChanged()
            }
        }
        Log.d(TAG, "onResponse: $list")
    }
}
```

运行一下可以发现viewpager可以滑动了，可以数一下页数就是类型的种数

### 显示壁纸

重头戏了

现在我们调用一下刚才写过的接口去调试一下

![image-20221217164255770](.\README.assets\image-20221217164255770.png)

然后复制一下json数据按同样的方式转成实体类

![image-20221217164436658](.\README.assets\image-20221217164436658.png)

首先写一下fragment_category.xml中的代码，这里recyclerView配置上LayoutManager和SpanCount就不用在代码中写了

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.CategoryFragment">

    <com.scwang.smart.refresh.layout.SmartRefreshLayout
        android:id="@+id/refresh_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.scwang.smart.refresh.header.ClassicsHeader
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_margin="1dp"
            app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
            app:spanCount="3" />

        <com.scwang.smart.refresh.footer.ClassicsFooter
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </com.scwang.smart.refresh.layout.SmartRefreshLayout>
</FrameLayout>
```

接着创建一个item_picture作为上面recyclerview的item

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="1dp">

    <com.google.android.material.imageview.ShapeableImageView
        android:id="@+id/iv_pic"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:scaleType="centerCrop"
        app:shapeAppearance="@style/img_corner_20dp" />

</RelativeLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="img_corner_20dp">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">5dp</item>
    </style>
</resources>
```

用ShapeableImageView的话可以加个圆角好看点

接着写一个PictureAdapter适配器

```kotlin
class PicAdapter(
    private val context: Context,
    private val list: MutableList<Vertical>
) : RecyclerView.Adapter<VH<ItemPictureBinding>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<ItemPictureBinding> {
        val mBinding = ItemPictureBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(mBinding)
    }

    override fun onBindViewHolder(holder: VH<ItemPictureBinding>, position: Int) {
        holder.binding.apply {
            Glide.with(context)
                .load(list[holder.adapterPosition].thumb)
                .into(ivPic)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
```

最后在CategoryFragment中使用

```kotlin
private lateinit var picAdapter: PicAdapter
private val list = mutableListOf<Vertical>()
private val limit = 30  //每次加载限制的个数
private var page = 0    //当前页数
```

```kotlin
override fun initData() {
    picAdapter = PicAdapter(requireContext(), list)
    mBinding.apply {
        recyclerView.adapter = picAdapter
    }
    loadPic()
}
```

```kotlin
override fun initEvent() {

    mBinding.apply {

        refreshLayout.setOnRefreshListener {
            page = 0
            loadPic()
        }

        refreshLayout.setOnLoadMoreListener {
            page++
            loadPic()
        }
    }
}
```

```kotlin
@SuppressLint("NotifyDataSetChanged")
private fun loadPic() {
    val random = Random(Date().time).nextInt(200)
    Log.d(TAG, "loadPic: $random")
    val url = "$BASE_URL/$id/vertical?limit=$limit&skip=${random * limit}&adult=false&first=1&order=new"
    Log.d(TAG, "loadPic: $url")
    httpGet(url) { success, msg ->
        if (success) {
            val picture = Gson().fromJson(msg, Picture::class.java)
            val size = list.size
            picture?.res?.vertical?.let {
                if (page == 0) {
                    list.clear()
                    picAdapter.notifyDataSetChanged()
                }
                list.addAll(it)
            }
            picAdapter.notifyItemRangeInserted(size, limit)
        } else {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
        mBinding.refreshLayout.finishRefresh()
        mBinding.refreshLayout.finishLoadMore()
    }
}
```

此处加载的网络请求我封装了一个方法方便调用，可以单独放在一个工具类中使用

```kotlin
fun httpGet(url: String, callBack: (Boolean, String) -> Unit) {
    Thread {
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()
        Log.d(TAG, "httpGet: $url")
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    callBack(false, "error1")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.body != null) {
                    val json = response.body!!.string()
                    val headers = response.networkResponse!!.request.headers
                    try {
                        Handler(Looper.getMainLooper()).post {
                            callBack(true, json)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "httpGet: $e")
                        Handler(Looper.getMainLooper()).post {
                            callBack(false, "error3:$e")
                        }
                    }
                } else {
                    Log.e(TAG, "httpGet: error2")
                    Handler(Looper.getMainLooper()).post {
                        callBack(false, "error2")
                    }
                }
            }
        })
    }.start()
}
```

同样，HomeFragment中的网络请求也可以用这个简化一下

```kotlin
override fun initData() {

    loadCategory()

    mBinding.apply {
        categoryAdapter = CategoryAdapter(requireActivity(), ids)
        viewPager.adapter = categoryAdapter
    }
}

override fun initEvent() {
}

@SuppressLint("NotifyDataSetChanged")
private fun loadCategory() {
    val url = "$BASE_URL?adult=true&first=1"
    httpGet(url) { success, msg ->
        if (success) {
            val result = Gson().fromJson(msg, Category::class.java)
            list.clear()
            ids.clear()
            if (result?.res?.category != null) {
                result.res.category.forEach {
                    list.add(it)
                    ids.add(it.id)
                }
                categoryAdapter.notifyDataSetChanged()
            }
        } else {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }
}
```

现在运行一下，不出意外可以成功显示了![4a6b2c8d45e844946a77d2db2c16a86](.\README.assets\4a6b2c8d45e844946a77d2db2c16a86.jpg)

### 界面美化

首先把顶部的actionbar去掉，实在太丑了，改一下下面的样式为NoActionBar

![image-20221217164740822](.\README.assets\image-20221217164740822.png)

再次运行发现没有了，但是顶部状态栏还是有个很违和的颜色，这里使用别人写好的一个工具类去透明化

```kotlin
/**
 * 状态栏工具类
 */
object StatusBarUtil {
    /**
     * 修改状态栏为全透明
     *
     * @param activity
     */
    @TargetApi(19)
    fun transparencyBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity.window
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param activity
     * @param colorId
     */
    fun setStatusBarColor(activity: Activity, colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            //      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.statusBarColor = activity.resources.getColor(colorId)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
            transparencyBar(activity)
            val tintManager = SystemBarTintManager(activity)
            tintManager.setStatusBarTintEnabled(true)
            tintManager.setStatusBarTintResource(colorId)
        }
    }

    /**
     * 状态栏亮色模式，设置状态栏黑色文字、图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    fun StatusBarLightMode(activity: Activity): Int {
        var result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (MIUISetStatusBarLightMode(activity, true)) {
                result = 1
            } else if (FlymeSetStatusBarLightMode(activity.window, true)) {
                result = 2
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                result = 3
            }
        }
        return result
    }

    /**
     * 已知系统类型时，设置状态栏黑色文字、图标。
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @param type     1:MIUUI 2:Flyme 3:android6.0
     */
    fun StatusBarLightMode(activity: Activity, type: Int) {
        if (type == 1) {
            MIUISetStatusBarLightMode(activity, true)
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(activity.window, true)
        } else if (type == 3) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    /**
     * 状态栏暗色模式，清除MIUI、flyme或6.0以上版本状态栏黑色文字、图标
     */
    fun StatusBarDarkMode(activity: Activity, type: Int) {
        if (type == 1) {
            MIUISetStatusBarLightMode(activity, false)
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(activity.window, false)
        } else if (type == 3) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    fun FlymeSetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                    .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                value = if (dark) {
                    value or bit
                } else {
                    value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {
            }
        }
        return result
    }

    /**
     * 需要MIUIV6以上
     *
     * @param activity
     * @param dark     是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    fun MIUISetStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
        var result = false
        val window = activity.window
        if (window != null) {
            val clazz: Class<*> = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod(
                    "setExtraFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag) //状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag) //清除黑色字体
                }
                result = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (dark) {
                        activity.window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }
            } catch (e: Exception) {
            }
        }
        return result
    }
}
```

在MainActivity中调用透明化的方法

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.transparencyBar(this)
    }
}
```

再次运行好看多了，当然也可以直接设置全屏，然后再recyclerview上下滑动时监听调用全屏的方法，按照自己的需求去取舍

![456a4bdda7658a6f8cdc5861d80d3cc](.\README.assets\456a4bdda7658a6f8cdc5861d80d3cc.jpg)

图片加载成功的时候也可以加上动画，这里自己写了一个自定义view

```kotlin
class ScaleImage : ShapeableImageView {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var num = Int.MAX_VALUE
    private val count = 40f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (num <= count) {
            scaleX = num / count
            scaleY = num / count
            num++
        }
        invalidate()
    }

    fun startAnim() {
        num = 0
    }
    companion object{
        const val TAG = "ScaleImage"
    }
}
```

核心就是继承ShapeableImageView，重写onDraw方法，在绘制时每次判断num条件，去增加scaleX和scaleY，startAnim方法就是将num置为0便可以播放动画

将item_picture.xml 中的ShapeableImageView换成这个ScaleImage

![image-20221218092441520](.\README.assets\image-20221218092441520.png)

最后改一下PicAdapter适配器的bindviewholder方法：在glide加载图片里加上监听器加载成功时播放动画，加载失败时加了一个失败的图片

```kotlin
override fun onBindViewHolder(holder: VH<ItemPictureBinding>, position: Int) {
        holder.binding.apply {
            Glide.with(context)
                .load(list[holder.adapterPosition].thumb)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        ivPic.setImageResource(R.drawable.ic_baseline_broken_image_24)
                        return true
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        ivPic.startAnim()
                        ivPic.setImageDrawable(resource)
                        return true
                    }
                })
                .into(ivPic)
        }
    }
```

之后运行一下，ok，效果很好

<video src=".\README.assets\48d878e532cbdc008e1c42b83fb2036b.mp4"></video>

再之后给图片点击加上水波纹，很简单加一个foreground属性就可以了

```xml
<com.zrq.nicepicture.view.ScaleImage
    android:id="@+id/iv_pic"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:foreground="@drawable/pressed_background"
    android:scaleType="centerCrop"
    app:shapeAppearance="@style/img_corner_20dp" />
```

pressed_background.xml的代码，这里ripple的color字段中的颜色对应着水波纹的颜色，可以按自己喜欢去配置

```xml
<?xml version="1.0" encoding="utf-8"?>
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/white">
</ripple>
```

运行发现水波纹并没有生效，调试发现要给这个控件的点击加上监听才可以

PicAdapter加上如下代码，便可以了，这里的点击后面会回调出去的这里先给个空方法

```kotlin
ivPic.setOnClickListener {  }
```

运行，可以正常显示

<video src=".\README.assets\cb9178ca3dbc567ca548f89cba587a33.mp4"></video>

### 壁纸详情页

首先将item的点击事件响应回调出去

![image-20221218111422013](.\README.assets\image-20221218111422013.png)

然后创建详情页的fragment

```kotlin
class PicFragment : BaseFragment<FragmentPicBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPicBinding {
        return FragmentPicBinding.inflate(inflater, container, false)
    }

    override fun initData() {
    }

    override fun initEvent() {
    }

}
```

添加到navigation中，直接点上面的加号就行

![image-20221218122557940](.\README.assets\image-20221218122557940.png)

改写PicAdapter调用时的构造方法，当点击时保存数据到viewModel，然后跳转到刚创建的fragment

```kotlin
picAdapter = PicAdapter(requireContext(), list) { _, pos ->
    mainModel.list.clear()
    mainModel.list.addAll(list)
    mainModel.pos = pos
    Navigation.findNavController(requireActivity(), R.id.fragment_container)
        .navigate(R.id.picFragment)
}
```

此处使用了ViewModel进行数据的共享，如下

```kotlin
class MainModel : ViewModel() {
    val list = mutableListOf<Vertical>()
    var pos = 0
}
```

传递来的数据是一个列表，这里用viewpage2来接收，实现上下滑动翻页浏览壁纸的效果

![image-20221218122939517](.\README.assets\image-20221218122939517.png)

用viewpager2的话还是要编写一个adapter，直接复制之前的改一下列表的数据类型和子fragment就可以了

```kotlin
class PicItemAdapter(
    fragmentActivity: FragmentActivity,
    private var list: MutableList<String>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return PicItemFragment(list[position])
    }
}
```

这个PicItemFragment就是显示壁纸大图的容器，所以需要接收图片的地址，从上面的adapter中传递给PicItemFragment的构造器

这里还做了点击返回简化操作手法

```kotlin
class PicItemFragment(private val url: String) : BaseFragment<FragmentPicItemBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPicItemBinding {
        return FragmentPicItemBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        mBinding.apply {
            Glide.with(requireActivity())
                .load(url)
                .into(image)
        }
    }

    override fun initEvent() {
        mBinding.apply {
            image.setOnClickListener {
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .popBackStack()
            }
        }
    }

}
```

其布局中就一个imageview，这里的fragment其实就是一个item，所以命名为PicItemFragment，当作之前写recylerview的item去编写，步骤都是相似的

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.PicItemFragment">

    <com.google.android.material.imageview.ShapeableImageView
        android:id="@+id/image"
        android:foreground="@drawable/pressed_background"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />

</FrameLayout>
```

现在给这个PicItemAdapter实例化，就在之前创建的PicFragment中进行，数据集就从ViewModel中共享

```kotlin
class PicFragment : BaseFragment<FragmentPicBinding>() {
    override fun providedViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPicBinding {
        return FragmentPicBinding.inflate(inflater, container, false)
    }

    private lateinit var adapter: PicItemAdapter
    private val list = mutableListOf<String>()

    override fun initData() {
        adapter = PicItemAdapter(requireActivity(), list)
        mBinding.apply {
            viewPager.adapter = adapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initEvent() {
        list.clear()
        mainModel.list.forEach {
            list.add(it.img)
        }
        mBinding.viewPager.setCurrentItem(mainModel.pos, false)
        adapter.notifyDataSetChanged()
    }

}
```

这样一整个就串起来了，现在运行一下

<video src=".\README.assets\a765e5192d0daac1633e13d2eac8b942.mp4"></video>

### 详情页美化

现在逻辑是通了，之后来继续加点动效

首先就是跳转的动画，连一条线从homeFragment到picFragment，然后加两个字段enterAnim和popExitAnim

![image-20221218131903834](.\README.assets\image-20221218131903834.png)

这里在res文件夹下创建一个anim文件夹专门存放我们的动画文件

![image-20221218131947637](.\README.assets\image-20221218131947637.png)

anim_enter.xml如下

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <alpha
        android:duration="600"
        android:fromAlpha="0"
        android:interpolator="@android:anim/accelerate_decelerate_interpolator"
        android:toAlpha="1" />
</set>
```

anim_pop_exit.xml如下

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <alpha
        android:duration="600"
        android:fromAlpha="1"
        android:interpolator="@android:anim/accelerate_decelerate_interpolator"
        android:toAlpha="0" />
</set>
```

这两个动画就是设置透明度的，实现了淡入淡出的效果，很简单

然后改下之前的跳转标识，就不要直接用fragment的id去标识了，改成我们新连的action，现在运行跳转就有效果了

```kotlin
Navigation.findNavController(requireActivity(), R.id.fragment_container)
    .navigate(R.id.action_homeFragment_to_picFragment)
```

之后给图片的显示也加上动画

这里我们继续用自定义的ScaleView

![image-20221218140556885](.\README.assets\image-20221218140556885.png)

加载时我们也加上监听和之前adapter里的监听一样直接复制过来

![image-20221218140655208](.\README.assets\image-20221218140655208.png)

运行一下发现有从小到大的效果了

但是突然的从小变大，在这里显得太突兀了，优化方案是修改scale的变化过程，改成从大概0.5到1而不是0到1，持续时间也相应的缩短一下

方案有了，接下来就是实施，直接从ScaleView开始起手

我们来声明两个成员变量一个是开始的比例，一个是持续时间，修饰符不加并且用var，这样可以暴露给使用者动态配置

之后就是 简单的修改一下draw方法和startAnim方法，看着不太好懂，但可以取几个值模拟一下可以得到类似的规律，写出来大概就是这样

```kotlin
  private var num = Int.MAX_VALUE
    var startScale = 0f		//开始的比例
    var duration = 400		//持续时间

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val count = duration / 10
        if (num <= count) {
            scaleX = num * 1f / count
            scaleY = num * 1f / count
            num++
            invalidate()
        }
    }

    fun startAnim() {
        if (startScale > 1 || startScale < 0) startScale = 0f
        num = (duration * startScale / 10).toInt()
    }

}
```

接下来我们来适配静态配置

创建res/values/attrs.xml

![image-20221218141541096](.\README.assets\image-20221218141541096.png)

代码如下

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <attr name="startScale" format="float"/>
    <attr name="duration" format="integer"/>
    <declare-styleable name="ScaleImage">
        <attr name="startScale"/>
        <attr name="duration"/>
    </declare-styleable>
</resources>
```

接着在ScaleView的构造器中引用

```kotlin
constructor(context: Context?) : this(context, null)
constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
    if (context != null) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleImage)
        startScale = typedArray.getFloat(R.styleable.ScaleImage_startScale, 0f)
        duration = typedArray.getInteger(R.styleable.ScaleImage_duration, 400)
        typedArray.recycle()
    }
}
```

这样就可以在布局文件中静态配置

添加如下两个字段

![image-20221218141911999](.\README.assets\image-20221218141911999.png)

再次运行一下，现在就好多了

<video src=".\README.assets\186135f99742a8895a93207c78738cde.mp4"></video>

### 壁纸下载

首先加上下载按钮，默认是隐藏的

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.PicItemFragment">

    <com.zrq.nicepicture.view.ScaleImage
        android:id="@+id/image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:foreground="@drawable/pressed_background"
        android:scaleType="centerCrop"
        app:duration="200"
        app:startScale="0.4" />

    <RelativeLayout
        android:id="@+id/relative_layout"
        android:visibility="gone"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/transparent">

        <TextView
            android:id="@+id/btn_download"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:layout_centerHorizontal="true"
            android:layout_marginBottom="30dp"
            android:background="@drawable/shape_btn_download"
            android:foreground="@drawable/pressed_background"
            android:text="下载到本地"
            android:textColor="@color/black" />
    </RelativeLayout>
</RelativeLayout>
```

pressed_background.xml文件如下

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <corners android:radius="4dp" />
    <padding
        android:bottom="10dp"
        android:left="10dp"
        android:right="10dp"
        android:top="10dp" />
    <solid android:color="#63FFFFFF" />
    <stroke
        android:width="1dp"
        android:color="@color/grey" />
</shape>
```

在PicItemFragment方法里加上这几个事件监听

```kotlin
    override fun initEvent() {
        mBinding.apply {
            image.setOnClickListener {
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .popBackStack()
            }

            image.setOnLongClickListener {
                relativeLayout.visibility = View.VISIBLE
                true
            }

            relativeLayout.setOnClickListener {
                relativeLayout.visibility = View.GONE
            }

            btnDownload.setOnClickListener {
                btnDownload.text = "正在下载"
                saveImage(requireContext(), url) { success, msg ->
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            btnDownload.text = "已下载"
                            btnDownload.isEnabled = false
                            relativeLayout.visibility = View.GONE
                        } else {
                            btnDownload.text = "下载失败"
                        }
                    }
                }
            }
        }
    }
```

下面是将url图片保存到本地的方法，我一开始没有申请读写权限发现也可以保存，所以就把权限那段删了，如果运行发现没有权限的话可以在AndroidManifest.xml里加上，然后再动态申请一下

```kotlin
fun saveImage(ctx: Context, url: String, callBack: (Boolean, String) -> Unit) {
    var bitmap: Bitmap? = null
    Thread {
        var picUrl: URL? = null
        try {
            picUrl = URL(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (picUrl != null) {
            var inputStream: InputStream? = null
            try {
                val connect: HttpURLConnection = picUrl.openConnection() as HttpURLConnection
                connect.doInput = true
                connect.connect()
                inputStream = connect.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                callBack(false, "图片保存失败: error4")
            } finally {
                inputStream?.close()
            }
        }

        if (bitmap != null) {
            val sdDir = ctx.getExternalFilesDir(null)
            val filePath = sdDir!!.absolutePath + File.separator + "nice_pic"
            Log.d(TAG, "saveImage: $filePath")
            val appDir = File(filePath)
            Log.d(TAG, "saveImage: ${appDir.exists()}")
            if (!appDir.exists()) {
                val mkdir = appDir.mkdir()
                Log.d(TAG, "saveImage: $mkdir")
            }
            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
            val fileName = "LSP_$time.jpg"

            val typeFor = URLConnection.getFileNameMap().getContentTypeFor(fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val value = ContentValues()
                value.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                value.put(MediaStore.MediaColumns.MIME_TYPE, typeFor)
                value.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)

                val contentResolver = ctx.contentResolver
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
                if (uri == null) {
                    callBack(false, "图片保存失败：error1")
                    return@Thread
                }
                var os: OutputStream? = null
                try {
                    os = contentResolver.openOutputStream(uri)
                    val success = bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    if (success) {
                        callBack(true, "图片保存成功")
                        ctx.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                    } else {
                        callBack(false, "图片保存失败：error3")
                    }
                } catch (e: IOException) {
                    callBack(false, "图片保存失败：error2")
                } finally {
                    os?.flush()
                    os?.close()
                }

            } else {
                MediaScannerConnection.scanFile(ctx, arrayOf(filePath), arrayOf(typeFor)) { _, _ ->
                    callBack(true, "图片保存成功")
                }
            }
        }
    }.start()
}
```

现在是可以下载了，但是后来调试发现使用preview这个字段下载下来的壁纸像素更高，这里我们把之前的改一下

首先PicFragment的这俩个位置改一下

![image-20221219160702552](./README.assets/image-20221219160702552.png)

adapter这里也改一下

![image-20221219160741144](./README.assets/image-20221219160741144.png)

把PicItemFragment的构造器改一下

```kotlin
class PicItemFragment(private val pic: Vertical)
```

现在下载的图片就是preview

![image-20221219160848376](./README.assets/image-20221219160848376.png)

运行如下

<video src="./README.assets/62a12a3b52b45ca93fd4c6ffe93ba86b.mp4"></video>

接下来在首页小图长按我们也加上下载功能

首先adapter里加上长按回调

![image-20221219163044973](./README.assets/image-20221219163044973.png)

在调用位置实现该方法

主要就是先获取到点击item的位置，用来定位下载按钮

```kotlin
picAdapter = PicAdapter(requireContext(), list,
    { _, pos ->
        mainModel.list.clear()
        mainModel.list.addAll(list)
        mainModel.pos = pos
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
            .navigate(R.id.action_homeFragment_to_picFragment)
    },
    { view, position ->
        val location = IntArray(2)
        view.getLocationInWindow(location)
        location[0] += view.width / 2
        location[1] += view.height / 2
        if (downloadBtn != null) {
            mBinding.root.removeView(downloadBtn)
        }
        downloadBtn = newBtn(location[0], location[1])
        downloadBtn?.let { btn ->
            btn.setOnClickListener {
                saveImage(requireContext(), list[position].preview) { success, msg ->
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            btn.text = "已下载"
                            btn.isEnabled = false
                        }
                    }
                }
            }
        }
    }
)
```

其中现在按钮如下，这里宽高可以自己配置，drawable我直接用的之前的

```kotlin
private var downloadBtn: Button? = null
```

```kotlin
@SuppressLint("UseCompatLoadingForDrawables")
private fun newBtn(x: Int, y: Int): Button {
    val btn = MaterialButton(requireContext())
    val width = 240
    val height = 120
    btn.layoutParams = RelativeLayout.LayoutParams(width, height)
    btn.cornerRadius = 20
    btn.background = resources.getDrawable(R.drawable.shape_btn_download)
    btn.text = "下载"
    btn.x = x.toFloat() - width / 2
    btn.y = y.toFloat() - height / 2
    mBinding.root.addView(btn)
    return btn
}
```

当然在滑动时销毁该button

```kotlin
recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        mBinding.root.removeView(downloadBtn)
    }
})
```

现在运行一下

<video src="./README.assets/e2b7371e4b3348f90e827c52c780c22e.mp4"></video>

可以，现在大功告成了，可以愉快的看涩图了

项目地址：[CanCanWorld/NicePic (github.com)](https://github.com/CanCanWorld/NicePic)











