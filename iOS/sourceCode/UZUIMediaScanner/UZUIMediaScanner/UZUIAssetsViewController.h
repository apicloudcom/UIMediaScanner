/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import <AssetsLibrary/AssetsLibrary.h>
#import <UIKit/UIKit.h>

@protocol AssetsViewCallBack ;

@interface UZUIAssetsViewController : UICollectionViewController {
    NSString *photoLoding;
}

@property (nonatomic, strong) ALAssetsGroup *assetsGroup;
@property (nonatomic, strong) ALAssetsLibrary *assetsLibrary;
@property (nonatomic, strong) NSMutableArray *groups;
@property (nonatomic, strong) NSDictionary *paramsDict;
@property (nonatomic, weak) id <AssetsViewCallBack> delegate;
@property (nonatomic, assign) BOOL isClassify;

- (id)initWithDict:(NSDictionary *)params;

@end

@protocol AssetsViewCallBack
<NSObject>

- (void)callBack:(NSDictionary *)listDict;
- (NSString *)getPath:(NSString *)path;

@end