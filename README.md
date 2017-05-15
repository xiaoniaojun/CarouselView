# CarouselView
A Carousel View use only one view

## How to use

1. set carousel images（设置轮播图片）

```java
setCarouselImages(List<Bitmap> carouseImages);
```

2. set interval（设置轮播时间间隔）
```java
//must be used before setCarouselImages()
// 必须在setCarouselImages()之前被调用
setInterval(millisecond);
```

3. on click call back （设置click事件回调）

```java

// implement this interface in activity （在Activity中实现回调接口）
public interface OnCarouselViewClickListener {
        public void onCarouselViewClick(int tag, int position);
    }

public class MainActivity extend ActivityCompat implement OnCarouselViewClickListener {

...

@Override
public void onCarouselViewClick(int tag, int position) {
  // do Something
}

}
    
```
