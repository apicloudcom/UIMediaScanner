/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "UZUIMediaScanner.h"
#import "UZUIAssetsViewController.h"

@interface UZUIAssetsGroupViewController : UIViewController

@property (nonatomic, strong) ALAssetsLibrary *assetsLibrary;
@property (nonatomic, strong) NSMutableArray *groups;
@property (nonatomic, strong) NSDictionary *paramsDict;
@property (nonatomic, weak) id<AssetsViewCallBack> delegate;
@property (nonatomic, assign) BOOL showPreview, showBrowser;
@property (nonatomic, strong) NSString *titleStr, *cancelBtnTitle;

+ (ALAssetsLibrary *)defaultAssetsLibrary;

@end
