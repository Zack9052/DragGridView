# DragGridView-GridView-
DragGridView for Android（Android可拖动GridView，在ScrollView中也可以使用）

welcome to star me or follow me on Github 

**Github: **[xiezilailai的github][1]

You alse can follow my weibo and communicate with me!

**Weibo：**[xiezilailai的微博][2]

**效果**

![这里写图片描述](http://img.blog.csdn.net/20160831204800559)


**如何使用：**

在项目中你可以看到一个 DragBaseAdapter类，你可以直接继承这个类，就像项目中的MyNewAdapter一样
```
public class MyNewAdapter extends DragBaseAdapter<String> {

    public MyNewAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_item_grid;
    }

    @Override
    protected void initView(ViewHolder holder) {
        holder.addView(R.id.item_text);
    }

    @Override
    protected void setViewValue(ViewHolder holder, int position) {
        ((TextView)holder.getView(R.id.item_text)).setText(getItem(position));
    }


}

```
正如你看到的一样，你只需要将你将layout和之中的view传进去，然后在setViewValue中指定特定position下各个view如何绑定数据即可。但是这个抽象类只是能普遍通用一些不是很复杂的adapter，如果你的adapter很复杂或者是有一些特殊的操作再或者你不想继承自BaseAdapter，你完全可以自己重新写一个Adapter，**但是你必须在你的adapter中实现moveItem方法**，这个并不复杂，你只需要将DragBaseAdapter中的代码按照你的需要改一下就行（DragBaseAdapter中用的是泛型）




  [1]: https://github.com/xiezilailai
  [2]: http://weibo.com/xiezilailai
