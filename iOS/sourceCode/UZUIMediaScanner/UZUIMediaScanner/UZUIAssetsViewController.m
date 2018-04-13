/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */


#import "UZUIAssetsViewController.h"
#import "UZAppUtils.h"
#import "NSDictionaryUtils.h"
#import "UZUIAssetsViewCell.h"
#import <AVFoundation/AVFoundation.h>
#import "PrivewViewController.h"

#define kPopoverContentSize self.view.bounds.size
#define kAssetsViewCellIdentifier           @"AssetsViewCellIdentifier"
#define kAssetsSupplementaryViewIdentifier  @"AssetsSupplementaryViewIdentifier"

@interface UZUIAssetsViewController ()
<UIScrollViewDelegate, AssetCellViewDelegate> {
    NSDictionary *markDict;
    NSString *_type;             //type类型：all/picture/video
    float collectionViewWidth;
    BOOL isAnimScroll, didShowPreview;
    NSTimer *_timer;
}

@property (nonatomic, strong) NSMutableArray *allAssetsAry;
@property (nonatomic, assign) NSInteger column;
@property (nonatomic, strong) NSMutableArray *assetsArrSelected;
@property (nonatomic, strong) NSMutableDictionary *selectedState;

@end

@implementation UZUIAssetsViewController

@synthesize delegate;

#pragma mark - lifeCycle -

- (id)initWithDict:(NSDictionary *)paramsDict_ {
    didShowPreview = NO;
    self.column = [paramsDict_ integerValueForKey:@"column" defaultValue:4];
    if (self.column == 0) {
        self.column = 4;
    }
    _type = [paramsDict_ stringValueForKey:@"type" defaultValue:@"all"];
    NSDictionary *style = [paramsDict_ dictValueForKey:@"styles" defaultValue:@{}];
    markDict = [style dictValueForKey:@"mark" defaultValue:@{}];
    BOOL bounces = [paramsDict_ boolValueForKey:@"bounces" defaultValue:false];
    self.paramsDict = [[NSDictionary alloc] initWithDictionary:paramsDict_];
    float width = [UIScreen mainScreen].bounds.size.width;
    collectionViewWidth = (width - (self.column + 1)*2)/self.column;//缩略图的大小
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.itemSize = CGSizeMake(collectionViewWidth, collectionViewWidth);
    layout.sectionInset = UIEdgeInsetsMake(5.0, 0, 0, 0);
    layout.minimumInteritemSpacing = 2.0;
    layout.minimumLineSpacing = 2.0;
    //最下面显示**张图片  **个视频的view大小
    layout.footerReferenceSize = CGSizeMake(0, 7.0);
    if (self = [super initWithCollectionViewLayout:layout]){
        Class assCell = [UZUIAssetsViewCell class];
        [self.collectionView registerClass:assCell forCellWithReuseIdentifier:kAssetsViewCellIdentifier];
        self.collectionView.allowsMultipleSelection = YES;
        
        [self.collectionView registerClass:[UICollectionReusableView class] forSupplementaryViewOfKind:UICollectionElementKindSectionFooter withReuseIdentifier:kAssetsSupplementaryViewIdentifier];
        //横竖屏转换监听
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(changeFrames:)
                                                     name:UIDeviceOrientationDidChangeNotification
                                                   object:nil];

    }
    self.collectionView.bounces = bounces;
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupViews];                //设置背景
}

- (void)viewWillAppear:(BOOL)animated {
    if (didShowPreview) {
        return;
    }
    [self setupButtons];              //设置导航条
    if (self.isClassify) {            //若是相册分组显示列表进来的,则获取当前分组中的照片并排序
        [self setupAssetsOfGroup];
    } else {
        [self setupGroup];            //若相册显示不分组，则获取所有照片并排序
    }
    //一段时间滚动到最底部区域显示
    NSDictionary *scrollToBottom = [self.paramsDict dictValueForKey:@"scrollToBottom" defaultValue:@{}];
    float timeInval = [scrollToBottom floatValueForKey:@"intervalTime" defaultValue:-1];
    isAnimScroll = [scrollToBottom boolValueForKey:@"anim" defaultValue:true];
    if (timeInval <= 0.0) {
        if (_timer) {
            [_timer invalidate];
            _timer = nil;
        }
    } else {
        _timer = [NSTimer scheduledTimerWithTimeInterval:timeInval target:self selector:@selector(scrollToBottom:) userInfo:nil repeats:NO];
    }
}

- (void)dealloc {
    if (_timer) {
        [_timer invalidate];
        _timer = nil;
    }
}

#pragma mark - lifeUtils -

+ (ALAssetsLibrary *)defaultAssetsLibrary {
    static dispatch_once_t pred = 0;
    static ALAssetsLibrary *library = nil;
    dispatch_once(&pred, ^{
        library = [[ALAssetsLibrary alloc] init];
    });
    return library;
}

- (NSMutableDictionary *)selectedState {
    if (!_selectedState) {
        _selectedState = [[NSMutableDictionary alloc]init];
    }
    return _selectedState;
}

- (NSMutableArray *)assetsArrSelected {
    if (!_assetsArrSelected) {
        _assetsArrSelected = [NSMutableArray array];
    }
    return _assetsArrSelected;
}
- (void)scrollToBottom:(id)info {
    if (self.allAssetsAry.count) {
        [self.collectionView scrollToItemAtIndexPath:[NSIndexPath indexPathForItem:self.allAssetsAry.count-1 inSection:0] atScrollPosition:UICollectionViewScrollPositionBottom animated:isAnimScroll];
    }
}

- (void)changeFrames:(NSNotification *)notification {//横竖屏转换时适配
    if ([[UIDevice currentDevice] orientation] == UIInterfaceOrientationPortrait
        || [[UIDevice currentDevice] orientation] == UIInterfaceOrientationPortraitUpsideDown) {
        self.collectionView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        float width = [UIScreen mainScreen].bounds.size.width;
        collectionViewWidth = (width - (self.column + 1)*2)/self.column;//缩略图的大小
    } else {
        self.collectionView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        float width = [UIScreen mainScreen].bounds.size.width;
        collectionViewWidth = (width - (self.column + 1)*2)/self.column;//缩略图的大小
    }
    [self.collectionView reloadData];
}

#pragma mark 初始化界面

- (void)setupViews {//背景
    NSString * bgColor = [[self.paramsDict dictValueForKey:@"styles" defaultValue:@{}] stringValueForKey:@"bg" defaultValue:@"#fff"];
    self.collectionView.backgroundColor = bgColor?[UZAppUtils colorFromNSString:bgColor]:[UIColor whiteColor];
}

- (void)setupButtons{//导航条
    NSDictionary *styles = [self.paramsDict dictValueForKey:@"styles" defaultValue:@{}];
    NSDictionary *navDic = [styles dictValueForKey:@"nav" defaultValue:@{}];
    NSDictionary *texts = [self.paramsDict dictValueForKey:@"texts" defaultValue:@{}];
    NSString *cancelBtnTittle = [texts stringValueForKey:@"cancelText" defaultValue:@"取消"];
    NSString *cancelTitileColorS = [navDic stringValueForKey:@"cancelColor" defaultValue:@"#000"];
    float cancelTitleSize = [navDic floatValueForKey:@"cancelSize" defaultValue:18.0];
    NSString *cancelBgColor = [navDic stringValueForKey:@"cancleBg" defaultValue:@"rgba(0,0,0,0)"];
    NSString *finishBtnTittle = [texts stringValueForKey:@"finishText" defaultValue:@"完成"];
    NSString *finishTitleColorStr = [navDic stringValueForKey:@"finishColor" defaultValue:@"#000"];
    float finishTitleSize = [navDic floatValueForKey:@"finishSize" defaultValue:18.0];
    NSString *finishBgColorStr = [navDic stringValueForKey:@"finishBg" defaultValue:@"rgba(0,0,0,0)"];
    NSString *stateTitleColorStr = [navDic stringValueForKey:@"stateColor" defaultValue:@"#000"];
    float stateTitleSize = [navDic floatValueForKey:@"stateSize" defaultValue:18.0];
    NSString *navBgColor = [navDic stringValueForKey:@"bg" defaultValue:@"#eee"];
    BOOL isExchange = [self.paramsDict boolValueForKey:@"exchange" defaultValue:false];
    if ([UZAppUtils isValidColor:navBgColor]) {
        if (isIOS7) {
            self.navigationController.navigationBar.barTintColor = [UZAppUtils colorFromNSString:navBgColor] ;
        } else {
            self.navigationController.navigationBar.tintColor = [UZAppUtils colorFromNSString:navBgColor] ;
        }
    } else {
        [self.navigationController.navigationBar setBackgroundImage:[UIImage imageWithContentsOfFile:[self.delegate getPath:navBgColor]]forBarMetrics:UIBarMetricsDefault];
    }
    //取消按钮
    UIColor *cancelTitleColor = [UZAppUtils colorFromNSString:cancelTitileColorS];
    UIColor *cancelBg;
    if ([UZAppUtils isValidColor:cancelBgColor]) {
        cancelBg = [UZAppUtils colorFromNSString:cancelBgColor];
    } else {
        cancelBg = [UIColor colorWithPatternImage:[UIImage imageWithContentsOfFile:[self.delegate getPath:cancelBgColor]]];
    }
    //完成按钮
    UIColor *finishTitleColor = [UZAppUtils colorFromNSString:finishTitleColorStr];
    UIColor *finishBg;
    if ([UZAppUtils isValidColor:finishBgColorStr]) {
        finishBg = [UZAppUtils colorFromNSString:finishBgColorStr];
    } else {
        finishBg = [UIColor colorWithPatternImage:[UIImage imageWithContentsOfFile:[self.delegate getPath:finishBgColorStr]]];
    }
    //state:导航栏文字(颜色、大小)
    UIColor *stateTitleColor = [UZAppUtils colorFromNSString:stateTitleColorStr];
    NSDictionary *stateTitleDic = [[NSDictionary alloc] initWithObjectsAndKeys:stateTitleColor,NSForegroundColorAttributeName,[UIFont systemFontOfSize:stateTitleSize],NSFontAttributeName, nil];
    [self.navigationController.navigationBar setTitleTextAttributes:stateTitleDic];
    //取消按钮
    UIButton *cancelBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    cancelBtn.frame = CGRectMake(0, 0, 50, 30);
    cancelBtn.backgroundColor = cancelBg;
    cancelBtn.titleLabel.font = [UIFont systemFontOfSize:cancelTitleSize];
    [cancelBtn setTitleColor:cancelTitleColor forState:UIControlStateNormal];
    [cancelBtn setTitle:cancelBtnTittle forState:UIControlStateNormal];
    [cancelBtn addTarget:self action:@selector(cancel) forControlEvents:UIControlEventTouchUpInside];
    //完成按钮
    UIButton *completeBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    completeBtn.frame = CGRectMake(0, 0, 50, 30);
    completeBtn.backgroundColor = finishBg;
    completeBtn.titleLabel.font = [UIFont systemFontOfSize:finishTitleSize];
    [completeBtn setTitleColor:finishTitleColor forState:UIControlStateNormal];
    [completeBtn setTitle:finishBtnTittle forState:UIControlStateNormal];
    [completeBtn addTarget:self action:@selector(done:) forControlEvents:UIControlEventTouchUpInside];
    if (isExchange) {
        self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:cancelBtn];
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:completeBtn];
    } else {
        self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:completeBtn];
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView: cancelBtn];
    }
}

#pragma mark 读取当前组中图片信息到self.UIassets中

- (void)setupAssetsOfGroup {
    NSDictionary *stateInfo = [self.paramsDict dictValueForKey:@"texts" defaultValue:@{}];
    self.title = [stateInfo stringValueForKey:@"stateText" defaultValue:@"已选择*项"];
    if (!self.allAssetsAry.count) {
        self.allAssetsAry = [NSMutableArray arrayWithCapacity:1];
    } else {
        [self.allAssetsAry removeAllObjects];
    }
    ALAssetsGroupEnumerationResultsBlock resultsBlock = ^(ALAsset *asset, NSUInteger index, BOOL *stop) {
        if (asset){
            if ([_type isEqualToString:@"picture"]) {
                if ([[asset valueForProperty:ALAssetPropertyType] isEqualToString:ALAssetTypePhoto]) {
                    [self.allAssetsAry addObject:asset];  //asset类型不一样
                }
            } else if ([_type isEqualToString:@"video"]) {
                if([[asset valueForProperty:ALAssetPropertyType] isEqualToString:ALAssetTypeVideo]) {
                    [self.allAssetsAry addObject:asset];  //asset类型不一样
                }
            } else {
                _type = @"all";
                [self.allAssetsAry addObject:asset];
            }
        }
    };
    [self.assetsGroup enumerateAssetsUsingBlock:resultsBlock ];
    [self sort];
}

#pragma mark 读取本地相册所有资源信息到self.groups数组中

- (void)setupGroup {//读取相册数据后刷新界面
    if (!self.assetsLibrary) {
        self.assetsLibrary = [self.class defaultAssetsLibrary];
    }
    if (!self.groups) {
        self.groups = [[NSMutableArray alloc] init];
    } else {
        [self.groups removeAllObjects];
    }
    ALAssetsLibraryGroupsEnumerationResultsBlock resultsBlock = ^(ALAssetsGroup *group, BOOL *stop) {
        if (group) {
            if (group.numberOfAssets > 0) {
                [self.groups addObject:group];
            }
            self.assetsGroup = self.groups.firstObject;
        } else {
            ALAssetsGroup *lastGroup = [[ALAssetsGroup alloc]init];
            [self.groups addObject:lastGroup];
            //刷新显示界面
            [self setupAssets];
        }
    };
    ALAssetsLibraryAccessFailureBlock failureBlock = ^(NSError *error) {
        NSLog(@"Turbo_UIMediascanner_no_right");
    };
    //遍历相册
    [self.assetsLibrary enumerateGroupsWithTypes:ALAssetsGroupSavedPhotos
                                      usingBlock:resultsBlock
                                    failureBlock:failureBlock];
}

#pragma mark 遍历self.groups读取图片信息到self.UIassets中，然后排序
- (void)setupAssets {
    NSDictionary *stateInfo = [self.paramsDict dictValueForKey:@"texts" defaultValue:@{}];
    self.title = [stateInfo stringValueForKey:@"stateText" defaultValue:@"已选择*项"];
    if (!self.allAssetsAry.count) {
        self.allAssetsAry = [NSMutableArray arrayWithCapacity:1];
    } else {
        [self.allAssetsAry removeAllObjects];
    }
    ALAssetsGroupEnumerationResultsBlock resultsBlock = ^(ALAsset *asset, NSUInteger index, BOOL *stop) {
        if (asset){
            if ([_type isEqualToString:@"picture"]) {
                if ([[asset valueForProperty:ALAssetPropertyType] isEqualToString:ALAssetTypePhoto]) {
                    [self.allAssetsAry addObject:asset];  //asset类型不一样
                }
            } else if ([_type isEqualToString:@"video"]) {
                if([[asset valueForProperty:ALAssetPropertyType] isEqualToString:ALAssetTypeVideo]) {
                    [self.allAssetsAry addObject:asset];  //asset类型不一样
                }
            } else {
                _type = @"all";
                [self.allAssetsAry addObject:asset];
            }
        }
    };
    void (^enumGroups)(id obj, NSUInteger idx, BOOL *stop) = ^(id obj, NSUInteger idx, BOOL *stop){
        ALAssetsGroup *assetGroup = (ALAssetsGroup *)obj;
        if (assetGroup.numberOfAssets > 0) {
            [assetGroup enumerateAssetsUsingBlock:resultsBlock];
        } else {
            [self sort];
        }
    };
    [self.groups enumerateObjectsUsingBlock:enumGroups];
}

#pragma mark 排序
- (void)sort {
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        NSDictionary *sortInfo = [self.paramsDict dictValueForKey:@"sort" defaultValue:@{}];
        NSString *keyStr = [sortInfo stringValueForKey:@"key" defaultValue:@"time"];
        NSString *orderStr = [sortInfo stringValueForKey:@"order" defaultValue:@"desc"];
        if ([keyStr isEqualToString:@"time"]) {
            NSArray *sortedArray = [self.allAssetsAry sortedArrayUsingComparator:^( ALAsset *asset1,ALAsset *asset2) {
                NSDate *pictureDate1 = [asset1 valueForProperty:ALAssetPropertyDate];
                NSDate *pictureDate2 = [asset2 valueForProperty:ALAssetPropertyDate];
                NSDate *date = [pictureDate1 earlierDate:pictureDate2];
                if (pictureDate1 == date && [orderStr isEqualToString:@"desc"]) {
                    return NSOrderedDescending;
                } else {
                    return NSOrderedAscending;
                }
            }];
            [self.allAssetsAry removeAllObjects];
            [self.allAssetsAry addObjectsFromArray:sortedArray];
        } else  {
            NSArray *sortedArray;
            if ([orderStr isEqualToString:@"desc"]) {
                sortedArray = [self.allAssetsAry sortedArrayUsingComparator:^( ALAsset *asset1,ALAsset *asset2) {
                    if (asset1.defaultRepresentation.size > asset2.defaultRepresentation.size) {
                        return NSOrderedDescending;
                    } else {
                        return NSOrderedAscending;
                    }
                }];
            } else {
                sortedArray = [self.allAssetsAry sortedArrayUsingComparator:^( ALAsset *asset1,ALAsset *asset2) {
                    if (asset1.defaultRepresentation.size < asset2.defaultRepresentation.size) {
                        return NSOrderedAscending;
                    } else {
                        return NSOrderedDescending;
                    }
                }];
            }
            [self.allAssetsAry removeAllObjects];
            [self.allAssetsAry addObjectsFromArray:sortedArray];
        }
        [self performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:NO];
    });
}

- (void)reloadData {
    [self.collectionView reloadData];
}
#pragma mark - CollectionDelegate -

#pragma mark 数据源代理

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.allAssetsAry.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = kAssetsViewCellIdentifier;
    UZUIAssetsViewCell *cell = [self.collectionView dequeueReusableCellWithReuseIdentifier:CellIdentifier forIndexPath:indexPath];
    cell.showPreview = self.showPreview;
    cell.showBrowser = self.showBrowser;
    cell.delegate = self;
    cell.collectionViewWidth = collectionViewWidth;
    cell.markInfo = markDict;
    __weak UZUIAssetsViewController *tempSelf = self;
    cell.getPath = ^(NSString *path){
        return [tempSelf.delegate getPath:path];
    };
    if (indexPath.row < self.allAssetsAry.count) {
        ALAsset *aset = [self.allAssetsAry objectAtIndex:indexPath.row];
        if (aset) {
            [cell bind:aset];
        }
    }
    // 刷新时重置cell的选中状态
    if (self.showPreview || self.showBrowser) {
        BOOL select = [[self.selectedState objectForKey:indexPath] boolValue];
        [cell.selectBtn setSelected:select];
        return cell;
    }
    cell.selected = [[self.selectedState objectForKey:indexPath] boolValue];
    if (cell.selected) {
        //设置为被选中Item，否则处于“非状态”，无法触发 shouldSelectItemAtIndexPath、shouldDeselectItemAtIndexPath代理方法
        [self.collectionView selectItemAtIndexPath:indexPath animated:YES scrollPosition:UICollectionViewScrollPositionNone];
        ALAsset *asset = [self.allAssetsAry objectAtIndex:indexPath.item];
        if (![self.assetsArrSelected containsObject:asset]) {
            [self.assetsArrSelected addObject:asset];
        }
        [self.selectedState setObject:[NSNumber numberWithBool:YES] forKey:indexPath];
        [self setTitleWithSelectedIndexPaths:self.assetsArrSelected];
    }
    return cell;
}

#pragma mark view代理

- (BOOL)collectionView:(UICollectionView *)collectionView shouldSelectItemAtIndexPath:(NSIndexPath *)indexPath {//判断是否可选
    if (self.showPreview || self.showBrowser) {
        return YES;
    }
    if (![self max:self.assetsArrSelected]) {
        return NO;
    } else {
        return YES;
    }
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {//选中时调用
    if (self.showPreview || self.showBrowser) {
        [collectionView reloadItemsAtIndexPaths:@[indexPath]];
        ALAsset *asset = [self.allAssetsAry objectAtIndex:indexPath.item];
        if ([self.assetsArrSelected containsObject:asset]) {
            NSInteger index = [self.assetsArrSelected indexOfObject:asset];
            //触发预览事件
            [self preview:index];
        }
        return;
    }
    ALAsset *asset = [self.allAssetsAry objectAtIndex:indexPath.item];
    if ([self.assetsArrSelected containsObject:asset]) {
        return;
    }
    [self.assetsArrSelected addObject:asset];
    [self.selectedState setObject:[NSNumber numberWithBool:YES] forKey:indexPath];
    [self setTitleWithSelectedIndexPaths:self.assetsArrSelected];
}

// called when the user taps on an already-selected item in multi-select mode
- (BOOL)collectionView:(UICollectionView *)collectionView shouldDeselectItemAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (void)collectionView:(UICollectionView *)collectionView didDeselectItemAtIndexPath:(NSIndexPath *)indexPath {//取消选中时调用
    if (self.showPreview || self.showBrowser) {
        return;
    }
    ALAsset *asset = [self.allAssetsAry objectAtIndex:indexPath.item];
    [self.assetsArrSelected removeObject:asset];
    [self.selectedState setObject:[NSNumber numberWithBool:NO] forKey:indexPath];
    [self setTitleWithSelectedIndexPaths:self.assetsArrSelected];
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {//缩略图的大小
    float width = [UIScreen mainScreen].bounds.size.width;
    CGFloat collectionWidth = (width - (self.column + 1)*2)/self.column;
    CGSize collectionViewSize = CGSizeMake(collectionWidth, collectionWidth);
    return collectionViewSize;
}

#pragma mark - AssetCellViewDelegate -

- (void)didSelectedAssetCell:(UZUIAssetsViewCell *)cell withSelected:(BOOL)select {
    NSIndexPath *indexPath = [self.collectionView indexPathForCell:cell];
    if (select) {
        if (![self max:self.assetsArrSelected]) {
            [cell.selectBtn setSelected:NO];
            return;
        }
        NSInteger index = indexPath.item;
        ALAsset *asset = [self.allAssetsAry objectAtIndex:index];
        if ([self.assetsArrSelected containsObject:asset]) {
            return;
        }
        [self.assetsArrSelected addObject:asset];
        [self.selectedState setObject:[NSNumber numberWithBool:YES] forKey:indexPath];
        [self setTitleWithSelectedIndexPaths:self.assetsArrSelected];
    } else {
        ALAsset *asset = [self.allAssetsAry objectAtIndex:indexPath.item];
        [self.assetsArrSelected removeObject:asset];
        [self.selectedState setObject:[NSNumber numberWithBool:NO] forKey:indexPath];
        [self setTitleWithSelectedIndexPaths:self.assetsArrSelected];
    }
}

#pragma mark - helper -

#pragma mark 动态改变标题文字
- (void)setTitleWithSelectedIndexPaths:(NSArray *)assets {
    if (assets.count == 0){
        NSDictionary *stateInfo = [self.paramsDict dictValueForKey:@"texts" defaultValue:@{}];
        self.title = [stateInfo stringValueForKey:@"stateText" defaultValue:@"已选择*项"];
        return;
    }
    BOOL photosSelected = NO;
    BOOL videoSelected  = NO;
    for (ALAsset *asset in assets) {
        if ([[asset valueForProperty:ALAssetPropertyType] isEqual:ALAssetTypePhoto])
            photosSelected  = YES;
        if ([[asset valueForProperty:ALAssetPropertyType] isEqual:ALAssetTypeVideo])
            videoSelected   = YES;
        if (photosSelected && videoSelected)
            break;
    }
    
    //将"已选择*项"中的"*"替换成：indexPaths.count
    NSString *stateTitle = nil;
    NSDictionary *stateInfo = [self.paramsDict dictValueForKey:@"texts" defaultValue:@{}];
    stateTitle = [stateInfo stringValueForKey:@"stateText" defaultValue:@"已选择*项"];
    NSRange range = [stateTitle rangeOfString:@"*"];
    if (range.length == NSNotFound) {
        self.title = stateTitle;
    } else {
        NSString *st = [stateTitle stringByReplacingOccurrencesOfString:@"*" withString:[NSString stringWithFormat:@"%lu",(unsigned long)assets.count]];
        self.title = st;
    }
}
#pragma mark 最多选择几张照片
- (BOOL)max:(NSArray *)indexPaths {
    NSInteger selectedMax = [self.paramsDict integerValueForKey:@"max" defaultValue:-1];
    if ((indexPaths.count+1) > selectedMax){
        NSDictionary *stateInfo = [self.paramsDict dictValueForKey:@"texts" defaultValue:@{}];
        NSString *selectedMaxText = [stateInfo stringValueForKey:@"selectedMaxText" defaultValue:@"最多显示*个资源"];
        NSRange range = [selectedMaxText rangeOfString:@"*"];
        if (range.length != NSNotFound) {
            selectedMaxText = [selectedMaxText stringByReplacingOccurrencesOfString:@"*" withString:[NSString stringWithFormat:@"%ld",(unsigned long)selectedMax]];
        }
        NSString *okBtnText = [stateInfo stringValueForKey:@"okBtnText" defaultValue:@"我知道了"];
        UIAlertView *alertView = [[UIAlertView alloc]initWithTitle:nil message:selectedMaxText delegate:self cancelButtonTitle:okBtnText otherButtonTitles: nil];
        [alertView show];
        return false;
    } else {
        return true;
    }
}
#pragma mark 取消按钮事件
- (void)cancel {
    [self dismissViewControllerAnimated:YES completion:^(void) {
        if ([self.delegate respondsToSelector:@selector(callBack:)]) {
            [self.delegate callBack:@{@"eventType" : @"cancel"}];
        }
        self.delegate = nil;
    }];
}
#pragma mark 预览按钮事件
- (void)preview:(NSInteger)index {
    if (self.showBrowser) {// 模块开启预览
        didShowPreview = YES;
        PrivewViewController *pvVC = [[PrivewViewController alloc]init];
        pvVC.index = index;
        pvVC.allSelectedAry = self.assetsArrSelected;
        pvVC.comBlock = ^(){
            [self done:YES];
        };
        [self.navigationController pushViewController:pvVC animated:YES];
    }
    if (self.showPreview) {//返回预览事件
        NSMutableArray *listMutableArr = [self getSelectedInfo];
        if ([self.delegate respondsToSelector:@selector(previewCallback:)]) {
            [self.delegate previewCallback:@{@"list" : listMutableArr, @"eventType" : @"preview"}];
        }
    }
}
#pragma mark 完成按钮事件
- (void)done:(BOOL)isPreview {
    NSMutableArray *listMutableArr = [self getSelectedInfo];
    [self dismissViewControllerAnimated:YES completion:^(void){
        //返回选中的资源的信息
        if ([self.delegate respondsToSelector:@selector(callBack:)]) {
            [self.delegate callBack:@{@"list" : listMutableArr, @"eventType" : @"confirm"}];
        }
        self.delegate = nil;
    }];
}
#pragma mark 提取选中图片信息
- (NSMutableArray *)getSelectedInfo {
    NSMutableArray *listMutableArr = [NSMutableArray array];
    //遍历读取选中的图片信息
    for (ALAsset *asset in self.assetsArrSelected) {
        //资源类型  PNG MOV JPG      //  IMG_0133.JPG
        NSString *filename = asset.defaultRepresentation.filename;
        NSString *mimeType = [[[filename componentsSeparatedByString:@"."] lastObject] lowercaseString];
        //获取创建时间信息
        NSDate *pictureDate = [asset valueForProperty:ALAssetPropertyDate];
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        NSString *strDate = [dateFormatter stringFromDate:pictureDate];
        //资源路径URL
        NSURL *assetUrls = asset.defaultRepresentation.url;
        //资源缩略图URL
        UIImage *image = [UIImage imageWithCGImage:asset.thumbnail];
        NSString *type  = [asset valueForProperty:ALAssetPropertyType];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        //缩略图存到library/Caches/UIMediaScanner目录下
        NSString *filePath = [NSHomeDirectory() stringByAppendingPathComponent:@"Library/Caches/UIMediaScanner"];
        if (![fileManager fileExistsAtPath:filePath]) {
            [fileManager createDirectoryAtPath:filePath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        NSData *data;
        if (UIImagePNGRepresentation(image) == nil) {
            data = UIImageJPEGRepresentation(image, 1);
        } else {
            data = UIImagePNGRepresentation(image);
        }
        NSString *imgPath;
        double seconds = -1;
        if ([type isEqualToString:ALAssetTypeVideo]) {//视频资源
            NSRange ran = [filename rangeOfString:@"."];
            filename= [filename substringWithRange:NSMakeRange(0, ran.location+1)];
            imgPath = [filePath stringByAppendingString:[NSString stringWithFormat:@"/%@%@",filename,@"png"]];
            AVURLAsset *asset = [AVURLAsset assetWithURL:assetUrls];
            CMTime   time = [asset duration];
            seconds = ceil(time.value/time.timescale);
        } else {//图片资源
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
        NSNumber *size = [NSNumber numberWithLongLong:asset.defaultRepresentation.size];
        if (asset && assetUrls && size && asset.thumbnail) {
            NSString *asUrl = [assetUrls absoluteString];
            NSDictionary *dic = [[NSDictionary alloc] initWithObjectsAndKeys:asUrl,@"path",[thumbUrl absoluteString],@"thumbPath",mimeType,@"suffix",size,@"size",strDate,@"time",@(seconds),@"duration", nil];
            [listMutableArr addObject:dic];
        }
    }
    return listMutableArr;
}
@end

