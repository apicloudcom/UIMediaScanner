/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import "UZUIMediaScanner.h"
#import "UZModule.h"
#import "UZUIAssetsViewController.h"
#import "NSDictionaryUtils.h"
#import "UZAppUtils.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import "UZUIAssetsGroupViewController.h"
#import "UZUIAssetNavigationController.h"

@interface UZUIMediaScanner ()
<AssetsViewCallBack> {
    NSInteger cbOpenId, cbScannerId, fecthCbId;
    NSMutableDictionary *_scanDict;
    NSMutableArray *_picAry, *_vidAry, *_allAry, *_cBAll;
    NSInteger capicity;          //每页数据容量
    BOOL preparedData;           //所需数据是否准备完
}

@property (nonatomic, strong) NSMutableArray *assets;
@property (nonatomic, strong) NSMutableDictionary *scanDict;
@property (nonatomic, strong) NSOperationQueue *transPathQueue;

@end

@implementation UZUIMediaScanner

@synthesize assets = _assets;
@synthesize scanDict = _scanDict;
static int fetchPosition = 0;

#pragma mark -
#pragma mark  lifeCycle
#pragma mark -

- (void)dispose {
    if (self.assets) {
        [self.assets removeAllObjects];
        self.assets = nil;
    }
    if (_scanDict) {
        self.scanDict = nil;
    }
    if (self.transPathQueue) {
        [self.transPathQueue cancelAllOperations];
        self.transPathQueue = nil;
    }
}

- (NSOperationQueue *)transPathQueue {
    if (!_transPathQueue) {
        _transPathQueue = [[NSOperationQueue alloc]init];
        NSInteger maxOperation = [[NSProcessInfo processInfo]activeProcessorCount];
        [_transPathQueue setMaxConcurrentOperationCount:maxOperation];
    }
    return _transPathQueue;
}

#pragma mark -
#pragma mark  interface
#pragma mark -

- (void)open:(NSDictionary *)paramsDict_ {
    cbOpenId = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    [self classify:paramsDict_];
}

- (void)scan:(NSDictionary *)paramsDict_ {
    preparedData = NO;
    fecthCbId = -1;
    fetchPosition = 0;
    self.assets = [NSMutableArray arrayWithCapacity:1];
    _scanDict = [NSMutableDictionary dictionaryWithDictionary:paramsDict_];
    cbScannerId = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    [NSThread detachNewThreadSelector:@selector(loadDataSource:) toTarget:self withObject:nil];
}

- (void)fetch:(NSDictionary *)paramsDict_ {
    fecthCbId = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    if (preparedData) {
        [self fetchCallBack];
    } else {
        //创建一个线程，用来取指定页码和数量的照片数据
        NSThread *thread2 = [[NSThread alloc] initWithTarget:self selector:@selector(getSpecifiedData:) object:nil];
        //启动线程
        [thread2 start];
    }
}

- (void)transPath:(NSDictionary *)paramsDict_ {
    NSInteger transCbId = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSString *path = [paramsDict_ stringValueForKey:@"path" defaultValue:nil];
    if (path.length == 0) {
        return;
    }
    //创建NSBlockOperation 来执行每一次转换，图片复制等耗时操作
    NSBlockOperation *operation = [NSBlockOperation blockOperationWithBlock:^{
        NSURL *url = [[NSURL alloc] initWithString:path];
        ALAssetsLibrary *assetslibrary = [[ALAssetsLibrary alloc] init];
        __block UIImage *imageAss = nil;
        ALAssetsLibraryAssetForURLResultBlock resultblock = ^(ALAsset *myasset) {
            imageAss = [UIImage imageWithCGImage:myasset.defaultRepresentation.fullResolutionImage
                                           scale:myasset.defaultRepresentation.scale
                                     orientation:(UIImageOrientation)myasset.defaultRepresentation.orientation];
            imageAss = [self imageCorrectedForCaptureOrientation:imageAss UIImageOrientation:imageAss.imageOrientation];
            [self save:imageAss imagePath:path cbId:transCbId];
        };
        ALAssetsLibraryAccessFailureBlock failureblock  = ^(NSError *myerror) {
            NSLog(@"Turbo_UIMediascanner_cant get image - %@",[myerror localizedDescription]);
        };
        [assetslibrary assetForURL:url resultBlock:resultblock failureBlock:failureblock];
    }];
    [self.transPathQueue addOperation:operation];
}

#pragma mark -
#pragma mark  AssetsViewCallBackDelegate
#pragma mark -

- (void)callBack:(NSDictionary *)listDict {
    if (cbOpenId != -1) {
        NSDictionary *sendDict = [[NSDictionary alloc]initWithDictionary:listDict];
        [self sendResultEventWithCallbackId:cbOpenId dataDict:sendDict errDict:nil doDelete:NO];
    }
}

- (NSString *)getPath:(NSString *)path {
    return [self getPathWithUZSchemeURL:path];
}

#pragma mark -
#pragma mark  Utility
#pragma mark -

- (void)loadDataSource:(NSString *)path {//加载本地相册里的数据
    @autoreleasepool {
        __block  NSMutableArray *all = [[NSMutableArray alloc] init];
        __block  NSMutableArray *pic = [[NSMutableArray alloc] init];
        __block  NSMutableArray *vid = [[NSMutableArray alloc] init];
        ALAssetsLibrary *assetsLibrary;
        assetsLibrary = [self.class defaultAssetsLibrary];
        [assetsLibrary enumerateGroupsWithTypes:ALAssetsGroupSavedPhotos usingBlock:^(ALAssetsGroup *group, BOOL *stop) {
            [group enumerateAssetsUsingBlock:^(ALAsset *result, NSUInteger index, BOOL *stop) {
                NSString *type = [result valueForProperty:ALAssetPropertyType];
                ALAsset *result2 = result;
                if (result != nil) {
                    [all addObject:result2];
                    if ([type isEqual:ALAssetTypePhoto]) {
                        [pic addObject:result2];
                    }
                    if ([type isEqual:ALAssetTypeVideo]) {
                        [vid addObject:result2];
                    }
                } else {
                    NSString *type = [_scanDict stringValueForKey:@"type" defaultValue:@"all"];
                    NSArray *arr;
                    if ([type isEqualToString:@"all"]) {
                        arr = all;
                    } else if ([type isEqualToString:@"picture"]) {
                        arr = pic;
                    } else if ([type isEqualToString:@"video"]) {
                        arr = vid;
                    }
                    _allAry = [[NSMutableArray alloc] initWithArray:all];
                    _picAry = [[NSMutableArray alloc] initWithArray:pic];
                    _vidAry = [[NSMutableArray alloc] initWithArray:vid];
                    for (ALAsset *asset in arr) {
                        [self.assets addObject:asset];
                    }
                    [self sortDataSource:nil];
                }
            }];
        } failureBlock:^(NSError *error) {
            NSLog(@"Turbo___mediaScanner__error:%@",error);
        }];
    }
}

//排序
- (void)sortDataSource: (id)obj {
    capicity = [self.scanDict floatValueForKey:@"count" defaultValue:(int)self.assets.count];
    NSDictionary *sortInfo = [self.scanDict dictValueForKey:@"sort" defaultValue:@{}];
    NSString *keyStr = [sortInfo stringValueForKey:@"key" defaultValue:@"time"];
    NSString *orderStr = [sortInfo stringValueForKey:@"order" defaultValue:@"desc"];
    if (keyStr) {
        if ([keyStr isEqualToString:@"time"]) {
            NSArray *sortedArray = [self.assets sortedArrayUsingComparator:^(ALAsset *asset1,ALAsset *asset2) {
                NSDate *pictureDate1 = [asset1 valueForProperty:ALAssetPropertyDate];
                NSDate *pictureDate2 = [asset2 valueForProperty:ALAssetPropertyDate];
                NSDate *date = [pictureDate1 earlierDate:pictureDate2];
                if (pictureDate1 == date && [orderStr isEqualToString:@"asc"]) {//升序
                    return NSOrderedAscending;
                } else {
                    return NSOrderedDescending;
                }
            }];
            [self.assets removeAllObjects];
            for (ALAsset *asset in sortedArray) {
                [self.assets addObject:asset];
            }
        } else {
            NSArray *sortedArray;
            if ([orderStr isEqualToString:@"desc"]) {
                sortedArray = [self.assets sortedArrayUsingComparator:^( ALAsset *asset1,ALAsset *asset2) {
                    if (asset1.defaultRepresentation.size > asset2.defaultRepresentation.size) {
                        return NSOrderedDescending;
                    } else {
                        return NSOrderedAscending;
                    }
                }];
            } else {
                sortedArray = [self.assets sortedArrayUsingComparator:^( ALAsset *asset1,ALAsset *asset2) {
                    if (asset1.defaultRepresentation.size < asset2.defaultRepresentation.size) {
                        return NSOrderedAscending;
                    } else {
                        return NSOrderedDescending;
                    }
                }];
            }
        }
    }
    //创建一个线程，用来取指定页码和数量的照片数据
    NSThread *specifiedData = [[NSThread alloc] initWithTarget:self selector:@selector(getSpecifiedData:) object:nil];
    [specifiedData start];
    //创建一个线程,用来准备资源信息（将缩略图拷贝到临时文件夹下）
    NSThread *prepareData = [[NSThread alloc] initWithTarget:self selector:@selector(prepareScannData:) object:nil];
    [prepareData start];
}

//获取指定页码和数量的照片数据，并回调给scann或fetch
- (void)getSpecifiedData:(id)obj {
    NSMutableArray *callBackArr = [NSMutableArray array];
    for (int i = fetchPosition; i < capicity+fetchPosition; i++) {
        if (i >= _assets.count) {
            break;
        }
        ALAsset *result = self.assets[i];
        //资源类型  PNG MOV JPG
        NSString *filename = result.defaultRepresentation.filename;
        NSString *mimeType = [[[filename componentsSeparatedByString:@"."] lastObject] lowercaseString];
        //获取创建时间信息
        NSDate *pictureDate = [result valueForProperty:ALAssetPropertyDate];
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        NSString *strDate = [dateFormatter stringFromDate:pictureDate];
        //资源路径URL
        NSURL *assetUrls = result.defaultRepresentation.url;
        //资源缩略图URL
        UIImage *image = [UIImage imageWithCGImage:result.thumbnail];
        NSString *imagePath = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        NSFileManager *fileManager = [NSFileManager defaultManager];
        NSString *filePath = [imagePath  stringByAppendingPathComponent:@"mediaScanner"];
        if (![fileManager fileExistsAtPath:imagePath]) {
            [fileManager createDirectoryAtPath:imagePath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        //创建路径
        [fileManager createDirectoryAtPath:filePath withIntermediateDirectories:YES attributes:nil error:nil];
        NSString *type  = [result valueForProperty:ALAssetPropertyType];
        NSString *imgPath;
        if ([type isEqualToString:ALAssetTypeVideo]) {
            NSRange ran = [filename rangeOfString:@"."];
            filename= [filename substringWithRange:NSMakeRange(0, ran.location+1)];
            imgPath = [filePath stringByAppendingString:[NSString stringWithFormat:@"/%@%@",filename,@"png"]];
        } else {
            imgPath = [filePath stringByAppendingString:[NSString stringWithFormat:@"/%@",filename]];
        }
        UIImage *img = [UIImage imageWithContentsOfFile:imgPath];
        if (!img) {
            NSData *data;
            if (UIImagePNGRepresentation(image) == nil) {
                data = UIImageJPEGRepresentation(image, 1);
            } else {
                data = UIImagePNGRepresentation(image);
            }
            //创建文件
            if (data) {
                if ([type isEqualToString:ALAssetTypeVideo]) {
                    NSRange ran = [filename rangeOfString:@"."];
                    filename= [filename substringWithRange:NSMakeRange(0, ran.location+1)];
                    [fileManager createFileAtPath:imgPath contents:data attributes:nil];
                } else {
                    [fileManager createFileAtPath:imgPath contents:data attributes:nil];
                }
            }
        }
        //根据路径得到URL
        NSURL *thumbUrl =  [NSURL URLWithString:imgPath];
        //资源-照片大小   单位：Bytes
        NSNumber *size = [NSNumber numberWithLongLong:result.defaultRepresentation.size];
        if (result && assetUrls && size && result.thumbnail) {
            NSString *temp = [assetUrls absoluteString];
            NSString *temp1 = [thumbUrl absoluteString];
            NSMutableDictionary * ttt =[NSMutableDictionary dictionaryWithCapacity:3];
            [ttt setObject:temp forKey:@"path"];
            [ttt setObject:temp1 forKey:@"thumbPath"];
            [ttt setObject:mimeType forKey:@"suffix"];
            [ttt setObject:size forKey:@"size"];
            [ttt setObject:strDate forKey:@"time"];
            [callBackArr addObject:ttt];
        }
    }
    fetchPosition += capicity;
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
    [sendDict setObject:callBackArr forKey:@"list"];
    if (fecthCbId >= 0) {
        [self sendResultEventWithCallbackId:fecthCbId dataDict:sendDict errDict:nil doDelete:NO];
    } else {
        [sendDict setObject:[NSNumber numberWithInteger:_assets.count] forKey:@"total"];
        [self sendResultEventWithCallbackId:cbScannerId dataDict:sendDict errDict:nil doDelete:NO];
    }
    //获得该方法所在的线程
    NSThread *t = [NSThread currentThread];
    //关闭线程
    [t cancel];
}

//准备资源信息（将缩略图拷贝到临时文件夹下）
- (void)prepareScannData:(id)obj {
    if (_cBAll) {
        [_cBAll removeAllObjects];
    } else {
        _cBAll = [NSMutableArray arrayWithCapacity:1];
    }
    for (int i = 0; i < self.assets.count; i++) {
        if (i >= _assets.count) {
            break;
        }
        ALAsset *result = self.assets[i];
        //资源类型  PNG MOV JPG
        NSString *filename = result.defaultRepresentation.filename;
        NSString *mimeType = [[[filename componentsSeparatedByString:@"."] lastObject] lowercaseString];
        //获取创建时间信息
        NSDate *pictureDate = [result valueForProperty:ALAssetPropertyDate];
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        NSString *strDate = [dateFormatter stringFromDate:pictureDate];
        //资源路径URL
        NSURL *assetUrls = result.defaultRepresentation.url;
        //资源缩略图URL
        UIImage *image = [UIImage imageWithCGImage:result.thumbnail];
        NSString *imagePath = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        NSFileManager *fileManager = [NSFileManager defaultManager];
        NSString *filePath = [imagePath  stringByAppendingPathComponent:@"mediaScanner"];
        if (![fileManager fileExistsAtPath:imagePath]) {
            [fileManager createDirectoryAtPath:imagePath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        //创建路径
        [fileManager createDirectoryAtPath:filePath withIntermediateDirectories:YES attributes:nil error:nil];
        NSString *type  = [result valueForProperty:ALAssetPropertyType];
        NSString *imgPath;
        if ([type isEqualToString:ALAssetTypeVideo]) {
            NSRange ran = [filename rangeOfString:@"."];
            filename= [filename substringWithRange:NSMakeRange(0, ran.location+1)];
            imgPath = [filePath stringByAppendingString:[NSString stringWithFormat:@"/%@%@",filename,@"png"]];
        } else {
            imgPath = [filePath stringByAppendingString:[NSString stringWithFormat:@"/%@",filename]];
        }
        UIImage *img = [UIImage imageWithContentsOfFile:imgPath];
        if (!img) {
            NSData *data;
            if (UIImagePNGRepresentation(image) == nil) {
                data = UIImageJPEGRepresentation(image, 1);
            } else {
                data = UIImagePNGRepresentation(image);
            }
            //创建文件
            if (data) {
                if ([type isEqualToString:ALAssetTypeVideo]) {
                    NSRange ran = [filename rangeOfString:@"."];
                    filename= [filename substringWithRange:NSMakeRange(0, ran.location+1)];
                    [fileManager createFileAtPath:imgPath contents:data attributes:nil];
                } else {
                    [fileManager createFileAtPath:imgPath contents:data attributes:nil];
                }
            }
        }
   
        //资源-照片大小   单位：Bytes
        NSNumber *size = [NSNumber numberWithLongLong:result.defaultRepresentation.size];
        if (result && assetUrls && size && result.thumbnail && imgPath) {
            NSString *temp = [assetUrls absoluteString];
            NSMutableDictionary * ttt =[NSMutableDictionary dictionaryWithCapacity:3];
            [ttt setObject:temp forKey:@"path"];
            [ttt setObject:imgPath forKey:@"thumbPath"];
            [ttt setObject:mimeType forKey:@"suffix"];
            [ttt setObject:size forKey:@"size"];
            [ttt setObject:strDate forKey:@"time"];
            [_cBAll addObject:ttt];
        }
    }
    preparedData = YES;
    //获得该方法所在的线程
    NSThread *t = [NSThread currentThread];
    //关闭线程
    [t cancel];
}

- (void)fetchCallBack {//遍历接口回调
    NSMutableArray *callBackArr = [NSMutableArray array];
    for (int i = fetchPosition; i < capicity+fetchPosition; i++) {
        if (i >= _cBAll.count) {
            break;
        }
        [callBackArr addObject:_cBAll[i]];
    }
    fetchPosition += capicity;
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionaryWithCapacity:1];
    [sendDict setObject:callBackArr forKey:@"list"];
    [self sendResultEventWithCallbackId:fecthCbId dataDict:sendDict errDict:nil doDelete:YES];
}

+ (ALAssetsLibrary *)defaultAssetsLibrary {
    static dispatch_once_t pred = 0;
    static ALAssetsLibrary *library = nil;
    dispatch_once(&pred, ^{
        library = [[ALAssetsLibrary alloc] init];
    });
    return library;
}

- (void)save:(UIImage *)img imagePath:(NSString *)path cbId:(NSInteger)cbId {//保存指定图片到临时位置并回调改位置路径
    UIImage *saveImg = img;
    NSRange ran2 = [path rangeOfString:@"id="];
    path = [path substringWithRange:NSMakeRange(ran2.location+3, path.length-ran2.location-3)];
    NSRange ran = [path rangeOfString:@"&ext="];
    NSString *name = [path substringToIndex:ran.location];
    NSString *type = [path substringWithRange:NSMakeRange(ran.location+ran.length, path.length-ran.location-ran.length)];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *filePath = [NSHomeDirectory() stringByAppendingPathComponent:@"Library/Caches/UIMediaScanner"];
    if (![fileManager fileExistsAtPath:filePath]) {        //创建路径
        [fileManager createDirectoryAtPath:filePath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    NSString *imgPath = [filePath stringByAppendingString:[NSString stringWithFormat:@"/%@.%@",name,type]];
    BOOL isSave = NO;
    NSData *data;
    if (![type isEqualToString:@"png"]) {
        data = UIImageJPEGRepresentation(saveImg, 1);
    } else {
        data = UIImagePNGRepresentation(saveImg);
    }
    //创建文件
    if (data) {
        [fileManager createFileAtPath:imgPath contents:data attributes:nil];
        isSave = YES;
    }
    //回到主线程
    dispatch_async(dispatch_get_main_queue(), ^{
        [self sendResultEventWithCallbackId:cbId dataDict:[NSDictionary dictionaryWithObjectsAndKeys:imgPath,@"path", nil] errDict:nil doDelete:YES];
    });
}

- (UIImage *)imageCorrectedForCaptureOrientation:(UIImage *)anImage UIImageOrientation:(UIImageOrientation)ImageOrientation {//旋转图片
    if (ImageOrientation == UIImageOrientationUp) {
        return anImage;
    }
    
    float rotation_radians = 0;
    bool perpendicular = false;
    switch (ImageOrientation) {
        case UIImageOrientationDown :
        case UIImageOrientationDownMirrored :
            rotation_radians = M_PI; // don't be scared of radians, if you're reading this, you're good at math
            break;
            
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            rotation_radians = M_PI_2;
            perpendicular = true;
            break;
            
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
            rotation_radians = -M_PI_2;
            perpendicular = true;
            break;
            
        default:
            break;
    }
    UIGraphicsBeginImageContext(CGSizeMake(anImage.size.width, anImage.size.height));
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextTranslateCTM(context, anImage.size.width / 2, anImage.size.height / 2);
    CGContextRotateCTM(context, rotation_radians);
    CGContextScaleCTM(context, 1.0, -1.0);
    float width = perpendicular ? anImage.size.height : anImage.size.width;
    float height = perpendicular ? anImage.size.width : anImage.size.height;
    CGContextDrawImage(context, CGRectMake(-width / 2, -height / 2, width, height), [anImage CGImage]);
    if (perpendicular) {
        CGContextTranslateCTM(context, -anImage.size.height / 2, -anImage.size.width / 2);
    }
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

- (void)classify:(NSDictionary *)paramsDict_ {
    BOOL classifyId = [paramsDict_ integerValueForKey:@"classify" defaultValue:NO];
    BOOL isRotation = [paramsDict_ boolValueForKey:@"rotation" defaultValue:false];
    if (classifyId) {
        UZUIAssetsGroupViewController *assetGroupVc = [[UZUIAssetsGroupViewController alloc]init];
        assetGroupVc.paramsDict = paramsDict_;
        assetGroupVc.delegate = self; 
        UZUIAssetNavigationController *assetGroupNavi = [[UZUIAssetNavigationController alloc]initWithRootViewController:assetGroupVc];
        assetGroupNavi.isRotation = isRotation;
        [self.viewController presentViewController:assetGroupNavi animated:YES completion:^{}];
    } else {
        UZUIAssetsViewController *assetVC = [[UZUIAssetsViewController alloc] initWithDict:paramsDict_];
        assetVC.delegate = self;
        UZUIAssetNavigationController *navi = [[UZUIAssetNavigationController alloc] initWithRootViewController:assetVC];
        navi.isRotation = isRotation;
        [self.viewController presentViewController:navi animated:YES completion:^{}];
    }
}

@end
