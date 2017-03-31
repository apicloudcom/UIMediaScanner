# **概述**

手机相册媒体资源读取模块（内含iOS和android）

APICloud 的 UIMediaScanner 模块是一个媒体浏览器。在 Android 平台上，它能扫描当前设备上所有的图片、视频媒体资源，并以 push 出一个 window 的形式展示出来。当然，APICloud 平台的开发者也可以用 scan 接口扫描资源，然后根据扫描到的资源完全自定义的展示页面。注意在 iOS 平台上由于系统权限限制，本模块只能扫描系统相册内的媒体资源。由于本模块 UI 布局界面为固定模式，不能满足日益增长的广大开发者对侧滑列表模块样式的需求。因此，广大原生模块开发者，可以参考此模块的开发方式、接口定义等开发规范，或者基于此模块开发出更多符合产品设计的新 UI 布局的模块，希望此模块能起到抛砖引玉的作用。

# **模块接口文档**

<p style="color: #ccc; margin-bottom: 30px;">来自于：官方</p>

<div class="outline">

[open](#open)
[scan](#scan)
[fetch](#fetch)
[transPath](#transPath)
[getVideoDuration](#getVideoDuration)

</div>

# **模块概述**

UIMediaScanner 是一个本地媒体资源扫描器，在 Android 平台上可扫描整个设备的资源，iOS 仅扫描相册内的资源。开发者可通过 open 内的 type 参数控制要扫描的资源类型。

本模块封装了两种方案。

方案一：

通过 open 接口打开一个自带 UI 界面的媒体资源浏览页面，相当于打开了一个 window 。开发者可通过相应参数配置部分样式，但不可改变其界面布局。当用户选择指定媒体资源，可返回绝对路径给前端开发者。前端开发者可通过此绝对路径读取指定媒体资源文件。**注意：在 iOS 平台上需要先调用 transPath 接口将路径转换之后才能读取目标资源媒体文件。**

方案二：

通过 scan 接口扫描指定数量的媒体资源文件，本接口是纯功能类接口，不带界面。开发者可根据此接口扫描到的文件自行开发展示页面，极大的提高了自定义性。注意展示页面要做成赖加载模式，以免占用内存过高导致 app 假死。懒加载模式可通过 fetch 接口实现持续向下加载更多功能。

以上两种方案详细功能，请参考接口说明。

**UIMediaScanner 模块是 mediaScanner 模块的优化升级版。**

![图片说明](http://docs.apicloud.com/img/docImage/UIMediaScanner.jpg)

**注意：使用本模块前需在云编译页面添加勾选访问相册权限，否则会有崩溃闪退现象**

## 模块接口

***本模块源码开源地址为：https://github.com/apicloudcom/UIMediaScanner***


## [实例widget下载地址](https://github.com/XM-Right/UIMediaScanner-Example/archive/master.zip)

<div id="open"></div>

# **open**

打开多媒体资源选择器，打开后会全屏显示

open({params}, callback(ret))

## params

type：

- 类型：字符串
- 描述：返回的资源种类；默认：'all'
- 取值范围：
    * all（图片和视频）
    * picture（图片）
    * video（视频）

column：

- 类型：数字
- 描述：（可选项）图片显示的列数，须大于1
- 默认值：4

classify：

- 类型：布尔
- 描述：（可选项）是否将图片分类显示（为 true 时，会首先跳转到相册分类列表页面）
- 默认值：false

max：

- 类型：数字
- 描述：（可选项）最多选择几张图片
- 默认值：5

sort：

- 类型：JSON 对象
- 描述：（可选项）图片排序方式
- 内部字段：

```js
{
    key: 'time',    //（可选项）字符串类型；排序方式，默认：'time'
                    //取值范围：
                    //time（按图片创建时间排序）
    order: 'desc'   //（可选项）字符串类型；默认：'desc'
                    //取值范围：
                    //asc（旧->新）
                    //desc（新->旧）
}
```

texts：

- 类型：JSON 对象
- 描述：（可选项）模块各部分的文字内容
- 内部字段：

```js
{
    stateText: '已选择*项',         //（可选项）字符串类型；状态文字内容；*号会被替换为已选择个数；默认：'已选择*项'
    cancelText: '取消',             //（可选项）字符串类型；取消按钮文字内容；默认：'取消'
    finishText: '完成'              //（可选项）字符串类型；完成按钮文字内容；默认：'完成'
}
```

styles：

- 类型：JSON 对象
- 描述：（可选项）模块各部分的样式
- 内部字段：

```js
{
    bg: '#FFFFFF',                      //（可选项）字符串类型；资源选择器背景，支持 rgb，rgba，#；默认：'#FFFFFF'
    mark: {                             //（可选项）JSON对象；选中图标的样式
        icon: '',                       //（可选项）字符串类型；图标路径（本地路径，支持fs://、widget://）；默认：对勾图标
        position: 'bottom_left',        //（可选项）字符串类型；图标的位置，默认：'bottom_left'
                                        // 取值范围：
                                        // top_left（左上角）
                                        // bottom_left（左下角）
                                        // top_right（右上角）
                                        // bottom_right（右下角）
        size: 20                        //（可选项）数字类型；图标的大小；默认：显示的缩略图的宽度的三分之一
    },
    nav: {                              //（可选项）JSON对象；导航栏样式
        bg: '#eee',                     //（可选项）字符串类型；导航栏背景，支持 rgb，rgba，#；默认：'#eee'
        stateColor: '#000',             //（可选项）字符串类型；状态文字颜色，支持 rgb，rgba，#；默认：'#000'
        stateSize: 18,                  //（可选项）数字类型；状态文字大小，默认：18
        cancelBg: 'rgba(0,0,0,0)',      //（可选项）字符串类型；取消按钮背景，支持 rgb，rgba，#；默认：'rgba(0,0,0,0)'
        cancelColor: '#000',            //（可选项）字符串类型；取消按钮的文字颜色；支持 rgb，rgba，#；默认：'#000'
        cancelSize: 18,                 //（可选项）数字类型；取消按钮的文字大小；默认：18
        finishBg: 'rgba(0,0,0,0)',      //（可选项）字符串类型；完成按钮的背景，支持 rgb，rgba，#；默认：'rgba(0,0,0,0)'
        finishColor: '#000',            //（可选项）字符串类型；完成按钮的文字颜色，支持 rgb，rgba，#；默认：'#000'
        finishSize: 18                  //（可选项）数字类型；完成按钮的文字大小；默认：18
    }
}
```

scrollToBottom：

- 类型：JSON 对象
- 默认值：见内部字段
- 描述：（可选项）打开媒体资源界面后间隔一段时间开始自动滚动到底部设置
- 内部字段：

```js
{
   intervalTime:       //（可选项）数字类型；打开媒体资源界面后间隔的时间开始自动滚动到底部，单位秒（s），小于零的数表示不滚动到底部；默认：-1
   anim:               //（可选项）布尔类型；滚动时是否添加动画，android 平台不支持动画效果；默认true
}
```

exchange：

- 类型：布尔
- 默认值：false
- 描述：是否交换‘确定’和‘取消’按钮的位置（默认‘取消’按钮在右边，‘确定’按钮在左边）

rotation：

- 类型：布尔
- 默认值：false
- 描述：屏幕是否旋转（横屏），为 true 时可以横竖屏旋转，false 时禁止横屏


showPreview：

- 类型：布尔
- 默认值：false
- 描述：是否支持返回预览事件
- 注意：

```js
{
   当本参数为 true 时，styles-》mark-》position 参数恒为 top_right，切此时模块会为 mark 提供一个未选中时的图标。当用户点击缩略图右上角时，触发选中/取消事件。当用户点击已选中的缩略图其它区域时，触发预览事件，并且模块会把当前所选中的所有图片信息回调给前端。
}
```

showBrowser：

- 类型：布尔
- 默认值：false
- 描述：是否支持打开已选图片预览效果
- 注意：

```js
{
   当本参数为 true 时，styles-》mark-》position 参数恒为 top_right，切此时模块会为 mark 提供一个未选中时的图标。当用户点击缩略图右上角时，触发选中/取消事件。当用户点击已选中的缩略图其它区域时，触发已选图片预览事件，并且模块自动跳转到图片预览界面。预览界面完成按钮事件同本接口回调函数里的confirm
}
```

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    eventType: cancel, // 字符串类型 按钮点击事件 取值范围
		       // confirm 点击确定按钮
		       // cancel 点击取消按钮
		       // preview 用户点击缩略图触发的预览事件，仅当 showPreview 为 true 时有效
    list: [{                         //数组类型；返回选定的资源信息数组
        path: '',                    //字符串类型；资源路径，返回资源在本地的绝对路径，注意：iOS 平台上需要用 transPath 接口转换之后才可读取原图
        thumbPath: '',               //字符串类型；缩略图路径，返回资源缩略图在本地的绝对路径
        suffix: '',                  //字符串类型；文件后缀名，如：png，jpg, mp4
        size: 1048576,               //数字类型；资源大小，单位（Bytes）
        time: '2015-06-29 15:49',    //字符串类型；资源创建时间，格式：yyyy-MM-dd HH:mm:ss
        duration: -1                 //数字类型；若资源为视频时，则返回其视频时长，若为图片时本次参数为-1，暂仅支持 iOS 平台                          
    }]
}
```

## 示例代码

```js
var UIMediaScanner = api.require('UIMediaScanner');
UIMediaScanner.open({
	type: 'picture',
	column: 4,
	classify: true,
	max: 4,
	sort: {
		key: 'time',
		order: 'desc'
	},
	texts: {
		stateText: '已选择*项',
		cancelText: '取消',
		finishText: '完成'
	},
	styles: {
		bg: '#fff',
		mark: {
			icon: '',
			position: 'bottom_left',
			size: 20
		},
		nav: {
			bg: '#eee',
			stateColor: '#000',
			stateSize: 18,
			cancelBg: 'rgba(0,0,0,0)',
			cancelColor: '#000',
			cancelSize: 18,
			finishBg: 'rgba(0,0,0,0)',
			finishColor: '#000',
			finishSize: 18
		}
	},
	scrollToBottom: {
		intervalTime: 3,
		anim: true
	},
	exchange: true,
	rotation: true
}, function(ret) {
	if (ret) {
		alert(JSON.stringify(ret));
	}
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="scan"></div>

# **scan**

扫描系统多媒体资源，可以通过 Web 代码自定义多选界面。**注意：页面展示的图片建议使用缩略图，一次显示的图片不宜过多（1至2屏）**

scan({params}, callback(ret, err))

## params

type：

- 类型：字符串
- 描述：返回的资源种类；默认：'all'
- 取值范围：
    * all（图片和视频）
    * picture（图片）
    * video（视频）

count：

- 类型：数字
- 描述：（可选项）每次返回的资源数量；
- 默认：全部资源数量

sort：

- 类型：JSON 对象
- 描述：（可选项）图片排序方式
- 内部字段：

```js
{
    key: 'time',    //（可选项）字符串类型；排序方式；默认：'time'
                    //取值范围：
                    //time（按图片创建时间排序）
    order: 'desc'   //（可选项）字符串类型；排列顺序；默认：'desc'
                    //取值范围：
                    //asc（旧->新）
                    //desc（新->旧）
}
```

thumbnail：

- 类型：JSON 对象
- 描述：（可选项）返回的缩略图配置，**建议本图片不要设置过大** 若已有缩略图，则使用已有的缩略图。若要重新生成缩略图，可先调用清除缓存接口api.clearCache()。
- 内部字段：

```js
{
      w: 100,     //（可选项）数字类型；返回的缩略图的宽；默认：100
      h: 100      //（可选项）数字类型；返回的缩略图的宽；默认：100
}
```

showGroup：

- 类型：布尔类型
- 描述：（可选项）是否返回图片所在分组名，本参数对 android 平台无效
- 默认：false（在 android 平台上本参数始终为 true）
- 注意：

```js
{
	由于系统平台差异，iOS 上和 android 上相册分组策略有所不同。

	iOS 上系统相册分组策略如下：
	相机胶卷（All组）:  a,b,c,d,e,f,g
	A组：a
	B组：b,c
	C组：f,g

	android 上系统相册分组策略如下：
	A组：a
	B组：b,c
	C组：d,e,f,g

	因此，若要在 android 平台上显示 All 组，开发者需自行组合。
}
```

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    total: 100,                      //数字类型；媒体资源总数
	list: [{                         //数组类型；返回指定的资源信息数组
		  path: '',                    //字符串类型；资源路径，返回资源在本地的绝对路径。注意：在 iOS 平台上需要先调用 transPath 接口将路径转换之后才能读取目标资源媒体文件
        thumbPath: '',               //字符串类型；缩略图路径，返回资源缩略图在本地的绝对路径
        suffix: '',                  //字符串类型；文件后缀名，如：png，jpg, mp4
        size: 1048576,               //数字类型；资源大小，单位（Bytes）
        time: '2015-06-29 15:49:22,  //字符串类型；资源创建时间，格式：yyyy-MM-dd HH:mm:ss
        groupName: ''                //字符串类型；所在相册分组的组名，在 iOS 平台上仅当 showGroup 为 true 时本参数有值
	}]
}
```

## 示例代码

```js
var UIMediaScanner = api.require('UIMediaScanner');
UIMediaScanner.scan({
	type: 'all',
	count: 10,
	sort: {
		key: 'time',
		order: 'desc'
	},
	thumbnail: {
		w: 100,
		h: 100
	}
}, function(ret) {
	if (ret) {
		alert(JSON.stringify(ret));
	}
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="fetch"></div>

# **fetch**

获取指定数量的多媒体资源，没有更多资源则返回空数组，**必须配合 scan 接口的 count 参数一起使用**。

fetch(callback(ret))

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
    list: [{                         //数组类型；返回指定的资源信息数组
        path: '',                    //字符串类型；资源路径，返回资源在本地的绝对路径。注意：在 iOS 平台上需要先调用 transPath 接口将路径转换之后才能读取目标资源媒体文件
        thumbPath: '',               //字符串类型；缩略图路径，返回资源缩略图在本地的绝对路径
        suffix: '',                  //字符串类型；文件后缀名，如：png，jpg, mp4
        size: 1048576,               //数字类型；资源大小，单位（Bytes）
        time: '2015-06-29 15:49',    //字符串类型；资源创建时间，格式：yyyy-MM-dd HH:mm:ss
        groupName: ''                //字符串类型；所在相册分组的组名，在 iOS 平台上仅当 scan 接口内 showGroup 为 true 时本参数有值
    }]
}
```

## 示例代码

```js
var UIMediaScanner = api.require('UIMediaScanner');
UIMediaScanner.fetch(function(ret, err) {
	if (ret) {
		alert(JSON.stringify(ret));
	} else {
		alert(JSON.stringify(err));
	}
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本

<div id="transPath"></div>

# **transPath**

将相册图片地址转换为可以直接使用的本地路径地址（临时文件夹的绝对路径），**相册图片会被拷贝到临时文件夹，调用 api.clearCache 接口可清除该临时图片文件**

transPath({params}, callback(ret))

## params

path：

- 类型：字符串
- 描述：要转换的图片路径（在相册库的绝对路径）

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
   path: ''     //字符串类型；相册内图片被拷贝到临时文件夹，返回已拷贝图片的绝对路径
}
```

## 示例代码

```js
var UIMediaScanner = api.require('UIMediaScanner');
UIMediaScanner.transPath({
	path: ''
}, function(ret, err) {
	if (ret) {
		alert(JSON.stringify(ret));
	} else {
		alert(JSON.stringify(err));
	}
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本


<div id="getVideoDuration"></div>

# **getVideoDuration**

getVideoDuration({params}, callback(ret))

## params

path：

- 类型：字符串
- 描述：视频资源路径（在相册库的绝对路径,另外支持 fs:// widget://路径）

## callback(ret)

ret：

- 类型：JSON 对象
- 内部字段：

```js
{
   duration: 60    //数字类型；视频时长
}
```

## 示例代码

```js
var UIMediaScanner = api.require('UIMediaScanner');
UIMediaScanner.getVideoDuration({
	path: ''
}, function(ret, err) {
	if (ret) {
		alert(JSON.stringify(ret));
	} else {
		alert(JSON.stringify(err));
	}
});
```

## 可用性

iOS系统，Android系统

可提供的1.0.0及更高版本