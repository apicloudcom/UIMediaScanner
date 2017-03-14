//
//  PrivewViewController.m
//  UZApp
//
//  Created by Turbo Sun on 17/2/13.
//  Copyright © 2017年 APICloud. All rights reserved.
//

#import "PrivewViewController.h"
#import "EBPhotoPagesDataSource.h"
#import "EBPhotoPagesDelegate.h"
#import "EBPhotoPagesController.h"
#import "EBPhotoViewController.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import "UZAppUtils.h"

@interface PrivewViewController ()
<EBPhotoPagesDataSource, EBPhotoPagesDelegate>
{
    BOOL showStatus;
}

@property (nonatomic, strong) EBPhotoPagesController *photoPagesController;
@property (nonatomic, strong) UIView *nBarView;

@end

@implementation PrivewViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [EBPhotoPagesController setZoomEnable:YES];
    _photoPagesController = [[EBPhotoPagesController alloc] initWithDataSource:self delegate:self photoAtIndex:self.index];
    _photoPagesController.forbideAnimStatus = YES;
    _photoPagesController.backgroundColor = [UIColor blackColor];
    [self addChildViewController:_photoPagesController];
    [self.view addSubview:_photoPagesController.view];
    //导航条
    self.nBarView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width,64)];
    [self.nBarView setBackgroundColor:[UZAppUtils colorFromNSString:@"rgba(0,0,0,0.6)"]];
    [self.view addSubview:self.nBarView];
    
    UIButton *button = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    NSString *backPath = [[NSBundle mainBundle]pathForResource:@"res_UIMediaScanner/back" ofType:@"png"];
    [button setImage:[UIImage imageWithContentsOfFile:backPath] forState:UIControlStateNormal];
    button.frame = CGRectMake(0, 20, 60, 44);
    [button setImageEdgeInsets:UIEdgeInsetsMake(12, 24, 12, 24)];
    [button addTarget:self action:@selector(backAction) forControlEvents:UIControlEventTouchUpInside];
    [self.nBarView addSubview:button];
    
    UIButton *complete = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    [complete setTitle:@"完成" forState:UIControlStateNormal];
    complete.backgroundColor = [UIColor clearColor];
    complete.titleLabel.textColor = [UZAppUtils colorFromNSString:@"#98FB98"];
    complete.frame = CGRectMake(self.view.frame.size.width-60, 20, 60, 44);
    [complete addTarget:self action:@selector(completeAction) forControlEvents:UIControlEventTouchUpInside];
    [self.nBarView addSubview:complete];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:YES animated:NO];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [self.navigationController setNavigationBarHidden:NO animated:NO];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Action -

- (void)backAction {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)completeAction {
    [self.navigationController popViewControllerAnimated:NO];
    self.comBlock();
}

#pragma mark - EBPhotoPagesDataSource -

- (BOOL)photoPagesController:(EBPhotoPagesController *)photoPagesController shouldExpectPhotoAtIndex:(NSInteger)index {
    if(index < self.allSelectedAry.count){
        return YES;
    }
    return NO;
}

- (void)photoPagesController:(EBPhotoPagesController *)controller imageAtIndex:(NSInteger)index completionHandler:(void (^)(UIImage *, BOOL))handler {
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(queue, ^{
        ALAsset *asset = [self.allSelectedAry objectAtIndex:index];
        NSURL *assetUrls = asset.defaultRepresentation.url;
        // 是否是本地图片?
        ALAssetsLibrary  *assetLib = [[ALAssetsLibrary alloc] init];
        [assetLib assetForURL:assetUrls resultBlock:^(ALAsset *asset) {
            // 使用asset来获取本地图片
            ALAssetRepresentation *assetRep = [asset defaultRepresentation];
            CGImageRef imgRef = [assetRep fullResolutionImage];
            UIImage *targetImage = [UIImage imageWithCGImage:imgRef
                                                       scale:assetRep.scale
                                                 orientation:(UIImageOrientation)assetRep.orientation];
            if (targetImage) {
                handler(targetImage,NO);
            }
        } failureBlock:^(NSError *error) {
            // 访问库文件被拒绝,则直接使用默认图片
        }];
    });
}
#pragma mark - EBPPhotoPagesDelegate

- (void)photoPagesControllerDidClick:(EBPhotoPagesController *)photoPagesController withIndex:(NSInteger)pageIndex {//图片未加载出来后的点击事件
    if (showStatus) {
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationDuration:0.3];
        self.nBarView.frame = CGRectMake(0, 0, self.view.frame.size.width,64);
        [UIView commitAnimations];
    } else {
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationDuration:0.3];
        //[UIView setAnimationDelegate:self];
        //[UIView setAnimationDidStopSelector:@selector(abc)];
        self.nBarView.frame = CGRectMake(0, -64, self.view.frame.size.width,64);
        [UIView commitAnimations];
    }
    showStatus = !showStatus;
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
