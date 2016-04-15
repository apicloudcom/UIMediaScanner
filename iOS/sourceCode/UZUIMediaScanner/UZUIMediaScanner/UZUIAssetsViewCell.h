/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>

typedef NSString *(^GetPathBolock)(NSString *);

@interface UZUIAssetsViewCell : UICollectionViewCell

@property (nonatomic, copy) GetPathBolock getPath;
@property (nonatomic, assign) float collectionViewWidth;
@property (nonatomic, strong) NSDictionary *markInfo;

- (void)bind:(ALAsset *)asset;

@end
